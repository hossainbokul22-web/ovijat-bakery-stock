package com.ovijat.bakerystock.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rm_items",
    indices = [
        Index(value = ["code"], unique = true)
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val code: String,

    val name: String,

    val uom: String = "kg",

    val opening: Double = 0.0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)