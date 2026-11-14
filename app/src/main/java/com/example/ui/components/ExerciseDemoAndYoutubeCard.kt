package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.Exercise
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OledBackground
import com.example.ui.theme.OledCardBorder
import com.example.ui.theme.OledSurface
import com.example.ui.theme.OledSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Strictly implements Requirement 3:
 * 1) The exercise's exact Form Cues & Mistakes to Avoid clearly formatted.
 * 2) A "Search YouTube Tutorial" button that launches an Android Intent.ACTION_VIEW to
 *    https://www.youtube.com/results?search_query=<encoded_search_query>.
 * 3) An "Embed YouTube Video" input field and "Save Video" button: when pasting any YouTube URL
 *    (youtube.com/watch?v=ID, youtu.be/ID, or youtube.com/shorts/ID), extracts VIDEO_ID, saves it
 *    persistently for that exercise, and renders an embedded Android WebView player loading
 *    https://www.youtube-nocookie.com/embed/VIDEO_ID right inside the card, plus a "Swap / Remove Video" button.
 */
@Composable
fun ExerciseFormAndYoutubeSection(
    exercise: Exercise,
    onSaveYoutubeVideo: (String) -> Boolean,
    onRemoveYoutubeVideo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var urlInput by rememberSaveable(exercise.id) { mutableStateOf("") }
    var isSwappingVideo by rememberSaveable(exercise.id) { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Exact Form Cues & Mistakes to Avoid
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
            color = EmeraldContainer.copy(alpha = 0.55f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Anatomical Form Cues",
                        tint = EmeraldBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ANATOMICAL FORM CUES",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldBright,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = exercise.executionCueText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }

        exercise.mistakeToAvoidText?.let { mistakeText ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AmberAccent.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
                color = AmberContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Mistakes to Avoid",
                            tint = AmberBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MISTAKES TO AVOID",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmberBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = mistakeText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }

        // 2. "Search YouTube Tutorial" Button (Intent.ACTION_VIEW)
        OutlinedButton(
            onClick = {
                val encodedQuery = Uri.encode(exercise.searchQuery)
                val url = "https://www.youtube.com/results?search_query=$encodedQuery"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("search_youtube_button_${exercise.id}"),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanBright.copy(alpha = 0.6f))
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search YouTube Tutorial",
                tint = CyanBright,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search YouTube Tutorial",
                color = CyanBright,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Opens YouTube search",
                tint = CyanBright,
                modifier = Modifier.size(15.dp)
            )
        }

        Text(
            text = "Search query: \"${exercise.searchQuery}\"",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // 3. Embedded YouTube Video Player OR Embed Input Field
        val savedVideoId = exercise.savedYoutubeId
        if (!savedVideoId.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OledCardBorder, RoundedCornerShape(16.dp)),
                color = OledSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleOutline,
                                contentDescription = "Embedded YouTube Demo",
                                tint = EmeraldBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Saved Demo (ID: $savedVideoId)",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldBright
                            )
                        }

                        TextButton(
                            onClick = {
                                val watchUrl = "https://www.youtube.com/watch?v=$savedVideoId"
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(watchUrl)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    )
                                } catch (_: Exception) {
                                }
                            }
                        ) {
                            Text(
                                text = "Open App",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanBright
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    key(savedVideoId) {
                        YoutubeNocookieWebView(
                            videoId = savedVideoId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("youtube_webview_${exercise.id}")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isSwappingVideo = !isSwappingVideo },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("swap_remove_video_button_${exercise.id}"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OledCardBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Swap Video",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Swap / Remove Video",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                isSwappingVideo = false
                                urlInput = ""
                                onRemoveYoutubeVideo()
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("remove_video_confirm_button_${exercise.id}"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CoralWarning.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remove Video",
                                tint = CoralWarning
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = savedVideoId.isNullOrBlank() || isSwappingVideo) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OledCardBorder, RoundedCornerShape(16.dp)),
                color = OledBackground.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Embed YouTube Video",
                            tint = EmeraldBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Embed YouTube Video",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste any YouTube URL (youtube.com/watch?v=ID, youtu.be/ID, or youtube.com/shorts/ID) to embed the player right inside this exercise card.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = {
                            Text(
                                text = "https://youtu.be/... or youtube.com/shorts/...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("embed_youtube_input_${exercise.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldBright,
                            unfocusedBorderColor = OledCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = OledSurface,
                            unfocusedContainerColor = OledSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (onSaveYoutubeVideo(urlInput)) {
                                urlInput = ""
                                isSwappingVideo = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_video_button_${exercise.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color(0xFF042219)
                        )
                    ) {
                        Text(
                            text = "Save Video",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YoutubeNocookieWebView(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val embedUrl = remember(videoId) {
        "https://www.youtube-nocookie.com/embed/$videoId?playsinline=1&rel=0&modestbranding=1"
    }

    val htmlContent = remember(embedUrl) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                html, body { margin: 0; padding: 0; width: 100%; height: 100%; background-color: #06090F; overflow: hidden; }
                iframe { width: 100%; height: 100%; border: 0; }
            </style>
        </head>
        <body>
            <iframe
                src="$embedUrl"
                frameborder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                allowfullscreen>
            </iframe>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = modifier
            .background(OledBackground)
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.parseColor("#06090F"))
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }
                    webChromeClient = WebChromeClient()
                    webViewClient = WebViewClient()
                    loadDataWithBaseURL(
                        "https://www.youtube-nocookie.com",
                        htmlContent,
                        "text/html",
                        "utf-8",
                        null
                    )
                }
            },
            update = { webView ->
                if (webView.tag != videoId) {
                    webView.tag = videoId
                    webView.loadDataWithBaseURL(
                        "https://www.youtube-nocookie.com",
                        htmlContent,
                        "text/html",
                        "utf-8",
                        null
                    )
                }
            },
            modifier = Modifier.matchParentSize()
        )
    }
}
