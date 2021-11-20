package com.ft.mapp.home.adapters

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentTransaction

@SuppressLint("CommitTransaction")
class HomeViewpagerAdapter(val fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var mCurTransaction: FragmentTransaction? = null
    private var mCurrentPrimaryItem: Fragment? = null

    private val fragmentList = mutableListOf<Fragment>()
    private val tabTitleList = mutableListOf<String>()

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        if (position > tabTitleList.size - 1) {
            return "default$position"
        }
        return tabTitleList[position]
    }

    fun addFragment(fragment: Fragment) {
        fragmentList.add(fragment)
    }

    fun addTab(tab: String) {
        tabTitleList.add(tab)
        notifyDataSetChanged()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (this.mCurTransaction == null) {
            this.mCurTransaction = fm.beginTransaction()
        }

        val name = getPageTitle(position).toString()
        var fragment = fm.findFragmentByTag(name)
        if (fragment != null) {
            this.mCurTransaction?.show(fragment)
        } else {
            fragment = this.getItem(position)
            this.mCurTransaction?.add(container.id, fragment, name)
        }

        if (fragment !== this.mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false)
            fragment.userVisibleHint = false
        }

        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (this.mCurTransaction == null) {
            this.mCurTransaction = fm.beginTransaction()
        }
        this.mCurTransaction?.hide(`object` as Fragment)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        val fragment = `object` as Fragment
        if (fragment !== this.mCurrentPrimaryItem) {
            this.mCurrentPrimaryItem?.setMenuVisibility(false)
            this.mCurrentPrimaryItem?.userVisibleHint = false

            fragment.setMenuVisibility(true)
            fragment.userVisibleHint = true
            this.mCurrentPrimaryItem = fragment
        }
    }

    override fun finishUpdate(container: ViewGroup) {
        this.mCurTransaction?.commitNowAllowingStateLoss()
        this.mCurTransaction = null
    }
}