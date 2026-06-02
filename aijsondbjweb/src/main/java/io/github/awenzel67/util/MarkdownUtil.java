package io.github.awenzel67.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class MarkdownUtil {
    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

    public static String markdownToHtml(String markdown) {
        if (markdown == null) {
            return "";
        }
        Node document = PARSER.parse(markdown);
        return RENDERER.render(document);
    }
}
