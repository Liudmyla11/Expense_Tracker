package com.example.expensetracker

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.PercentFormatter
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // отримання списоку витрат
        val expenses = intent.getSerializableExtra("expenses") as? ArrayList<Expense> ?: ArrayList()

        if (expenses.isEmpty()) {
            // якщо дані відсутні, показується повідомлення
            Toast.makeText(this, "Дані для статистики відсутні", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            // якщо дані є, показується статистика
            val tvSelectedPeriod: TextView = findViewById(R.id.tvSelectedPeriod)
            val periodText = calculatePeriod(expenses)
            tvSelectedPeriod.text = periodText

            // ініціалізація кругової діаграми
            pieChart = findViewById(R.id.pieChart)

            // мапа категорій з кольорами
            val existingCategories = mapOf(
                "🛁 Догляд" to R.color.colorCare,
                "🎁 Подарунки" to R.color.colorGifts,
                "🏠 Будинок" to R.color.colorHome,
                "🎮 Ігри" to R.color.colorGames,
                "🚕 Таксі" to R.color.colorTaxi,
                "🦮 Тварини" to R.color.colorPets,
                "🥑 Їжа" to R.color.colorFood,
                "🚗 Автомобіль" to R.color.colorCar,
                "✈️ Подорожі" to R.color.colorTravel,
                "🎡 Розваги" to R.color.colorEntertainment,
                "💊 Здоров’я" to R.color.colorHealth,
                "📝 Рахунки" to R.color.colorBills,
                "⚽️ Спорт" to R.color.colorSport
            )

            // фільтрація витрат по категоріям
            val filteredData = expenses.filter { it.category in existingCategories.keys }
            setupPieChart(filteredData, existingCategories)

            val aggregatedData = expenses.groupBy { it.category }
                .map { (category, expenseList) ->
                    category to expenseList.sumOf { it.amount }
                }
                .sortedByDescending { it.second }

            // ініціалізація адаптера для відображення статистики
            val statisticsAdapter = StatisticsAdapter(aggregatedData)

            // налаштування RecyclerView для відображення статистики
            val rvCategories: RecyclerView = findViewById(R.id.rvCategories)
            rvCategories.layoutManager = LinearLayoutManager(this)
            rvCategories.adapter = statisticsAdapter
        }
    }

    private fun calculatePeriod(expenses: List<Expense>): String {
        val inputDateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputDateFormat = java.text.SimpleDateFormat("d MMM yyyy 'р.'", Locale("uk"))

        // сортування витрат за датами
        val sortedExpenses = expenses.sortedBy { it.date }

        val firstDate = sortedExpenses.first().date
        val lastDate = sortedExpenses.last().date

        // форматування дат у потрібний формат
        val formattedFirstDate = outputDateFormat.format(inputDateFormat.parse(firstDate)!!)
        val formattedLastDate = outputDateFormat.format(inputDateFormat.parse(lastDate)!!)

        return "$formattedFirstDate — $formattedLastDate"
    }

    // налаштування кругової діаграми
    private fun setupPieChart(filteredData: List<Expense>, categoryColors: Map<String, Int>) {
        val aggregatedData = filteredData.groupBy { it.category }
            .map { (category, expenseList) ->
                category to expenseList.sumOf { it.amount }
            }
            .sortedByDescending { it.second } // підсумовування витрат для кожної категорії

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        // підрахунок загальної суми витрат для розрахунку відсотків
        val totalAmount = aggregatedData.sumOf { it.second }

        // заповнення даних для кругової діаграми
        for ((category, totalAmountForCategory) in aggregatedData) {
            val percentage = (totalAmountForCategory / totalAmount * 100).toFloat()
            entries.add(PieEntry(percentage, category)) // назва категорії передається для легенди
            val colorRes = categoryColors[category] ?: R.color.colorOther
            colors.add(ContextCompat.getColor(this, colorRes))
        }

        val dataSet = PieDataSet(entries, null)
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.selectedColor)

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(pieChart))
        pieData.setDrawValues(true)

        // налаштування діаметра отвору
        pieChart.setHoleRadius(40f)
        pieChart.setTransparentCircleRadius(43f)

        // налаштування кругової діаграми
        pieChart.data = pieData
        pieChart.description.isEnabled = false // відключення опису
        pieChart.setUsePercentValues(true) // включення відсоткового відображення
        pieChart.setDrawEntryLabels(false) // відключення підписів на самій діаграмі

        // налаштування легенди
        val legend = pieChart.legend
        legend.isEnabled = true // включення легенди
        legend.textSize = 12f
        legend.textColor = ContextCompat.getColor(this, R.color.selectedColor)
        legend.form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE // форма маркерів
        legend.formSize = 12f
        legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT // вирівнювання справа
        legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.CENTER // вирівнювання по центру вертикалі
        legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL // вертикальне розташування

        pieChart.animateY(1000)
        pieChart.invalidate()
    }
}
