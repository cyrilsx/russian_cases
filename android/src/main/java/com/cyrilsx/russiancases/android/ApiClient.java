package com.cyrilsx.russiancases.android;

import com.cyrilsx.russiancases.android.model.AnswerResult;
import com.cyrilsx.russiancases.android.model.Level;
import com.cyrilsx.russiancases.android.model.ProgressSummary;
import com.cyrilsx.russiancases.android.model.Question;
import com.cyrilsx.russiancases.android.model.QuizSession;
import com.cyrilsx.russiancases.android.model.Rule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApiClient {

    public static class ApiException extends Exception {
        public final int status;

        public ApiException(int status, String message) {
            super(message);
            this.status = status;
        }
    }

    public static class AuthResult {
        public String token;
        public String username;
    }

    private final String baseUrl;
    private final String token;

    public ApiClient(String baseUrl, String token) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.token = token;
    }

    public AuthResult register(String username, String password) throws ApiException, IOException, JSONException {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);
        return parseAuth(request("POST", "/api/auth/register", body.toString()));
    }

    public AuthResult login(String username, String password) throws ApiException, IOException, JSONException {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);
        return parseAuth(request("POST", "/api/auth/login", body.toString()));
    }

    public List<Level> getLevels() throws ApiException, IOException, JSONException {
        JSONArray array = new JSONArray(request("GET", "/api/levels", null));
        List<Level> levels = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            levels.add(Level.fromJson(array.getJSONObject(i)));
        }
        return levels;
    }

    public List<Question> getQuestions(long levelId) throws ApiException, IOException, JSONException {
        JSONArray array = new JSONArray(request("GET", "/api/levels/" + levelId + "/questions", null));
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            questions.add(Question.fromJson(array.getJSONObject(i)));
        }
        return questions;
    }

    public AnswerResult answer(long questionId, int optionIndex) throws ApiException, IOException, JSONException {
        JSONObject body = new JSONObject();
        body.put("optionIndex", optionIndex);
        return AnswerResult.fromJson(new JSONObject(request("POST", "/api/questions/" + questionId + "/answer", body.toString())));
    }

    public QuizSession createSession(long levelId) throws ApiException, IOException, JSONException {
        return QuizSession.fromJson(new JSONObject(request("POST", "/api/levels/" + levelId + "/sessions", null)));
    }

    public AnswerResult answerInSession(long sessionId, long questionId, int optionIndex)
            throws ApiException, IOException, JSONException {
        JSONObject body = new JSONObject();
        body.put("questionId", questionId);
        body.put("optionIndex", optionIndex);
        return AnswerResult.fromJson(new JSONObject(request("POST", "/api/sessions/" + sessionId + "/answer", body.toString())));
    }

    public Rule getRule(String caseName) throws ApiException, IOException, JSONException {
        JSONArray array = new JSONArray(request("GET", "/api/rules?case=" + caseName, null));
        if (array.length() == 0) {
            throw new ApiException(404, "Not found");
        }
        return Rule.fromJson(array.getJSONObject(0));
    }

    public ProgressSummary getSummary() throws ApiException, IOException, JSONException {
        return ProgressSummary.fromJson(new JSONObject(request("GET", "/api/progress/me/summary", null)));
    }

    private AuthResult parseAuth(String text) throws ApiException, IOException, JSONException {
        JSONObject json = new JSONObject(text);
        AuthResult result = new AuthResult();
        result.token = json.getString("token");
        result.username = json.getString("username");
        return result;
    }

    private String request(String method, String path, String bodyJson) throws ApiException, IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Accept-Language", Locale.getDefault().getLanguage());
            if (token != null) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }
            if (bodyJson != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                OutputStream out = connection.getOutputStream();
                out.write(bodyJson.getBytes(StandardCharsets.UTF_8));
                out.flush();
                out.close();
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String text = readFully(stream);
            if (status >= 400) {
                String message = "HTTP " + status;
                try {
                    JSONObject error = new JSONObject(text);
                    if (error.has("message")) {
                        message = error.getString("message");
                    }
                } catch (Exception ignored) {
                    // keep default message
                }
                throw new ApiException(status, message);
            }
            return text;
        } finally {
            connection.disconnect();
        }
    }

    private static String readFully(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read;
        while ((read = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        stream.close();
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
