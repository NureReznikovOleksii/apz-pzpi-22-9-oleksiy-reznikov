package com.electricmonitor.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.electricmonitor.mobile.databinding.FragmentAddDeviceBinding
import com.electricmonitor.mobile.ui.adapters.AddDevicePagerAdapter
import com.electricmonitor.mobile.ui.viewmodels.DeviceViewModel
import com.google.android.material.tabs.TabLayoutMediator

class AddDeviceFragment : Fragment() {

    private var _binding: FragmentAddDeviceBinding? = null
    private val binding get() = _binding!!

    private val deviceViewModel: DeviceViewModel by viewModels()
    private lateinit var pagerAdapter: AddDevicePagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupViewPager()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupViewPager() {
        pagerAdapter = AddDevicePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Create New"
                1 -> "Add Existing"
                else -> "Tab $position"
            }
        }.attach()
    }

    private fun observeViewModel() {
        deviceViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                deviceViewModel.clearMessages()
                findNavController().popBackStack()
            }
        }

        deviceViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                deviceViewModel.clearMessages()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

