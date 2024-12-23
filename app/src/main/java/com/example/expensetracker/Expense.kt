package com.example.expensetracker

import java.io.Serializable
import java.text.SimpleDateFormat
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Locale

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val amount: Double,
    val date: String,
    val note: String
) : Serializable {
    fun formattedDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        val parsedDate = sdf.parse(date) ?: return date
        val expenseDate = Calendar.getInstance().apply { time = parsedDate }
        val daysDifference = (today.timeInMillis - expenseDate.timeInMillis) / (1000 * 60 * 60 * 24)

        return when (daysDifference) {
            0L -> "Сьогодні"
            1L -> "Вчора"
            2L -> "Позавчора"
            else -> SimpleDateFormat("d MMM yyyy р.", Locale("uk")).format(parsedDate)
        }
    }
}
