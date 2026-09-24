package com.ovijat.bakerystock.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "batch_log")
data class BatchLog(
    @PrimaryKey
    @ColumnInfo(name = "batch_id")
    val batchId: String, // e.g. M2B_YYYYMMDD_HHMMSS_xxxx

    @ColumnInfo(name = "batch_date")
    val batchDate: Long, // Epoch millis

    @ColumnInfo(name = "total_qty")
    val totalQty: Double,

    @ColumnInfo(name = "item_count")
    val itemCount: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)