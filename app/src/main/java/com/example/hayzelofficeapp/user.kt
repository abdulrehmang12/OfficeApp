package com.example.hayzelofficeapp

data class User(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val department: String = "",
    val position: String = "",
    val role: String = "Employee", // CEO, HR, Manager, Employee
    val managerId: String = "",
    val profileImage: String = "",
    val employmentStatus: String = "", // Full-time, Part-time
    val joinDate: String = "",
    val isActive: Boolean = true
)