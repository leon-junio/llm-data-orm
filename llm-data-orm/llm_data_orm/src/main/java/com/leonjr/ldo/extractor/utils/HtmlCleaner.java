package com.leonjr.ldo.extractor.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class HtmlCleaner {

    /**
     * Cleans HTML content by removing potentially unsafe elements and attributes
     * while preserving
     * essential formatting and structure elements.
     * 
     * <p>
     * This method uses a restrictive safelist that allows only specific HTML tags
     * and attributes
     * considered safe for display. The following elements are permitted:
     * </p>
     * 
     * <ul>
     * <li>Text formatting: p, h1-h6, b, i, u, strong, em, br, span</li>
     * <li>Lists: ul, ol, li</li>
     * <li>Tables: table, thead, tbody, tr, td, th</li>
     * </ul>
     * 
     * <p>
     * Additional rules applied:
     * </p>
     * <ul>
     * <li>span elements may contain style attributes</li>
     * <li>table elements will automatically have border="1" attribute enforced</li>
     * </ul>
     * 
     * @param html the raw HTML string to be cleaned
     * @return a cleaned HTML string with only safe elements and attributes
     *         preserved
     * @throws IllegalArgumentException if the input HTML is null
     */
    public static String cleanHtml(String html) {
        Safelist safelist = Safelist.none()
                .addTags("p", "h1", "h2", "h3", "h4", "h5", "h6",
                        "ul", "ol", "li", "b", "i", "u", "strong", "em", "br", "span",
                        "table", "thead", "tbody", "tr", "td", "th")
                .addAttributes("span", "style")
                .addEnforcedAttribute("table", "border", "1");
        return Jsoup.clean(html, safelist);
    }
}
