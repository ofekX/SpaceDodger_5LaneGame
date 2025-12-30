package com.example.exercise2.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.exercise2.R
import com.example.exercise2.utilities.Record


class RecordAdapter(
    private val records: List<Record>,
    private val onClick: (Record) -> Unit
) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        holder.rank.text = (position + 1).toString()

        holder.distance.text = record.distance.toString()
        holder.name.text = record.name ?: "Player"

        holder.itemView.setOnClickListener { onClick(record) }
    }

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rank: TextView = itemView.findViewById(R.id.row_LBL_rank)
        val distance: TextView = itemView.findViewById(R.id.row_LBL_distance)
        val name: TextView = itemView.findViewById(R.id.row_LBL_name)
    }

    override fun getItemCount(): Int = records.size
}
