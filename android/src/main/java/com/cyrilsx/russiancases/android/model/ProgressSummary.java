package com.cyrilsx.russiancases.android.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProgressSummary {

    public final long totalAttempts;
    public final long totalCorrect;
    public final int percent;
    public final List<LevelProgress> levels;

    public ProgressSummary(long totalAttempts, long totalCorrect, int percent, List<LevelProgress> levels) {
        this.totalAttempts = totalAttempts;
        this.totalCorrect = totalCorrect;
        this.percent = percent;
        this.levels = levels;
    }

    public static ProgressSummary fromJson(JSONObject json) throws JSONException {
        JSONArray levelsJson = json.getJSONArray("levels");
        List<LevelProgress> levels = new ArrayList<>();
        for (int i = 0; i < levelsJson.length(); i++) {
            JSONObject l = levelsJson.getJSONObject(i);
            levels.add(new LevelProgress(
                    l.getString("title"),
                    l.getLong("correct"),
                    l.getLong("total"),
                    l.getInt("percent")));
        }
        return new ProgressSummary(
                json.getLong("totalAttempts"),
                json.getLong("totalCorrect"),
                json.getInt("percent"),
                levels);
    }

    public static class LevelProgress {
        public final String title;
        public final long correct;
        public final long total;
        public final int percent;

        public LevelProgress(String title, long correct, long total, int percent) {
            this.title = title;
            this.correct = correct;
            this.total = total;
            this.percent = percent;
        }
    }
}
