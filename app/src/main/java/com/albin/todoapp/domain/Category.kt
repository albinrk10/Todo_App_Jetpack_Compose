package com.albin.todoapp.domain

enum class Category {
    WORK,
    PERSONAL,
    SHOPPING,
    OTHER;

    companion object {
        fun fromOrdinal(ordinal: Int): Category? {
            return entries.find { it.ordinal == ordinal }
        }
    }
}