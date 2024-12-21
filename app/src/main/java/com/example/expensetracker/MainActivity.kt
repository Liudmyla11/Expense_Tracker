package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var tvNoExpenses: TextView
    private lateinit var tvTotalAmount: TextView
    private var expenses = mutableListOf<Expense>()
    private val ADD_EXPENSE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)

        recyclerView = findViewById(R.id.rvExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        expenseAdapter = ExpenseAdapter(expenses)
        recyclerView.adapter = expenseAdapter

        tvNoExpenses = findViewById(R.id.tvNoExpenses)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        updateTotalExpenses()

        addTestData()

        val fabAddExpense: FloatingActionButton = findViewById(R.id.fabAddExpense)
        fabAddExpense.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivityForResult(intent, ADD_EXPENSE_REQUEST)
        }

        val btnStatistics: ImageButton = findViewById(R.id.btnMyExpenses)
        btnStatistics.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        val btnDelete: ImageButton = findViewById(R.id.btnDelete)
        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        checkExpensesVisibility()

        btnStatistics.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            intent.putExtra("expenses", ArrayList(expenses))
            startActivity(intent)
        }
    }

    private fun addTestData() {
        expenses.add(Expense("\uD83E\uDD51 Їжа", 100.0, "2024-06-23", "Обід"))
        expenses.add(Expense("\uD83D\uDE97 Автомобіль", 155.0, "2024-06-16", ""))
        expenses.add(Expense("\uD83C\uDFA1 Розваги", 350.0, "2024-06-19", "Парк-розваг"))
        expenses.add(Expense("\uD83C\uDFA1 Розваги", 150.0, "2024-07-03", "Кіно"))
        expenses.add(Expense("\uD83E\uDDAE Тварини", 200.0, "2024-12-15", "Корм"))
        expenses.add(Expense("\uD83E\uDDAE Тварини", 200.0, "2024-12-14", "Аксесуари"))
        expenses.add(Expense("\uD83E\uDD51 Їжа", 190.0, "2024-12-16", "Кафе"))
        expenses.add(Expense("\uD83E\uDD51 Їжа", 247.42, "2024-12-19", "Продукти"))
        expenses.add(Expense("⚽\uFE0F Спорт", 4000.0, "2024-11-11", "Абонемент"))

        sortExpensesByDate()

        expenseAdapter.updateExpenses(expenses)
        updateTotalExpenses()
        checkExpensesVisibility()
    }

    private fun updateTotalExpenses() {
        val totalAmount = expenses.sumOf { it.amount }
        val formattedAmount = "%.2f".format(totalAmount)
        val displayedAmount = when {
            totalAmount > 0 -> "-$formattedAmount"
            totalAmount < 0 -> formattedAmount
            else -> formattedAmount
        }
        tvTotalAmount.text = "$displayedAmount ₴"
    }

    private fun checkExpensesVisibility() {
        if (expenses.isEmpty()) {
            tvNoExpenses.visibility = TextView.VISIBLE
            recyclerView.visibility = RecyclerView.GONE
        } else {
            tvNoExpenses.visibility = TextView.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
        }
    }

    private fun showDeleteConfirmationDialog() {
        if (expenses.isEmpty()) {
            Toast.makeText(this, "Витрати для видалення відсутні", Toast.LENGTH_SHORT).show()
            return
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("Підтвердження видалення")
            .setMessage("Ви впевнені, що хочете очистити всі свої витрати?")
            .setPositiveButton("Так") { _, _ ->
                expenses.clear()
                expenseAdapter.updateExpenses(expenses)
                updateTotalExpenses()
                checkExpensesVisibility()
                Toast.makeText(applicationContext, "Витрати очищено", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Ні", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EXPENSE_REQUEST && resultCode == RESULT_OK) {
            val newExpense = data?.getSerializableExtra("expense") as? Expense
            newExpense?.let {
                    expenses.add(it)
                    sortExpensesByDate()
                    expenseAdapter.updateExpenses(expenses)
                    updateTotalExpenses()
                    checkExpensesVisibility()
            }
        }
    }

    private fun sortExpensesByDate() {
        expenses.sortByDescending { it.date }
    }
}
