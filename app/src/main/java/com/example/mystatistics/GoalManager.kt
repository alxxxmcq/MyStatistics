package com.example.mystatistics

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.time.YearMonth

class GoalManager(private val activity: AppCompatActivity) {

    private val preferences = activity.getSharedPreferences(
        "football_goal",
        Context.MODE_PRIVATE
    )

    private val green = Color.parseColor("#218354")
    private val darkGreen = Color.parseColor("#173B29")
    private val muted = Color.parseColor("#68796E")
    private val lightGreen = Color.parseColor("#DFEBE3")

    private val months = listOf(
        "Январь",
        "Февраль",
        "Март",
        "Апрель",
        "Май",
        "Июнь",
        "Июль",
        "Август",
        "Сентябрь",
        "Октябрь",
        "Ноябрь",
        "Декабрь"
    )

    private fun dp(value: Int): Int {
        return (
                value * activity.resources.displayMetrics.density
                ).toInt()
    }

    // Обновляем цель на главном экране.
    fun update() {
        val goalText = activity.findViewById<TextView>(
            R.id.goalText
        )

        val progressBar = activity.findViewById<ProgressBar>(
            R.id.goalProgress
        )

        val editButton = activity.findViewById<Button>(
            R.id.editGoalButton
        )

        editButton.setOnClickListener {
            showGoalDialog()
        }

        try {
            val target = preferences.getInt("target", 0)

            if (target <= 0) {
                goalText.text = "Цель пока не установлена"
                progressBar.progress = 0
                return
            }

            val monthText = preferences.getString(
                "month",
                YearMonth.now().toString()
            ) ?: YearMonth.now().toString()

            val month = YearMonth.parse(monthText)

            val storage = MatchStorage(activity)
            val matches = storage.getMatches()

            var completedGoals = 0L

            for (match in matches) {
                if (match.date.startsWith("$monthText-")) {
                    completedGoals += match.goals
                }
            }

            val percent = (completedGoals * 100 / target)
                .coerceIn(0L, 100L)
                .toInt()

            val monthName = months[month.monthValue - 1]

            val status = if (completedGoals >= target) {
                "Цель достигнута!"
            } else {
                "Осталось голов: ${target - completedGoals}"
            }

            goalText.text = """
                $monthName ${month.year}
                Цель: забить $target голов
                Выполнено: $completedGoals из $target · $percent%
                $status
            """.trimIndent()

            progressBar.progress = percent
            progressBar.contentDescription =
                "Выполнено $completedGoals из $target голов"

        } catch (e: Exception) {
            goalText.text = "Не удалось загрузить цель"
            progressBar.progress = 0

            Toast.makeText(
                activity,
                "Ошибка: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showGoalDialog() {
        val currentMonth = YearMonth.now()

        val savedMonth = preferences.getString(
            "month",
            currentMonth.toString()
        ) ?: currentMonth.toString()

        val selectedMonth = try {
            YearMonth.parse(savedMonth)
        } catch (e: Exception) {
            currentMonth
        }

        val savedTarget = preferences.getInt("target", 0)

        // Форма внутри окна.
        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        // Прокрутка для небольшого экрана и открытой клавиатуры.
        val scroll = ScrollView(activity).apply {
            setBackgroundResource(R.drawable.bg_card)
            addView(layout)
        }

        val sectionText = TextView(activity).apply {
            text = "МОЙ ФУТБОЛ"
            textSize = 12f
            setTextColor(green)
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.15f
        }
        layout.addView(sectionText)

        val title = TextView(activity).apply {
            text = "Цель на месяц"
            textSize = 26f
            setTextColor(darkGreen)
            setTypeface(typeface, Typeface.BOLD)

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        }
        layout.addView(title)

        val description = TextView(activity).apply {
            text = "Выбери месяц и количество голов"
            textSize = 14f
            setTextColor(muted)

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        }
        layout.addView(description)

        // Подпись перед полем.
        fun addLabel(value: String) {
            val label = TextView(activity).apply {
                text = value
                textSize = 13f
                setTextColor(muted)

                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(20)
                }
            }

            layout.addView(label)
        }

        // Поле для ввода числа.
        fun createNumberInput(hintText: String): EditText {
            return EditText(activity).apply {
                hint = hintText
                inputType = InputType.TYPE_CLASS_NUMBER
                textSize = 18f
                setTextColor(darkGreen)
                setHintTextColor(Color.parseColor("#89988E"))
                backgroundTintList = ColorStateList.valueOf(green)
                minHeight = dp(48)
                setSingleLine(true)

                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        addLabel("Сколько голов хочешь забить?")

        val targetInput = createNumberInput("Например, 10")
        targetInput.filters = arrayOf(
            InputFilter.LengthFilter(6)
        )

        if (savedTarget > 0) {
            targetInput.setText(savedTarget.toString())
        }

        layout.addView(targetInput)

        addLabel("Месяц")

        // Настраиваем цвета выбранного месяца и списка.
        val adapter = object : ArrayAdapter<String>(
            activity,
            android.R.layout.simple_spinner_item,
            months
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getView(
                    position,
                    convertView,
                    parent
                ) as TextView

                view.setTextColor(darkGreen)
                view.textSize = 17f

                return view
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(
                    position,
                    convertView,
                    parent
                ) as TextView

                view.setTextColor(darkGreen)
                view.setBackgroundColor(Color.WHITE)

                return view
            }
        }

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        val monthSpinner = Spinner(activity).apply {
            this.adapter = adapter
            setSelection(selectedMonth.monthValue - 1)
            backgroundTintList = ColorStateList.valueOf(green)

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
            )
        }

        layout.addView(monthSpinner)

        addLabel("Год")

        val yearInput = createNumberInput("Например, 2026")
        yearInput.filters = arrayOf(
            InputFilter.LengthFilter(4)
        )
        yearInput.setText(selectedMonth.year.toString())
        layout.addView(yearInput)

        val saveButton = Button(activity).apply {
            text = "Сохранить цель"
            isAllCaps = false
            textSize = 16f
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(green)
            minHeight = dp(48)

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(24)
            }
        }

        layout.addView(saveButton)

        val cancelButton = Button(activity).apply {
            text = "Отмена"
            isAllCaps = false
            setTextColor(darkGreen)
            backgroundTintList = ColorStateList.valueOf(lightGreen)
            minHeight = dp(48)
        }

        layout.addView(
            cancelButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val dialog = AlertDialog.Builder(activity)
            .setView(scroll)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val target = targetInput.text.toString().toIntOrNull()
            val year = yearInput.text.toString().toIntOrNull()

            if (target == null || target !in 1..100000) {
                targetInput.error = "Введите число от 1 до 100000"
                targetInput.requestFocus()
                return@setOnClickListener
            }

            if (year == null || year !in 2000..2100) {
                yearInput.error = "Введите год от 2000 до 2100"
                yearInput.requestFocus()
                return@setOnClickListener
            }

            val monthNumber = monthSpinner.selectedItemPosition + 1
            val month = YearMonth.of(year, monthNumber)

            val saved = preferences.edit()
                .putInt("target", target)
                .putString("month", month.toString())
                .commit()

            if (saved) {
                dialog.dismiss()
                update()
            } else {
                Toast.makeText(
                    activity,
                    "Не удалось сохранить цель",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        dialog.show()

        // Убираем стандартный прямоугольный фон окна.
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
    }
}