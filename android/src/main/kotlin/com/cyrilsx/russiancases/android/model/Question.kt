package com.cyrilsx.russiancases.android.model

import org.json.JSONException
import org.json.JSONObject

data class Question(
    val id: Long,
    val caseName: String,
    val caseLabel: String,
    val prompt: String,
    val options: List<String>,
    val hint: String
) {
    companion object {
        @JvmStatic
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): Question {
            val optionsJson = json.getJSONArray("options")
            val options = (0 until optionsJson.length()).map { optionsJson.getString(it) }
            return Question(
                id = json.getLong("id"),
                caseName = json.getString("caseName"),
                caseLabel = json.getString("caseLabel"),
                prompt = json.getString("prompt"),
                options = options,
                hint = json.optString("hint")
            )
        }
    }
}
