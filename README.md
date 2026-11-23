# NanaDrop - Digital Store App

# Description
**NanaDrop** is a modern Android application built for purchasing digital goods, game keys, software accounts, and subscriptions. The app provides a clean, user-friendly interface for browsing digital products, filtering by categories, managing a shopping cart, viewing detailed product specifications, and managing marketplace listings with persistent offline storage and REST API connectivity.

# Group Members
* CEO (крутой сео): Zhuma Nursultan (Best Dev in Company крч)

# Main Features
- **Marketplace listings with Room local persistence:** Create, Read, Update, Delete, sold status
- **Cart integration from Marketplace:** "Buy" / "Added" state, cart badge counter, CartScreen
- **ListingDetailsScreen:** listingId navigation argument, back navigation, Add to Cart, and Buy Now
- **Search and filtering:** Search, category filtering, Hide Sold, My Listings Only filter, active listings counter, and sorting (Newest, Price: Low to High, Price: High to Low)
- **Top bar menu & preferences persistence via DataStore:** Top bar three-line menu, Dark Theme, Hide Sold
- **External REST API integration:** Integration using Retrofit, Coroutines, StateFlow, Coil, Loading/Success/Error/Empty states, and Retry
- **Modern Architecture:** Built using the **MVVM** (Model-View-ViewModel) architectural pattern, **Jetpack Compose** for UI, and **Navigation Compose** with safe argument passing.