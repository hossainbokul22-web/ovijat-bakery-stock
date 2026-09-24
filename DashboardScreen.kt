package com.ovijat.bakerystock.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ovijat.bakerystock.data.entity.Item
import com.ovijat.bakerystock.ui.components.StatCard
import com.ovijat.bakerystock.ui.components.StatusBadge
import com.ovijat.bakerystock.ui.theme.DeepBlueLight
import com.ovijat.bakerystock.ui.theme.DeepBluePrimary
import com.ovijat.bakerystock.ui.theme.ErrorRedLight
import com.ovijat.bakerystock.ui.theme.WarningOrangeLight
import com.ovijat.bakerystock.viewmodel.AppViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val items by viewModel.items.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()

    val totalOpening = items.sumOf { it.opening }
    val totalReceive = monthlyStats.sumOf { it.totalReceive }
    val totalUsage = monthlyStats.sumOf { it.totalUsage }
    val totalClosing = monthlyStats.sumOf { it.closing }

    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    val activeSelectedStats = monthlyStats.find { it.id == selectedItem?.id }

    val shortageItems = monthlyStats.filter { it.closing < 0.0 }
    val zeroItems = monthlyStats.filter { it.closing == 0.0 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Ovijat Food & Beverage Industries Ltd.",
                style = MaterialTheme.typography.titleMedium,
                color = DeepBluePrimary
            )
            Text(
                text = "Bakery Section (Mixing-2 RM Monitoring)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // ৪টি প্রধান মেট্রিক কার্ড
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Opening",
                    value = "${String.format(Locale.US, "%.1f", totalOpening)} kg",
                    icon = Icons.Default.Inventory,
                    backgroundColor = DeepBlueLight
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Receive",
                    value = "${String.format(Locale.US, "%.1f", totalReceive)} kg",
                    icon = Icons.Default.CallReceived,
                    backgroundColor = Color(0xFFE8F5E9)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Usage",
                    value = "${String.format(Locale.US, "%.1f", totalUsage)} kg",
                    icon = Icons.Default.Archive,
                    backgroundColor = WarningOrangeLight
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Closing",
                    value = "${String.format(Locale.US, "%.1f", totalClosing)} kg",
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = if (totalClosing < 0) ErrorRedLight else Color(0xFFF3E5F5)
                )
            }
        }

        // Low Stock Alert সেকশন
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🚨 Stock Status Alert",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (shortageItems.isEmpty() && zeroItems.isEmpty()) {
                        Text(
                            text = "সব কাঁচামালের স্টক সন্তোষজনক রয়েছে।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    } else {
                        if (shortageItems.isNotEmpty()) {
                            Text(
                                text = "Shortage Items (${shortageItems.size}):",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                            shortageItems.forEach {
                                Text(
                                    text = "• [${it.code}] ${it.name}: ${String.format(Locale.US, "%.3f", it.closing)} kg",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        if (zeroItems.isNotEmpty()) {
                            Text(
                                text = "Zero Stock Items (${zeroItems.size}):",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE65100)
                            )
                            zeroItems.take(5).forEach {
                                Text(
                                    text = "• [${it.code}] ${it.name}",
                                    fontSize = 13.sp,
                                    color = Color(0xFFE65100)
                                )
                            }
                            if (zeroItems.size > 5) {
                                Text(text = "...আরও ${zeroItems.size - 5}টি আইটেম", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // Quick Item Lookup Dropdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔍 Item Quick Stock Lookup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedItem?.let { "${it.code} - ${it.name}" } ?: "আইটেম নির্বাচন করুন",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            items.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.code} — ${item.name}") },
                                    onClick = {
                                        selectedItem = item
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (activeSelectedStats != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeSelectedStats.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Code: ${activeSelectedStats.code} | UoM: ${activeSelectedStats.uom}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            StatusBadge(closing = activeSelectedStats.closing)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Monthly Opening: ${String.format(Locale.US, "%.3f", activeSelectedStats.opening)} kg")
                            Text("+ Receive: ${String.format(Locale.US, "%.3f", activeSelectedStats.totalReceive)} kg")
                            Text("- Usage: ${String.format(Locale.US, "%.3f", activeSelectedStats.totalUsage)} kg")
                            Text(
                                "Closing Stock: ${String.format(Locale.US, "%.3f", activeSelectedStats.closing)} kg",
                                fontWeight = FontWeight.Bold,
                                color = if (activeSelectedStats.closing < 0) MaterialTheme.colorScheme.error else DeepBluePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}