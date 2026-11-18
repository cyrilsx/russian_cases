package com.cyrilsx.russiancases.android.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Level {

    public final long id;
    public final String title;
    public final String description;
    public final long questionCount;

    public Level(long id, String title, String description, long questionCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.questionCount = questionCount;
    }

    public static Level fromJson(JSONObject json) throws JSONException {
        return new Level(
                json.getLong("id"),
                json.getString("title"),
                json.optString("description"),
                json.optLong("questionCount"));
    }
}
