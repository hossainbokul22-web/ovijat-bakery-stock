package com.ovijat.bakerystock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ovijat.bakerystock.data.dao.DailyUsageRow
import com.ovijat.bakerystock.data.dao.ItemStockCalculation
import com.ovijat.bakerystock.data.dao.ReceiveWithItem
import com.ovijat.bakerystock.data.database.AppDatabase
import com.ovijat.bakerystock.data.entity.BatchLog
import com.ovijat.bakerystock.data.entity.Item
import com.ovijat.bakerystock.data.entity.Mixing2Usage
import com.ovijat.bakerystock.data.entity.StoreReceive
import com.ovijat.bakerystock.data.repository.RmRepository
import com.ovijat.bakerystock.utils.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class CurrentBatchItem(
    val item: Item,
    val qty: Double
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RmRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = RmRepository(db.rmDao())
    }

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    // ১. কাঁচামাল মাস্টার তালিকা
    val items: StateFlow<List<Item>> = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ২. স্টোর রিসিভ তালিকা
    val recentReceives: StateFlow<List<ReceiveWithItem>> = repository.getRecentReceives()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ৩. লাইভ স্টক তারিখ ও স্টেট
    private val _liveStockDate = MutableStateFlow(DateUtils.getStartOfDay())
    val liveStockDate: StateFlow<Long> = _liveStockDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val liveStockList: StateFlow<List<ItemStockCalculation>> = _liveStockDate.flatMapLatest { date ->
        repository.getLiveStock(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ৪. মাসিক ব্যবহারের স্টেট
    private val currentCal = Calendar.getInstance()
    private val _selectedYear = MutableStateFlow(currentCal.get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(currentCal.get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyStats: StateFlow<List<ItemStockCalculation>> = _selectedMonth.flatMapLatest { month ->
        val year = _selectedYear.value
        val start = DateUtils.getStartOfMonth(year, month)
        val end = DateUtils.getEndOfMonth(year, month)
        repository.getMonthlyItemStats(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyDailyUsages: StateFlow<List<DailyUsageRow>> = _selectedMonth.flatMapLatest { month ->
        val year = _selectedYear.value
        val start = DateUtils.getStartOfMonth(year, month)
        val end = DateUtils.getEndOfMonth(year, month)
        repository.getDailyUsages(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ৫. Mixing-2 ব্যাচ ট্রানজ্যাকশন স্টেট
    private val _currentBatchDate = MutableStateFlow(DateUtils.getStartOfDay())
    val currentBatchDate: StateFlow<Long> = _currentBatchDate.asStateFlow()

    private val _currentBatchItems = MutableStateFlow<List<CurrentBatchItem>>(emptyList())
    val currentBatchItems: StateFlow<List<CurrentBatchItem>> = _currentBatchItems.asStateFlow()

    fun setLiveStockDate(dateMillis: Long) {
        _liveStockDate.value = DateUtils.getStartOfDay(dateMillis)
    }

    fun setBatchDate(dateMillis: Long) {
        _currentBatchDate.value = DateUtils.getStartOfDay(dateMillis)
    }

    fun setMonthYear(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
    }

    // স্টোর রিসিভ সংরক্ষণ
    fun addStoreReceive(itemId: Long, dateMillis: Long, qty: Double, note: String?) {
        if (qty <= 0) {
            viewModelScope.launch { _snackbarEvent.emit("⚠️ Quantity অবশ্যই শূন্যের বেশি হতে হবে!") }
            return
        }
        viewModelScope.launch {
            val receive = StoreReceive(
                itemId = itemId,
                entryDate = DateUtils.getStartOfDay(dateMillis),
                qty = qty,
                note = note?.trim()?.ifEmpty { null }
            )
            repository.insertReceive(receive)
            _snackbarEvent.emit("✅ Store Receive সফলভাবে সংরক্ষণ করা হয়েছে!")
        }
    }

    fun deleteReceive(id: Long) {
        viewModelScope.launch {
            repository.deleteReceiveById(id)
            _snackbarEvent.emit("🗑️ Receive এন্ট্রি মুছে ফেলা হয়েছে!")
        }
    }

    // Mixing-2 ব্যাচে আইটেম যুক্ত করা (ডুপ্লিকেট হলে স্বয়ংক্রিয় যোগ হবে)
    fun addItemToBatch(item: Item, qty: Double) {
        if (qty <= 0) {
            viewModelScope.launch { _snackbarEvent.emit("⚠️ সঠিক Quantity লিখুন!") }
            return
        }
        val currentList = _currentBatchItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.item.id == item.id }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(qty = existing.qty + qty)
        } else {
            currentList.add(CurrentBatchItem(item = item, qty = qty))
        }
        _currentBatchItems.value = currentList
    }

    fun removeItemFromBatch(item: Item) {
        _currentBatchItems.value = _currentBatchItems.value.filter { it.item.id != item.id }
    }

    fun clearBatch() {
        _currentBatchItems.value = emptyList()
    }

    // সম্পূর্ণ ব্যাচ একসাথে সাবমিট করা
    fun submitBatch() {
        val itemsList = _currentBatchItems.value
        if (itemsList.isEmpty()) {
            viewModelScope.launch { _snackbarEvent.emit("⚠️ ব্যাচে কোনো আইটেম যোগ করা হয়নি!") }
            return
        }

        val batchId = DateUtils.generateBatchId()
        val totalQty = itemsList.sumOf { it.qty }
        val batchDate = _currentBatchDate.value

        val batchLog = BatchLog(
            batchId = batchId,
            batchDate = batchDate,
            totalQty = totalQty,
            itemCount = itemsList.size
        )

        val usageEntries = itemsList.map {
            Mixing2Usage(
                itemId = it.item.id,
                usageDate = batchDate,
                qty = it.qty,
                batchId = batchId
            )
        }

        viewModelScope.launch {
            repository.submitBatch(batchLog, usageEntries)
            _snackbarEvent.emit("✅ Batch $batchId submitted — ${itemsList.size} items, ${String.format(Locale.US, "%.3f", totalQty)} kg")
            clearBatch()
        }
    }
}