package com.example.hayzelofficeapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hayzelofficeapp.models.Employee
import de.hdodenhof.circleimageview.CircleImageView

class EmployeeAdapter(
    private var employees: List<Employee> = emptyList(),
    private val onItemClick: (Employee) -> Unit
) : RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {

    class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: CircleImageView = itemView.findViewById(R.id.profileImageView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val roleTextView: TextView = itemView.findViewById(R.id.roleTextView)
        val departmentTextView: TextView = itemView.findViewById(R.id.departmentTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        val phoneTextView: TextView = itemView.findViewById(R.id.phoneTextView)
        val actionButton: ImageView = itemView.findViewById(R.id.actionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_employee, parent, false)
        return EmployeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val employee = employees[position]

        // Bind data to views
        holder.nameTextView.text = employee.name ?: "Unknown Name"
        holder.roleTextView.text = employee.role ?: "No Role"
        holder.departmentTextView.text = employee.department ?: "No Department"
        holder.emailTextView.text = employee.email ?: "No Email"
        holder.phoneTextView.text = employee.phone ?: "No Phone"

        // Load profile image
        if (!employee.profileImage.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(employee.profileImage)
                .placeholder(R.drawable.ic_person)
                .into(holder.profileImageView)
        } else {
            // Set default avatar
            holder.profileImageView.setImageResource(R.drawable.ic_person)
        }

        // Set click listeners
        holder.itemView.setOnClickListener {
            onItemClick(employee)
        }

        holder.actionButton.setOnClickListener {
            onItemClick(employee)
        }

        // Add animation
        holder.itemView.alpha = 0f
        holder.itemView.animate().alpha(1f).setDuration(300).start()
    }

    override fun getItemCount(): Int = employees.size

    fun updateList(newList: List<Employee>) {
        employees = newList
        notifyDataSetChanged()
    }
}