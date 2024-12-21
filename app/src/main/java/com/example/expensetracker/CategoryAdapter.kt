package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: Array<String>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0 // для зберігання вибраної категорії

    interface OnItemClickListener {
        fun onItemClick(category: String)
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)

        init {
            itemView.setOnClickListener {
                // оновлення вибраної категорії
                selectedPosition = adapterPosition
                onItemClickListener.onItemClick(categories[adapterPosition])
                notifyDataSetChanged() // оновлення даних в адаптері
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.categoryTextView.text = categories[position]

        // встановлення різного стилю в залежності від того, чи вибрано елемент
        if (position == selectedPosition) {
            holder.categoryTextView.textSize = 24f
            holder.categoryTextView.setTextColor(holder.itemView.context.getColor(R.color.selectedColor))
        } else {
            holder.categoryTextView.textSize = 16f
            holder.categoryTextView.setTextColor(holder.itemView.context.getColor(R.color.defaultColor))
        }
    }

    override fun getItemCount() = categories.size
}

