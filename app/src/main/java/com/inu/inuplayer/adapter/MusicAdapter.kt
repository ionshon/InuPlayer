package com.inu.inuplayer.adapter

import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inu.inuplayer.R
import com.inu.inuplayer.repository.model.MediaStoreMusic
import com.inu.inuplayer.utils.GlideApp
import java.text.SimpleDateFormat


class MusicAdapter(val onClick: (MediaStoreMusic, View) -> Unit,
                   val itemClick: (MediaStoreMusic) -> Unit) :
    ListAdapter<MediaStoreMusic, MusicAdapter.MusicViewHolder>(MediaStoreMusic.DiffCallback){

    val resId = R.drawable.outline_music_note_24

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_layout, parent, false)
        return MusicViewHolder(view, onClick, itemClick)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = getItem(position)
        holder.rootView.tag = MediaStoreMusic

        holder.setMusic(music)
        GlideApp.with(holder.imageAlbum)
            .load(music.albumUri)
            .thumbnail(0.33f)
            .placeholder(resId)
            .error(resId)
            .centerCrop()
            .into(holder.imageAlbum)

//        holder.imageIsPlay.visibility = View.INVISIBLE
        GlideApp.with(holder.imageIsPlay)
            .load(R.raw.bounc)
            .into(holder.imageIsPlay)
        if (music.isSelected == true) {
            holder.imageIsPlay.visibility = View.VISIBLE
        } else holder.imageIsPlay.visibility = View.INVISIBLE

        // 삭제 정상작동
       /* holder.imageAlbum.setOnClickListener {
//            val music = rootView.tag as? MediaStoreMusic ?: return@setOnClickListener
            Log.d("setOnClickListener","${music.title}")
            onClick(music)
        }*/

        holder.imageMenu.setOnClickListener {
            onClick(music, holder.imageMenu)
        }
        holder.rootView.setOnClickListener {
            music.currentPosition = position
            Log.d("holder.rootView.setO","currentPosition= ${music.currentPosition}" )
            itemClick(music)
        }
       /* holder.imageMenu.setOnClickListener {
            val pop= PopupMenu(holder.rootView.context, it)
            pop.inflate(R.menu.itemlist_menu)
            pop.setOnMenuItemClickListener { item->
                when(item.itemId)
                {
                    R.id.action_info->{
                        showInfo(holder.rootView, music)
                    }
                    R.id.action_modify->{
                        musicUpdate(music)
                    }
                    R.id.action_delete-> {}
                }
                true
            }
            pop.show()
        }*/
    }

    inner class MusicViewHolder(
        view: View, onClick: (MediaStoreMusic, View) -> Unit,
        itemClick: (MediaStoreMusic) -> Unit
    ) :
        RecyclerView.ViewHolder(view) {
        val rootView = view
//        val imageView: ImageView = view.findViewById(R.id.image)
        val textTitle: TextView = view.findViewById(R.id.textTitle)
        val textAtist: TextView = view.findViewById(R.id.textAtist)
        val textDuration: TextView = view.findViewById(R.id.textDuration)
        val textViewGenre: TextView = view.findViewById(R.id.textView_genre)
        val imageAlbum: ImageView = view.findViewById(R.id.imageAlbum)
        val imageIsPlay: ImageView = view.findViewById(R.id.imageView_isplay)
        val imageMenu: ImageView = view.findViewById(R.id.imageView_menu)

        /*init { // music을 받아오기 힘듬
            rootView.setOnClickListener {
                val music = rootView.tag as? MediaStoreMusic ?: return@setOnClickListener
                onClick(music)
            }
        }*/

        fun setMusic(music: MediaStoreMusic) {
            textTitle.text = music.title
            textAtist.text = music.artist
            textViewGenre.text = music.genre
            val sdf = SimpleDateFormat("mm:ss")
            textDuration.text = sdf.format(music.duration)

//                1. 로드할 대상 Uri    2. 입력될 이미지뷰
           /* Glide.with(rootView.context)
                .load(music.albumUri)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(resId)
                .error(resId)
//                .thumbnail(GlideApp.with(binding.root.context).load(resId).override(100, 100))
                .into(imageAlbum)*/

        }
    }
/*
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menu?.add(Menu.NONE, R.id.imageAlbum,
            Menu.NONE, R.string.info);
        menu?.add(Menu.NONE, R.id.imageView_isplay,
            Menu.NONE, R.string.delete);
    }*/

    fun notifyChanged(music: MediaStoreMusic) {
        notifyItemChanged(music.currentPosition)
        Log.d("notifyChanged","currentPosition= ${music.currentPosition}, isSelected= ${music.isSelected}" )
    }
}


/*
class MusicAdapter : RecyclerView.Adapter<MusicAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(view)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val music = musicList[position]
        holder.setMusic(music)
    }



    inner class Holder(val binding: ItemLayoutBinding): RecyclerView.ViewHolder(binding.root){

        val resId = R.drawable.outline_music_note_24
        fun setMusic(music: Music) {
            with(binding) {
                textTitle.text = music.title
                textAtist.text = music.artist
                textViewGenre.text = music.genre
                val sdf = SimpleDateFormat("mm:ss")
                textDuration.text = sdf.format(music.duration)
            }

//                1. 로드할 대상 Uri    2. 입력될 이미지뷰
            Glide.with(binding.root.context)
                .load(music.albumUri)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(resId)
                .error(resId)
//                .thumbnail(GlideApp.with(binding.root.context).load(resId).override(100, 100))
                .into(binding.imageAlbum)


            */
/* GlideApp.with(binding.root.context)
                 .load(R.raw.bounc)
                 .into(binding.imageViewIsplay)

             binding.imageViewIsplay.visibility = View.INVISIBLE
             if (music.isSelected) {
                 binding.imageViewIsplay.visibility = View.VISIBLE
             }*//*

        }
    }
}*/
