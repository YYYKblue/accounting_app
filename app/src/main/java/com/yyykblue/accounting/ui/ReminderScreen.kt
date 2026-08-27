package com.yyykblue.accounting.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yyykblue.accounting.data.DailyReminderEntity
import com.yyykblue.accounting.model.DailyReminderRules
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlinx.coroutines.delay

@Composable
fun ReminderScreen(
    reminders: List<DailyReminderEntity>,
    onAdd: (String) -> Unit,
    onToggle: (DailyReminderEntity, Long) -> Unit,
    onDelete: (DailyReminderEntity) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    val todayEpochDay = rememberTodayEpochDay()
    val completedCount = reminders.count {
        DailyReminderRules.isCompletedOn(it.completedEpochDay, todayEpochDay)
    }

    fun addReminder() {
        if (title.isBlank()) return
        onAdd(title)
        title = ""
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("每日提醒", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "今天完成后打勾，第二天会自动恢复为未完成。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Text(
                    "今日完成  $completedCount / ${reminders.size}",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(40) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("新增每日待办") },
                placeholder = { Text("例如：吃维生素、吃鱼油") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { addReminder() }),
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { addReminder() },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("添加待办")
            }
            Spacer(Modifier.height(12.dp))
        }

        if (reminders.isEmpty()) {
            item {
                Text(
                    "还没有每日待办，可以先添加需要每天完成的事项。",
                    modifier = Modifier.padding(vertical = 28.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(reminders, key = { it.id }) { reminder ->
                ReminderRow(
                    reminder = reminder,
                    todayEpochDay = todayEpochDay,
                    onToggle = { onToggle(reminder, todayEpochDay) },
                    onDelete = { onDelete(reminder) },
                )
            }
        }
    }
}

@Composable
private fun ReminderRow(
    reminder: DailyReminderEntity,
    todayEpochDay: Long,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val completed = DailyReminderRules.isCompletedOn(reminder.completedEpochDay, todayEpochDay)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = completed, onCheckedChange = { onToggle() })
            Text(
                text = reminder.title,
                modifier = Modifier.weight(1f),
                color = if (completed) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (completed) TextDecoration.LineThrough else null,
                fontWeight = FontWeight.Medium,
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "删除提醒",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun rememberTodayEpochDay(): Long {
    var todayEpochDay by remember { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                todayEpochDay = LocalDate.now().toEpochDay()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(todayEpochDay) {
        val now = ZonedDateTime.now()
        val nextDay = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
        delay(Duration.between(now, nextDay).toMillis().coerceAtLeast(1_000L) + 250L)
        todayEpochDay = LocalDate.now().toEpochDay()
    }

    return todayEpochDay
}
