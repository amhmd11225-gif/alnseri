package com.example.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChannelEvent
import com.example.data.model.EventStatus
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.GoldOutlineChip
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GoldBright
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AlAnsariUiState

@Composable
fun EventsScreen(
    uiState: AlAnsariUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val filters = listOf("الكل", EventStatus.ONGOING.arabicLabel, EventStatus.UPCOMING.arabicLabel, EventStatus.ARCHIVED.arabicLabel)
    var selectedFilter by remember { mutableStateOf("الكل") }

    val filteredEvents = remember(uiState.events, selectedFilter) {
        if (selectedFilter == "الكل") uiState.events
        else uiState.events.filter { it.status.arabicLabel == selectedFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "الفعاليات والمناسبات",
            badgeText = "تقويم قناة الأنصاري",
            onBackClick = onBackClick,
        )

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filters) { filter ->
                        GoldOutlineChip(
                            text = filter,
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            testTag = "events_filter_$filter",
                        )
                    }
                }
            }

            items(filteredEvents, key = { it.id }) { event ->
                EventItemCard(
                    event = event,
                    onAddCalendarReminder = {
                        val calIntent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, event.title)
                            putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                            putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startTimeMillis)
                            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.startTimeMillis + 7_200_000L)
                        }
                        runCatching { context.startActivity(calIntent) }
                    },
                    onShareEvent = {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${event.title}\n${event.hijriDate} (${event.gregorianDate}) - ${event.timeLabel}\n${event.location}",
                            )
                        }
                        context.startActivity(Intent.createChooser(share, "مشاركة الفعالية"))
                    },
                )
            }
        }
    }
}

@Composable
private fun EventItemCard(
    event: ChannelEvent,
    onAddCalendarReminder: () -> Unit,
    onShareEvent: () -> Unit,
) {
    AlAnsariGoldCard(testTag = "event_card_${event.id}") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CrimsonPillBadge(text = event.status.arabicLabel, horizontalPadding = 12.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = GoldBright,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = event.hijriDate,
                    style = MaterialTheme.typography.labelMedium.copy(color = GoldBright),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = event.title,
            style = MaterialTheme.typography.titleMedium.copy(color = PureWhite),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${event.gregorianDate} • ${event.timeLabel}",
            style = MaterialTheme.typography.labelMedium.copy(color = GoldBright, fontSize = 12.5.sp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = GoldBright,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = event.location,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = event.description,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.5.sp),
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Button(
                onClick = onAddCalendarReminder,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                shape = RoundedCornerShape(50),
            ) {
                Icon(
                    imageVector = Icons.Filled.EventAvailable,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة للتقويم", style = MaterialTheme.typography.labelLarge)
            }
            IconButton(
                onClick = onShareEvent,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "مشاركة الفعالية",
                    tint = GoldBright,
                )
            }
        }
    }
}
