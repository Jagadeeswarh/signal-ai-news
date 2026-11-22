package com.vantage.app

import android.os.Bundle
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect

private val Ink = Color(0xFF111014)
private val Surface = Color(0xFF1B161B)
private val Raised = Color(0xFF241A20)
private val Burgundy = Color(0xFF87364F)
private val Rose = Color(0xFFD28BA0)
private val Paper = Color(0xFFF4EDF0)
private val Muted = Color(0xFFB9AAB0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = EditionRepository(this)
        val readerPreferences = getSharedPreferences("vantage_reader_preferences", MODE_PRIVATE)
        VantageSyncScheduler.schedule(this)
        setContent {
            VantageTheme {
                VantageApp(repository, readerPreferences)
            }
        }
    }
}

@Composable
private fun VantageTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Rose, secondary = Burgundy, background = Ink,
            surface = Surface, onPrimary = Ink, onBackground = Paper,
            onSurface = Paper, onSurfaceVariant = Muted
        ),
        content = content
    )
}

private enum class Destination(val label: String) {
    EDITION("Edition"), SECTIONS("Sections"), SAVED("Saved")
}

@Composable
private fun VantageApp(repository: EditionRepository, readerPreferences: SharedPreferences) {
    var destination by remember { mutableStateOf(Destination.EDITION) }
    var edition by remember { mutableStateOf(repository.loadCached()) }
    var selected by remember { mutableStateOf<Story?>(null) }
    var savedIds by remember { mutableStateOf(repository.savedIds()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var readerFontScale by remember { mutableFloatStateOf(readerPreferences.getFloat("font_scale", 1f)) }
    var highContrast by remember { mutableStateOf(readerPreferences.getBoolean("high_contrast", false)) }
    var paragraphSpacing by remember { mutableFloatStateOf(readerPreferences.getFloat("paragraph_spacing", 1f)) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.refresh()?.let { edition = it }
    }

    fun refresh() {
        scope.launch {
            isRefreshing = true
            repository.refresh()?.let { edition = it }
            isRefreshing = false
        }
    }

    Scaffold(
        containerColor = Ink,
        topBar = {
            Column(Modifier.fillMaxWidth().background(Ink).windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 22.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("VANTAGE", color = Paper, fontSize = 23.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                        Text("UNDERSTAND THE WORLD YOU'RE BUILDING IN.", color = Muted, fontSize = 9.sp, letterSpacing = 1.2.sp)
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Outlined.Tune, contentDescription = "Reader settings", tint = Rose)
                    }
                    IconButton(onClick = { refresh() }, enabled = !isRefreshing) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh edition", tint = Rose)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).background(Rose, RoundedCornerShape(50)))
                    Spacer(Modifier.width(8.dp))
                    Text("THE DAILY BRIEFING", color = Rose, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
                    Spacer(Modifier.weight(1f))
                    Text(edition.dateLabel, color = Muted, fontSize = 11.sp)
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Surface, contentColor = Muted) {
                Destination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item; selected = null },
                        icon = {
                            Icon(
                                when (item) {
                                    Destination.EDITION -> Icons.Outlined.MenuBook
                                    Destination.SECTIONS -> Icons.Outlined.Explore
                                    Destination.SAVED -> Icons.Outlined.Bookmark
                                }, contentDescription = item.label
                            )
                        },
                        label = { Text(item.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Rose, selectedTextColor = Rose,
                            indicatorColor = Burgundy, unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        )
                    )
                }
            }
        }
    ) { padding ->
        if (selected != null) {
            StoryDetail(
                story = selected!!,
                isSaved = savedIds.contains(selected!!.id),
                fontScale = readerFontScale,
                highContrast = highContrast,
                paragraphSpacing = paragraphSpacing,
                onBack = { selected = null },
                onSave = {
                    repository.toggleSaved(selected!!.id)
                    savedIds = repository.savedIds()
                },
                modifier = Modifier.padding(padding)
            )
        } else {
            when (destination) {
                Destination.EDITION -> EditionScreen(edition, savedIds, ::refresh, { selected = it }, {
                    repository.toggleSaved(it); savedIds = repository.savedIds()
                }, isRefreshing, Modifier.padding(padding))
                Destination.SECTIONS -> SectionsScreen(edition, savedIds, { selected = it }, {
                    repository.toggleSaved(it); savedIds = repository.savedIds()
                }, Modifier.padding(padding))
                Destination.SAVED -> SavedScreen(edition, savedIds, { selected = it }, {
                    repository.toggleSaved(it); savedIds = repository.savedIds()
                }, Modifier.padding(padding))
            }
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            containerColor = Surface,
            title = { Text("Reader preferences", color = Paper, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TEXT SIZE", color = Rose, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                    Slider(value = readerFontScale, onValueChange = { readerFontScale = it; readerPreferences.edit().putFloat("font_scale", it).apply() }, valueRange = 0.9f..1.3f)
                    HorizontalDivider(color = Burgundy)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Higher contrast", color = Paper)
                            Text("Brighter article text", color = Muted, fontSize = 11.sp)
                        }
                        Switch(checked = highContrast, onCheckedChange = { highContrast = it; readerPreferences.edit().putBoolean("high_contrast", it).apply() })
                    }
                    HorizontalDivider(color = Burgundy)
                    Text("PARAGRAPH SPACING", color = Rose, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                    Slider(value = paragraphSpacing, onValueChange = { paragraphSpacing = it; readerPreferences.edit().putFloat("paragraph_spacing", it).apply() }, valueRange = 0.8f..1.5f)
                }
            },
            confirmButton = { TextButton(onClick = { showSettings = false }) { Text("Done", color = Rose) } }
        )
    }
}

@Composable
private fun EditionScreen(
    edition: Edition, saved: Set<String>, refresh: () -> Unit,
    open: (Story) -> Unit, toggle: (String) -> Unit, refreshing: Boolean, modifier: Modifier
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column(Modifier.fillMaxWidth().background(Raised, RoundedCornerShape(18.dp)).padding(20.dp)) {
                Text(edition.kicker.uppercase(), color = Rose, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp)
                Spacer(Modifier.height(10.dp))
                Text(edition.title, color = Paper, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(edition.description, color = Muted, fontSize = 13.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${edition.stories.size} STORIES", color = Rose, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.weight(1f))
                    Text(if (refreshing) "SYNCING…" else "GITHUB EDITION", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
                }
            }
        }
        item { SectionHeading("THE BIG PICTURE", "Context beyond the headline") }
        item {
            Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(14.dp)).padding(16.dp)) {
                Text(edition.bigPicture, color = Paper, fontSize = 13.sp, lineHeight = 21.sp)
            }
        }
        item { SectionHeading("TODAY'S SIGNAL", "Stories worth your attention") }
        items(edition.stories, key = { it.id }) { story ->
            StoryCard(story, saved.contains(story.id), { open(story) }, { toggle(story.id) })
        }
        if (edition.stories.isEmpty()) item { EmptyState("No stories in this edition yet", "Pull to refresh when the next VANTAGE edition is published.") }
        item {
            TextButton(onClick = refresh, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Refresh, null, tint = Rose)
                Spacer(Modifier.width(8.dp))
                Text("Refresh edition", color = Rose)
            }
        }
    }
}

@Composable
private fun SectionsScreen(edition: Edition, saved: Set<String>, open: (Story) -> Unit, toggle: (String) -> Unit, modifier: Modifier) {
    val sections = edition.stories.groupBy { it.section }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Explore the landscape.", color = Paper, fontSize = 25.sp, fontWeight = FontWeight.Bold) }
        item { Text("Follow the ideas and industries shaping what comes next.", color = Muted, fontSize = 13.sp) }
        sections.forEach { (section, stories) ->
            item { SectionHeading(section.uppercase(), "${stories.size} stories") }
            items(stories, key = { it.id }) { story -> StoryCard(story, saved.contains(story.id), { open(story) }, { toggle(story.id) }) }
        }
        if (sections.isEmpty()) item { EmptyState("Sections are on their way", "New stories will appear here as editions are published.") }
    }
}

@Composable
private fun SavedScreen(edition: Edition, saved: Set<String>, open: (Story) -> Unit, toggle: (String) -> Unit, modifier: Modifier) {
    val stories = edition.stories.filter { saved.contains(it.id) }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Your reading list.", color = Paper, fontSize = 25.sp, fontWeight = FontWeight.Bold) }
        item { Text("Saved stories stay on this device.", color = Muted, fontSize = 13.sp) }
        items(stories, key = { it.id }) { story -> StoryCard(story, true, { open(story) }, { toggle(story.id) }) }
        if (stories.isEmpty()) item { EmptyState("Nothing saved yet", "Tap the bookmark on any story to keep it here for later.") }
    }
}

@Composable
private fun StoryCard(story: Story, isSaved: Boolean, open: () -> Unit, toggle: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(16.dp)).clickable(onClick = open).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(story.section.uppercase(), color = Rose, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Spacer(Modifier.weight(1f))
            Text(story.readTime, color = Muted, fontSize = 10.sp)
            IconButton(onClick = toggle, modifier = Modifier.size(32.dp)) {
                Icon(if (isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = "Save story", tint = if (isSaved) Rose else Muted, modifier = Modifier.size(19.dp))
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(story.title, color = Paper, fontSize = 18.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(7.dp))
        Text(story.summary, color = Muted, fontSize = 12.sp, lineHeight = 18.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(12.dp))
        Text("READ THE STORY  ↗", color = Rose, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun StoryDetail(story: Story, isSaved: Boolean, fontScale: Float, highContrast: Boolean, paragraphSpacing: Float, onBack: () -> Unit, onSave: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        TextButton(onClick = onBack) { Text("←  BACK TO VANTAGE", color = Rose, fontSize = 10.sp, letterSpacing = 1.sp) }
        Spacer(Modifier.height(22.dp))
        Text(story.section.uppercase(), color = Rose, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(12.dp))
        Text(story.title, color = Paper, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("${story.readTime}  ·  ${story.source}", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(24.dp))
        Text(story.summary, color = Paper, fontSize = (16f * fontScale).sp, lineHeight = (26f * paragraphSpacing).sp)
        Spacer(Modifier.height((24f * paragraphSpacing).dp))
        Text("THE BIGGER PICTURE", color = Rose, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(8.dp))
        Text(story.context, color = if (highContrast) Paper else Muted, fontSize = (14f * fontScale).sp, lineHeight = (23f * paragraphSpacing).sp)
        Spacer(Modifier.height((26f * paragraphSpacing).dp))
        OutlinedButton(onClick = onSave, colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose)) {
            Icon(if (isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, null)
            Spacer(Modifier.width(8.dp))
            Text(if (isSaved) "Saved to your list" else "Save for later")
        }
        Spacer(Modifier.height(16.dp))
        Text("Source link: ${story.url.ifBlank { "Not available in this preview edition." }}", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column {
        Text(title, color = Paper, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.MenuBook, null, tint = Burgundy, modifier = Modifier.size(34.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = Paper, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(5.dp))
        Text(body, color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
    }
}
