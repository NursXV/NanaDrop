# NanaDrop - Digital Store App

# Description
**NanaDrop** is a modern Android application built for purchasing digital goods, game keys, software accounts, and subscriptions. The app provides a clean, user-friendly interface for browsing digital products, filtering by categories, managing a shopping cart, viewing detailed product specifications, and managing marketplace listings with persistent offline storage and REST API connectivity.

# Group Members
* CEO (крутой сео): Zhuma Nursultan (Best Dev in Company крч)

# Main Features
- **Digital Products Catalog:** Displays a list of available game keys, accounts, and subscriptions using `LazyColumn`.
- **Category Filtering:** Allows users to filter items dynamically using `CategoryChip` components (`Keys`, `Accounts`, `Subscriptions`).
- **Real-time Search:** Search bar integrated with `StateFlow` to filter products instantly by title and description.
- **Product Details Screen:** Displays comprehensive information (price, platform, description) based on the selected product ID.
- **Cart Management:** Enables users to add digital items to their cart and manage their purchases.
- **Marketplace CRUD & Room Persistence:** Fully featured marketplace listings management backed by Room database for robust offline capabilities and data persistence across sessions.
- **REST API Integration:** Seamless integration with remote backends using Retrofit, Kotlin Coroutines, and `StateFlow`, complete with image loading via Coil and robust UI state handling (`Loading`, `Success`, `Error`, `Empty`) with Retry mechanism.
- **Preferences DataStore:** Modern lightweight key-value storage for user settings, preferences, and session state.
- **Modern Architecture:** Built using the **MVVM** (Model-View-ViewModel) architectural pattern, **Jetpack Compose** for UI, and **Navigation Compose** with safe argument passing.
