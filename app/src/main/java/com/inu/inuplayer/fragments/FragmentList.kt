package com.inu.inuplayer.fragments

import android.Manifest
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.inu.inuplayer.R
import com.inu.inuplayer.adapter.MusicAdapter
import com.inu.inuplayer.databinding.FragmentListBinding
import com.inu.inuplayer.repository.model.MediaStoreMusic
import com.inu.inuplayer.repository.model.MusicDevice
import com.inu.inuplayer.repository.model.MusicDevice.isLoadAll
import com.inu.inuplayer.utils.MyApplication
import com.inu.inuplayer.viewmodel.MusicViewModel
import com.inu.inuplayer.viewmodel.ViewModelFactory
import kotlin.system.exitProcess

private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045
private const val DELETE_PERMISSION_REQUEST = 0x1033
private const val UPDATE_PERMISSION_REQUEST = 0x1023
class FragmentList : Fragment() {

    private lateinit var viewModel: MusicViewModel
    private lateinit var viewModelFactory: ViewModelFactory

    lateinit var permissions: Array<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
//    private val viewModel: MusicViewModel by viewModels(  )
    private lateinit var binding: FragmentListBinding

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModelFactory = MyApplication.repository?.let { ViewModelFactory(repository = it) }!!
        viewModel = ViewModelProvider(this, viewModelFactory).get(MusicViewModel::class.java)

        binding = FragmentListBinding.inflate(inflater, container, false)
        val searchView = binding.searchViewMusic
        searchView.bringToFront()
        val musicAdapter =  MusicAdapter( { music, view -> menuActions(music, view) },
            { music -> bookmarkUpdate(music) })

        Log.i("FragmentListVM", "onCreateView ${MusicDevice.musicList.size} musics")
        binding.recyclerViewList.also { view ->
            view.layoutManager = GridLayoutManager(requireContext(), 1)
            view.adapter = musicAdapter
        }

        // 화면회전하면 onResume() 전에 실행
        viewModel.musics.observe(requireActivity(), Observer {
            musicAdapter.submitList(it)
            Log.i("FragmentListVM", "viewModel.musics.observe(FragList2) ${MusicDevice.musicList.size} musics")
        })

        viewModel.bookmarkUpdate.observe(requireActivity(), Observer{
            it?.let {
                musicAdapter.notifyChanged(it)
                Log.d(".observe","currentPosition= ${it.currentPosition}, isSelected= ${it.isSelected}" )
            }
        })

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchQueryMusics(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        binding.buttonAllmusic.setOnClickListener {
            isLoadAll = false
            Log.d("FragmentListVM", "buttonAllmusic(reLoad) " +
                    "${MusicDevice.musicList.size} musics, isLoadAll= $isLoadAll" )
            viewModel.reLoadMusics()
        }

        permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                if (!isLoadAll) // 부팅시만 전체 로딩
                    viewModel.loadMusics("") // 제일 처음 실행
            } else {
                Toast.makeText(requireContext(), "권한 요청 실행해야지 앱 실행", Toast.LENGTH_SHORT).show()
                exitProcess(0)
            }
        }
        permissions.forEach { p ->
            requestPermissionLauncher.launch(p)
        }

        return binding.root
    }

    private fun menuActions(music: MediaStoreMusic, view: View) {

        val pop= PopupMenu(view.context, view)
        pop.inflate(R.menu.itemlist_menu)
        pop.setOnMenuItemClickListener { item->
            when(item.itemId)
            {
                R.id.action_info->{
                    showInfo(view, music)
                }
                R.id.action_modify->{
                    Log.d("performUpdateMusic","R.id.action_modify")

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.update_dialog_title)
                        .setMessage(getString(R.string.update_dialog_message, music.title))
                        .setPositiveButton(R.string.update_dialog_positive) { _: DialogInterface, _: Int ->
                            viewModel.updateMusic(music)
                        }
                        .setNegativeButton(R.string.delete_dialog_negative) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                        }
                        .show()
                }
                R.id.action_delete-> {
                    Log.d("performUpdateMusic","R.id.action_delete")
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete_dialog_title)
                        .setMessage(getString(R.string.delete_dialog_message, music.title))
                        .setPositiveButton(R.string.delete_dialog_positive) { _: DialogInterface, _: Int ->
                            viewModel.deleteMusic(music)
                        }
                        .setNegativeButton(R.string.delete_dialog_negative) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            true
        }
        pop.show()
    }

    private fun bookmarkUpdate(music: MediaStoreMusic) {
        Log.d("bookmarkUpdate","currentPosition= ${music.currentPosition}, isSelected= ${music.isSelected}" )

        viewModel.selectMusicUpdate(music)
    }

    private fun showInfo(view: View, music: MediaStoreMusic) {
        val inflater: LayoutInflater = LayoutInflater.from(view.context)
        //view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.popup_info, null)
        val textViewInfo: TextView = popupView.findViewById(R.id.textView_info)

        textViewInfo.text = "\n\n- Title: \n    ${music.title}\n\n" +
                "- Artist: \n   ${music.artist}\n\n- Genre or Path: \n    ${music.genre}\n\n- Bookmark: \n  ${music.bookmark}"

        // create the popup window
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        // dismiss the popup window when touched
        popupView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                popupWindow.dismiss()
                return true
            }
        })
    }

/*
    override fun onDestroy() {
        super.onDestroy()
        Log.i("FragmentListVM", "onDestroy ${MusicDevice.musicList.size} musics")
    }

    override fun onResume() {
        super.onResume()
        Log.i("FragmentListVM", "onResume ${MusicDevice.musicList.size} musics")
    }

    override fun onStop() {
        super.onStop()
        Log.i("FragmentListVM", "onStop ${MusicDevice.musicList.size} musics")
    }

    override fun onPause() {
        super.onPause()
        Log.i("FragmentListVM", "onPause ${MusicDevice.musicList.size} musics")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("FragmentListVM", "onViewCreated ${MusicDevice.musicList.size} musics")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("FragmentListVM", "onCreate ${MusicDevice.musicList.size} musics")
    }

    override fun onStart() {
        super.onStart()
        Log.i("FragmentListVM", "onStart ${MusicDevice.musicList.size} musics")
    }*/
}