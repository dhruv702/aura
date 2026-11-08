package com.example.data.repository

import android.content.Context
import com.example.data.db.EduDatabase
import com.example.data.model.EducationalResource
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class EducationalRepository(context: Context) {
    private val database = EduDatabase.getDatabase(context)
    private val resourceDao = database.resourceDao()
    private val userProfileDao = database.userProfileDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            if (resourceDao.getResourceCount() == 0) {
                seedInitialData()
            }
            if (userProfileDao.getProfile() == null) {
                userProfileDao.insertOrUpdateProfile(
                    UserProfile(
                        id = "current_user",
                        name = "Alex Vance",
                        handle = "@you",
                        institution = "Open Science & Tech Educator",
                        bio = "Educator and lifelong learner. Believer in open-access knowledge for every human on earth.",
                        role = "Educator",
                        avatarLetter = "A",
                        totalUploadsCount = 1,
                        totalDownloadsHelped = 340
                    )
                )
            }
        }
    }

    fun getResources(formatFilter: String, subjectFilter: String, searchQuery: String): Flow<List<EducationalResource>> {
        val baseFlow = resourceDao.getAllResources()

        return baseFlow.map { list ->
            list.filter { resource ->
                val matchesFormat = when (formatFilter) {
                    "ALL" -> true
                    "VIDEO" -> resource.resourceType == "VIDEO"
                    "FILE_DOCUMENT" -> resource.resourceType == "FILE_DOCUMENT"
                    "WEB_ARTICLE" -> resource.resourceType == "WEB_ARTICLE"
                    else -> true
                }

                val matchesSubject = if (subjectFilter == "ALL" || subjectFilter.isBlank()) {
                    true
                } else {
                    resource.subject.equals(subjectFilter, ignoreCase = true)
                }

                val matchesQuery = if (searchQuery.isBlank()) {
                    true
                } else {
                    resource.title.contains(searchQuery, ignoreCase = true) ||
                            resource.description.contains(searchQuery, ignoreCase = true) ||
                            resource.authorName.contains(searchQuery, ignoreCase = true) ||
                            resource.subject.contains(searchQuery, ignoreCase = true) ||
                            resource.fileFormat.contains(searchQuery, ignoreCase = true)
                }

                matchesFormat && matchesSubject && matchesQuery
            }
        }
    }

    fun getBookmarkedResources(): Flow<List<EducationalResource>> =
        resourceDao.getBookmarkedResources()

    fun getMyUploadedResources(): Flow<List<EducationalResource>> =
        resourceDao.getMyUploadedResources()

    fun observeResource(id: String): Flow<EducationalResource?> =
        resourceDao.observeResourceById(id)

    fun getUserProfile(): Flow<UserProfile?> =
        userProfileDao.getProfileFlow()

    suspend fun updateUserProfile(
        name: String,
        handle: String,
        institution: String,
        bio: String,
        role: String
    ) {
        val current = userProfileDao.getProfile()
        val updated = (current ?: UserProfile()).copy(
            name = name.trim(),
            handle = if (handle.startsWith("@")) handle.trim() else "@${handle.trim()}",
            institution = institution.trim(),
            bio = bio.trim(),
            role = role,
            avatarLetter = name.trim().take(1).uppercase()
        )
        userProfileDao.insertOrUpdateProfile(updated)
    }

    suspend fun toggleBookmark(id: String) {
        val res = resourceDao.getResourceById(id) ?: return
        resourceDao.updateBookmark(id, !res.isBookmarked)
    }

    suspend fun toggleLike(id: String) {
        val res = resourceDao.getResourceById(id) ?: return
        val newIsLiked = !res.isLiked
        val delta = if (newIsLiked) 1 else -1
        resourceDao.updateLike(id, delta, newIsLiked)
    }

    suspend fun recordDownload(id: String) {
        resourceDao.incrementDownload(id)
    }

    suspend fun uploadResource(
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
    ): EducationalResource {
        val currentProfile = userProfileDao.getProfile()
        val authorName = currentProfile?.name ?: "Alex Vance"
        val authorHandle = currentProfile?.handle ?: "@you"
        val authorInst = currentProfile?.institution ?: "Open Educator"

        val resource = EducationalResource(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            resourceType = resourceType,
            subject = subject.uppercase(),
            authorName = authorName,
            authorHandle = authorHandle,
            authorInstitution = authorInst,
            mediaUrl = "https://open-edu.org/resource/${UUID.randomUUID().toString().take(8)}",
            fileFormat = fileFormat.uppercase(),
            fileSizeString = if (fileSizeString.isBlank()) "3.5 MB" else fileSizeString,
            durationMinutes = if (durationMinutes <= 0) 15 else durationMinutes,
            content = content.trim(),
            difficulty = difficulty,
            isBookmarked = false,
            likesCount = 1,
            isLiked = true,
            downloadCount = 0,
            coverDrawableName = coverDrawable.ifBlank { "img_editorial_architecture" },
            createdAt = System.currentTimeMillis()
        )

        resourceDao.insertResource(resource)

        if (currentProfile != null) {
            userProfileDao.insertOrUpdateProfile(
                currentProfile.copy(totalUploadsCount = currentProfile.totalUploadsCount + 1)
            )
        }

        return resource
    }

    private suspend fun seedInitialData() {
        val initialResources = listOf(
            EducationalResource(
                id = "res-1",
                title = "Deep Neural Networks: Backpropagation & Computational Graphs",
                description = "Comprehensive university lecture breaking down mathematical intuition, matrix gradients, and PyTorch autograd from scratch.",
                resourceType = "VIDEO",
                subject = "COMPUTER SCIENCE",
                authorName = "Prof. Andrew Lin",
                authorHandle = "@andrewlin",
                authorInstitution = "Stanford Open Courseware",
                mediaUrl = "https://storage.googleapis.com/edu-samples/backprop.mp4",
                fileFormat = "MP4",
                fileSizeString = "480 MB",
                durationMinutes = 42,
                difficulty = "Intermediate",
                isBookmarked = true,
                likesCount = 384,
                downloadCount = 1290,
                coverDrawableName = "img_editorial_architecture",
                content = """
                    ### Lecture Overview
                    Backpropagation is the workhorse algorithm behind modern deep learning. At its core, it is an efficient application of the multivariable calculus chain rule to computational graphs.

                    ### Timestamped Chapters
                    • 00:00 - Introduction & Forward Computation Pass
                    • 09:30 - The Scalar Chain Rule & Local Jacobians
                    • 21:15 - Vectorized Gradients for Batch Normalization
                    • 32:45 - Vanishing & Exploding Gradients: He Initialization
                    • 39:00 - Live PyTorch Implementation & Autograd

                    ### Key Equation
                    dL/dW = (dL/dY) * (dY/dW)
                    Where dY/dW is the local gradient of the forward transformation and dL/dY is the upstream gradient flowing back from the loss.

                    ### Study Notes
                    Always check dimensions during gradient derivations: if W is (D, M), then dL/dW must be precisely (D, M).
                """.trimIndent(),
                createdAt = System.currentTimeMillis() - 86400000L * 4
            ),
            EducationalResource(
                id = "res-2",
                title = "Linear Algebra & Matrix Calculus Complete Cheatsheet",
                description = "High-density printable PDF guide covering Eigenvalues, SVD, Positive Semidefinite matrices, and vector calculus proofs.",
                resourceType = "FILE_DOCUMENT",
                subject = "MATHEMATICS",
                authorName = "Dr. Gilbert Sterling",
                authorHandle = "@gsterling",
                authorInstitution = "MIT Open Mathematics",
                mediaUrl = "https://storage.googleapis.com/edu-samples/linear_algebra_cheat.pdf",
                fileFormat = "PDF",
                fileSizeString = "3.2 MB",
                durationMinutes = 20,
                difficulty = "Advanced",
                isBookmarked = true,
                likesCount = 520,
                downloadCount = 3450,
                coverDrawableName = "img_editorial_nature",
                content = """
                    ### Document Structure & Contents
                    This 18-page downloadable PDF document summarizes all essential matrix theorems required for Machine Learning, Robotics, and Quantum Mechanics.

                    ### Document Highlights
                    1. Spectral Theorem for Symmetric Matrices:
                       Every real symmetric matrix A can be factorized as A = Q * Lambda * Q^T where Q is orthonormal.
                    2. Singular Value Decomposition (SVD):
                       A = U * Sigma * V^T, breaking any arbitrary rectangular transformation into rotation, scaling, and rotation.
                    3. Matrix Inverses & Pseudoinverse:
                       Moore-Penrose Pseudoinverse A+ = (A^T * A)^(-1) * A^T for least squares solutions.

                    ### Recommended Usage
                    Print double-sided as a quick reference sheet or load into your tablet note-taking app.
                """.trimIndent(),
                createdAt = System.currentTimeMillis() - 86400000L * 3
            ),
            EducationalResource(
                id = "res-3",
                title = "Quantum Entanglement & Bell's Theorem: An Intuitive Guide",
                description = "A conceptual and mathematical breakdown of why the universe is fundamentally non-local, explained with zero fluff.",
                resourceType = "WEB_ARTICLE",
                subject = "PHYSICS",
                authorName = "Elena Vance, Ph.D.",
                authorHandle = "@elenavance",
                authorInstitution = "Perimeter Institute for Theoretical Physics",
                mediaUrl = "https://open-edu.org/articles/bells-theorem",
                fileFormat = "ARTICLE",
                fileSizeString = "120 KB",
                durationMinutes = 15,
                difficulty = "Intermediate",
                isBookmarked = false,
                likesCount = 290,
                downloadCount = 890,
                coverDrawableName = "img_editorial_architecture",
                content = """
                    In 1935, Albert Einstein, Boris Podolsky, and Nathan Rosen published their historic EPR paper questioning whether quantum mechanics could be a complete description of physical reality.

                    They argued that if two entangled particles could instantly coordinate their states across vast cosmic distances, either information was traveling faster than light, or "hidden variables" predetermined the measurements at the moment of separation.

                    For thirty years, physicists regarded this as mere philosophical quibbling. Then, in 1964, a quiet Northern Irish theorist named John Stewart Bell changed physics forever.

                    Bell proved that ANY theory based on local hidden variables must obey an exact mathematical inequality (the Bell Inequality):
                    |E(a, b) - E(a, c)| <= 1 + E(b, c)

                    When experimentalists finally tested entangled photons in the 1980s and 2010s, quantum mechanics violated Bell's inequality conclusively. The physical universe is non-local: entangled particles do not possess predetermined values before measurement.
                """.trimIndent(),
                createdAt = System.currentTimeMillis() - 86400000L * 2
            ),
            EducationalResource(
                id = "res-4",
                title = "Distributed Systems: Consensus with the Raft Algorithm",
                description = "A clear video walkthrough of Raft's leader election, replicated state machine, log synchronization, and network split recovery.",
                resourceType = "VIDEO",
                subject = "COMPUTER SCIENCE",
                authorName = "Diego Morales",
                authorHandle = "@morales_cs",
                authorInstitution = "Open Cloud Computing Foundation",
                mediaUrl = "https://storage.googleapis.com/edu-samples/raft_consensus.mp4",
                fileFormat = "MP4",
                fileSizeString = "340 MB",
                durationMinutes = 35,
                difficulty = "Advanced",
                isBookmarked = false,
                likesCount = 415,
                downloadCount = 1870,
                coverDrawableName = "img_editorial_architecture",
                content = """
                    ### Lecture Curriculum
                    Paxos was notoriously difficult to understand and implement correctly. Raft was deliberately designed around understandability by decomposing consensus into three independent sub-problems.

                    ### Modules Covered:
                    1. Leader Election: Heartbeat timers, candidate states, randomized election timeouts to avoid split votes.
                    2. Log Replication: Leader receives commands, appends them to its local log, and pushes AppendEntries RPCs to followers.
                    3. Safety Invariants: Election safety, leader append-only property, and commit state machine consistency.
                """.trimIndent(),
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            EducationalResource(
                id = "res-5",
                title = "Complete Data Structures & Algorithms Lab Notebooks",
                description = "A downloadable bundle containing 45 executable Python Jupyter notebooks implementing Graphs, Balanced BSTs, and Dynamic Programming.",
                resourceType = "FILE_DOCUMENT",
                subject = "COMPUTER SCIENCE",
                authorName = "Alex Vance",
                authorHandle = "@you",
                authorInstitution = "Open Science & Tech Educator",
                mediaUrl = "https://storage.googleapis.com/edu-samples/dsa_notebooks.zip",
                fileFormat = "ZIP",
                fileSizeString = "8.4 MB",
                durationMinutes = 60,
                difficulty = "Beginner",
                isBookmarked = false,
                likesCount = 612,
                downloadCount = 4200,
                coverDrawableName = "img_editorial_nature",
                content = """
                    ### Notebook Bundle Details
                    This free open-source archive includes 45 clean Python notebooks with automated unit tests for every data structure.

                    ### Included Units:
                    • 01_arrays_and_hashmaps.ipynb
                    • 02_linked_lists_and_two_pointers.ipynb
                    • 03_trees_avl_and_red_black.ipynb
                    • 04_graphs_bfs_dfs_dijkstra.ipynb
                    • 05_dynamic_programming_knapsack.ipynb

                    ### Installation
                    Unzip and run with `jupyter lab` or import into Google Colab with one click.
                """.trimIndent(),
                createdAt = System.currentTimeMillis()
            )
        )

        resourceDao.insertResources(initialResources)
    }
}
