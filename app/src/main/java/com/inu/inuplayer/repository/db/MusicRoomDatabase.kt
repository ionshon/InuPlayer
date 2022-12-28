package com.inu.inuplayer.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inu.inuplayer.repository.dao.RoomMusicDao
import com.inu.inuplayer.repository.model.MediaStoreMusic

@Database(entities = [MediaStoreMusic::class], version = 1, exportSchema = false)
@TypeConverters(MusicTypeConverters::class)
abstract class MusicRoomDatabase: RoomDatabase() {
    abstract fun musicDao(): RoomMusicDao

    companion object {
        private var  instance: MusicRoomDatabase? = null

        // Music 변경으로 필요, 버전올리고 아래 .addMigrations(migration_1_2) 추가하면 됨
        val migration_1_2 = object: Migration(1,2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //만약, 테이블이 추가 되었다면 어떤 테이블이 추가 되었는지 알려주는 query문장이 필요
                database.execSQL("CREATE TABLE 'REVIEW' ('id' INTEGER, 'review' TEXT, " + "PRIMARY KEY('id'))")
            }
        }

        @Synchronized
        fun getInstance(context: Context): MusicRoomDatabase? {
            if (instance == null)
                synchronized(MusicRoomDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MusicRoomDatabase::class.java,
                        "music_database"
                    ).fallbackToDestructiveMigration() // Room 은 migration schema version 이 유실되어 migration 이 실패하는 경우 app db table 을 재생성한다.(destructive recreation) 이 때 영구적으로 모든 데이터가 유실될 수 있음
                     .build()
                }
            return instance
        }

        fun destroyInstance() {
            instance = null
        }
    }
}