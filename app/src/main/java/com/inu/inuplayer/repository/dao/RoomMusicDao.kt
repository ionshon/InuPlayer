package com.inu.inuplayer.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.inu.inuplayer.repository.model.MediaStoreMusic

@Dao
interface RoomMusicDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(music: MediaStoreMusic)

    @Query("SELECT * FROM music_table WHERE id like :id")
    fun  getMusic(id : Int) : MediaStoreMusic

    @Query("SELECT * FROM music_table")// ORDER BY music ASC")
    fun getAllMusics() : MutableList<MediaStoreMusic>

    @Query("SELECT * FROM music_table WHERE artist like :query")
    fun searchArtist(query: String): MutableList<MediaStoreMusic>

    @Query("DELETE FROM music_table WHERE id like :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM music_table")
    suspend fun deleteAlMusics()


}