package com.past9.phoneaos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.past9.phoneaos.ui.AppCard
import com.past9.phoneaos.ui.SubScreen
import com.past9.phoneaos.ui.i18n.tr
import com.past9.phoneaos.ui.i18n.trArgs
import com.past9.phoneaos.ui.theme.Eyebrow
import com.past9.phoneaos.ui.theme.LocalExtra

/**
 * Download & setup screen for on-device Farsi Whisper STT.
 * Follows the exact same pattern as SubSetupScreen: auto-start download,
 * progress bar, success/error states, details toggle.
 */
@Composable
fun WhisperSetupScreen(
    modelSize: String,
    modelPath: String?,
    isDownloading: Boolean,
    downloadProgress: Float,
    downloadError: String?,
    onBack: () -> Unit,
    onStartDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onRetry: () -> Unit,
    onDone: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val extra = LocalExtra.current
    var showLog by remember { mutableStateOf(false) }

    SubScreen(tr("On-device Farsi voice"), null, onBack) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState())
                .padding(horizontal = com.past9.phoneaos.ui.sidePad(16.dp, com.past9.phoneaos.ui.Readable.FORM))
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Model size info card
            val (sizeLabel, sizeDesc) = when (modelSize) {
                "tiny"  -> tr("Tiny (39 MB)") to tr("Fast, good for short commands")
                "base"  -> tr("Base (148 MB)") to tr("Balanced, recommended for Farsi")
                "small" -> tr("Small (488 MB)") to tr("Better accuracy, slower")
                else    -> modelSize to ""
            }

            // 1. Not downloaded yet
            if (modelPath == null && !isDownloading && downloadError == null) {
                AppCard {
                    Text(tr("SETUP"), style = Eyebrow, color = cs.primary)
                    Text(trArgs("Download %1\$s model for offline Farsi voice", sizeLabel), style = MaterialTheme.typography.titleLarge)
                    Text(sizeDesc, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onStartDownload, modifier = Modifier.weight(1f).heightIn(min = 52.dp), shapes = ButtonDefaults.shapes()) {
                            Icon(Icons.Rounded.Download, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(tr("Download"))
                        }
                        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).heightIn(min = 52.dp), shapes = ButtonDefaults.shapes()) {
                            Text(tr("Cancel"))
                        }
                    }
                }
            }
            // 2. Downloading
            else if (isDownloading) {
                AppCard {
                    Text(tr("DOWNLOADING"), style = Eyebrow, color = cs.primary)
                    Text(trArgs("Downloading %1\$s…", sizeLabel), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    LinearWavyProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = cs.primary,
                        trackColor = cs.primary.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(trArgs("%1\$d%%", (downloadProgress * 100).toInt()), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onCancelDownload, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shapes = ButtonDefaults.shapes()) {
                        Text(tr("Cancel download"))
                    }
                }
            }
            // 3. Error
            else if (downloadError != null) {
                AppCard(container = cs.errorContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ErrorOutline, null, tint = cs.error)
                        Spacer(Modifier.width(10.dp))
                        Text(tr("Download failed"), style = MaterialTheme.typography.titleMedium, color = cs.onErrorContainer)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(downloadError, style = MaterialTheme.typography.bodyMedium, color = cs.onErrorContainer)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onRetry, modifier = Modifier.weight(1f).heightIn(min = 52.dp), shapes = ButtonDefaults.shapes()) {
                            Text(tr("Retry"))
                        }
                        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).heightIn(min = 52.dp), shapes = ButtonDefaults.shapes()) {
                            Text(tr("Back"))
                        }
                    }
                }
            }
            // 4. Success - model ready
            else if (modelPath != null) {
                AppCard(container = extra.successContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = extra.success)
                        Spacer(Modifier.width(10.dp))
                        Text(trArgs("%1\$s ready", sizeLabel), style = MaterialTheme.typography.titleLarge, color = extra.onSuccess)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(tr("Offline Farsi voice is ready. No internet needed, no data leaves your phone."), style = MaterialTheme.typography.bodyMedium, color = extra.onSuccess)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onDone, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shapes = ButtonDefaults.shapes()) {
                        Text(tr("Done"))
                    }
                }
            }
        }
    }
}

/**
 * Bottom sheet for choosing Whisper model size.
 * Mirrors the VoiceSheet provider picker pattern.
 */
@Composable
fun WhisperModelPickerSheet(
    currentSize: String,
    onSizeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val sizes = listOf("tiny" to tr("Tiny (39 MB)"), "base" to tr("Base (148 MB)"), "small" to tr("Small (488 MB)"))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(tr("Choose model size"), style = MaterialTheme.typography.headlineSmall)
            Text(tr("Larger = better accuracy, more storage & RAM"), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sizes.forEach { (id, label) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSizeSelected(id); onDismiss() }
                            .padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = if (currentSize == id) cs.primaryContainer else cs.surfaceContainerHighest
                    ) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            if (currentSize == id) Icon(Icons.Rounded.Check, null, tint = cs.primary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}
