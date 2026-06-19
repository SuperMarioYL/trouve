package com.lei6393.trouve.server.controller;

import java.util.Collection;
import java.util.Map;

/**
 * 渲染轻量管理面板 HTML（自包含，服务端内联当前指标与健康实例，无需前端鉴权二次请求）。
 * 图标采用 Tabler 风格内联 SVG，配色贴合品牌青色。
 *
 * @author trouve
 */
final class DashboardRenderer {

    private DashboardRenderer() {
    }

    static String render(String namespace, Map<String, Long> metrics, Collection<String> healthInstances) {
        StringBuilder cards = new StringBuilder();
        appendCard(cards, "requests", "请求总数", metrics.getOrDefault("requests", 0L));
        appendCard(cards, "upstream", "上游响应", metrics.getOrDefault("upstreamResponses", 0L));
        appendCard(cards, "fail", "转发失败(502/504)", metrics.getOrDefault("forwardFailures", 0L));
        appendCard(cards, "retry", "重试(故障转移)", metrics.getOrDefault("retries", 0L));
        appendCard(cards, "reject", "限流拒绝(503)", metrics.getOrDefault("rejected", 0L));

        StringBuilder health = new StringBuilder();
        if (healthInstances == null || healthInstances.isEmpty()) {
            health.append("<div class='empty'>暂无健康实例</div>");
        } else {
            for (String id : healthInstances) {
                health.append("<div class='pill'><span class='dot'></span>").append(escape(id)).append("</div>");
            }
        }

        return "<!doctype html><html lang='zh'><head><meta charset='utf-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<meta http-equiv='refresh' content='5'>"
                + "<title>Trouve Gateway</title><style>"
                + ":root{--bg:#0a1622;--panel:#11333a;--line:#21505a;--teal:#2dd4bf;--tealh:#5eead4;--muted:#7fb4ba;--text:#eafbfb}"
                + "*{box-sizing:border-box}body{margin:0;font-family:-apple-system,Segoe UI,Helvetica,Arial,sans-serif;"
                + "background:linear-gradient(180deg,#0a1622,#0c2630);color:var(--text);padding:28px}"
                + ".wrap{max-width:980px;margin:0 auto}h1{font-size:22px;margin:0 0 2px;display:flex;align-items:center;gap:10px}"
                + ".sub{color:var(--muted);font-size:13px;margin-bottom:22px}"
                + ".grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:14px;margin-bottom:26px}"
                + ".card{background:var(--panel);border:1px solid var(--line);border-radius:14px;padding:16px 18px}"
                + ".card .k{color:var(--muted);font-size:12.5px;display:flex;align-items:center;gap:7px}"
                + ".card .v{font-size:30px;font-weight:800;margin-top:8px;color:var(--tealh)}"
                + "h2{font-size:15px;color:var(--text);margin:0 0 12px;display:flex;align-items:center;gap:8px}"
                + ".pills{display:flex;flex-wrap:wrap;gap:8px}.pill{background:var(--panel);border:1px solid var(--line);"
                + "border-radius:999px;padding:6px 12px;font-size:12.5px;color:#bfe9ec;display:flex;align-items:center;gap:7px}"
                + ".dot{width:8px;height:8px;border-radius:50%;background:var(--tealh)}"
                + ".empty{color:var(--muted);font-size:13px}svg{stroke:var(--teal);fill:none;stroke-width:2;"
                + "stroke-linecap:round;stroke-linejoin:round}.foot{margin-top:26px;color:var(--muted);font-size:12px}"
                + "</style></head><body><div class='wrap'>"
                + "<h1>" + icon("target") + "Trouve Gateway</h1>"
                + "<div class='sub'>namespace: " + escape(namespace == null ? "-" : namespace) + " · 每 5 秒自动刷新</div>"
                + "<div class='grid'>" + cards + "</div>"
                + "<h2>" + icon("heart") + "健康实例</h2><div class='pills'>" + health + "</div>"
                + "<div class='foot'>JSON: <code>/trouve/manager/metrics</code> · Prometheus: <code>/trouve/manager/prometheus</code></div>"
                + "</div></body></html>";
    }

    private static void appendCard(StringBuilder sb, String iconName, String label, long value) {
        sb.append("<div class='card'><div class='k'>").append(icon(iconName)).append(escape(label))
                .append("</div><div class='v'>").append(value).append("</div></div>");
    }

    private static String icon(String name) {
        // Tabler 风格内联 SVG（24x24）
        switch (name) {
            case "target":
                return "<svg width='22' height='22' viewBox='0 0 24 24'><circle cx='12' cy='12' r='9'/>"
                        + "<circle cx='12' cy='12' r='5'/><circle cx='12' cy='12' r='1'/></svg>";
            case "heart":
                return "<svg width='16' height='16' viewBox='0 0 24 24'><path d='M19.5 12.6 12 20l-7.5-7.4a5 5 0 1 1 7.5-6.6 5 5 0 1 1 7.5 6.6Z'/></svg>";
            case "requests":
                return "<svg width='15' height='15' viewBox='0 0 24 24'><path d='M5 12h14M13 6l6 6-6 6'/></svg>";
            case "upstream":
                return "<svg width='15' height='15' viewBox='0 0 24 24'><path d='M5 12h14M11 6l-6 6 6 6'/></svg>";
            case "fail":
                return "<svg width='15' height='15' viewBox='0 0 24 24'><path d='M12 9v4M12 17h.01M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z'/></svg>";
            case "retry":
                return "<svg width='15' height='15' viewBox='0 0 24 24'><path d='M21 12a9 9 0 1 1-3-6.7L21 8M21 3v5h-5'/></svg>";
            case "reject":
                return "<svg width='15' height='15' viewBox='0 0 24 24'><circle cx='12' cy='12' r='9'/><path d='M5.6 5.6 18.4 18.4'/></svg>";
            default:
                return "";
        }
    }

    private static String escape(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
