const fs = require("fs");
const path = require("path");
const sqlite3 = require("sqlite3").verbose();

const dataDir = path.join(__dirname, "..", "data");
const dbPath = path.join(dataDir, "report-tool.db");

if (!fs.existsSync(dataDir)) {
  fs.mkdirSync(dataDir, { recursive: true });
}

const db = new sqlite3.Database(dbPath);

function run(sql, params = []) {
  return new Promise((resolve, reject) => {
    db.run(sql, params, function onRun(error) {
      if (error) {
        reject(error);
        return;
      }
      resolve({ lastID: this.lastID, changes: this.changes });
    });
  });
}

function get(sql, params = []) {
  return new Promise((resolve, reject) => {
    db.get(sql, params, (error, row) => {
      if (error) {
        reject(error);
        return;
      }
      resolve(row);
    });
  });
}

function all(sql, params = []) {
  return new Promise((resolve, reject) => {
    db.all(sql, params, (error, rows) => {
      if (error) {
        reject(error);
        return;
      }
      resolve(rows);
    });
  });
}

function isSelectSql(sql) {
  if (!sql || typeof sql !== "string") {
    return false;
  }

  const trimmed = sql.trim().replace(/;+\s*$/, "");
  if (!trimmed) {
    return false;
  }

  const startsWithReadOnly = /^(select|with)\s/i.test(trimmed);
  if (!startsWithReadOnly) {
    return false;
  }

  const dangerousKeywords = /\b(insert|update|delete|drop|truncate|alter|create|replace|attach|vacuum|pragma)\b/i;
  return !dangerousKeywords.test(trimmed);
}

async function seedTickets() {
  const countRow = await get("SELECT COUNT(*) AS total FROM tickets");
  if (countRow && countRow.total > 0) {
    return;
  }

  const now = new Date();
  const sampleTickets = [
    ["登录失败排查", "account", "open", "high", "王磊", "2026-01-12", "2026-01-08"],
    ["接口超时优化", "api", "in_progress", "high", "赵敏", "2026-02-20", "2026-02-02"],
    ["数据库索引调整", "database", "closed", "medium", "陈超", "2026-01-20", "2026-01-15"],
    ["支付回调异常", "payment", "closed", "high", "李颖", "2026-03-10", "2026-03-01"],
    ["导出功能空白", "frontend", "open", "low", "孙凯", null, "2026-03-11"],
    ["短信服务降级", "notification", "closed", "medium", "王磊", "2026-03-26", "2026-03-20"],
    ["任务调度失败", "job", "closed", "high", "赵敏", "2026-04-01", "2026-03-25"],
    ["工单搜索慢查询", "database", "in_progress", "medium", "陈超", null, "2026-03-28"],
    ["页面样式错乱", "frontend", "closed", "low", "李颖", "2026-04-03", "2026-04-02"],
    ["权限模块重构", "account", "open", "high", "孙凯", null, "2026-04-10"],
    ["日志采集缺失", "infrastructure", "closed", "medium", "王磊", "2026-04-12", "2026-04-08"],
    ["监控告警误报", "monitoring", "closed", "low", "赵敏", "2026-04-14", "2026-04-11"],
  ];

  for (const ticket of sampleTickets) {
    await run(
      `INSERT INTO tickets (title, category, status, priority, assignee, resolved_at, created_at, updated_at)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [...ticket, now.toISOString()]
    );
  }
}

async function seedConfigurations() {
  const apiCount = await get("SELECT COUNT(*) AS total FROM api_configs");
  let apiConfigId = null;

  if (apiCount.total === 0) {
    const created = await run(
      `INSERT INTO api_configs (name, base_url, path, method, cookie, timeout_ms, created_at, updated_at)
       VALUES (?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))`,
      ["本地工单数据 API", "http://127.0.0.1:3000", "/api/data/query", "POST", "session_id=demo", 10000]
    );
    apiConfigId = created.lastID;
  } else {
    const firstApi = await get("SELECT id FROM api_configs ORDER BY id ASC LIMIT 1");
    apiConfigId = firstApi ? firstApi.id : null;
  }

  const reportCount = await get("SELECT COUNT(*) AS total FROM report_configs");
  if (reportCount.total > 0 || !apiConfigId) {
    return;
  }

  const defaultReports = [
    {
      name: "工单状态分布",
      reportType: "pie",
      tableHeaders: JSON.stringify(["状态", "数量"]),
      sqlText: "SELECT status AS label, COUNT(*) AS value FROM tickets GROUP BY status ORDER BY value DESC",
      description: "统计不同状态工单数量"
    },
    {
      name: "分类工单数量",
      reportType: "bar",
      tableHeaders: JSON.stringify(["分类", "数量"]),
      sqlText: "SELECT category AS label, COUNT(*) AS value FROM tickets GROUP BY category ORDER BY value DESC",
      description: "按工单分类展示处理量"
    },
    {
      name: "高优先级工单明细",
      reportType: "table",
      tableHeaders: JSON.stringify(["标题", "状态", "负责人", "创建时间"]),
      sqlText:
        "SELECT title, status, assignee, created_at FROM tickets WHERE priority = 'high' ORDER BY datetime(created_at) DESC",
      description: "展示高优工单详情"
    }
  ];

  for (const report of defaultReports) {
    await run(
      `INSERT INTO report_configs
      (name, report_type, table_headers, sql_text, api_config_id, description, created_at, updated_at)
      VALUES (?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))`,
      [report.name, report.reportType, report.tableHeaders, report.sqlText, apiConfigId, report.description]
    );
  }
}

async function initDatabase() {
  await run(`CREATE TABLE IF NOT EXISTS tickets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    category TEXT NOT NULL,
    status TEXT NOT NULL,
    priority TEXT NOT NULL,
    assignee TEXT NOT NULL,
    resolved_at TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
  )`);

  await run(`CREATE TABLE IF NOT EXISTS api_configs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    base_url TEXT NOT NULL,
    path TEXT NOT NULL,
    method TEXT NOT NULL,
    cookie TEXT DEFAULT '',
    timeout_ms INTEGER DEFAULT 10000,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
  )`);

  await run(`CREATE TABLE IF NOT EXISTS report_configs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    report_type TEXT NOT NULL,
    table_headers TEXT NOT NULL,
    sql_text TEXT NOT NULL,
    api_config_id INTEGER NOT NULL,
    description TEXT DEFAULT '',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(api_config_id) REFERENCES api_configs(id)
  )`);

  await seedTickets();
  await seedConfigurations();
}

module.exports = {
  db,
  run,
  get,
  all,
  initDatabase,
  isSelectSql
};
