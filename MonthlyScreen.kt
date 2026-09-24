package com.ovijat.bakerystock.ui.screens.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ovijat.bakerystock.ui.theme.BorderGray
import com.ovijat.bakerystock.ui.theme.DeepBluePrimary
import com.ovijat.bakerystock.ui.theme.ErrorRed
import com.ovijat.bakerystock.ui.theme.SuccessGreen
import com.ovijat.bakerystock.utils.DateUtils
import com.ovijat.bakerystock.utils.ExportUtils
import com.ovijat.bakerystock.viewmodel.AppViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun MonthlyScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val dailyUsages by viewModel.monthlyDailyUsages.collectAsState()

    val daysInMonth = DateUtils.getDaysInMonth(selectedYear, selectedMonth)
    val horizontalScrollState = rememberScrollState()

    // দ্রুত খোঁজার জন্য Map তৈরি: itemId -> dayOfMonth -> qty
    val dailyUsageMap = remember(dailyUsages) {
        val map = mutableMapOf<Long, MutableMap<Int, Double>>()
        dailyUsages.forEach { row ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = row.usageDate
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val itemMap = map.getOrPut(row.itemId) { mutableMapOf() }
            itemMap[day] = (itemMap[day] ?: 0.0) + row.qty
        }
        map
    }

    val totalMonthUsage = monthlyStats.sumOf { it.totalUsage }
    val totalMonthReceive = monthlyStats.sumOf { it.totalReceive }
    val totalClosing = monthlyStats.sumOf { it.closing }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // ১. মাস ও বছর পরিবর্তনের হেডার
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (selectedMonth == 0) {
                    viewModel.setMonthYear(selectedYear - 1, 11)
                } else {
                    viewModel.setMonthYear(selectedYear, selectedMonth - 1)
                }
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "পূর্ববর্তী মাস", tint = DeepBluePrimary)
            }

            Text(
                text = DateUtils.formatMonthYear(selectedYear, selectedMonth),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepBluePrimary
            )

            IconButton(onClick = {
                if (selectedMonth == 11) {
                    viewModel.setMonthYear(selectedYear + 1, 0)
                } else {
                    viewModel.setMonthYear(selectedYear, selectedMonth + 1)
                }
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "পরবর্তী মাস", tint = DeepBluePrimary)
            }
        }

        // ২. সামারি ইনফো ও এক্সপোর্ট বাটন
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Usage: ${String.format(Locale.US, "%.1f", totalMonthUsage)} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                    Text(
                        text = "Receive: ${String.format(Locale.US, "%.1f", totalMonthReceive)} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                    Text(
                        text = "Closing: ${String.format(Locale.US, "%.1f", totalClosing)} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBluePrimary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val exportedFile = ExportUtils.exportMonthlyUsageCsv(
                            context = context,
                            year = selectedYear,
                            month = selectedMonth,
                            items = monthlyStats,
                            dailyUsages = dailyUsages
                        )
                        exportedFile?.let { file ->
                            ExportUtils.shareFile(context, file)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBluePrimary)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("📤 EXPORT TO EXCEL / CSV", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ৩. অনুভূমিক স্ক্রলেবল ম্যাট্রিক্স টেবিল (Day 1 - Day 31)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                // টেবিল হেডার রো
                Row(
                    modifier = Modifier
                        .background(Color(0xFFE8ECEF))
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(text = "Code", width = 50.dp, isHeader = true)
                    TableCell(text = "Raw Material Name", width = 160.dp, isHeader = true, alignment = Alignment.CenterStart)
                    TableCell(text = "Total Usage", width = 90.dp, isHeader = true)

                    for (day in 1..daysInMonth) {
                        TableCell(text = "D$day", width = 55.dp, isHeader = true)
                    }
                }

                // ডাটা রো তালিকা
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(monthlyStats, key = { it.id }) { item ->
                        val itemDayUsage = dailyUsageMap[item.id] ?: emptyMap()
                        Row(
                            modifier = Modifier
                                .border(0.5.dp, Color(0xFFF0F0F0))
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell(text = item.code, width = 50.dp)
                            TableCell(text = item.name, width = 160.dp, alignment = Alignment.CenterStart)
                            TableCell(
                                text = String.format(Locale.US, "%.2f", item.totalUsage),
                                width = 90.dp,
                                isBold = true,
                                textColor = DeepBluePrimary
                            )

                            for (day in 1..daysInMonth) {
                                val qty = itemDayUsage[day] ?: 0.0
                                TableCell(
                                    text = if (qty > 0) String.format(Locale.US, "%.2f", qty) else "-",
                                    width = 55.dp,
                                    textColor = if (qty > 0) ErrorRed else Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableCell(
    text: String,
    width: Dp,
    isHeader: Boolean = false,
    isBold: Boolean = false,
    textColor: Color = Color.Black,
    alignment: Alignment = Alignment.Center
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = alignment
    ) {
        Text(
            text = text,
            fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isHeader) 12.sp else 11.sp,
            color = if (isHeader) DeepBluePrimary else textColor,
            textAlign = TextAlign.Center
        )
    }
}