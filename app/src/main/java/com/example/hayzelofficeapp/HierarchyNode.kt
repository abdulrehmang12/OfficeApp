package com.example.hayzelofficeapp

data class HierarchyNode(
    val user: User,
    val level: Int // 0 = CEO, 1 = Manager, 2 = Employee, etc.
)