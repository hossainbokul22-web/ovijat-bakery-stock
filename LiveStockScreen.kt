package com.ovijat.bakerystock.ui.screens.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ovijat.bakerystock.ui.components.DatePickerButton
import com.ovijat.bakerystock.ui.components.StatusBadge
import com.ovijat.bakerystock.ui.theme.DeepBluePrimary
import com.ovijat.bakerystock.utils.DateUtils
import com.ovijat.bakerystock.viewmodel.AppViewModel
import java.util.Locale

@Composable
fun LiveStockScreen(viewModel: AppViewModel) {
    val liveStockDate by viewModel.liveStockDate.collectAsState()
    val stockList by viewModel.liveStockList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DatePickerButton(
                modifier = Modifier.weight(1f),
                selectedDate = liveStockDate,
                onDateSelected = { viewModel.setLiveStockDate(it) },
                label = "স্টক তারিখ"
            )
            IconButton(
                onClick = { viewModel.setLiveStockDate(DateUtils.getStartOfDay()) }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "আজকের তারিখে রিফ্রেশ",
                    tint = DeepBluePrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(stockList, key = { it.id }) { itemStock ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${itemStock.code} — ${itemStock.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(closing = itemStock.closing)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Opening: ${String.format(Locale.US, "%.3f", itemStock.opening)} ${itemStock.uom}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "+ Receive: ${String.format(Locale.US, "%.3f", itemStock.totalReceive)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "- Usage: ${String.format(Locale.US, "%.3f", itemStock.totalUsage)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC62828)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Closing Stock:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.3f", itemStock.closing)} ${itemStock.uom}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (itemStock.closing < 0) MaterialTheme.colorScheme.error else DeepBluePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}