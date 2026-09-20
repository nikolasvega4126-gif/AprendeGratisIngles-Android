package com.aprendegratisingles.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostItem {
    public String title;
    public String url;
    public String summary;
    public String contentHtml;
    public String published;
    public List<String> labels = new ArrayList<>();

    public static PostItem fromJson(JSONObject entry) {
        PostItem p = new PostItem();
        p.title = entry.optJSONObject("title") != null ? entry.optJSONObject("title").optString("$t", "Lección") : "Lección";
        p.summary = entry.optJSONObject("summary") != null ? entry.optJSONObject("summary").optString("$t", "") : "";
        p.contentHtml = entry.optJSONObject("content") != null ? entry.optJSONObject("content").optString("$t", "") : "";
        p.published = entry.optJSONObject("published") != null ? entry.optJSONObject("published").optString("$t", "") : "";

        JSONArray links = entry.optJSONArray("link");
        if (links != null) {
            for (int i = 0; i < links.length(); i++) {
                JSONObject link = links.optJSONObject(i);
                if (link != null && "alternate".equals(link.optString("rel"))) {
                    p.url = link.optString("href", "");
                    break;
                }
            }
        }

        JSONArray categories = entry.optJSONArray("category");
        if (categories != null) {
            for (int i = 0; i < categories.length(); i++) {
                JSONObject c = categories.optJSONObject(i);
                if (c != null) {
                    String term = c.optString("term", "");
                    if (!term.isEmpty()) p.labels.add(term);
                }
            }
        }
        return p;
    }
}
