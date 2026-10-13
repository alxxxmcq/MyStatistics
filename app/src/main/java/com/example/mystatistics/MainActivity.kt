package com.example.mystatistics

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Отступы для системных панелей телефона.
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // Находим элементы из XML по их id.
        val nameInput = findViewById<EditText>(R.id.nameInput)
        val numberInput = findViewById<EditText>(R.id.numberInput)
        val positionSpinner = findViewById<Spinner>(
            R.id.positionSpinner
        )

        val profileText = findViewById<TextView>(R.id.profileText)

        val saveProfileButton = findViewById<Button>(
            R.id.saveProfileButton
        )
        val addMatchButton = findViewById<Button>(
            R.id.addMatchButton
        )
        val historyButton = findViewById<Button>(
            R.id.historyButton
        )

        // Варианты игровых позиций.
        val positions = listOf(
            "Вратарь",
            "Защитник",
            "Полузащитник",
            "Нападающий"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            positions
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        positionSpinner.adapter = adapter

        // Хранилище профиля.
        val preferences = getSharedPreferences(
            "player",
            MODE_PRIVATE
        )

        // Загружаем сохранённый профиль.
        val savedName = preferences.getString("name", "") ?: ""
        val savedNumber = preferences.getInt("number", 10)
        val savedPosition = preferences.getInt("position", 0)
            .coerceIn(0, positions.lastIndex)

        nameInput.setText(savedName)
        numberInput.setText(savedNumber.toString())
        positionSpinner.setSelection(savedPosition)

        if (savedName.isNotEmpty()) {
            profileText.text =
                "$savedName\n№$savedNumber · ${positions[savedPosition]}"
        }

        // Сохраняем профиль.
        saveProfileButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val number = numberInput.text.toString().toIntOrNull()
            val positionIndex = positionSpinner.selectedItemPosition

            if (name.isEmpty()) {
                nameInput.error = "Введите имя"
                return@setOnClickListener
            }

            if (number == null || number !in 0..99) {
                numberInput.error = "Введите номер от 0 до 99"
                return@setOnClickListener
            }

            preferences.edit()
                .putString("name", name)
                .putInt("number", number)
                .putInt("position", positionIndex)
                .apply()

            profileText.text =
                "$name\n№$number · ${positions[positionIndex]}"

            Toast.makeText(
                this,
                "Профиль сохранён",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Открываем экран добавления матча.
        addMatchButton.setOnClickListener {
            val intent = Intent(this, AddMatchActivity::class.java)
            startActivity(intent)
        }

        historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatistics()
        GoalManager(this).update()
    }

    private fun updateStatistics() {
        val statisticsText = findViewById<TextView>(
            R.id.statisticsText
        )

        try {
            val storage = MatchStorage(this)
            val matches = storage.getMatches()

            var totalGoals = 0L
            var totalAssists = 0L
            var totalMinutes = 0L
            var wins = 0

            for (match in matches) {
                totalGoals += match.goals
                totalAssists += match.assists
                totalMinutes += match.minutes

                if (match.teamScore > match.opponentScore) {
                    wins++
                }
            }

            val matchCount = matches.size

            var averageGoals = 0.0
            var winPercent = 0.0

            // Защита от деления на ноль.
            if (matchCount > 0) {
                averageGoals = totalGoals.toDouble() / matchCount
                winPercent = wins.toDouble() / matchCount * 100
            }

            val averageText = String.format(
                Locale("ru"),
                "%.2f",
                averageGoals
            )

            val percentText = String.format(
                Locale("ru"),
                "%.1f",
                winPercent
            )

            statisticsText.text = """
                Матчей: $matchCount
                Голов: $totalGoals
                Ассистов: $totalAssists
                Минут на поле: $totalMinutes
                Голов за матч: $averageText
                Побед: $percentText%
            """.trimIndent()

        } catch (e: Exception) {
            statisticsText.text = "Не удалось загрузить статистику"

            Toast.makeText(
                this,
                "Ошибка загрузки: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}