package com.example.nanadrop.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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
        val WALLET_BALANCE = doublePreferencesKey("wallet_balance")
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

    val walletBalance: Flow<Double> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.WALLET_BALANCE] ?: 500.00
        }

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

    suspend fun setWalletBalance(balance: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WALLET_BALANCE] = balance
        }
    }

    suspend fun deductWalletBalance(amount: Double): Boolean {
        var success = false
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.WALLET_BALANCE] ?: 500.00
            if (current >= amount) {
                preferences[PreferencesKeys.WALLET_BALANCE] = current - amount
                success = true
            } else {
                success = false
            }
        }
        return success
    }

    suspend fun addWalletBalance(amount: Double) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.WALLET_BALANCE] ?: 500.00
            preferences[PreferencesKeys.WALLET_BALANCE] = current + amount
        }
    }
}
