package com.cyrilsx.russiancases.android.model

import org.json.JSONException
import org.json.JSONObject

data class ProgressSummary(
    val totalAttempts: Long,
    val totalCorrect: Long,
    val percent: Int,
    val levels: List<LevelProgress>,
) {
    data class LevelProgress(
        val title: String,
        val correct: Long,
        val total: Long,
        val percent: Int,
    )

    companion object {
        @JvmStatic
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): ProgressSummary {
            val levelsJson = json.getJSONArray("levels")
            val levels = (0 until levelsJson.length()).map { i ->
                val l = levelsJson.getJSONObject(i)
                LevelProgress(
                    title = l.getString("title"),
                    correct = l.getLong("correct"),
                    total = l.getLong("total"),
                    percent = l.getInt("percent"),
                )
            }
            return ProgressSummary(
                totalAttempts = json.getLong("totalAttempts"),
                totalCorrect = json.getLong("totalCorrect"),
                percent = json.getInt("percent"),
                levels = levels,
            )
        }
    }
}
