package com.example.mystatistics

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Один объект FootballMatch — один сыгранный матч.
data class FootballMatch(
    val date: String,
    val opponent: String,
    val teamScore: Int,
    val opponentScore: Int,
    val goals: Int,
    val assists: Int,
    val minutes: Int
)

class MatchStorage(context: Context) {

    private val preferences = context.getSharedPreferences(
        "football_matches",
        Context.MODE_PRIVATE
    )

    // Получаем все сохранённые матчи.
    fun getMatches(): MutableList<FootballMatch> {
        val matches = mutableListOf<FootballMatch>()

        val text = preferences.getString("matches", "[]") ?: "[]"
        val array = JSONArray(text)

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)

            val match = FootballMatch(
                date = item.getString("date"),
                opponent = item.getString("opponent"),
                teamScore = item.getInt("teamScore"),
                opponentScore = item.getInt("opponentScore"),
                goals = item.getInt("goals"),
                assists = item.getInt("assists"),
                minutes = item.getInt("minutes")
            )

            matches.add(match)
        }

        return matches
    }

    // Добавляем новый матч к существующим.
    fun addMatch(match: FootballMatch) {
        val matches = getMatches()
        matches.add(match)

        val array = JSONArray()

        for (item in matches) {
            val json = JSONObject()

            json.put("date", item.date)
            json.put("opponent", item.opponent)
            json.put("teamScore", item.teamScore)
            json.put("opponentScore", item.opponentScore)
            json.put("goals", item.goals)
            json.put("assists", item.assists)
            json.put("minutes", item.minutes)

            array.put(json)
        }

        val saved = preferences.edit()
            .putString("matches", array.toString())
            .commit()

        if (!saved) {
            throw IllegalStateException("Не удалось сохранить матч")
        }
    }

    // Удаляем матч по его индексу в сохранённом списке.
    fun deleteMatch(index: Int) {
        val text = preferences.getString("matches", "[]") ?: "[]"
        val array = JSONArray(text)

        if (index !in 0 until array.length()) {
            throw IllegalArgumentException("Матч не найден")
        }

        array.remove(index)

        val saved = preferences.edit()
            .putString("matches", array.toString())
            .commit()

        if (!saved) {
            throw IllegalStateException("Не удалось удалить матч")
        }
    }
}