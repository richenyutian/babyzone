const path = require("path");
const express = require("express");
const cors = require("cors");
const axios = require("axios");
const { initDatabase, all, get, run, isSelectSql } = require("./database");

const app = express();
const PORT = Number(process.env.PORT) || 3000;

app.use(cors());
app.use(express.json({ limit: "1mb" }));
app.use(express.static(path.join(__dirname, "..", "public")));

function normalizeMethod(method) {
  const fallback = "POST";
  if (!method || typeof method !== "string") {
    return fallback;
  }
  const upper = method.trim().toUpperCase();
  return ["GET", "POST"].includes(upper) ? upper : fallback;
}

function parseHeaders(rawHeaders) {
  if (!Array.isArray(rawHeaders)) {
    return [];
  }
  return rawHeaders
    .map((value) => (typeof value === "string" ? value.trim() : ""))
    .filter(Boolean);
}

app.get("/api/health", (req, res) => {
  res.json({ success: true, message: "ok" });
});

app.get("/api/config/apis", async (req, res) => {
  try {
    const rows = await all("SELECT * FROM api_configs ORDER BY id DESC");
    res.json({ success: true, data: rows });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

app.post("/api/config/apis", async (req, res) => {
  try {
    const { name, baseUrl, path: apiPath, method, cookie, timeoutMs } = req.body || {};
    if (!name || !baseUrl || !apiPath) {
      return res.status(400).json({ success: false, message: "name、baseUrl、path 不能为空" });
    }

    const created = await run(
      `INSERT INTO api_configs (name, base_url, path, method, cookie, timeout_ms, created_at, updated_at)
       VALUES (?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))`,
      [name.trim(), baseUrl.trim(), apiPath.trim(), normalizeMethod(method), cookie || "", Number(timeoutMs) || 10000]
    );

    const saved = await get("SELECT * FROM api_configs WHERE id = ?", [created.lastID]);
    return res.json({ success: true, data: saved });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

app.put("/api/config/apis/:id", async (req, res) => {
  try {
    const id = Number(req.params.id);
    if (!id) {
      return res.status(400).json({ success: false, message: "无效 id" });
    }

    const existing = await get("SELECT * FROM api_configs WHERE id = ?", [id]);
    if (!existing) {
      return res.status(404).json({ success: false, message: "API 配置不存在" });
    }

    const payload = req.body || {};
    const nextName = (payload.name || existing.name).trim();
    const nextBaseUrl = (payload.baseUrl || existing.base_url).trim();
    const nextPath = (payload.path || existing.path).trim();

    await run(
      `UPDATE api_configs
       SET name = ?, base_url = ?, path = ?, method = ?, cookie = ?, timeout_ms = ?, updated_at = datetime('now')
       WHERE id = ?`,
      [
        nextName,
        nextBaseUrl,
        nextPath,
        normalizeMethod(payload.method || existing.method),
        payload.cookie !== undefined ? payload.cookie : existing.cookie,
        Number(payload.timeoutMs || existing.timeout_ms) || 10000,
        id
      ]
    );

    const updated = await get("SELECT * FROM api_configs WHERE id = ?", [id]);
    return res.json({ success: true, data: updated });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

app.delete("/api/config/apis/:id", async (req, res) => {
  try {
    const id = Number(req.params.id);
    if (!id) {
      return res.status(400).json({ success: false, message: "无效 id" });
    }

    const relatedReport = await get("SELECT id FROM report_configs WHERE api_config_id = ? LIMIT 1", [id]);
    if (relatedReport) {
      return res.status(400).json({ success: false, message: "该 API 已被报表使用，无法删除" });
    }

    await run("DELETE FROM api_configs WHERE id = ?", [id]);
    return res.json({ success: true });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

app.get("/api/config/reports", async (req, res) => {
  try {
    const rows = await all(
      `SELECT r.*, a.name AS api_name
       FROM report_configs r
       LEFT JOIN api_configs a ON a.id = r.api_config_id
       ORDER BY r.id DESC`
    );
    const mapped = rows.map((item) => ({
      ...item,
      table_headers: (() => {
        try {
          return JSON.parse(item.table_headers || "[]");
        } catch (error) {
          return [];
        }
      })()
    }));
    res.json({ success: true, data: mapped });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

app.post("/api/config/reports", async (req, res) => {
  try {
    const { name, reportType, tableHeaders, sqlText, apiConfigId, description } = req.body || {};
    if (!name || !reportType || !sqlText || !apiConfigId) {
      return res.status(400).json({ success: false, message: "name、reportType、sqlText、apiConfigId 不能为空" });
    }
    if (!isSelectSql(sqlText)) {
      return res.status(400).json({ success: false, message: "仅允许 SELECT/CTE 查询语句" });
    }

    const apiExists = await get("SELECT id FROM api_configs WHERE id = ?", [Number(apiConfigId)]);
    if (!apiExists) {
      return res.status(400).json({ success: false, message: "关联 API 不存在" });
    }

    const headers = parseHeaders(tableHeaders);
    const created = await run(
      `INSERT INTO report_configs
      (name, report_type, table_headers, sql_text, api_config_id, description, created_at, updated_at)
      VALUES (?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))`,
      [name.trim(), reportType, JSON.stringify(headers), sqlText.trim(), Number(apiConfigId), description || ""]
    );

    const saved = await get("SELECT * FROM report_configs WHERE id = ?", [created.lastID]);
    saved.table_headers = JSON.parse(saved.table_headers || "[]");
    return res.json({ success: true, data: saved });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

app.put("/api/config/reports/:id", async (req, res) => {
  try {
    const id = Number(req.params.id);
    if (!id) {
      return res.status(400).json({ success: false, message: "无效 id" });
    }
    const existing = await get("SELECT * FROM report_configs WHERE id = ?", [id]);
    if (!existing) {
      return res.status(404).json({ success: false, message: "报表配置不存在" });
    }

    const payload = req.body || {};
    const nextSql = (payload.sqlText || existing.sql_text).trim();
    if (!isSelectSql(nextSql)) {
      return res.status(400).json({ success: false, message: "仅允许 SELECT/CTE 查询语句" });
    }

    const nextApiId = Number(payload.apiConfigId || existing.api_config_id);
    const apiExists = await get("SELECT id FROM api_configs WHERE id = ?", [nextApiId]);
    if (!apiExists) {
      return res.status(400).json({ success: false, message: "关联 API 不存在" });
    }

    await run(
      `UPDATE report_configs
       SET name = ?, report_type = ?, table_headers = ?, sql_text = ?, api_config_id = ?, description = ?, updated_at = datetime('now')
       WHERE id = ?`,
      [
        (payload.name || existing.name).trim(),
        payload.reportType || existing.report_type,
        JSON.stringify(parseHeaders(payload.tableHeaders || JSON.parse(existing.table_headers || "[]"))),
        nextSql,
        nextApiId,
        payload.description !== undefined ? payload.description : existing.description,
        id
      ]
    );

    const updated = await get("SELECT * FROM report_configs WHERE id = ?", [id]);
    updated.table_headers = JSON.parse(updated.table_headers || "[]");
    return res.json({ success: true, data: updated });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

app.delete("/api/config/reports/:id", async (req, res) => {
  try {
    const id = Number(req.params.id);
    if (!id) {
      return res.status(400).json({ success: false, message: "无效 id" });
    }
    await run("DELETE FROM report_configs WHERE id = ?", [id]);
    return res.json({ success: true });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

app.post("/api/data/query", async (req, res) => {
  try {
    const { sql } = req.body || {};
    if (!sql || typeof sql !== "string") {
      return res.status(400).json({ success: false, message: "sql 不能为空" });
    }
    if (!isSelectSql(sql)) {
      return res.status(400).json({ success: false, message: "仅允许 SELECT/CTE 查询语句" });
    }
    const rows = await all(sql);
    return res.json({ success: true, data: rows });
  } catch (error) {
    return res.status(500).json({ success: false, message: `SQL 执行失败: ${error.message}` });
  }
});

app.get("/api/reports/run/:id", async (req, res) => {
  try {
    const reportId = Number(req.params.id);
    if (!reportId) {
      return res.status(400).json({ success: false, message: "无效报表 id" });
    }

    const report = await get("SELECT * FROM report_configs WHERE id = ?", [reportId]);
    if (!report) {
      return res.status(404).json({ success: false, message: "报表配置不存在" });
    }

    const apiConfig = await get("SELECT * FROM api_configs WHERE id = ?", [report.api_config_id]);
    if (!apiConfig) {
      return res.status(400).json({ success: false, message: "关联 API 配置不存在" });
    }

    const queryUrl = `${apiConfig.base_url.replace(/\/+$/, "")}/${apiConfig.path.replace(/^\/+/, "")}`;
    const response = await axios({
      method: normalizeMethod(apiConfig.method),
      url: queryUrl,
      timeout: Number(apiConfig.timeout_ms) || 10000,
      headers: apiConfig.cookie ? { Cookie: apiConfig.cookie } : {},
      data: { sql: report.sql_text }
    });

    const payload = response.data || {};
    if (!payload.success) {
      return res.status(400).json({ success: false, message: payload.message || "报表数据查询失败" });
    }

    return res.json({
      success: true,
      data: {
        reportId: report.id,
        reportName: report.name,
        reportType: report.report_type,
        tableHeaders: JSON.parse(report.table_headers || "[]"),
        rows: payload.data || []
      }
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

app.use((error, req, res, next) => {
  if (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
  return next();
});

async function bootstrap() {
  try {
    await initDatabase();
    app.listen(PORT, () => {
      console.log(`report-tool server started at http://127.0.0.1:${PORT}`);
    });
  } catch (error) {
    console.error("failed to bootstrap server:", error);
    process.exit(1);
  }
}

bootstrap();
