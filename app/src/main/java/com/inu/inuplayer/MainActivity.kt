package com.inu.inuplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import com.google.android.material.tabs.TabLayoutMediator
import com.inu.inuplayer.databinding.ActivityMainBinding
import com.inu.inuplayer.fragments.FragmentList
import com.inu.inuplayer.fragments.FragmentPlay
import com.inu.inuplayer.fragments.FragmentRadio
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {

        var isReady = false

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        thread(start = true) {
            for (i in 1..1) {
                Thread.sleep(0)
            }
            isReady = true
        }

        // Set up an OnPreDrawListener to the root view.
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Check if the initial data is ready.
                    return if (isReady) {
                        // The content is ready; start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        // The content is not ready; suspend.
                        false
                    }
                }
            }
        )
        // 1. 페이지 데이터 로드
        val list = listOf(FragmentList(), FragmentPlay(), FragmentRadio())
        // 2. 아답터 생성
        val pagerAdapter = com.inu.inuplayer.adapter.FragmentPagerAdapter(list, this)

        binding.viewPager.adapter = pagerAdapter
        // 4. 탭 메뉴의 갯수만큼 제목을 목록으로 생성
        val titles = listOf("음악",  "PLAY"/*,"RADIO"*/, "라디오")
        // 5. 탭레이아웃과 뷰페이저 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()


    }
}