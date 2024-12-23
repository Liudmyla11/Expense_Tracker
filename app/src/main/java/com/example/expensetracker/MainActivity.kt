package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.ItemTouchHelper

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var tvNoExpenses: TextView
    private lateinit var tvTotalAmount: TextView

    private lateinit var database: AppDatabase
    private lateinit var expenseDao: ExpenseDao

    private var expenses = mutableListOf<Expense>()
    private val ADD_EXPENSE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)

        database = AppDatabase.getDatabase(this)
        expenseDao = database.expenseDao()

        recyclerView = findViewById(R.id.rvExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        expenseAdapter = ExpenseAdapter(expenses)
        recyclerView.adapter = expenseAdapter

        tvNoExpenses = findViewById(R.id.tvNoExpenses)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)

        setupSwipeToDelete()
        observeExpenses()

        val fabAddExpense: FloatingActionButton = findViewById(R.id.fabAddExpense)
        fabAddExpense.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivityForResult(intent, ADD_EXPENSE_REQUEST)
        }

        val btnStatistics: ImageButton = findViewById(R.id.btnMyExpenses)
        btnStatistics.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            intent.putExtra("expenses", ArrayList(expenses))
            startActivity(intent)
        }

        val btnDelete: ImageButton = findViewById(R.id.btnDelete)
        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun observeExpenses() {
        lifecycleScope.launch {
            expenseDao.getAllExpenses().collectLatest { expenses ->
                this@MainActivity.expenses = expenses.toMutableList()
                sortExpensesByDate()
                expenseAdapter.updateExpenses(expenses)
                updateTotalExpenses(expenses)
                checkExpensesVisibility(expenses)
            }
        }
    }

    private fun updateTotalExpenses(expenses: List<Expense>) {
        val totalAmount = expenses.sumOf { it.amount }
        val formattedAmount = "%.2f".format(totalAmount)
        val displayedAmount = when {
            totalAmount > 0 -> "-$formattedAmount"
            totalAmount < 0 -> formattedAmount
            else -> formattedAmount
        }
        tvTotalAmount.text = "$displayedAmount ₴"
    }

    private fun checkExpensesVisibility(expenses: List<Expense>) {
        if (expenses.isEmpty()) {
            tvNoExpenses.visibility = TextView.VISIBLE
            recyclerView.visibility = RecyclerView.GONE
        } else {
            tvNoExpenses.visibility = TextView.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                // перевірка, чи це витрата, а не заголовок
                val item = expenseAdapter.getItemAtPosition(position)
                if (item !is Expense) {
                    expenseAdapter.notifyItemChanged(position) // відновлення елементу, якщо це не витрата
                    return
                }

                val expenseToDelete = item

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        expenseDao.delete(expenseToDelete)

                        withContext(Dispatchers.Main) {
                            expenses.remove(expenseToDelete)
                            expenseAdapter.updateExpenses(expenses)

                            Toast.makeText(
                                this@MainActivity,
                                "Витрату видалено",
                                Toast.LENGTH_SHORT
                            ).show()
                            checkExpensesVisibility(expenses)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Помилка при видаленні: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            expenseAdapter.notifyItemChanged(position)
                        }
                    }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
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
                lifecycleScope.launch(Dispatchers.IO) {
                    expenseDao.deleteAll()
                }
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
                lifecycleScope.launch(Dispatchers.IO) {
                    expenseDao.insert(it)
                }
            }
        }
    }

    private fun sortExpensesByDate() {
        expenses.sortByDescending { it.date }
    }
}

