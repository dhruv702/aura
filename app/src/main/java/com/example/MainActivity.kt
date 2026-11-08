package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.EduBottomBar
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResourceDetailScreen
import com.example.ui.screens.UploadResourceScreen
import com.example.ui.theme.AuraTheme
import com.example.ui.viewmodel.EduAuraViewModel
import com.example.ui.viewmodel.EduTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuraTheme {
                EduAuraApp()
            }
        }
    }
}

@Composable
fun EduAuraApp(viewModel: EduAuraViewModel = viewModel()) {
    val activeResource by viewModel.activeResource.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val resources by viewModel.resources.collectAsStateWithLifecycle()
    val bookmarkedResources by viewModel.bookmarkedResources.collectAsStateWithLifecycle()
    val myUploadedResources by viewModel.myUploadedResources.collectAsStateWithLifecycle()
    val formatFilter by viewModel.formatFilter.collectAsStateWithLifecycle()
    val subjectFilter by viewModel.subjectFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isVideoPlaying by viewModel.isVideoPlaying.collectAsStateWithLifecycle()
    val downloadProgressMap by viewModel.downloadProgressMap.collectAsStateWithLifecycle()

    BackHandler(enabled = activeResource != null || currentTab != EduTab.CATALOG) {
        if (activeResource != null) {
            viewModel.closeResource()
        } else {
            viewModel.selectTab(EduTab.CATALOG)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (activeResource == null) {
                EduBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = Pair(activeResource != null, currentTab),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { targetState ->
                val hasActive = targetState.first
                val tab = targetState.second

                if (hasActive && activeResource != null) {
                    val res = activeResource!!
                    ResourceDetailScreen(
                        resource = res,
                        isVideoPlaying = isVideoPlaying,
                        downloadProgress = downloadProgressMap[res.id],
                        onBack = { viewModel.closeResource() },
                        onToggleBookmark = { viewModel.toggleBookmark(it) },
                        onToggleLike = { viewModel.toggleLike(it) },
                        onTogglePlayVideo = { viewModel.toggleVideoPlayPause() },
                        onDownload = { viewModel.downloadResource(it) }
                    )
                } else {
                    when (tab) {
                        EduTab.CATALOG -> {
                            CatalogScreen(
                                resources = resources,
                                formatFilter = formatFilter,
                                subjectFilter = subjectFilter,
                                searchQuery = searchQuery,
                                isSearchActive = isSearchActive,
                                userProfile = userProfile,
                                onFormatFilterChange = { viewModel.setFormatFilter(it) },
                                onSubjectFilterChange = { viewModel.setSubjectFilter(it) },
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onToggleSearch = { viewModel.toggleSearchActive() },
                                onResourceClick = { viewModel.openResource(it) },
                                onBookmark = { viewModel.toggleBookmark(it) },
                                onLike = { viewModel.toggleLike(it) },
                                onProfileClick = { viewModel.selectTab(EduTab.PROFILE) }
                            )
                        }

                        EduTab.UPLOAD -> {
                            UploadResourceScreen(
                                onUpload = { title, desc, type, subject, format, size, duration, content, diff, cover ->
                                    viewModel.uploadResource(
                                        title = title,
                                        description = desc,
                                        resourceType = type,
                                        subject = subject,
                                        fileFormat = format,
                                        fileSizeString = size,
                                        durationMinutes = duration,
                                        content = content,
                                        difficulty = diff,
                                        coverDrawable = cover
                                    )
                                }
                            )
                        }

                        EduTab.PROFILE -> {
                            ProfileScreen(
                                userProfile = userProfile,
                                bookmarkedResources = bookmarkedResources,
                                myUploadedResources = myUploadedResources,
                                onResourceClick = { viewModel.openResource(it) },
                                onBookmark = { viewModel.toggleBookmark(it) },
                                onLike = { viewModel.toggleLike(it) },
                                onUpdateProfile = { name, handle, inst, bio, role ->
                                    viewModel.updateProfile(name, handle, inst, bio, role)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
