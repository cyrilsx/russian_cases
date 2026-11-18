package com.cyrilsx.russiancases.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

    private EditText serverUrlInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private TextView errorText;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        serverUrlInput = (EditText) findViewById(R.id.server_url);
        usernameInput = (EditText) findViewById(R.id.username);
        passwordInput = (EditText) findViewById(R.id.password);
        errorText = (TextView) findViewById(R.id.auth_error);
        loginButton = (Button) findViewById(R.id.login_button);
        registerButton = (Button) findViewById(R.id.register_button);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        serverUrlInput.setText(prefs.getString("base_url", getString(R.string.default_server_url)));
        usernameInput.setText(prefs.getString("username", ""));

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate(false);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate(true);
            }
        });
    }

    private void authenticate(final boolean register) {
        final String baseUrl = serverUrlInput.getText().toString().trim();
        final String username = usernameInput.getText().toString().trim();
        final String password = passwordInput.getText().toString();
        setBusy(true);
        errorText.setText("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ApiClient client = new ApiClient(baseUrl, null);
                    ApiClient.AuthResult result = register
                            ? client.register(username, password)
                            : client.login(username, password);
                    SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                    prefs.edit()
                            .putString("base_url", baseUrl)
                            .putString("username", result.username)
                            .putString("token", result.token)
                            .apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } catch (final ApiClient.ApiException e) {
                    showError(e.status == 409 ? R.string.username_taken : R.string.auth_error);
                } catch (final Exception e) {
                    showError(R.string.network_error);
                }
            }
        }).start();
    }

    private void showError(final int messageRes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setBusy(false);
                errorText.setText(messageRes);
            }
        });
    }

    private void setBusy(boolean busy) {
        loginButton.setEnabled(!busy);
        registerButton.setEnabled(!busy);
    }
}
