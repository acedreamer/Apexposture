package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.CompletedWorkoutLog
import com.example.ui.components.formatMinutesSeconds
import com.example.ui.theme.AmberBright
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OledBackground
import com.example.ui.theme.OledCardBorder
import com.example.ui.theme.OledSurface
import com.example.ui.theme.OledSurfaceElevated
import com.example.ui.theme.OledSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun HistoryAndBackupScreen(
    logs: List<CompletedWorkoutLog>,
    currentStreak: Int,
    bestStreak: Int,
    onCreateBackupJson: () -> String,
    onRestoreBackupJson: (String) -> Boolean,
    onResetDefaultRoutine: () -> Unit,
    onPostStatus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGeneratingApkLink by remember { mutableStateOf(false) }
    var generatedApkDownloadUrl by remember { mutableStateOf<String?>(null) }

    // ActivityResultContracts.CreateDocument for Export JSON Backup
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val jsonPayload = onCreateBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonPayload.toByteArray(Charsets.UTF_8))
                }
                onPostStatus("Exported JSON backup successfully!")
            } catch (e: Exception) {
                onPostStatus("Failed to export JSON backup: ${e.localizedMessage}")
            }
        }
    }

    // ActivityResultContracts.OpenDocument for Import JSON Backup
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader(Charsets.UTF_8).readText()
                }
                if (!jsonString.isNullOrBlank()) {
                    onRestoreBackupJson(jsonString)
                } else {
                    onPostStatus("Selected JSON file was empty.")
                }
            } catch (e: Exception) {
                onPostStatus("Failed to read JSON backup: ${e.localizedMessage}")
            }
        }
    }

    // ActivityResultContracts.CreateDocument to Export Installed Base APK file
    val exportApkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { uri ->
        if (uri != null) {
            try {
                val apkFile = File(context.applicationInfo.sourceDir)
                FileInputStream(apkFile).use { input ->
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        input.copyTo(output)
                    }
                }
                onPostStatus("Saved ApexPosture.apk to device storage!")
            } catch (e: Exception) {
                onPostStatus("Could not export APK: ${e.localizedMessage}")
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_backup_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Download / Export Android APK Card (No Google Play Console Required!)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyanBright.copy(alpha = 0.45f), RoundedCornerShape(24.dp)),
                color = OledSurface,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = "Android APK Download",
                            tint = EmeraldBright,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FREE APK DOWNLOAD (NO $25 PLAY CONSOLE NEEDED)",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldBright,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Get ApexPosture.apk on Your Android Phone",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tap 'Generate Direct Phone Download Link' below to upload this live compiled APK and get a short URL you can open directly in your phone's Chrome browser—or push to GitHub to run the pre-configured GitHub Actions APK builder for free.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (!isGeneratingApkLink) {
                                isGeneratingApkLink = true
                                scope.launch {
                                    val result = uploadCompiledApkForDirectDownload(
                                        File(context.applicationInfo.sourceDir)
                                    )
                                    isGeneratingApkLink = false
                                    result.fold(
                                        onSuccess = { url ->
                                            generatedApkDownloadUrl = url
                                            onPostStatus("Direct APK download link ready!")
                                        },
                                        onFailure = { err ->
                                            onPostStatus("Link upload failed: ${err.localizedMessage}")
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_apk_link_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color(0xFF042219)
                        )
                    ) {
                        if (isGeneratingApkLink) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF042219),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Uploading APK & Creating Link...",
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Generate Direct APK Link",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate Direct Phone Download Link (.APK)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    generatedApkDownloadUrl?.let { downloadUrl ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, EmeraldBright, RoundedCornerShape(14.dp)),
                            color = OledBackground,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "OPEN THIS URL IN CHROME ON YOUR PHONE:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldBright,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = downloadUrl,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AmberBright,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("ApexPosture APK URL", downloadUrl))
                                        onPostStatus("Copied APK URL: $downloadUrl")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldBright.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Link",
                                        tint = EmeraldBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Direct APK Link", color = EmeraldBright, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            exportApkLauncher.launch("ApexPosture_v1.0.apk")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_apk_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanBright.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save APK to Device Storage",
                            tint = CyanBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save APK to Local Storage (ApexPosture.apk)",
                            color = CyanBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // JSON Backup & Restore Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.45f), RoundedCornerShape(24.dp)),
                color = OledSurfaceVariant,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "LOCAL PERSISTENCE & JSON BACKUP",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldBright,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Export & Import Workout Data",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All custom routines, exercise edits, saved YouTube Video IDs, workout logs, and streak counters are saved locally. Export a portable JSON backup file or restore from an existing backup anytime.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val dateStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                exportLauncher.launch("apex_posture_backup_$dateStamp.json")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("export_json_backup_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color(0xFF042219)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Export JSON Backup",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON Backup", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("import_json_backup_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanBright.copy(alpha = 0.65f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Import JSON Backup",
                                tint = CyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON Backup", color = CyanBright, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onResetDefaultRoutine,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_default_routine_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OledCardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Reset Plan to Defaults",
                            tint = AmberBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset 4-Day Routine to Pre-Loaded Defaults",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Completed Workout History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Completed Workout Logs",
                        tint = EmeraldBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COMPLETED WORKOUT LOGS (${logs.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldBright,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Streak: $currentStreak • Best: $bestStreak",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberBright
                )
            }
        }

        if (logs.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OledCardBorder, RoundedCornerShape(20.dp)),
                    color = OledSurface,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Workout Sessions Logged Yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start any Day 1–4 routine in the Live Tracker and tap 'Complete & Log' to build your posture & upper chest recomposition streak.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                val formattedDate = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
                    .format(Date(log.completedAtMillis))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OledCardBorder, RoundedCornerShape(18.dp))
                        .testTag("workout_log_card_${log.id}"),
                    color = OledSurface,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(OledSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "D${log.dayNumber}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = EmeraldBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = log.dayTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${log.completedSets}/${log.totalSets} sets",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldBright,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatMinutesSeconds(log.durationSeconds),
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun uploadCompiledApkForDirectDownload(apkFile: File): Result<String> =
    withContext(Dispatchers.IO) {
        // Attempt 1: Litterbox temporary 24h file host (returns clean direct https://litter.catbox.moe/xxxxxx.apk link)
        try {
            val boundary = "----ApexPostureBoundary${UUID.randomUUID()}"
            val url = URL("https://litterbox.catbox.moe/resources/internals/api.php")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                doInput = true
                doOutput = true
                useCaches = false
                connectTimeout = 20000
                readTimeout = 60000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                setRequestProperty("User-Agent", "ApexPostureAndroid/1.0")
            }

            DataOutputStream(conn.outputStream).use { out ->
                fun writeFormField(name: String, value: String) {
                    out.writeBytes("--$boundary\r\n")
                    out.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
                    out.writeBytes("$value\r\n")
                }

                writeFormField("reqtype", "fileupload")
                writeFormField("time", "24h")

                out.writeBytes("--$boundary\r\n")
                out.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"ApexPosture.apk\"\r\n")
                out.writeBytes("Content-Type: application/vnd.android.package-archive\r\n\r\n")

                FileInputStream(apkFile).use { fileIn ->
                    val buffer = ByteArray(16 * 1024)
                    var read: Int
                    while (fileIn.read(buffer).also { read = it } != -1) {
                        out.write(buffer, 0, read)
                    }
                }
                out.writeBytes("\r\n--$boundary--\r\n")
                out.flush()
            }

            if (conn.responseCode in 200..299) {
                val response = conn.inputStream.bufferedReader().readText().trim()
                if (response.startsWith("http")) {
                    return@withContext Result.success(response)
                }
            }
        } catch (_: Exception) {
        }

        // Attempt 2: 0x0.st fallback
        return@withContext try {
            val boundary = "----ApexPostureFallback${UUID.randomUUID()}"
            val url = URL("https://0x0.st")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                doInput = true
                doOutput = true
                useCaches = false
                connectTimeout = 20000
                readTimeout = 60000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                setRequestProperty("User-Agent", "curl/8.0.0")
            }

            DataOutputStream(conn.outputStream).use { out ->
                out.writeBytes("--$boundary\r\n")
                out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"ApexPosture.apk\"\r\n")
                out.writeBytes("Content-Type: application/vnd.android.package-archive\r\n\r\n")
                FileInputStream(apkFile).use { fileIn ->
                    val buffer = ByteArray(16 * 1024)
                    var read: Int
                    while (fileIn.read(buffer).also { read = it } != -1) {
                        out.write(buffer, 0, read)
                    }
                }
                out.writeBytes("\r\n--$boundary--\r\n")
                out.flush()
            }

            val response = conn.inputStream.bufferedReader().readText().trim()
            if (response.startsWith("http")) {
                Result.success(response)
            } else {
                Result.failure(IllegalStateException("Unexpected response from transfer host"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

