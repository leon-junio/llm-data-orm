package com.leonjr.ldo.extractor.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class HtmlCleaner {

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
