# EduAura: Open Free Educational Platform

Transforming Aura into a community-driven, 100% free educational resource platform where students and educators worldwide can create profiles and share learning materials across video lectures, downloadable documents/files, and comprehensive web blog articles.

### User Review & Critical Decisions

> [!IMPORTANT]
> The clarifying questions were skipped, so the plan proceeds with the recommended defaults:
> - **Seamless Account Management**: Built-in profile manager enabling instant educator/student account creation, biography setup, subject expertise badges, and contribution tracking without forcing rigid third-party login walls.
> - **Multimodal Resource Catalog**: First-class support for three primary educational modalities:
>   1. **Video Resources**: Lectures and video walkthroughs with duration, chapter markers, transcript highlights, and playback controls.
>   2. **File & Document Resources**: Study guides, PDF lecture slides, code repositories, and cheat sheets with file format badges (`.PDF`, `.ZIP`, `.PY`, `.EPUB`), file size metadata, and in-app document viewer.
>   3. **Web Blog & Interactive Articles**: Deep-dive theoretical guides, tutorials, and concept breakdowns with formulas and formatted pull quotes.
> - **Taxonomy & Search**: Filtering by Subject (*Computer Science*, *Mathematics*, *Physics & Chemistry*, *Humanities*, *Design & Architecture*) and Resource Type (*Videos*, *Files*, *Articles*).
> - **Zero-Cost Publishing**: Simple upload studio allowing any user to publish free educational materials with preview generation and immediate community availability.

---

### 1. Overview & Core Concept

- **What It Does**: EduAura is an open knowledge hub democratizing education. Anyone can create an account, browse peer-reviewed and community-authored learning resources, bookmark study materials to a personal shelf, and upload new resources free of charge.
- **Target Audience**: Self-taught learners, university students, educators, and open-source researchers seeking structured, high-signal knowledge free of intrusive ads and paywalls.
- **Key Value**: Eliminates fractured learning across disparate sites by unifying video lectures, companion study files, and textbook-grade articles into one cohesive, distraction-free Android interface.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Explore & Search Catalog (The Knowledge Hub)**:
   - Filter by format tabs: **All**, **📹 Videos**, **📄 Documents & Files**, and **📰 Web Articles**.
   - Topic chip carousel (*Computer Science, Mathematics, Physics, Humanities, Data Science*).
   - Instant search across titles, descriptions, file formats, and instructor names.
2. **Interactive Resource Detail Views**:
   - **Video Lecture Player**: Simulated video playback canvas with play/pause, scrub bar, duration, lesson outline, and key takeaways.
   - **Document Viewer**: Formatted sheet viewer with file metadata (`2.4 MB PDF`), table of contents, preview text, and one-tap download action.
   - **Interactive Web Blog Reader**: Full-screen article with adjustable typography, subheads, and concept summaries.
3. **Open Resource Upload Studio**:
   - Creator selects resource format (`VIDEO`, `FILE`, `ARTICLE`).
   - Fields for title, subject domain, difficulty level (*Beginner*, *Intermediate*, *Advanced*), attachment URLs/presets, and companion notes.
   - Live preview card showing how the resource will appear to learners.
   - One-tap free publishing to the global repository.
4. **User Profile & Account Hub**:
   - Account creator: Edit Name, Avatar monogram, Institution / Title, and Bio.
   - Tabbed overview of **Saved Resources (Study Shelf)** and **My Uploaded Contributions**.
   - Community impact metrics: Total learners helped, free resources published, and hours learned.

#### Visual Identity & Theme
- **Theme**: Academic Editorial & Clean Modern Knowledge (combining deep scholarly Navy/Charcoal `#0B132B`, warm parchment `#F9FAF6`, emerald verification `#10B981`, and amber accent `#F59E0B`).
- **Typography**: Playfair Display for academic titles and subject anthologies; Plus Jakarta Sans for dense technical documentation, code snippets, and UI navigation.
- **Component Styling**: Clean card containers with distinctive media pill badges (e.g. purple for Video, emerald for PDF, cyan for Web Blog).

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Tri-Modal Resource Model (Video + File + Web Article)**
  - *Chosen Approach*: Unified `EducationalResource` schema polymorphic across the 3 formats with specialized UI components for each media type.
  - *Why*: Allows teachers to share full course bundles (e.g., a video lecture paired with downloadable Python slides and an article).
- **Decision 2: Local Reactive Storage with Comprehensive Seed Curriculum**
  - *Chosen Approach*: Extended Room database with pre-populated university-grade materials in Computer Science (Algorithms, Neural Networks), Physics (Quantum Mechanics), Mathematics (Linear Algebra), and Architecture.
  - *Why*: Immediate out-of-the-box value with zero empty states, while full upload capability allows users to contribute their own resources dynamically.
- **Decision 3: Frictionless Account Creation**
  - *Chosen Approach*: In-app educator and student account switcher and creator with persistent profile storage, avoiding rigid external OAuth requirements while enabling authentic user authoring.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                      EduAura UI                        │
├─────────────────┬───────────────────┬──────────────────┤
│ CatalogScreen   │ ResourceDetail    │ UploadStudio     │
│ (Video/File/    │ (Video Player,    │ (Format Picker,  │
│  Blog Filters)  │  Doc Viewer, etc) │  File Attacher)  │
├─────────────────┴───────────────────┴──────────────────┤
│            Profile & Study Shelf Screen                │
└──────────────────────────┬─────────────────────────────┘
                           │ State & Events
                           ▼
┌────────────────────────────────────────────────────────┐
│                   EduAuraViewModel                     │
│   • resources (Flow<List<EducationalResource>>)        │
│   • activeResource (StateFlow<EducationalResource?>)   │
│   • currentUserProfile (StateFlow<UserProfile>)        │
│   • formatFilter & subjectFilter                       │
│   • uploadResource() & toggleBookmark()                │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                 EducationalRepository                  │
│   • Room Database (ResourceDao, ProfileDao)            │
│   • Pre-seeded CS, Math, and Science learning units    │
└────────────────────────────────────────────────────────┘
```

#### Core Data Entities
- **EducationalResource**:
  - `id`: Unique identifier
  - `title`: Resource name
  - `description`: Overview of learning outcomes
  - `resourceType`: `VIDEO`, `FILE_DOCUMENT`, `WEB_ARTICLE`
  - `subject`: `COMPUTER_SCIENCE`, `MATHEMATICS`, `PHYSICS`, `HUMANITIES`, `DESIGN`
  - `authorName`: Uploader's display name
  - `authorHandle`: Uploader's handle / institution
  - `mediaUrl`: Video streaming link or document URL
  - `fileFormat`: e.g. `"PDF"`, `"ZIP"`, `"EPUB"`, `"MP4"`
  - `fileSizeString`: e.g. `"4.8 MB"`
  - `durationMinutes`: e.g. `24` (for videos) or `8` (for reading)
  - `content`: Rich educational body text or lecture notes
  - `difficulty`: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`
  - `isBookmarked`: Boolean for user's personal study shelf
  - `likesCount`: Appreciation counter
  - `downloadCount`: Total learner downloads

- **UserProfile**:
  - `id`, `name`, `handle`, `institution`, `bio`, `role` (*Learner* or *Educator*), `avatarLetter`.
