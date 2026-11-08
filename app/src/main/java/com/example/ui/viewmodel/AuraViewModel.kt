package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Reflection
import com.example.data.model.Story
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationTab {
    FEED,
    EXPLORE,
    CREATE,
    LIBRARY
}

class AuraViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuraRepository(application)

    private val _currentTab = MutableStateFlow(NavigationTab.FEED)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _activeStory = MutableStateFlow<Story?>(null)
    val activeStory: StateFlow<Story?> = _activeStory.asStateFlow()

    private val _fontSizeMultiplier = MutableStateFlow(1.0f)
    val fontSizeMultiplier: StateFlow<Float> = _fontSizeMultiplier.asStateFlow()

    private val _isFocusMode = MutableStateFlow(false)
    val isFocusMode: StateFlow<Boolean> = _isFocusMode.asStateFlow()

    private val _readingMinutes = MutableStateFlow(28)
    val readingMinutes: StateFlow<Int> = _readingMinutes.asStateFlow()

    // Stories feed reactive flow
    val stories: StateFlow<List<Story>> = combine(
        _selectedCategory,
        _searchQuery
    ) { category, query ->
        category to query
    }.flatMapLatest { (category, query) ->
        repository.getStories(category, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bookmarked stories
    val bookmarkedStories: StateFlow<List<Story>> = repository.getBookmarkedStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // My stories
    val myStories: StateFlow<List<Story>> = repository.getMyStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active story reflections
    val activeReflections: StateFlow<List<Reflection>> = _activeStory
        .flatMapLatest { story ->
            if (story != null) repository.getReflections(story.id)
            else MutableStateFlow(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
        if (_activeStory.value != null && tab != NavigationTab.FEED) {
            _activeStory.value = null
        }
    }

    fun openStory(story: Story) {
        _activeStory.value = story
        _readingMinutes.value += 1
    }

    fun closeStory() {
        _activeStory.value = null
        _isFocusMode.value = false
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
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

    fun toggleApplaud(storyId: String) {
        viewModelScope.launch {
            repository.toggleApplaud(storyId)
            val current = _activeStory.value
            if (current != null && current.id == storyId) {
                val newIsApplauded = !current.isApplauded
                val delta = if (newIsApplauded) 1 else -1
                _activeStory.value = current.copy(
                    isApplauded = newIsApplauded,
                    applaudCount = current.applaudCount + delta
                )
            }
        }
    }

    fun toggleBookmark(storyId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(storyId)
            val current = _activeStory.value
            if (current != null && current.id == storyId) {
                _activeStory.value = current.copy(isBookmarked = !current.isBookmarked)
            }
        }
    }

    fun addReflection(storyId: String, text: String) {
        viewModelScope.launch {
            repository.addReflection(storyId, text)
        }
    }

    fun publishStory(
        title: String,
        subtitle: String,
        content: String,
        category: String,
        coverDrawable: String
    ) {
        viewModelScope.launch {
            val newStory = repository.publishStory(
                title = title,
                subtitle = subtitle,
                content = content,
                category = category,
                coverDrawableName = coverDrawable
            )
            _selectedCategory.value = "ALL"
            _currentTab.value = NavigationTab.FEED
            _activeStory.value = newStory
        }
    }

    fun setFontSizeMultiplier(multiplier: Float) {
        _fontSizeMultiplier.value = multiplier
    }

    fun toggleFocusMode() {
        _isFocusMode.value = !_isFocusMode.value
    }
}
