package com.example.nanadrop.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ListingEntity::class], version = 1, exportSchema = false)
abstract class MarketplaceDatabase : RoomDatabase() {

    abstract fun listingDao(): ListingDao

    companion object {
        @Volatile
        private var INSTANCE: MarketplaceDatabase? = null

        fun getDatabase(context: Context): MarketplaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarketplaceDatabase::class.java,
                    "marketplace_database"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getDatabase(context).listingDao()
                    populateInitialData(dao)
                }
            }
        }

        private suspend fun populateInitialData(dao: ListingDao) {
            val initialListings = listOf(
                ListingEntity(
                    title = "Steam Key: Elden Ring Nightrein Edition",
                    category = "Keys",
                    price = 45.00,
                    sellerName = "Alex_Gamer",
                    description = "Global Steam key for Elden Ring + Shadow of the Erdtree DLC.",
                    platform = "Steam",
                    isUserListing = true,
                    isSold = false
                ),
                ListingEntity(
                    title = "PlayStation Plus Deluxe 12 Months",
                    category = "Subscriptions",
                    price = 59.99,
                    sellerName = "SonyDigital",
                    description = "Annual PS Plus subscription code for Turkish / Global accounts.",
                    platform = "PlayStation",
                    isUserListing = false,
                    isSold = false
                ),
                ListingEntity(
                    title = "Valorant Account (Radiant Rank + Skins)",
                    category = "Accounts",
                    price = 120.00,
                    sellerName = "ProSeller99",
                    description = "High tier account with Vandal Prime, Phantom Kuronami and 500 VP.",
                    platform = "Riot Games",
                    isUserListing = true,
                    isSold = false
                )
            )
            dao.insertListings(initialListings)
        }
    }
}
