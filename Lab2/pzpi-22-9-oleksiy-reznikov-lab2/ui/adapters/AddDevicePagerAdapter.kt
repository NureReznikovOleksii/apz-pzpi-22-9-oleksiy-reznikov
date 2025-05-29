package com.electricmonitor.mobile.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.electricmonitor.mobile.ui.fragments.AddExistingDeviceFragment
import com.electricmonitor.mobile.ui.fragments.CreateDeviceFragment

class AddDevicePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CreateDeviceFragment()
            1 -> AddExistingDeviceFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}