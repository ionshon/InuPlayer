package com.inu.inuplayer.repository.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inu.inuplayer.repository.dao.RoomMusicDao
import com.inu.inuplayer.repository.model.MediaStoreMusic

@Database(entities = [MediaStoreMusic::class], version = 1)
abstract class RoomMusicHelper: RoomDatabase() {
    abstract fun roomMusicDao(): RoomMusicDao
}