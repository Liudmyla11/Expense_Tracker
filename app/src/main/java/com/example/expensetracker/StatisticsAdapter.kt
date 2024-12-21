package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StatisticsAdapter(
    private val categoriesWithAmounts: List<Pair<String, Double>> // список категорій з сумами
) : RecyclerView.Adapter<StatisticsAdapter.StatisticsViewHolder>() {

    inner class StatisticsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        val categoryAmountTextView: TextView = itemView.findViewById(R.id.categoryAmountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_statistics_category, parent, false)
        return StatisticsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatisticsViewHolder, position: Int) {
        val (categoryName, amount) = categoriesWithAmounts[position]

        holder.categoryNameTextView.text = categoryName

        val formattedAmount = "-${String.format("%.2f", Math.abs(amount))}₴"

        holder.categoryAmountTextView.text = formattedAmount
    }


    override fun getItemCount() = categoriesWithAmounts.size
}
