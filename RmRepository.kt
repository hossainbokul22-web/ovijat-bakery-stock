package com.ovijat.bakerystock.data.repository

import com.ovijat.bakerystock.data.dao.DailyUsageRow
import com.ovijat.bakerystock.data.dao.ItemStockCalculation
import com.ovijat.bakerystock.data.dao.ReceiveWithItem
import com.ovijat.bakerystock.data.dao.RmDao
import com.ovijat.bakerystock.data.entity.BatchLog
import com.ovijat.bakerystock.data.entity.Item
import com.ovijat.bakerystock.data.entity.Mixing2Usage
import com.ovijat.bakerystock.data.entity.StoreReceive
import kotlinx.coroutines.flow.Flow

class RmRepository(private val rmDao: RmDao) {

    fun getAllItems(): Flow<List<Item>> = rmDao.getAllItemsFlow()

    suspend fun getAllItemsList(): List<Item> = rmDao.getAllItems()

    suspend fun insertReceive(receive: StoreReceive): Long = rmDao.insertReceive(receive)

    suspend fun deleteReceiveById(id: Long) = rmDao.deleteReceiveById(id)

    fun getRecentReceives(): Flow<List<ReceiveWithItem>> = rmDao.getRecentReceivesFlow()

    suspend fun submitBatch(batch: BatchLog, usages: List<Mixing2Usage>) {
        rmDao.submitBatchTransaction(batch, usages)
    }

    fun getLiveStock(targetDate: Long): Flow<List<ItemStockCalculation>> =
        rmDao.getLiveStockFlow(targetDate)

    fun getMonthlyItemStats(startOfMonth: Long, endOfMonth: Long): Flow<List<ItemStockCalculation>> =
        rmDao.getMonthlyItemStatsFlow(startOfMonth, endOfMonth)

    fun getDailyUsages(startOfMonth: Long, endOfMonth: Long): Flow<List<DailyUsageRow>> =
        rmDao.getDailyUsagesForMonthFlow(startOfMonth, endOfMonth)

    suspend fun getAllReceives(): List<StoreReceive> = rmDao.getAllReceives()
    suspend fun getAllUsages(): List<Mixing2Usage> = rmDao.getAllUsages()
    suspend fun getAllBatches(): List<BatchLog> = rmDao.getAllBatches()
}