package com.example.data.repository

import android.content.Context
import com.example.data.db.AuraDatabase
import com.example.data.model.Reflection
import com.example.data.model.Story
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AuraRepository(context: Context) {
    private val database = AuraDatabase.getDatabase(context)
    private val storyDao = database.storyDao()
    private val reflectionDao = database.reflectionDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            if (storyDao.getStoryCount() == 0) {
                seedInitialStories()
            }
        }
    }

    fun getStories(category: String, query: String): Flow<List<Story>> {
        val baseFlow = if (category == "ALL" || category.isBlank()) {
            storyDao.getAllStories()
        } else {
            storyDao.getStoriesByCategory(category)
        }

        return if (query.isBlank()) {
            baseFlow
        } else {
            baseFlow.map { list ->
                list.filter { story ->
                    story.title.contains(query, ignoreCase = true) ||
                            story.subtitle.contains(query, ignoreCase = true) ||
                            story.authorName.contains(query, ignoreCase = true) ||
                            story.category.contains(query, ignoreCase = true)
                }
            }
        }
    }

    fun getBookmarkedStories(): Flow<List<Story>> = storyDao.getBookmarkedStories()

    fun getMyStories(): Flow<List<Story>> = storyDao.getMyPublishedStories()

    fun observeStory(storyId: String): Flow<Story?> = storyDao.observeStoryById(storyId)

    fun getReflections(storyId: String): Flow<List<Reflection>> =
        reflectionDao.getReflectionsForStory(storyId)

    suspend fun toggleApplaud(storyId: String) {
        val story = storyDao.getStoryById(storyId) ?: return
        val newIsApplauded = !story.isApplauded
        val delta = if (newIsApplauded) 1 else -1
        storyDao.updateApplaud(storyId, delta, newIsApplauded)
    }

    suspend fun toggleBookmark(storyId: String) {
        val story = storyDao.getStoryById(storyId) ?: return
        storyDao.updateBookmark(storyId, !story.isBookmarked)
    }

    suspend fun addReflection(storyId: String, text: String, authorName: String = "You", authorHandle: String = "@you") {
        if (text.isBlank()) return
        val reflection = Reflection(
            id = UUID.randomUUID().toString(),
            storyId = storyId,
            authorName = authorName,
            authorHandle = authorHandle,
            content = text.trim(),
            timestamp = "Just now",
            createdAt = System.currentTimeMillis()
        )
        reflectionDao.insertReflection(reflection)
    }

    suspend fun publishStory(
        title: String,
        subtitle: String,
        content: String,
        category: String,
        coverDrawableName: String
    ): Story {
        val wordCount = content.split("\\s+".toRegex()).size
        val readTime = maxOf(1, wordCount / 180)
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        val story = Story(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            subtitle = subtitle.trim(),
            authorName = "You",
            authorHandle = "@you",
            authorBio = "Writer, visual thinker, and explorer of quiet spaces.",
            coverDrawableName = coverDrawableName.ifBlank { "img_editorial_architecture" },
            coverImageUrl = "",
            content = content.trim(),
            category = category.uppercase(Locale.getDefault()),
            readTimeMinutes = readTime,
            publishDate = dateFormat.format(Date()),
            applaudCount = 1,
            isApplauded = true,
            isBookmarked = false,
            createdAt = System.currentTimeMillis()
        )
        storyDao.insertStory(story)
        return story
    }

    private suspend fun seedInitialStories() {
        val stories = listOf(
            Story(
                id = "story-1",
                title = "The Geometry of Silence: Why Brutalism Calms the Overstimulated Mind",
                subtitle = "In an era of chromatic frenzy and endless notifications, raw monolithic concrete offers an unexpected sanctuary for contemplative thought.",
                authorName = "Maya Lindqvist",
                authorHandle = "@mayalind",
                authorBio = "Architectural essayist based in Stockholm. Examining tactile materials, natural shadows, and monastic spaces.",
                coverDrawableName = "img_editorial_architecture",
                content = """
                    We live in an age that fears stillness. Every digital surface screams for micro-attention with flashing counters, neon badges, and algorithmic dopamine hooks designed by behavioral engineers.

                    Yet when you step into a brutalist pavilion—such as Carlo Scarpa’s Brion Tomb or Tadao Ando’s Church of the Light—the world drops away into solemn, tactile peace.

                    Concrete is honest. It does not pretend to be something else. It wears the grain of its wooden shuttering, accepts the weathering of autumn rain, and frames sunlight with unapologetic permanence. Where mainstream social feeds give you fractured, five-second clips of performative luxury, monolithic architecture demands patience. You cannot swipe through a concrete courtyard; you can only inhabit it.

                    "Light only exists where there is shadow," Ando famously observed. "The presence of mass gives stillness its shape."

                    When we strip our surroundings of decorative clutter, our internal monologue finally quiets down. Silence is not empty; it is full of room to breathe.
                """.trimIndent(),
                category = "ARCHITECTURE",
                readTimeMinutes = 5,
                publishDate = "November 4, 2026",
                applaudCount = 124,
                isApplauded = false,
                isBookmarked = true,
                createdAt = System.currentTimeMillis() - 86400000L * 3
            ),
            Story(
                id = "story-2",
                title = "Morning In The Fog: Rediscovering Presence in Alpine Solitude",
                subtitle = "Leaving the perpetual stream behind to watch dawn break through the high pine tree canopies.",
                authorName = "Julian Chen",
                authorHandle = "@julianchen",
                authorBio = "Landscape photographer and essayist roaming the high passes of the Pacific Northwest.",
                coverDrawableName = "img_editorial_nature",
                content = """
                    At 5,800 feet above sea level, before the sun crests the ridge, the mountain mist moves like slow smoke through the old-growth firs. There is no cellular signal here. For the first two hours, your thumb involuntarily twitches for a pocket rectangle that isn't connected to anything.

                    And then, something extraordinary happens: your perception recalibrates.

                    The sound of dew dripping off needles onto damp moss becomes as captivating as an orchestral overture. You notice the gradient of cool lavender sky melting into pale apricot along the eastern horizon.

                    We were never meant to process three thousand strangers' opinions before breakfast. We were built to watch the sun rise, feel cold air fill our lungs, and let our thoughts mature like cedar trees—slowly, deeply, rooted in the earth.
                """.trimIndent(),
                category = "SLOW LIVING",
                readTimeMinutes = 4,
                publishDate = "November 6, 2026",
                applaudCount = 89,
                isApplauded = true,
                isBookmarked = false,
                createdAt = System.currentTimeMillis() - 86400000L * 2
            ),
            Story(
                id = "story-3",
                title = "The Art of Negative Space: How Less Visual Noise Unlocks Deep Thought",
                subtitle = "Why the most profound graphic design decisions are about what you choose to delete rather than what you add.",
                authorName = "Elena Rostova",
                authorHandle = "@erostova",
                authorBio = "Creative director and author of 'Typography as Architecture'.",
                coverDrawableName = "img_editorial_architecture",
                content = """
                    In Japanese aesthetics, the concept of *Ma* (間) refers to the empty space between things. It is not void or emptiness; it is pregnant with meaning. In music, it is the pause between notes that creates tension and resolution. In typography, it is the generous white margin that allows the ink to sing.

                    Modern social feeds are terrified of negative space. Every square millimeter must be filled with sponsored recommendations, recommended reels, floating reaction bubbles, and looping autoplay videos. The consequence is cognitive exhaustion.

                    When we design spaces—both physical and digital—with wide gutters, restrained palettes, and calm typography, we give dignity back to the reader. We treat human attention not as an extractive commodity to be harvested, but as a sacred vessel to be honored.
                """.trimIndent(),
                category = "DESIGN",
                readTimeMinutes = 6,
                publishDate = "November 7, 2026",
                applaudCount = 210,
                isApplauded = false,
                isBookmarked = false,
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            Story(
                id = "story-4",
                title = "Analog Reverie: Why Medium Format Film Still Moves the Soul",
                subtitle = "Restricting yourself to ten frames per roll changes not just how you take photographs, but how you perceive the fleeting world.",
                authorName = "Kaito Tanaka",
                authorHandle = "@kaitofilm",
                authorBio = "Tokyo-based documentarian shooting 6x7 medium format silver gelatin and chromes.",
                coverDrawableName = "img_editorial_nature",
                content = """
                    On a digital camera or smartphone, taking a photograph costs nothing. You hold the shutter down and take a burst of forty identical frames. But when every click of the shutter costs three dollars and requires winding a mechanical crank, your relationship with reality transforms.

                    You stop taking photos to prove you were somewhere. You start watching how sunlight glances off linen curtains. You wait ten minutes for a solitary pedestrian to walk into the frame with the exact right stride.

                    The limitation becomes the liberation. In a world of infinite, low-effort abundance, intentional constraints are the ultimate luxury.
                """.trimIndent(),
                category = "PHOTOGRAPHY",
                readTimeMinutes = 4,
                publishDate = "November 8, 2026",
                applaudCount = 95,
                isApplauded = false,
                isBookmarked = false,
                createdAt = System.currentTimeMillis()
            )
        )

        storyDao.insertStories(stories)

        // Seed initial thoughtful reflections
        val initialReflections = listOf(
            Reflection(
                id = "ref-1",
                storyId = "story-1",
                authorName = "Klara Lind",
                authorHandle = "@klaralind",
                content = "The distinction between void and sanctuary in Ando's work is spot on. Thank you for this oasis of calm writing.",
                timestamp = "2 days ago",
                createdAt = System.currentTimeMillis() - 86400000L * 2
            ),
            Reflection(
                id = "ref-2",
                storyId = "story-1",
                authorName = "Marcus Aurel",
                authorHandle = "@maurel",
                content = "Reading this on a distraction-free feed feels like stepping out of a noisy subway station into a quiet museum gallery.",
                timestamp = "Yesterday",
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            Reflection(
                id = "ref-3",
                storyId = "story-2",
                authorName = "Nora Solis",
                authorHandle = "@norasolis",
                content = "That involuntary thumb twitch is so real. Took me a full weekend in the woods to finally feel my breath deepen.",
                timestamp = "5 hours ago",
                createdAt = System.currentTimeMillis() - 18000000L
            )
        )
        reflectionDao.insertReflections(initialReflections)
    }
}
