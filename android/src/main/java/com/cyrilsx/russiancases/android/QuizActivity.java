package com.cyrilsx.russiancases.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cyrilsx.russiancases.android.model.AnswerResult;
import com.cyrilsx.russiancases.android.model.Question;
import com.cyrilsx.russiancases.android.model.Rule;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends Activity {

    private ApiClient api;
    private List<Question> questions = new ArrayList<>();
    private int index = 0;
    private boolean answered = false;

    private TextView titleText;
    private TextView counterText;
    private TextView caseText;
    private TextView promptText;
    private RadioGroup optionsGroup;
    private TextView feedbackText;
    private TextView hintText;
    private TextView ruleText;
    private Button submitButton;
    private Button hintButton;
    private Button ruleButton;
    private Button nextButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        api = new ApiClient(prefs.getString("base_url", getString(R.string.default_server_url)),
                prefs.getString("token", null));

        titleText = (TextView) findViewById(R.id.level_title);
        counterText = (TextView) findViewById(R.id.question_counter);
        caseText = (TextView) findViewById(R.id.question_case);
        promptText = (TextView) findViewById(R.id.question_prompt);
        optionsGroup = (RadioGroup) findViewById(R.id.options_group);
        feedbackText = (TextView) findViewById(R.id.feedback);
        hintText = (TextView) findViewById(R.id.hint_box);
        ruleText = (TextView) findViewById(R.id.rule_box);
        submitButton = (Button) findViewById(R.id.submit_button);
        hintButton = (Button) findViewById(R.id.hint_button);
        ruleButton = (Button) findViewById(R.id.rule_button);
        nextButton = (Button) findViewById(R.id.next_button);
        backButton = (Button) findViewById(R.id.back_button);

        titleText.setText(getIntent().getStringExtra("level_title"));

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAnswer();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index + 1 < questions.size()) {
                    index++;
                    showQuestion();
                } else {
                    finish();
                }
            }
        });
        hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = hintText.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
                hintText.setVisibility(visibility);
            }
        });
        ruleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRule();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadQuestions();
    }

    private void loadQuestions() {
        final long levelId = getIntent().getLongExtra("level_id", 0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Question> loaded = api.getQuestions(levelId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            questions = loaded;
                            if (!questions.isEmpty()) {
                                showQuestion();
                            } else {
                                feedbackText.setText(R.string.network_error);
                            }
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            feedbackText.setText(R.string.network_error);
                        }
                    });
                }
            }
        }).start();
    }

    private void showQuestion() {
        answered = false;
        Question q = questions.get(index);
        counterText.setText(getString(R.string.question_counter, index + 1, questions.size()));
        caseText.setText(q.caseLabel);
        promptText.setText(q.prompt);
        feedbackText.setText("");
        hintText.setText(q.hint);
        hintText.setVisibility(View.GONE);
        ruleText.setVisibility(View.GONE);
        submitButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        optionsGroup.removeAllViews();
        for (int i = 0; i < q.options.size(); i++) {
            RadioButton option = new RadioButton(this);
            option.setId(i);
            option.setText(q.options.get(i));
            optionsGroup.addView(option);
        }
        optionsGroup.clearCheck();
    }

    private void submitAnswer() {
        if (answered) {
            return;
        }
        final int selected = optionsGroup.getCheckedRadioButtonId();
        if (selected < 0) {
            return;
        }
        answered = true;
        final Question q = questions.get(index);
        submitButton.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final AnswerResult result = api.answer(q.id, selected);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            feedbackText.setText(result.correct ? R.string.correct : R.string.wrong);
                            hintText.setText(result.hint);
                            if (result.ruleTitle != null) {
                                ruleText.setText(result.ruleTitle + "\n\n" + result.ruleContent);
                            }
                            submitButton.setVisibility(View.GONE);
                            nextButton.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            answered = false;
                            submitButton.setEnabled(true);
                            feedbackText.setText(R.string.network_error);
                        }
                    });
                }
            }
        }).start();
    }

    private void loadRule() {
        final Question q = questions.get(index);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Rule rule = api.getRule(q.caseName);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ruleText.setText(rule.title + "\n\n" + rule.content);
                            ruleText.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ruleText.setText(R.string.network_error);
                            ruleText.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();
    }
}
