package com.ovijat.bakerystock.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mixing2_usage",
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["item_id"]),
        Index(value = ["usage_date"]),
        Index(value = ["batch_id"])
    ]
)
data class Mixing2Usage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "item_id")
    val itemId: Long,

    @ColumnInfo(name = "usage_date")
    val usageDate: Long, // Epoch millis (দিনের midnight)

    val qty: Double,

    @ColumnInfo(name = "batch_id")
    val batchId: String // BatchLog টেবিলের সাথে রেফারেন্স
)