package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import android.text.TextWatcher
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity(), CategoryAdapter.OnItemClickListener {

    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)

        val categories = resources.getStringArray(R.array.categories)
        val adapter = CategoryAdapter(categories, this)

        val recyclerView: RecyclerView = findViewById(R.id.rvCategories)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val etAmount: EditText = findViewById(R.id.etAmount)
        etAmount.setText("0 ₴")

        etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, after: Int) {
                if (charSequence.isNotEmpty() && !charSequence.toString().endsWith(" ₴")) {
                    etAmount.setText(charSequence.toString() + " ₴")
                    etAmount.setSelection(etAmount.text.length - 2)
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val btnAddExpense: Button = findViewById(R.id.btnAddExpense)
        btnAddExpense.setOnClickListener {
            val amountText = etAmount.text.toString().replace(" ₴", "").trim()
            val amount = if (amountText.isNotEmpty()) amountText.toDouble() else 0.0
            val note = findViewById<EditText>(R.id.etNote).text.toString().trim()

            if (amount <= 0) {
                Toast.makeText(this, "Будь ласка, введіть суму витрат!", Toast.LENGTH_SHORT).show()
            } else if (selectedCategory == null) {
                Toast.makeText(this, "Будь ласка, виберіть категорію витрат!", Toast.LENGTH_SHORT).show()
            } else {
                val expense = Expense(
                    category = selectedCategory!!,
                    amount = amount,
                    date = getCurrentDate(),
                    note = note
                )
                val resultIntent = Intent()
                resultIntent.putExtra("expense", expense)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onItemClick(category: String) {
        selectedCategory = category
    }
}
