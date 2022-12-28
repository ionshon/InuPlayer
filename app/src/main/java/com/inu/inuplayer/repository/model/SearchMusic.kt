package com.inu.inuplayer.repository.model

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "search_table")
data class SearchMusic(
    @PrimaryKey //(autoGenerate = true)
    val id: Int,
    val title:String?,
    val artist:String?,
    val album:String?,
    val albumId:Long?,
    val duration: Long,
    val albumUri: String?,
//    val path : String,
    val genre: String?,
    var isSelected: Boolean?, // playing song
    val bookmark: String?,
    val contentUri: String,
    var currentPosition: Int
) {

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<MediaStoreMusic>() {
            override fun areItemsTheSame(oldItem: MediaStoreMusic, newItem: MediaStoreMusic) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MediaStoreMusic, newItem: MediaStoreMusic) =
                oldItem == newItem
        }
    }
}
