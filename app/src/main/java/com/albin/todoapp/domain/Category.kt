package com.albin.todoapp.domain

enum class Category(val displayName: String) {
    WORK("Trabajo"),
    PERSONAL("Personal"),
    SHOPPING("Compras"),
    OTHER("Otro");

    companion object {
        fun fromOrdinal(ordinal: Int): Category? {
            return entries.find { it.ordinal == ordinal }
        }
    }
}
