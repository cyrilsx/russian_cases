package com.cyrilsx.russiancases.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.cyrilsx.russiancases.android.model.Level;
import com.cyrilsx.russiancases.android.model.ProgressSummary;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private ApiClient api;
    private List<Level> levels = new ArrayList<>();
    private ArrayAdapter<String> levelsAdapter;
    private TextView userText;
    private TextView summaryText;
    private ListView levelsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            startLogin();
            return;
        }
        api = new ApiClient(prefs.getString("base_url", getString(R.string.default_server_url)), token);

        setContentView(R.layout.activity_main);
        userText = (TextView) findViewById(R.id.current_user);
        summaryText = (TextView) findViewById(R.id.summary);
        levelsList = (ListView) findViewById(R.id.levels_list);
        Button logoutButton = (Button) findViewById(R.id.logout_button);

        userText.setText(getString(R.string.logged_in_as, prefs.getString("username", "")));
        levelsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        levelsList.setAdapter(levelsAdapter);
        levelsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Level level = levels.get(position);
                Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                intent.putExtra("level_id", level.id);
                intent.putExtra("level_title", level.title);
                startActivity(intent);
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply();
                startLogin();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (api != null) {
            loadData();
        }
    }

    private void startLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadData() {
        summaryText.setText(R.string.loading);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Level> loadedLevels = api.getLevels();
                    final ProgressSummary summary = api.getSummary();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            levels = loadedLevels;
                            levelsAdapter.clear();
                            for (Level level : levels) {
                                levelsAdapter.add(level.title + " (" + level.questionCount + ")");
                            }
                            levelsAdapter.notifyDataSetChanged();
                            StringBuilder text = new StringBuilder();
                            text.append(getString(R.string.overall_score, summary.totalCorrect, summary.totalAttempts, summary.percent));
                            for (ProgressSummary.LevelProgress lp : summary.levels) {
                                text.append("\n").append(lp.title).append(" : ")
                                        .append(lp.correct).append("/").append(lp.total)
                                        .append(" (").append(lp.percent).append(" %)");
                            }
                            summaryText.setText(text.toString());
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            summaryText.setText(R.string.network_error);
                        }
                    });
                }
            }
        }).start();
    }
}
