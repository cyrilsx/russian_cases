package com.cyrilsx.russiancases.android.model

import org.json.JSONException
import org.json.JSONObject

data class AnswerResult(
    val correct: Boolean,
    val correctIndex: Int,
    val hint: String,
    val ruleTitle: String?,
    val ruleContent: String?,
    val answered: Int,
    val total: Int
) {
    companion object {
        @JvmStatic
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): AnswerResult = AnswerResult(
            correct = json.getBoolean("correct"),
            correctIndex = json.getInt("correctIndex"),
            hint = json.optString("hint"),
            ruleTitle = json.optString("ruleTitle", null),
            ruleContent = json.optString("ruleContent", null),
            answered = json.optInt("answered", 0),
            total = json.optInt("total", 0)
        )
    }
}
