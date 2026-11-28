package com.example.nanadrop.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val HIDE_SOLD_LISTINGS = booleanPreferencesKey("hide_sold_listings")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val BIO = stringPreferencesKey("user_bio")
        val RATING = doublePreferencesKey("user_rating")
        val COMPLETED_DEALS_COUNT = intPreferencesKey("user_completed_deals_count")
        val IS_VERIFIED_SELLER = booleanPreferencesKey("user_is_verified_seller")
    }

    val hideSoldListings: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.HIDE_SOLD_LISTINGS] ?: false
        }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DARK_MODE] ?: false
        }

    val userProfile: Flow<UserProfile> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserProfile(
                displayName = preferences[PreferencesKeys.DISPLAY_NAME] ?: "Alex River",
                bio = preferences[PreferencesKeys.BIO] ?: "Gamer & digital item collector on NanaDrop. Fast delivery guaranteed!",
                rating = preferences[PreferencesKeys.RATING] ?: 4.9,
                completedDealsCount = preferences[PreferencesKeys.COMPLETED_DEALS_COUNT] ?: 24,
                isVerifiedSeller = preferences[PreferencesKeys.IS_VERIFIED_SELLER] ?: true
            )
        }

    // NOTE: the wallet balance is no longer stored in DataStore.
    // All money lives in Room (see WalletRepository / WalletDao) so every screen agrees.

    suspend fun setHideSoldListings(hide: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_SOLD_LISTINGS] = hide
        }
    }

    suspend fun setDarkMode(darkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = darkMode
        }
    }

    suspend fun updateDisplayName(displayName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISPLAY_NAME] = displayName
        }
    }

    suspend fun updateBio(bio: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIO] = bio
        }
    }

    suspend fun updateProfile(displayName: String, bio: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISPLAY_NAME] = displayName
            preferences[PreferencesKeys.BIO] = bio
        }
    }
}
