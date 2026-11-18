package com.cyrilsx.russiancases.android.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuizSession {

    public final long id;
    public final int total;
    public final int answered;
    public final List<Question> questions;

    public QuizSession(long id, int total, int answered, List<Question> questions) {
        this.id = id;
        this.total = total;
        this.answered = answered;
        this.questions = questions;
    }

    public static QuizSession fromJson(JSONObject json) throws JSONException {
        JSONArray questionsJson = json.getJSONArray("questions");
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < questionsJson.length(); i++) {
            questions.add(Question.fromJson(questionsJson.getJSONObject(i)));
        }
        return new QuizSession(
                json.getLong("id"),
                json.getInt("total"),
                json.getInt("answered"),
                questions);
    }
}
