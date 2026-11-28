package com.example.nanadrop.data.local

/**
 * Data model for the user's profile state.
 *
 * @property displayName User's display name shown on profile and listings.
 * @property bio User's bio or description.
 * @property rating Seller/buyer rating out of 5.0.
 * @property completedDealsCount Total completed trades/deals on NanaDrop.
 * @property isVerifiedSeller Whether the user is a verified seller.
 */
data class UserProfile(
    val displayName: String = "Alex River",
    val bio: String = "Gamer & digital item collector on NanaDrop. Fast delivery guaranteed!",
    val rating: Double = 4.9,
    val completedDealsCount: Int = 24,
    val isVerifiedSeller: Boolean = true
)
