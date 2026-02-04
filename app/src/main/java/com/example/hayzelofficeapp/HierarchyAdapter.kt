package com.example.hayzelofficeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HierarchyAdapter(private val nodes: List<HierarchyNode>) :
    RecyclerView.Adapter<HierarchyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val roleTextView: TextView = view.findViewById(R.id.roleTextView)
        val departmentTextView: TextView = view.findViewById(R.id.departmentTextView)
        val indentView: View = view.findViewById(R.id.indentView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hierarchy, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val node = nodes[position]
        val user = node.user

        // Set indentation based on level
        val margin = node.level * 50 // 50dp per level
        holder.indentView.layoutParams.width = margin

        // Set user data
        holder.nameTextView.text = user.fullName
        holder.roleTextView.text = "${user.position} (${user.role})"
        holder.departmentTextView.text = user.department
    }

    override fun getItemCount() = nodes.size
}