package com.cyrilsx.russiancases.android.model

import org.json.JSONException
import org.json.JSONObject

data class ReportResult(
    val id: Long,
    val status: String
) {
    companion object {
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): ReportResult =
            ReportResult(json.getLong("id"), json.getString("status"))
    }
}
