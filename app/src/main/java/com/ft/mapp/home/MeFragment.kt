package com.ft.mapp.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ft.mapp.R
import com.ft.mapp.widgets.MineRowView

class MeFragment : Fragment() {
    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_me, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = MeFragment()
    }

    private fun bindViews(view: View) {
        view.findViewById<MineRowView>(R.id.setting_about)?.setOnClickListener {
            startActivity(Intent(activity, AboutActivity::class.java))
        }

        view.findViewById<MineRowView>(R.id.setting_single)?.setOnClickListener {
            startActivity(Intent(activity, AppLaunchConfigActivity::class.java))
        }
    }
}
