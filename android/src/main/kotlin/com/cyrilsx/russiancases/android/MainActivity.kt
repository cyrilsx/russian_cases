package com.cyrilsx.russiancases.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.cyrilsx.russiancases.android.model.Level
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private var api: ApiClient? = null
    private var levels: List<Level> = emptyList()
    private lateinit var levelsAdapter: ArrayAdapter<String>
    private lateinit var userText: TextView
    private lateinit var summaryText: TextView
    private lateinit var levelsList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        if (token == null) {
            startLogin()
            return
        }
        val client = ApiClient(prefs.getString("base_url", getString(R.string.default_server_url)), token)
        api = client

        setContentView(R.layout.activity_main)
        userText = findViewById(R.id.current_user) as TextView
        summaryText = findViewById(R.id.summary) as TextView
        levelsList = findViewById(R.id.levels_list) as ListView
        val logoutButton: Button = findViewById(R.id.logout_button) as Button

        userText.setText(getString(R.string.logged_in_as, prefs.getString("username", "")))
        levelsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        levelsList.adapter = levelsAdapter
        levelsList.setOnItemClickListener { _, _, position, _ ->
            val level = levels[position]
            val intent = Intent(this@MainActivity, QuizActivity::class.java)
            intent.putExtra("level_id", level.id)
            intent.putExtra("level_title", level.title)
            startActivity(intent)
        }
        logoutButton.setOnClickListener {
            getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
            startLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        api?.let { loadData(it) }
    }

    private fun startLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun loadData(client: ApiClient) {
        summaryText.setText(R.string.loading)
        thread {
            try {
                val loadedLevels = client.getLevels()
                val summary = client.getSummary()
                runOnUiThread {
                    levels = loadedLevels
                    levelsAdapter.clear()
                    loadedLevels.forEach { levelsAdapter.add("${it.title} (${it.questionCount})") }
                    levelsAdapter.notifyDataSetChanged()
                    val text = StringBuilder(
                        getString(R.string.overall_score, summary.totalCorrect, summary.totalAttempts, summary.percent)
                    )
                    summary.levels.forEach { lp ->
                        text.append("\n").append(lp.title).append(" : ")
                            .append(lp.correct).append("/").append(lp.total)
                            .append(" (").append(lp.percent).append(" %)")
                    }
                    summaryText.text = text.toString()
                }
            } catch (e: Exception) {
                runOnUiThread { summaryText.setText(R.string.network_error) }
            }
        }
    }
}
