package com.example.nanadrop.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings ORDER BY id DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id")
    fun getListingById(id: Long): Flow<ListingEntity?>

    @Query("SELECT COUNT(*) FROM listings")
    fun getListingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertListing(listing: ListingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertListings(listings: List<ListingEntity>)

    @Update
    fun updateListing(listing: ListingEntity)

    @Delete
    fun deleteListing(listing: ListingEntity)

    @Query("DELETE FROM listings WHERE id = :id")
    fun deleteListingById(id: Long)
}
