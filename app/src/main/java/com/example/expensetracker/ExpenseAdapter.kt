package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter(private var expenses: List<Expense>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    private val displayList = mutableListOf<Any>()

    init {
        prepareDisplayList()
    }

    // підготовка даних: виконано групування витрат за датою та додано загальну суму
    private fun prepareDisplayList() {
        displayList.clear()
        val groupedExpenses = expenses.groupBy { it.formattedDate() }

        groupedExpenses.forEach { (date, expenseList) ->
            val totalSum = expenseList.sumOf { it.amount }
            val formattedSum = "%.2f".format(totalSum)

            val displayedSum = if (totalSum >= 0) "-$formattedSum" else formattedSum

            displayList.add("$date " +
                    "\nЗагальна сума: $displayedSum ₴") //додавання символу валюти
            displayList.addAll(expenseList) //додавання витрат до списку
        }
    }

    // оновлення списку витрат
    fun updateExpenses(newExpenses: List<Expense>) {
        expenses = newExpenses
        prepareDisplayList()
        notifyDataSetChanged()
    }

    fun getItemAtPosition(position: Int): Any {
        return displayList[position]
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayList[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
            ExpenseViewHolder(view)
        }
    }

    override fun getItemCount(): Int = displayList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind(displayList[position] as String)
        } else if (holder is ExpenseViewHolder) {
            holder.bind(displayList[position] as Expense)
        }
    }

    // ViewHolder для заголовка
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tvHeader)

        fun bind(headerText: String) {
            tvHeader.text = headerText
        }
    }

    // ViewHolder для витрат
    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.categoryTextView)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        fun bind(expense: Expense) {
            tvCategory.text = "${expense.category}${if (expense.note.isNotEmpty()) " (${expense.note})" else ""}"
            tvAmount.text = String.format("%.2f ₴", expense.amount)
        }
    }

}
