package com.cyrilsx.russiancases.android.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Question {

    public final long id;
    public final String caseName;
    public final String caseLabel;
    public final String prompt;
    public final List<String> options;
    public final String hint;

    public Question(long id, String caseName, String caseLabel, String prompt, List<String> options, String hint) {
        this.id = id;
        this.caseName = caseName;
        this.caseLabel = caseLabel;
        this.prompt = prompt;
        this.options = options;
        this.hint = hint;
    }

    public static Question fromJson(JSONObject json) throws JSONException {
        JSONArray optionsJson = json.getJSONArray("options");
        List<String> options = new ArrayList<>();
        for (int i = 0; i < optionsJson.length(); i++) {
            options.add(optionsJson.getString(i));
        }
        return new Question(
                json.getLong("id"),
                json.getString("caseName"),
                json.getString("caseLabel"),
                json.getString("prompt"),
                options,
                json.optString("hint"));
    }
}
