package com.cyrilsx.russiancases.android.model;

import org.json.JSONException;
import org.json.JSONObject;

public class AnswerResult {

    public final boolean correct;
    public final int correctIndex;
    public final String hint;
    public final String ruleTitle;
    public final String ruleContent;

    public AnswerResult(boolean correct, int correctIndex, String hint, String ruleTitle, String ruleContent) {
        this.correct = correct;
        this.correctIndex = correctIndex;
        this.hint = hint;
        this.ruleTitle = ruleTitle;
        this.ruleContent = ruleContent;
    }

    public static AnswerResult fromJson(JSONObject json) throws JSONException {
        return new AnswerResult(
                json.getBoolean("correct"),
                json.getInt("correctIndex"),
                json.optString("hint"),
                json.optString("ruleTitle", null),
                json.optString("ruleContent", null));
    }
}
