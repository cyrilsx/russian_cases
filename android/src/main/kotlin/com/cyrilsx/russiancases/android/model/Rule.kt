package com.cyrilsx.russiancases.android.model

import org.json.JSONException
import org.json.JSONObject

data class Rule(
    val caseLabel: String,
    val title: String,
    val content: String
) {
    companion object {
        @JvmStatic
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): Rule = Rule(
            caseLabel = json.getString("caseLabel"),
            title = json.getString("title"),
            content = json.getString("content")
        )
    }
}
