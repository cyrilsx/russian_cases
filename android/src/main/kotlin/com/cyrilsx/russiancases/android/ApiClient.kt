package com.cyrilsx.russiancases.android

import com.cyrilsx.russiancases.android.model.AnswerResult
import com.cyrilsx.russiancases.android.model.Level
import com.cyrilsx.russiancases.android.model.ProgressSummary
import com.cyrilsx.russiancases.android.model.Question
import com.cyrilsx.russiancases.android.model.QuizSession
import com.cyrilsx.russiancases.android.model.Rule
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale

class ApiClient(baseUrl: String, private val token: String?) {

    class ApiException(val status: Int, message: String) : Exception(message)

    class AuthResult {
        var token: String? = null
        var username: String? = null
    }

    private val baseUrl: String = baseUrl.trimEnd('/')

    @Throws(ApiException::class, IOException::class, JSONException::class)
    fun register(username: String, password: String): AuthResult =
        parseAuth(request("POST", "/api/auth/register", authBody(username, password)))

    @Throws(ApiException::class, IOException::class, JSONException::class)
    fun login(username: String, password: String): AuthResult =
        parseAuth(request("POST", "/api/auth/login", authBody(username, password)))

    @Throws(ApiException::class, IOException::class, JSONException::class)
    fun getLevels(): List<Level> {
        val array = JSONArray(request("GET", "/api/levels", null))
        return (0 until array.length()).map { Level.fromJson(array.getJSONObject(it)) }
    }

    @Throws(ApiException::class, IOException::class, JSONException::class)
    fun getQuestions(levelId: Long): List<Question> {
        val array = JSONArray(request("GET", "/api/levels/$levelId/questions", null))
        return (0 until array.length()).map { Question.fromJson(array.getJSONObject(it)) }
    }

    @Throws(ApiException::class, IOException::class, JSONException::class)
    fun answer(questionId: Long, optionIndex: Int): AnswerResult {
        val body = JSONObject().put("optionIndex", optionIndex)
        return AnswerResult.fromJson(JSONObject(request("POST", "/api/questions/$questionId/answer", body.toString())))
    }

    @Throws(ApiException::class, IOException::class, JSONException::class)
    fun createSession(levelId: Long): QuizSession =
        QuizSession.fromJson(JSONObject(request("POST", "/api/levels/$levelId/sessions", null)))

    @Throws(ApiException::class, IOException::class, JSONException::class)
    fun answerInSession(sessionId: Long, questionId: Long, optionIndex: Int): AnswerResult {
        val body = JSONObject().put("questionId", questionId).put("optionIndex", optionIndex)
        return AnswerResult.fromJson(JSONObject(request("POST", "/api/sessions/$sessionId/answer", body.toString())))
    }

    @Throws(ApiException::class, IOException::class, JSONException::class)
    fun getRule(caseName: String): Rule {
        val array = JSONArray(request("GET", "/api/rules?case=$caseName", null))
        if (array.length() == 0) {
            throw ApiException(404, "Not found")
        }
        return Rule.fromJson(array.getJSONObject(0))
    }

    @Throws(ApiException::class, IOException::class, JSONException::class)
    fun getSummary(): ProgressSummary =
        ProgressSummary.fromJson(JSONObject(request("GET", "/api/progress/me/summary", null)))

    private fun authBody(username: String, password: String): String =
        JSONObject().put("username", username).put("password", password).toString()

    @Throws(ApiException::class, IOException::class, JSONException::class)
    private fun parseAuth(text: String): AuthResult {
        val json = JSONObject(text)
        return AuthResult().apply {
            token = json.getString("token")
            username = json.getString("username")
        }
    }

    @Throws(ApiException::class, IOException::class, JSONException::class)
    private fun request(method: String, path: String, bodyJson: String?): String {
        val connection = URL(baseUrl + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            connection.setRequestProperty("Accept-Language", Locale.getDefault().language)
            if (token != null) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }
            if (bodyJson != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { out -> out.write(bodyJson.toByteArray(StandardCharsets.UTF_8)) }
            }
            val status = connection.responseCode
            val stream = if (status >= 400) connection.errorStream else connection.inputStream
            val text = readFully(stream)
            if (status >= 400) {
                var message = "HTTP $status"
                try {
                    val error = JSONObject(text)
                    if (error.has("message")) {
                        message = error.getString("message")
                    }
                } catch (ignored: Exception) {
                }
                throw ApiException(status, message)
            }
            return text
        } finally {
            connection.disconnect()
        }
    }

    private fun readFully(stream: InputStream?): String {
        if (stream == null) {
            return ""
        }
        val buffer = ByteArrayOutputStream()
        stream.use { input ->
            val chunk = ByteArray(4096)
            while (true) {
                val read = input.read(chunk)
                if (read == -1) break
                buffer.write(chunk, 0, read)
            }
        }
        return String(buffer.toByteArray(), StandardCharsets.UTF_8)
    }
}
