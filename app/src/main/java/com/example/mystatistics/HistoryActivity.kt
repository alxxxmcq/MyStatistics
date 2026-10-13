package com.example.mystatistics

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HistoryActivity : AppCompatActivity() {

    private lateinit var storage: MatchStorage
    private lateinit var matchesList: ListView
    private lateinit var emptyText: TextView

    private var displayedMatches:
            List<IndexedValue<FootballMatch>> = emptyList()

    private val dateFormat = DateTimeFormatter.ofPattern(
        "dd.MM.yyyy"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

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

        storage = MatchStorage(this)
        matchesList = findViewById(R.id.matchesList)
        emptyText = findViewById(R.id.emptyText)

        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        matchesList.setOnItemClickListener { _, _, position, _ ->
            val selected = displayedMatches[position]
            val match = selected.value

            AlertDialog.Builder(this)
                .setTitle("Удалить матч?")
                .setMessage(
                    "${match.opponent}\n" +
                            "Счёт: ${match.teamScore}:${match.opponentScore}"
                )
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Удалить") { _, _ ->
                    try {
                        storage.deleteMatch(selected.index)
                        loadMatches()

                        Toast.makeText(
                            this,
                            "Матч удалён",
                            Toast.LENGTH_SHORT
                        ).show()

                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "Ошибка удаления: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .show()
        }

        loadMatches()
    }

    private fun loadMatches() {
        try {
            val matches = storage.getMatches()

            displayedMatches = matches.withIndex()
                .sortedByDescending { it.value.date }

            matchesList.adapter = object : BaseAdapter() {

                override fun getCount(): Int {
                    return displayedMatches.size
                }

                override fun getItem(position: Int): FootballMatch {
                    return displayedMatches[position].value
                }

                override fun getItemId(position: Int): Long {
                    return displayedMatches[position].index.toLong()
                }

                override fun getView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val row = convertView ?: layoutInflater.inflate(
                        R.layout.item_match,
                        parent,
                        false
                    )

                    val match = getItem(position)

                    val dateText = row.findViewById<TextView>(
                        R.id.matchDateText
                    )
                    val opponentText = row.findViewById<TextView>(
                        R.id.matchOpponentText
                    )
                    val scoreText = row.findViewById<TextView>(
                        R.id.matchScoreText
                    )
                    val resultText = row.findViewById<TextView>(
                        R.id.matchResultText
                    )
                    val detailsText = row.findViewById<TextView>(
                        R.id.matchDetailsText
                    )

                    dateText.text = LocalDate.parse(match.date)
                        .format(dateFormat)

                    opponentText.text = "Против ${match.opponent}"

                    scoreText.text =
                        "${match.teamScore} : ${match.opponentScore}"

                    when {
                        match.teamScore > match.opponentScore -> {
                            resultText.text = "ПОБЕДА"
                            resultText.setTextColor(
                                Color.parseColor("#218354")
                            )
                        }

                        match.teamScore < match.opponentScore -> {
                            resultText.text = "ПОРАЖЕНИЕ"
                            resultText.setTextColor(
                                Color.parseColor("#B54A4A")
                            )
                        }

                        else -> {
                            resultText.text = "НИЧЬЯ"
                            resultText.setTextColor(
                                Color.parseColor("#68796E")
                            )
                        }
                    }

                    detailsText.text =
                        "Голы: ${match.goals} · Ассисты: ${match.assists}\n" +
                                "На поле: ${match.minutes} мин"

                    return row
                }
            }

            if (displayedMatches.isEmpty()) {
                emptyText.text = "Матчей пока нет"
                emptyText.visibility = View.VISIBLE
                matchesList.visibility = View.GONE
            } else {
                emptyText.visibility = View.GONE
                matchesList.visibility = View.VISIBLE
            }

        } catch (e: Exception) {
            displayedMatches = emptyList()
            matchesList.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
            emptyText.text = "Не удалось загрузить историю"

            Toast.makeText(
                this,
                "Ошибка загрузки: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}