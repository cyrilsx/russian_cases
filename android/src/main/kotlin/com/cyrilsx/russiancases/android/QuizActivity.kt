package com.cyrilsx.russiancases.android

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.cyrilsx.russiancases.android.model.AnswerResult
import com.cyrilsx.russiancases.android.model.Question
import com.cyrilsx.russiancases.android.model.QuizSession
import kotlin.concurrent.thread

class QuizActivity : Activity() {

    private lateinit var api: ApiClient
    private var session: QuizSession? = null
    private var questions: List<Question> = emptyList()
    private var index = 0
    private var answered = false
    private var correctCount = 0

    private lateinit var titleText: TextView
    private lateinit var counterText: TextView
    private lateinit var caseText: TextView
    private lateinit var promptText: TextView
    private lateinit var optionsGroup: RadioGroup
    private lateinit var feedbackText: TextView
    private lateinit var hintText: TextView
    private lateinit var ruleText: TextView
    private lateinit var submitButton: Button
    private lateinit var hintButton: Button
    private lateinit var ruleButton: Button
    private lateinit var nextButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        api = ApiClient(
            prefs.getString("base_url", getString(R.string.default_server_url)),
            prefs.getString("token", null)
        )

        titleText = findViewById(R.id.level_title) as TextView
        counterText = findViewById(R.id.question_counter) as TextView
        caseText = findViewById(R.id.question_case) as TextView
        promptText = findViewById(R.id.question_prompt) as TextView
        optionsGroup = findViewById(R.id.options_group) as RadioGroup
        feedbackText = findViewById(R.id.feedback) as TextView
        hintText = findViewById(R.id.hint_box) as TextView
        ruleText = findViewById(R.id.rule_box) as TextView
        submitButton = findViewById(R.id.submit_button) as Button
        hintButton = findViewById(R.id.hint_button) as Button
        ruleButton = findViewById(R.id.rule_button) as Button
        nextButton = findViewById(R.id.next_button) as Button
        backButton = findViewById(R.id.back_button) as Button

        titleText.text = intent.getStringExtra("level_title")

        submitButton.setOnClickListener { submitAnswer() }
        nextButton.setOnClickListener {
            if (index + 1 < questions.size) {
                index++
                showQuestion()
            } else {
                finish()
            }
        }
        hintButton.setOnClickListener {
            hintText.visibility = if (hintText.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        ruleButton.setOnClickListener { loadRule() }
        backButton.setOnClickListener { finish() }

        loadSession()
    }

    private fun loadSession() {
        val levelId = intent.getLongExtra("level_id", 0)
        thread {
            try {
                val loaded = api.createSession(levelId)
                runOnUiThread {
                    session = loaded
                    questions = loaded.questions
                    if (questions.isNotEmpty()) {
                        showQuestion()
                    } else {
                        feedbackText.setText(R.string.network_error)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { feedbackText.setText(R.string.network_error) }
            }
        }
    }

    private fun showQuestion() {
        answered = false
        val q = questions[index]
        counterText.setText(getString(R.string.question_counter, index + 1, questions.size))
        caseText.text = q.caseLabel
        promptText.text = q.prompt
        feedbackText.setText("")
        hintText.text = q.hint
        hintText.visibility = View.GONE
        ruleText.visibility = View.GONE
        submitButton.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
        optionsGroup.removeAllViews()
        q.options.forEachIndexed { i, optionText ->
            val option = RadioButton(this)
            option.id = i
            option.text = optionText
            optionsGroup.addView(option)
        }
        optionsGroup.clearCheck()
    }

    private fun submitAnswer() {
        if (answered) {
            return
        }
        val selected = optionsGroup.checkedRadioButtonId
        if (selected < 0) {
            return
        }
        answered = true
        val q = questions[index]
        val currentSession = session ?: return
        submitButton.isEnabled = false
        thread {
            try {
                val result = api.answerInSession(currentSession.id, q.id, selected)
                runOnUiThread { onAnswerResult(result) }
            } catch (e: Exception) {
                runOnUiThread {
                    answered = false
                    submitButton.isEnabled = true
                    feedbackText.setText(R.string.network_error)
                }
            }
        }
    }

    private fun onAnswerResult(result: AnswerResult) {
        if (result.correct) {
            correctCount++
        }
        val base = getString(if (result.correct) R.string.correct else R.string.wrong)
        feedbackText.text = if (result.answered == result.total) {
            "$base ${getString(R.string.quiz_done, correctCount, result.total)}"
        } else {
            base
        }
        hintText.text = result.hint
        if (result.ruleTitle != null) {
            ruleText.text = result.ruleTitle + "\n\n" + result.ruleContent
        }
        submitButton.visibility = View.GONE
        nextButton.visibility = View.VISIBLE
    }

    private fun loadRule() {
        val q = questions[index]
        thread {
            try {
                val rule = api.getRule(q.caseName)
                runOnUiThread {
                    ruleText.text = rule.title + "\n\n" + rule.content
                    ruleText.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                runOnUiThread {
                    ruleText.setText(R.string.network_error)
                    ruleText.visibility = View.VISIBLE
                }
            }
        }
    }
}
