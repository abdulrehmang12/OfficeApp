package com.example.hayzelofficeapp.models

import java.io.Serializable

data class Employee(
    val id: String = "",
    val name: String? = null,
    val role: String? = null,
    val department: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val profileImage: String? = null
) : Serializable