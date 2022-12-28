package com.inu.inuplayer.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.IntentSender
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.inu.inuplayer.repository.Repository
import com.inu.inuplayer.repository.model.MediaStoreMusic
import com.inu.inuplayer.repository.model.MusicDevice.isLoadAll
import com.inu.inuplayer.repository.model.MusicDevice.musicList
import com.inu.inuplayer.utils.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


private const val TAG = "FragmentListVM"
class ViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class MusicViewModel(/*application: Application,*/ private val repository: Repository)
    : /*AndroidViewModel(application)*/ ViewModel() {

    private val _musics = MutableLiveData<List<MediaStoreMusic>>()
    val musics: LiveData<List<MediaStoreMusic>> get() = _musics
    private var contentObserver: ContentObserver? = null
    private var pendingUpdateMusic: MediaStoreMusic? = null
    private val _permissionNeededForUpdate = MutableLiveData<IntentSender?>()
    val permissionNeededForUpdate: LiveData<IntentSender?> = _permissionNeededForUpdate
    private val _bookmarkUpdate = MutableLiveData<MediaStoreMusic?>()
    val bookmarkUpdate: LiveData<MediaStoreMusic?> get() = _bookmarkUpdate
    var query = ""

    fun loadMusics(query: String?) {
//        if (!isLoadAll) {
            viewModelScope.launch {
                musicList = queryMusics(query) // 부팅시 전체불러오기
                _musics.postValue(musicList) // 룸에서 꺼낸거
                isLoadAll = true
                Log.i(TAG, "loadMusics ${musicList.size} musics")
                if (contentObserver == null) {
                    contentObserver =
                        MyApplication.applicationContext().contentResolver.registerObserver(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        ) {
                            loadMusics(query)
                        }
                }
            }
//        }
    }

    fun searchQueryMusics(query: String?) {
        var musics = mutableListOf<MediaStoreMusic>()
        CoroutineScope(Dispatchers.IO).launch {
            musics = query?.let { repository.search("%${it}%") }!!
            Log.i(TAG, "searchQueryMusics(Found) ${musics.size} musics")
            musicList = musics
            _musics.postValue(musicList) // 라이브데이터, 화면 설정
        }
    }

    fun reLoadMusics() {
        var musics = mutableListOf<MediaStoreMusic>()
        CoroutineScope(Dispatchers.IO).launch {
            musics = repository.getAllMusics()
            Log.i(TAG, "reLoadMusics(Found) ${musics.size} musics")
            musicList = musics
            _musics.postValue(musicList) // 라이브데이터, 화면 설정
        }
    }

    fun insert(music: MediaStoreMusic) = viewModelScope.launch {
        repository.insert(music)
    }

    fun deleteMusic(music: MediaStoreMusic) {
        Log.d("performUpdateMusic","deleteMusic")
        viewModelScope.launch {
            performDeleteMusic(music)
        }
    }

    fun deletePendingMusic() {
        Log.d("performUpdateMusic","deletePendingMusic")
        pendingUpdateMusic?.let {
            pendingUpdateMusic = null
            deleteMusic(it)
        }
    }
    private suspend fun performDeleteMusic(music: MediaStoreMusic) {
        Log.d("performUpdateMusic","performDeleteMusic")
        withContext(Dispatchers.IO) {
            try {
                MyApplication.applicationContext().contentResolver.delete(
                    Uri.parse(music.contentUri),
                    "${MediaStore.Audio.Media._ID} = ?",
                    arrayOf(music.id.toString())
                )
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException
                            ?: throw securityException

                    // Signal to the Activity that it needs to request permission and
                    // try the delete again if it succeeds.
                    pendingUpdateMusic = music
                    _permissionNeededForUpdate.postValue(
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    )
                } else {
                    throw securityException
                }
            }
        }
    }

    fun updateMusic(music: MediaStoreMusic) {

        Log.d("performUpdateMusic","updateMusic")
        viewModelScope.launch {
            performUpdateMusic(music)
        }
    }
    fun updatePendingMusic() {
        Log.d("performUpdateMusic","updatePendingMusic")
        pendingUpdateMusic?.let { image ->
            pendingUpdateMusic = null
            updateMusic(image)
        }
    }

    private suspend fun performUpdateMusic(music: MediaStoreMusic) {
        Log.d("performUpdateMusic","performUpdateMusic")
        withContext(Dispatchers.IO) {
            try {
                val values = ContentValues()
                values.put(MediaStore.Audio.Media.BOOKMARK, 1)
                MyApplication.applicationContext().contentResolver.update(
                    Uri.parse(music.contentUri),
                    values,
                    null,
                    null
                )
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException
                            ?: throw securityException

                    // Signal to the Activity that it needs to request permission and
                    // try the delete again if it succeeds.
                    pendingUpdateMusic = music
                    _permissionNeededForUpdate.postValue(
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    )
                } else {
                    throw securityException
                }
            }
        }
    }

    fun selectMusicUpdate(music: MediaStoreMusic) {
        Log.d("selectMusicUpdate","currentPosition= ${music.currentPosition}, isSelected= ${music.isSelected}" )
        viewModelScope.launch {
            performSelectUpdateMusic(music)
        }
    }

    private fun performSelectUpdateMusic(music: MediaStoreMusic) {
        music.isSelected = true
        _bookmarkUpdate.postValue(music)
        Log.d("performSelectUpdateMusic","currentPosition= ${music.currentPosition}, isSelected= ${music.isSelected}" )

    }
    /**
     * Convenience extension method to register a [ContentObserver] given a lambda.
     */
    private fun ContentResolver.registerObserver(
        uri: Uri,
        observer: (selfChange: Boolean) -> Unit
    ): ContentObserver {
        val contentObserver = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean) {
                observer(selfChange)
            }
        }
        registerContentObserver(uri, true, contentObserver)
        return contentObserver
    }
    /**
     * Since we register a [ContentObserver], we want to unregister this when the `ViewModel`
     * is being released.
     */
    override fun onCleared() {
        contentObserver?.let {
            MyApplication().applicationContext.contentResolver.unregisterContentObserver(it)
        }
    }
    private suspend fun queryMusics(query: String?): MutableList<MediaStoreMusic> {
        var musics = mutableListOf<MediaStoreMusic>()

        val projection =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { arrayOf(
            MediaStore.Audio.AudioColumns.ALBUM, // 0
            MediaStore.Audio.AudioColumns.TITLE, // 1
            MediaStore.Audio.AudioColumns.ARTIST,// 2
            MediaStore.Audio.AudioColumns.ALBUM_ID, // 3
            MediaStore.Audio.AudioColumns.DURATION, // 4
            MediaStore.Audio.AudioColumns.DATA, // 5
            MediaStore.Audio.AudioColumns.GENRE,//6
            MediaStore.Audio.AudioColumns.BOOKMARK, //7
            MediaStore.Audio.AudioColumns._ID //8
        ) } else {
            arrayOf(
            MediaStore.Audio.AudioColumns.ALBUM, // 0
            MediaStore.Audio.AudioColumns.TITLE, // 1
            MediaStore.Audio.AudioColumns.ARTIST,// 2
            MediaStore.Audio.AudioColumns.ALBUM_ID, // 3
            MediaStore.Audio.AudioColumns.DURATION, // 4
            MediaStore.Audio.AudioColumns.DATA, // 5
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,//6
            MediaStore.Audio.AudioColumns.BOOKMARK, //7
            MediaStore.Audio.AudioColumns._ID //8
        )

        }

        withContext(Dispatchers.IO) {
            var selectionClause: String? = ""
            val selectionArgs = "%${query}%"?.takeIf { it.isNotEmpty() }?.let {
                selectionClause = "${MediaStore.Audio.AudioColumns.ARTIST} LIKE ? "
                arrayOf(it)
            } ?: run {
                selectionClause = null
                emptyArray<String>()
            }
            Log.d("selectionClause", "query=$query, selectionArgs= $selectionArgs")
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            MyApplication.applicationContext().contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, // mCursorCols
                selectionClause, //"artist = "+ "'장나라'"
                selectionArgs, // null
                sortOrder
            )?.use { cursor ->

                Log.i(TAG, "Found1 ${cursor.count} musics")
                while (cursor.moveToNext()) {
                    val title = cursor.getString(1)
                    val duration = cursor.getLong(4)
                    if (duration > 10000 && !title.contains("통화 녹음")) {
                        val album = cursor.getString(0)
                        val id = cursor.getInt(8)
                        val artist = cursor.getString(2)
                        val albumID = cursor.getLong(3)
                        val albumUri = Uri.parse("content://media/external/audio/albumart/$albumID")
                        val genre = cursor.getString(6)
                        val bookmark = cursor.getString(7)
                        val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toLong() )
                        val currentPosition = -1
                        val isSelected = false

                        val music = MediaStoreMusic(id,title,artist,album, albumID, duration,
                                    albumUri.toString(), genre,isSelected, bookmark,contentUri.toString(),currentPosition )
                        insert(music)
//                        musics += music
//                        Log.d("cursor.moveToNext", "currentPosition= ${music.currentPosition}, isSelected= ${music.isSelected}")
                    }
                }
                cursor.close()
            }
            musics = repository.getAllMusics()
        }

        Log.v(TAG, "Found2 ${musics.size} musics")/*
        if (query != "") {
//            searchList.clear()
            for (m in musicList) {
                if (m.artist?.lowercase()?.contains(query.toString())!! || m.title?.lowercase()?.contains(query.toString())!!){
                    searchList.add(m)
                }
            }
        }*/
        return musics
    }

    @Suppress("SameParameterValue")
    @SuppressLint("SimpleDateFormat")
    private fun dateToTimestamp(day: Int, month: Int, year: Int): Long =
        SimpleDateFormat("dd.MM.yyyy").let { formatter ->
            TimeUnit.MICROSECONDS.toSeconds(formatter.parse("$day.$month.$year")?.time ?: 0)
        }
}

/*
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM)
                val albumIDColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
                val genreColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.GENRE)
                val bookmarkColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.BOOKMARK)
                val dateModifiedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)


 val id = cursor.getLong(idColumn)
                    val dateModified =
                        Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
                    val displayName = cursor.getString(displayNameColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val album = cursor.getString(albumColumn)
                    val albumID = cursor.getLong(albumIDColumn)
                    val duration = cursor.getLong(durationColumn)
                    val albumUri = Uri.parse("content://media/external/audio/albumart/$albumID")
                    val genre = cursor.getString(genreColumn)
                    val isSelected = false
                    val bookmark = cursor.getString(bookmarkColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )*/