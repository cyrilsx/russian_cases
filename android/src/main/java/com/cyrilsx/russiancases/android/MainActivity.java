package com.cyrilsx.russiancases.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String[] QUESTIONS = {
            "Это ___ (книга).",
            "Я читаю ___ (книга).",
            "У меня нет ___ (сестра).",
            "Я пишу ___ (ручка).",
            "Они живут в ___ (Москва)."
    };

    private int questionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView question = (TextView) findViewById(R.id.question);
        Button nextButton = (Button) findViewById(R.id.next_button);

        question.setText(QUESTIONS[questionIndex]);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionIndex = (questionIndex + 1) % QUESTIONS.length;
                question.setText(QUESTIONS[questionIndex]);
            }
        });
    }
}
