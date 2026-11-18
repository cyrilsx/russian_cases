package com.cyrilsx.russiancases.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlin.concurrent.thread

class LoginActivity : Activity() {

    private lateinit var serverUrlInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var errorText: TextView
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        serverUrlInput = findViewById(R.id.server_url)
        usernameInput = findViewById(R.id.username)
        passwordInput = findViewById(R.id.password)
        errorText = findViewById(R.id.auth_error)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        serverUrlInput.setText(prefs.getString("base_url", getString(R.string.default_server_url)))
        usernameInput.setText(prefs.getString("username", ""))

        loginButton.setOnClickListener { authenticate(false) }
        registerButton.setOnClickListener { authenticate(true) }
    }

    private fun authenticate(register: Boolean) {
        val baseUrl = serverUrlInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString()
        setBusy(true)
        errorText.setText("")
        thread {
            try {
                val client = ApiClient(baseUrl, null)
                val result = if (register) client.register(username, password) else client.login(username, password)
                getSharedPreferences("auth", MODE_PRIVATE).edit()
                    .putString("base_url", baseUrl)
                    .putString("username", result.username)
                    .putString("token", result.token)
                    .apply()
                runOnUiThread {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            } catch (e: ApiClient.ApiException) {
                showError(if (e.status == 409) R.string.username_taken else R.string.auth_error)
            } catch (e: Exception) {
                showError(R.string.network_error)
            }
        }
    }

    private fun showError(messageRes: Int) {
        runOnUiThread {
            setBusy(false)
            errorText.setText(messageRes)
        }
    }

    private fun setBusy(busy: Boolean) {
        loginButton.isEnabled = !busy
        registerButton.isEnabled = !busy
    }
}
