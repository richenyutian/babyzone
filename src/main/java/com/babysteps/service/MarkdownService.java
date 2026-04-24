package com.babysteps.service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

@Service
public class MarkdownService {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .escapeHtml(true)
            .sanitizeUrls(true)
            .softbreak("<br />\n")
            .build();

    public String renderHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "<p class=\"empty-markdown\">开始输入 Markdown，右侧会实时显示预览。</p>";
        }
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
