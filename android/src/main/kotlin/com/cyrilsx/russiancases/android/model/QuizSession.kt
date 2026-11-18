package com.cyrilsx.russiancases.android.model

import org.json.JSONException
import org.json.JSONObject

data class QuizSession(
    val id: Long,
    val total: Int,
    val answered: Int,
    val questions: List<Question>,
) {
    companion object {
        @JvmStatic
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): QuizSession {
            val questionsJson = json.getJSONArray("questions")
            val questions = (0 until questionsJson.length()).map { Question.fromJson(questionsJson.getJSONObject(it)) }
            return QuizSession(
                id = json.getLong("id"),
                total = json.getInt("total"),
                answered = json.getInt("answered"),
                questions = questions,
            )
        }
    }
}
