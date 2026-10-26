package com.past9.phoneaos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.past9.phoneaos.agent.ModelInfo
import com.past9.phoneaos.ui.StatusPill
import com.past9.phoneaos.ui.i18n.tr
import com.past9.phoneaos.ui.theme.Eyebrow
import java.time.LocalDate

/** Slide-up model switcher: the models this account can use right now, newest first. */
@Composable
fun ModelPickerSheet(title: String, models: List<ModelInfo>?, current: String, error: String?, onPick: (ModelInfo) -> Unit, onDismiss: () -> Unit, subtitle: String? = null, allowManualEntry: Boolean = false, onManualEntry: ((String) -> Unit)? = null) {
    var q by remember { mutableStateOf("") }
    val cs = MaterialTheme.colorScheme
    val recent = LocalDate.now().minusDays(150).toString()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp)) {
            Text(tr("MODEL"), style = Eyebrow, color = cs.primary)
            Text(tr(title), style = MaterialTheme.typography.headlineSmall)
            Text(tr(subtitle ?: "Pulled live from your provider. Newest first."), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            if ((models?.size ?: 0) > 10) OutlinedTextField(q, { q = it }, placeholder = { Text(tr("Search models")) }, leadingIcon = { Icon(Icons.Rounded.Search, null) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge)
        }
        when {
            error != null -> Text(error, Modifier.padding(20.dp), color = cs.error)
            models == null -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { LoadingIndicator() }
            models.isEmpty() && !allowManualEntry -> Text(tr("No models came back for this account."), Modifier.padding(20.dp), color = cs.onSurfaceVariant)
            else -> Column(Modifier.fillMaxWidth()) {
                if (models != null && models.isNotEmpty()) {
                    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 560.dp), contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 32.dp)) {
                        items(models.filter { q.isBlank() || it.name.contains(q, true) || it.id.contains(q, true) }, key = { it.id.ifBlank { "default" } }) { m ->
                            val sel = m.id == current
                            Surface(onClick = { onPick(m) }, shape = MaterialTheme.shapes.large, color = if (sel) cs.primaryContainer else androidx.compose.ui.graphics.Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(tr(m.name), style = MaterialTheme.typography.titleMedium, color = if (sel) cs.onPrimaryContainer else cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        val sub = listOfNotNull(m.note.takeIf { it.isNotBlank() }?.let { tr(it) }, m.id.takeIf { it.isNotBlank() && it != m.name }, m.released.takeIf { it.isNotBlank() }).joinToString(" · ")
                                        if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = if (sel) cs.onPrimaryContainer else cs.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                    if (m.released >= recent && m.released.isNotBlank()) StatusPill(tr("New"), cs.tertiaryContainer, cs.onTertiaryContainer, Modifier.padding(start = 6.dp))
                                    if (m.vision) Icon(Icons.Rounded.Image, tr("Sees images"), Modifier.padding(start = 8.dp).size(18.dp), tint = if (sel) cs.onPrimaryContainer else cs.onSurfaceVariant)
                                    if (sel) Icon(Icons.Rounded.Check, tr("Selected"), Modifier.padding(start = 8.dp), tint = cs.onPrimaryContainer)
                                }
                            }
                        }
                    }
                }
                if (allowManualEntry) {
                    val manualHandler = onManualEntry
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth()) {
                        Divider()
                        Spacer(Modifier.height(8.dp))
                        Text(tr("Or enter model ID manually"), style = Eyebrow, color = cs.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = q,
                            onValueChange = { q = it },
                            label = { Text(tr("Model ID (e.g. deepseek/deepseek-chat)")) },
                            placeholder = { Text(tr("Model ID")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { manualHandler?.invoke(q.trim()) })
                        )
                    }
                }
            }
        }
    }
}
