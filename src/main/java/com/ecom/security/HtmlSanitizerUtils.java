package com.ecom.security;


import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class HtmlSanitizerUtils {

    private static final Safelist QUILL_SAFE_LIST =
            Safelist.relaxed()
                    .addTags("span")
                    .addAttributes("span", "style", "class")
                    .addAttributes("p", "style", "class")
                    .addAttributes("img", "src", "alt", "title");

    public String sanitizeQuillHtml(String html) {
        if (html == null) {
            return null;
        }
        return Jsoup.clean(html, QUILL_SAFE_LIST);
    }
}
