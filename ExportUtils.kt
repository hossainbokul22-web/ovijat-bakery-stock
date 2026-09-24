package com.ovijat.bakerystock.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.ovijat.bakerystock.data.dao.DailyUsageRow
import com.ovijat.bakerystock.data.dao.ItemStockCalculation
import java.io.File
import java.io.FileWriter
import java.util.Calendar
import java.util.Locale

object ExportUtils {

    fun exportMonthlyUsageCsv(
        context: Context,
        year: Int,
        month: Int,
        items: List<ItemStockCalculation>,
        dailyUsages: List<DailyUsageRow>
    ): File? {
        return try {
            val daysInMonth = DateUtils.getDaysInMonth(year, month)
            val fileName = "Monthly_Usage_${year}_${month + 1}.csv"
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            val file = File(exportDir, fileName)

            val writer = FileWriter(file)
            val header = buildString {
                append("Code,Item Name,UoM,Opening,Receive,Usage,Closing")
                for (day in 1..daysInMonth) {
                    append(",Day $day")
                }
            }
            writer.append(header).append("\n")

            val usageMap = mutableMapOf<Long, MutableMap<Int, Double>>()
            dailyUsages.forEach { row ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = row.usageDate
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val itemMap = usageMap.getOrPut(row.itemId) { mutableMapOf() }
                itemMap[day] = (itemMap[day] ?: 0.0) + row.qty
            }

            for (item in items) {
                val rowStr = buildString {
                    append("\"${item.code}\",\"${item.name}\",\"${item.uom}\",")
                    append(String.format(Locale.US, "%.3f,%.3f,%.3f,%.3f", item.opening, item.totalReceive, item.totalUsage, item.closing))
                    val itemDayMap = usageMap[item.id] ?: emptyMap()
                    for (day in 1..daysInMonth) {
                        val dayQty = itemDayMap[day] ?: 0.0
                        append(String.format(Locale.US, ",%.3f", dayQty))
                    }
                }
                writer.append(rowStr).append("\n")
            }

            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Stock Report Export করুন"))
    }
}