package com.inu.inuplayer.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Query
import com.inu.inuplayer.repository.dao.RoomMusicDao
import com.inu.inuplayer.repository.model.MediaStoreMusic


class Repository (private val musicDao: RoomMusicDao){

//    val allMusics:  List<MediaStoreMusic> = musicDao.getAllMusics()

//    @Suppress("RedundantSuspendModifier")
//    @WorkerThread
    suspend fun insert(music: MediaStoreMusic) {
        musicDao.insert(music)
    }

    suspend fun getAllMusics(): MutableList<MediaStoreMusic> {
        return musicDao.getAllMusics()
    }

    suspend fun search(query: String): MutableList<MediaStoreMusic> {
        return musicDao.searchArtist(query)
    }

    suspend fun deleteAll() {
        musicDao.deleteAlMusics()
    }

    suspend fun getMusic(id: Int) : MediaStoreMusic {
        return musicDao.getMusic(id)
    }

    companion object {
        private var INSTANCE: Repository? = null

        fun initialize(context: Context, musicDao: RoomMusicDao) {
            if (INSTANCE == null) {
                INSTANCE = Repository(musicDao)
            }
        }

        fun get(): Repository {
            return INSTANCE ?:
            throw IllegalStateException("MusicRepository must be initialized")
        }
    }

}