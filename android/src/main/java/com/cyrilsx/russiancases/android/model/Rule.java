package com.cyrilsx.russiancases.android.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Rule {

    public final String caseLabel;
    public final String title;
    public final String content;

    public Rule(String caseLabel, String title, String content) {
        this.caseLabel = caseLabel;
        this.title = title;
        this.content = content;
    }

    public static Rule fromJson(JSONObject json) throws JSONException {
        return new Rule(
                json.getString("caseLabel"),
                json.getString("title"),
                json.getString("content"));
    }
}
