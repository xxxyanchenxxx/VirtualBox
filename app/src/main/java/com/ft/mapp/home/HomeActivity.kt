package com.ft.mapp.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.ft.mapp.R
import com.ft.mapp.abs.ui.VActivity
import com.ft.mapp.home.adapters.HomeViewpagerAdapter
import com.google.android.material.tabs.TabLayout
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.activity_home.*

open class HomeActivity : VActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        StatusBarUtil.setTranslucent(this, 0)
        bindViews()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun bindViews() {
        val pagerAdapter = HomeViewpagerAdapter(
            supportFragmentManager)
            .apply {
                addFragment(LaunchFragment.newInstance())
                addTab(resources.getString(R.string.tab_index))
                addFragment(MeFragment.newInstance())
                addTab(resources.getString(R.string.tab_me))
            }
        viewPager?.let {
            tabLayout.setupWithViewPager(it)
            it.adapter = pagerAdapter
            it.currentItem = 0
            it.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(p0: Int) {}

                override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

                override fun onPageSelected(position: Int) {
                }
            })
        }
        var index = 0
        tabLayout.getTabAt(index++)?.customView =
            getTabView(resources.getString(R.string.tab_index),
                R.drawable.tab_index_select)

        tabLayout.getTabAt(index++)?.customView =
            getTabView(resources.getString(R.string.tab_me),
                R.drawable.tab_me_select)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
                //
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun getTabView(title: String?, image_src: Int): View {
        val v: View =
            LayoutInflater.from(applicationContext).inflate(R.layout.tab_item_view, null)
        val textView: TextView = v.findViewById<View>(R.id.textView) as TextView
        textView.text = title
        val imageView: ImageView = v.findViewById<View>(R.id.imageView) as ImageView
        imageView.setImageResource(image_src)
        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        for (fragment in supportFragmentManager.fragments) {
            fragment?.onActivityResult(requestCode, resultCode, data);
        }
    }

    companion object {
        @JvmStatic
        fun goHome(context: Context) {
            val intent =
                Intent(context, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onClick(v: View?) {

    }
}