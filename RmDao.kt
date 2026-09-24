package com.ovijat.bakerystock.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ovijat.bakerystock.data.entity.BatchLog
import com.ovijat.bakerystock.data.entity.Item
import com.ovijat.bakerystock.data.entity.Mixing2Usage
import com.ovijat.bakerystock.data.entity.StoreReceive
import kotlinx.coroutines.flow.Flow

// Store Receive টেবিলের সাথে Item টেবিল JOIN করার মডেল
data class ReceiveWithItem(
    val id: Long,
    val itemId: Long,
    val code: String,
    val name: String,
    val uom: String,
    val entryDate: Long,
    val qty: Double,
    val note: String?
)

// লাইভ স্টক এবং মাসিক স্টকের গাণিতিক রেজাল্ট মডেল
data class ItemStockCalculation(
    val id: Long,
    val code: String,
    val name: String,
    val uom: String,
    val opening: Double,
    val totalReceive: Double,
    val totalUsage: Double,
    val closing: Double
)

// মাসিক ম্যাট্রিক্সের জন্য প্রতিটি দিনের ব্যবহারের মডেল
data class DailyUsageRow(
    val itemId: Long,
    val code: String,
    val name: String,
    val uom: String,
    val usageDate: Long,
    val qty: Double
)

@Dao
interface RmDao {

    // ----------------------------------------------------
    // ১. RM ITEMS (কাঁচামালের মাস্টার তালিকা)
    // ----------------------------------------------------
    @Query("SELECT * FROM rm_items ORDER BY CAST(code AS INTEGER) ASC")
    fun getAllItemsFlow(): Flow<List<Item>>

    @Query("SELECT * FROM rm_items ORDER BY CAST(code AS INTEGER) ASC")
    suspend fun getAllItems(): List<Item>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllItems(items: List<Item>)

    @Query("SELECT COUNT(*) FROM rm_items")
    suspend fun getItemCount(): Int

    // ----------------------------------------------------
    // ২. STORE RECEIVE (স্টোর রিসিভ এন্ট্রি ও সাম্প্রতিক তালিকা)
    // ----------------------------------------------------
    @Insert
    suspend fun insertReceive(receive: StoreReceive): Long

    @Delete
    suspend fun deleteReceive(receive: StoreReceive)

    @Query("DELETE FROM store_receive WHERE id = :id")
    suspend fun deleteReceiveById(id: Long)

    @Query("""
        SELECT 
            r.id, 
            r.item_id as itemId, 
            i.code, 
            i.name, 
            i.uom, 
            r.entry_date as entryDate, 
            r.qty, 
            r.note 
        FROM store_receive r 
        INNER JOIN rm_items i ON r.item_id = i.id 
        ORDER BY r.created_at DESC 
        LIMIT 60
    """)
    fun getRecentReceivesFlow(): Flow<List<ReceiveWithItem>>

    @Query("SELECT * FROM store_receive")
    suspend fun getAllReceives(): List<StoreReceive>

    // ----------------------------------------------------
    // ৩. MIXING-2 BATCH (অ্যাটমিক ব্যাচ সাবমিশন ট্রানজ্যাকশন)
    // ----------------------------------------------------
    @Insert
    suspend fun insertBatchLog(batch: BatchLog)

    @Insert
    suspend fun insertUsageList(usages: List<Mixing2Usage>)

    @Transaction
    suspend fun submitBatchTransaction(batch: BatchLog, usages: List<Mixing2Usage>) {
        insertBatchLog(batch)
        insertUsageList(usages)
    }

    @Query("SELECT * FROM mixing2_usage")
    suspend fun getAllUsages(): List<Mixing2Usage>

    @Query("SELECT * FROM batch_log ORDER BY created_at DESC")
    suspend fun getAllBatches(): List<BatchLog>

    // ----------------------------------------------------
    // ৪. LIVE STOCK CALCULATION (যেকোনো তারিখ D পর্যন্ত লাইভ স্টক)
    // Formula: Closing = Opening + SUM(Receive <= D) - SUM(Usage <= D)
    // ----------------------------------------------------
    @Query("""
        SELECT 
            i.id,
            i.code,
            i.name,
            i.uom,
            i.opening,
            COALESCE((SELECT SUM(r.qty) FROM store_receive r WHERE r.item_id = i.id AND r.entry_date <= :targetDate), 0.0) AS totalReceive,
            COALESCE((SELECT SUM(u.qty) FROM mixing2_usage u WHERE u.item_id = i.id AND u.usage_date <= :targetDate), 0.0) AS totalUsage,
            (i.opening + 
             COALESCE((SELECT SUM(r.qty) FROM store_receive r WHERE r.item_id = i.id AND r.entry_date <= :targetDate), 0.0) - 
             COALESCE((SELECT SUM(u.qty) FROM mixing2_usage u WHERE u.item_id = i.id AND u.usage_date <= :targetDate), 0.0)
            ) AS closing
        FROM rm_items i
        ORDER BY CAST(i.code AS INTEGER) ASC
    """)
    fun getLiveStockFlow(targetDate: Long): Flow<List<ItemStockCalculation>>

    // ----------------------------------------------------
    // ৫. MONTHLY SUMMARY & USAGE MATRIX CALCULATION
    // ----------------------------------------------------
    @Query("""
        SELECT 
            i.id,
            i.code,
            i.name,
            i.uom,
            i.opening,
            COALESCE((SELECT SUM(r.qty) FROM store_receive r WHERE r.item_id = i.id AND r.entry_date BETWEEN :startOfMonth AND :endOfMonth), 0.0) AS totalReceive,
            COALESCE((SELECT SUM(u.qty) FROM mixing2_usage u WHERE u.item_id = i.id AND u.usage_date BETWEEN :startOfMonth AND :endOfMonth), 0.0) AS totalUsage,
            (i.opening + 
             COALESCE((SELECT SUM(r.qty) FROM store_receive r WHERE r.item_id = i.id AND r.entry_date <= :endOfMonth), 0.0) - 
             COALESCE((SELECT SUM(u.qty) FROM mixing2_usage u WHERE u.item_id = i.id AND u.usage_date <= :endOfMonth), 0.0)
            ) AS closing
        FROM rm_items i
        ORDER BY CAST(i.code AS INTEGER) ASC
    """)
    fun getMonthlyItemStatsFlow(startOfMonth: Long, endOfMonth: Long): Flow<List<ItemStockCalculation>>

    @Query("""
        SELECT 
            u.item_id as itemId,
            i.code,
            i.name,
            i.uom,
            u.usage_date as usageDate,
            SUM(u.qty) as qty
        FROM mixing2_usage u
        INNER JOIN rm_items i ON u.item_id = i.id
        WHERE u.usage_date BETWEEN :startOfMonth AND :endOfMonth
        GROUP BY u.item_id, u.usage_date
    """)
    fun getDailyUsagesForMonthFlow(startOfMonth: Long, endOfMonth: Long): Flow<List<DailyUsageRow>>
}