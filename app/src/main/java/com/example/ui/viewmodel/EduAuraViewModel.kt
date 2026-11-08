package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.EducationalResource
import com.example.data.model.UserProfile
import com.example.data.repository.EducationalRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class EduTab {
    CATALOG,
    UPLOAD,
    PROFILE
}

class EduAuraViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EducationalRepository(application)

    private val _currentTab = MutableStateFlow(EduTab.CATALOG)
    val currentTab: StateFlow<EduTab> = _currentTab.asStateFlow()

    private val _formatFilter = MutableStateFlow("ALL")
    val formatFilter: StateFlow<String> = _formatFilter.asStateFlow()

    private val _subjectFilter = MutableStateFlow("ALL")
    val subjectFilter: StateFlow<String> = _subjectFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _activeResource = MutableStateFlow<EducationalResource?>(null)
    val activeResource: StateFlow<EducationalResource?> = _activeResource.asStateFlow()

    private val _isVideoPlaying = MutableStateFlow(false)
    val isVideoPlaying: StateFlow<Boolean> = _isVideoPlaying.asStateFlow()

    private val _downloadProgressMap = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgressMap: StateFlow<Map<String, Float>> = _downloadProgressMap.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val resources: StateFlow<List<EducationalResource>> = combine(
        _formatFilter,
        _subjectFilter,
        _searchQuery
    ) { format, subject, query ->
        Triple(format, subject, query)
    }.flatMapLatest { (format, subject, query) ->
        repository.getResources(format, subject, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedResources: StateFlow<List<EducationalResource>> = repository.getBookmarkedResources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myUploadedResources: StateFlow<List<EducationalResource>> = repository.getMyUploadedResources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: EduTab) {
        _currentTab.value = tab
        if (_activeResource.value != null && tab != EduTab.CATALOG) {
            _activeResource.value = null
        }
    }

    fun openResource(resource: EducationalResource) {
        _activeResource.value = resource
        _isVideoPlaying.value = false
    }

    fun closeResource() {
        _activeResource.value = null
        _isVideoPlaying.value = false
    }

    fun setFormatFilter(format: String) {
        _formatFilter.value = format
    }

    fun setSubjectFilter(subject: String) {
        _subjectFilter.value = subject
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearchActive() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    fun toggleBookmark(resourceId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(resourceId)
            val current = _activeResource.value
            if (current != null && current.id == resourceId) {
                _activeResource.value = current.copy(isBookmarked = !current.isBookmarked)
            }
        }
    }

    fun toggleLike(resourceId: String) {
        viewModelScope.launch {
            repository.toggleLike(resourceId)
            val current = _activeResource.value
            if (current != null && current.id == resourceId) {
                val newLiked = !current.isLiked
                val delta = if (newLiked) 1 else -1
                _activeResource.value = current.copy(
                    isLiked = newLiked,
                    likesCount = current.likesCount + delta
                )
            }
        }
    }

    fun toggleVideoPlayPause() {
        _isVideoPlaying.value = !_isVideoPlaying.value
    }

    fun downloadResource(resourceId: String) {
        viewModelScope.launch {
            repository.recordDownload(resourceId)
            val current = _activeResource.value
            if (current != null && current.id == resourceId) {
                _activeResource.value = current.copy(downloadCount = current.downloadCount + 1)
            }
            // Animate realistic download progress
            for (p in 1..10) {
                val progress = p / 10f
                _downloadProgressMap.value = _downloadProgressMap.value + (resourceId to progress)
                delay(120)
            }
        }
    }

    fun uploadResource(
        title: String,
        description: String,
        resourceType: String,
        subject: String,
        fileFormat: String,
        fileSizeString: String,
        durationMinutes: Int,
        content: String,
        difficulty: String,
        coverDrawable: String
    ) {
        viewModelScope.launch {
            val newResource = repository.uploadResource(
                title = title,
                description = description,
                resourceType = resourceType,
                subject = subject,
                fileFormat = fileFormat,
                fileSizeString = fileSizeString,
                durationMinutes = durationMinutes,
                content = content,
                difficulty = difficulty,
                coverDrawable = coverDrawable
            )
            _formatFilter.value = "ALL"
            _subjectFilter.value = "ALL"
            _currentTab.value = EduTab.CATALOG
            _activeResource.value = newResource
        }
    }

    fun updateProfile(
        name: String,
        handle: String,
        institution: String,
        bio: String,
        role: String
    ) {
        viewModelScope.launch {
            repository.updateUserProfile(name, handle, institution, bio, role)
        }
    }
}
