# Changelog

All notable changes to the **NanaDrop** project will be documented in this file.

## [0.0.3] - 2025-03-XX
### Added
- **Marketplace CRUD & Room Persistence:** Added full Create, Read, Update, and Delete (CRUD) operations for marketplace items with local Room database storage, ensuring all data persists reliably across app sessions.
- **REST API Integration:** Implemented robust network communication using Retrofit, Kotlin Coroutines, and `StateFlow`.
- **Enhanced UI States & Error Handling:** Added comprehensive UI state management supporting `Loading`, `Success`, `Error`, and `Empty` states alongside a convenient Retry mechanism for failed network requests.
- **Image Loading with Coil:** Integrated Coil for efficient and smooth asynchronous image loading for products and marketplace items.
- **Preferences DataStore:** Integrated Jetpack Preferences DataStore for persistent user preferences and settings management.

## [0.0.2] - Previous Release
### Added
- Cart management and detailed product view specifications.
- Category filtering and real-time search with `StateFlow`.

## [0.0.1] - Initial Release
### Added
- Initial project setup with Jetpack Compose and MVVM architecture.
- Digital products catalog using `LazyColumn`.
- Basic author and project information.
