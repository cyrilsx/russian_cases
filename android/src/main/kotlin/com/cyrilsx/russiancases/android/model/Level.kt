package com.cyrilsx.russiancases.android.model

import org.json.JSONException
import org.json.JSONObject

data class Level(
    val id: Long,
    val title: String,
    val description: String,
    val questionCount: Long
) {
    companion object {
        @JvmStatic
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): Level = Level(
            id = json.getLong("id"),
            title = json.getString("title"),
            description = json.optString("description"),
            questionCount = json.optLong("questionCount")
        )
    }
}
