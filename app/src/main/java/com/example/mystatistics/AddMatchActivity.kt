package com.example.mystatistics

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddMatchActivity : AppCompatActivity() {

    private var selectedDate = LocalDate.now()

    private val dateFormat = DateTimeFormatter.ofPattern(
        "dd.MM.yyyy"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_match)

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

        // Восстанавливаем выбранную дату после поворота экрана.
        val savedDate = savedInstanceState?.getString("date")

        if (savedDate != null) {
            selectedDate = LocalDate.parse(savedDate)
        }

        val dateButton = findViewById<Button>(R.id.dateButton)
        val opponentInput = findViewById<EditText>(R.id.opponentInput)

        val teamScoreInput = findViewById<EditText>(
            R.id.teamScoreInput
        )
        val opponentScoreInput = findViewById<EditText>(
            R.id.opponentScoreInput
        )

        val goalsInput = findViewById<EditText>(R.id.goalsInput)
        val assistsInput = findViewById<EditText>(R.id.assistsInput)
        val minutesInput = findViewById<EditText>(R.id.minutesInput)

        val saveButton = findViewById<Button>(R.id.saveMatchButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        dateButton.text = "Дата: ${selectedDate.format(dateFormat)}"

        // Открываем календарь.
        dateButton.setOnClickListener {
            val dialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate = LocalDate.of(
                        year,
                        month + 1,
                        day
                    )

                    dateButton.text =
                        "Дата: ${selectedDate.format(dateFormat)}"
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            )

            // Сыгранный матч не может иметь будущую дату.
            dialog.datePicker.maxDate = System.currentTimeMillis()
            dialog.show()
        }

        saveButton.setOnClickListener {
            val opponent = opponentInput.text.toString().trim()

            if (opponent.isEmpty()) {
                opponentInput.error = "Введите соперника"
                return@setOnClickListener
            }

            val numberFields = listOf(
                teamScoreInput,
                opponentScoreInput,
                goalsInput,
                assistsInput,
                minutesInput
            )

            val numbers = mutableListOf<Int>()

            // Проверяем каждое числовое поле.
            for (field in numberFields) {
                val value = field.text.toString().toIntOrNull()

                if (value == null || value < 0) {
                    field.error = "Введите число от 0"
                    field.requestFocus()
                    return@setOnClickListener
                }

                numbers.add(value)
            }

            val teamScore = numbers[0]
            val opponentScore = numbers[1]
            val goals = numbers[2]
            val assists = numbers[3]
            val minutes = numbers[4]

            if (goals + assists > teamScore) {
                Toast.makeText(
                    this,
                    "Сумма моих голов и ассистов превышает голы команды",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            val match = FootballMatch(
                date = selectedDate.toString(),
                opponent = opponent,
                teamScore = teamScore,
                opponentScore = opponentScore,
                goals = goals,
                assists = assists,
                minutes = minutes
            )

            try {
                val storage = MatchStorage(this)
                storage.addMatch(match)

                Toast.makeText(
                    this,
                    "Матч сохранён",
                    Toast.LENGTH_SHORT
                ).show()

                // Закрываем форму и возвращаемся на главный экран.
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Ошибка сохранения: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("date", selectedDate.toString())
    }
}