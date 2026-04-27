const http = axios.create({
  baseURL: "",
  timeout: 15000
});

new Vue({
  el: "#app",
  data() {
    return {
      apiConfigs: [],
      reportConfigs: [],
      activeReportId: null,
      activeReportMeta: null,
      reportData: {
        rows: [],
        tableHeaders: []
      },
      chartInstance: null,
      loading: {
        apis: false,
        reports: false,
        run: false
      },
      apiDialog: {
        visible: false,
        id: null,
        form: {
          name: "",
          baseUrl: "http://127.0.0.1:3000",
          path: "/api/data/query",
          method: "POST",
          cookie: "session_id=demo",
          timeoutMs: 10000
        }
      },
      reportDialog: {
        visible: false,
        id: null,
        form: {
          name: "",
          reportType: "bar",
          apiConfigId: null,
          tableHeaders: "",
          sqlText: "",
          description: ""
        }
      }
    };
  },
  computed: {
    tableColumns() {
      const rows = this.reportData.rows || [];
      const firstRow = rows[0] || {};
      const keys = Object.keys(firstRow);
      const headers = this.reportData.tableHeaders || [];
      return keys.map((key, index) => ({
        prop: key,
        label: headers[index] || key
      }));
    }
  },
  mounted() {
    window.addEventListener("resize", this.resizeChart);
    this.init();
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.resizeChart);
    if (this.chartInstance) {
      this.chartInstance.dispose();
      this.chartInstance = null;
    }
  },
  methods: {
    async init() {
      await Promise.all([this.fetchApis(), this.fetchReports()]);
      if (this.reportConfigs.length > 0) {
        this.activeReportId = this.reportConfigs[0].id;
        await this.runReport(this.reportConfigs[0]);
      }
    },
    showError(error, fallback) {
      const message = (error && error.response && error.response.data && error.response.data.message) || error.message || fallback;
      this.$message.error(message);
    },
    resetApiDialog() {
      this.apiDialog.id = null;
      this.apiDialog.form = {
        name: "",
        baseUrl: "http://127.0.0.1:3000",
        path: "/api/data/query",
        method: "POST",
        cookie: "session_id=demo",
        timeoutMs: 10000
      };
    },
    openApiDialog(item) {
      this.resetApiDialog();
      if (item) {
        this.apiDialog.id = item.id;
        this.apiDialog.form = {
          name: item.name || "",
          baseUrl: item.base_url || "",
          path: item.path || "",
          method: item.method || "POST",
          cookie: item.cookie || "",
          timeoutMs: item.timeout_ms || 10000
        };
      }
      this.apiDialog.visible = true;
    },
    async fetchApis() {
      this.loading.apis = true;
      try {
        const { data } = await http.get("/api/config/apis");
        this.apiConfigs = (data && data.data) || [];
      } catch (error) {
        this.showError(error, "加载 API 配置失败");
      } finally {
        this.loading.apis = false;
      }
    },
    async saveApi() {
      const payload = { ...this.apiDialog.form };
      if (!payload.name || !payload.baseUrl || !payload.path) {
        this.$message.warning("请完整填写 API 必填项");
        return;
      }
      try {
        if (this.apiDialog.id) {
          await http.put(`/api/config/apis/${this.apiDialog.id}`, payload);
          this.$message.success("API 配置已更新");
        } else {
          await http.post("/api/config/apis", payload);
          this.$message.success("API 配置已新增");
        }
        this.apiDialog.visible = false;
        await this.fetchApis();
      } catch (error) {
        this.showError(error, "保存 API 配置失败");
      }
    },
    async removeApi(item) {
      try {
        await this.$confirm(`确定删除 API「${item.name}」吗？`, "提示", { type: "warning" });
        await http.delete(`/api/config/apis/${item.id}`);
        this.$message.success("API 配置已删除");
        await Promise.all([this.fetchApis(), this.fetchReports()]);
      } catch (error) {
        if (error !== "cancel") {
          this.showError(error, "删除 API 配置失败");
        }
      }
    },
    resetReportDialog() {
      this.reportDialog.id = null;
      this.reportDialog.form = {
        name: "",
        reportType: "bar",
        apiConfigId: this.apiConfigs[0] ? this.apiConfigs[0].id : null,
        tableHeaders: "",
        sqlText: "",
        description: ""
      };
    },
    openReportDialog(item) {
      this.resetReportDialog();
      if (item) {
        this.reportDialog.id = item.id;
        this.reportDialog.form = {
          name: item.name || "",
          reportType: item.report_type || "bar",
          apiConfigId: item.api_config_id || null,
          tableHeaders: Array.isArray(item.table_headers) ? item.table_headers.join(",") : "",
          sqlText: item.sql_text || "",
          description: item.description || ""
        };
      }
      this.reportDialog.visible = true;
    },
    async fetchReports() {
      this.loading.reports = true;
      try {
        const { data } = await http.get("/api/config/reports");
        this.reportConfigs = (data && data.data) || [];
      } catch (error) {
        this.showError(error, "加载报表配置失败");
      } finally {
        this.loading.reports = false;
      }
    },
    async saveReport() {
      const form = this.reportDialog.form;
      if (!form.name || !form.reportType || !form.apiConfigId || !form.sqlText) {
        this.$message.warning("请完整填写报表必填项");
        return;
      }
      const payload = {
        ...form,
        tableHeaders: form.tableHeaders
          .split(",")
          .map((item) => item.trim())
          .filter(Boolean)
      };
      try {
        if (this.reportDialog.id) {
          await http.put(`/api/config/reports/${this.reportDialog.id}`, payload);
          this.$message.success("报表配置已更新");
        } else {
          await http.post("/api/config/reports", payload);
          this.$message.success("报表配置已新增");
        }
        this.reportDialog.visible = false;
        await this.fetchReports();
      } catch (error) {
        this.showError(error, "保存报表配置失败");
      }
    },
    async removeReport(item) {
      try {
        await this.$confirm(`确定删除报表「${item.name}」吗？`, "提示", { type: "warning" });
        await http.delete(`/api/config/reports/${item.id}`);
        this.$message.success("报表配置已删除");
        await this.fetchReports();
      } catch (error) {
        if (error !== "cancel") {
          this.showError(error, "删除报表失败");
        }
      }
    },
    async handleReportChange(id) {
      const report = this.reportConfigs.find((item) => item.id === id);
      if (!report) {
        return;
      }
      await this.runReport(report);
    },
    normalizeChartData(rows) {
      if (!Array.isArray(rows)) {
        return { labels: [], values: [] };
      }
      const labels = [];
      const values = [];
      rows.forEach((row, idx) => {
        const keys = Object.keys(row);
        if (!keys.length) {
          return;
        }
        const labelKey = keys[0];
        const valueKey = keys[1] || keys[0];
        labels.push(String(row[labelKey] === undefined ? `项${idx + 1}` : row[labelKey]));
        values.push(Number(row[valueKey]) || 0);
      });
      return { labels, values };
    },
    renderChart() {
      this.$nextTick(() => {
        if (!this.activeReportMeta || this.activeReportMeta.report_type === "table") {
          return;
        }
        const chartDom = document.getElementById("chart");
        if (!chartDom) {
          return;
        }
        if (!this.chartInstance) {
          this.chartInstance = echarts.init(chartDom);
        }
        const { labels, values } = this.normalizeChartData(this.reportData.rows);
        const isPie = this.activeReportMeta.report_type === "pie";
        const option = isPie
          ? {
              tooltip: { trigger: "item" },
              legend: { bottom: 0 },
              series: [
                {
                  type: "pie",
                  radius: ["30%", "65%"],
                  data: labels.map((name, index) => ({ name, value: values[index] })),
                  label: { formatter: "{b}: {c}" }
                }
              ]
            }
          : {
              tooltip: { trigger: "axis" },
              xAxis: { type: "category", data: labels, axisLabel: { interval: 0, rotate: labels.length > 6 ? 25 : 0 } },
              yAxis: { type: "value" },
              series: [
                {
                  type: "bar",
                  data: values,
                  barMaxWidth: 40,
                  itemStyle: { color: "#5b8ff9", borderRadius: [6, 6, 0, 0] }
                }
              ]
            };
        this.chartInstance.setOption(option, true);
      });
    },
    resizeChart() {
      if (this.chartInstance) {
        this.chartInstance.resize();
      }
    },
    async runReport(report) {
      this.loading.run = true;
      try {
        const { data } = await http.get(`/api/reports/run/${report.id}`);
        const payload = (data && data.data) || {};
        this.activeReportMeta = report;
        this.activeReportId = report.id;
        this.reportData = {
          rows: Array.isArray(payload.rows) ? payload.rows : [],
          tableHeaders: Array.isArray(payload.tableHeaders) ? payload.tableHeaders : []
        };
        if ((this.activeReportMeta.report_type || "").toLowerCase() !== "table") {
          this.renderChart();
        }
      } catch (error) {
        this.showError(error, "运行报表失败");
      } finally {
        this.loading.run = false;
      }
    }
  }
});
