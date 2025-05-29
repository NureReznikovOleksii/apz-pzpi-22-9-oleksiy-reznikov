package com.electricmonitor.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.electricmonitor.mobile.R
import com.electricmonitor.mobile.databinding.FragmentDevicesBinding
import com.electricmonitor.mobile.ui.adapters.DevicesAdapter
import com.electricmonitor.mobile.ui.viewmodels.AuthViewModel
import com.electricmonitor.mobile.ui.viewmodels.DeviceViewModel

class DevicesFragment : Fragment() {

    private var _binding: FragmentDevicesBinding? = null
    private val binding get() = _binding!!

    private val deviceViewModel: DeviceViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var devicesAdapter: DevicesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupUI()
        observeViewModel()

        // Load devices
        deviceViewModel.loadDevices()
    }

    private fun setupRecyclerView() {
        devicesAdapter = DevicesAdapter { device ->
            // Navigate to device details
            try {
                val action = DevicesFragmentDirections.actionDevicesFragmentToDeviceDetailFragment(device.deviceId)
                findNavController().navigate(action)
            } catch (e: Exception) {
                // Fallback navigation if action class doesn't exist
                try {
                    val bundle = Bundle().apply {
                        putString("deviceId", device.deviceId)
                    }
                    findNavController().navigate(R.id.deviceDetailFragment, bundle)
                } catch (ex: Exception) {
                    Toast.makeText(requireContext(), "Navigation error: ${ex.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.recyclerViewDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = devicesAdapter
        }
    }

    private fun setupUI() {
        binding.apply {
            // Setup toolbar
            toolbar.inflateMenu(R.menu.menu_devices)
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_profile -> {
                        try {
                            findNavController().navigate(R.id.action_devicesFragment_to_profileFragment)
                        } catch (e: Exception) {
                            try {
                                findNavController().navigate(R.id.profileFragment)
                            } catch (ex: Exception) {
                                Toast.makeText(requireContext(), "Profile feature coming soon", Toast.LENGTH_SHORT).show()
                            }
                        }
                        true
                    }
                    R.id.action_refresh -> {
                        deviceViewModel.loadDevices(refresh = true)
                        true
                    }
                    R.id.action_logout -> {
                        authViewModel.logout()
                        true
                    }
                    else -> false
                }
            }

            // Floating action button for adding devices
            fabAddDevice.setOnClickListener {
                try {
                    findNavController().navigate(R.id.action_devicesFragment_to_addDeviceFragment)
                } catch (e: Exception) {
                    try {
                        findNavController().navigate(R.id.addDeviceFragment)
                    } catch (ex: Exception) {
                        Toast.makeText(requireContext(), "Add device feature coming soon", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Pull to refresh
            swipeRefreshLayout.setOnRefreshListener {
                deviceViewModel.loadDevices(refresh = true)
            }

            // Configure SwipeRefreshLayout colors
            swipeRefreshLayout.setColorSchemeResources(
                R.color.primary_color,
                R.color.secondary_color
            )
        }
    }

    private fun observeViewModel() {
        // Observe devices
        deviceViewModel.devices.observe(viewLifecycleOwner) { devices ->
            devices?.let {
                devicesAdapter.submitList(it)
                updateEmptyState(it.isEmpty())
            }
        }

        // Observe loading state
        deviceViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            isLoading?.let { loading ->
                // Use safe null check instead of !!
                val isRefreshing = deviceViewModel.isRefreshing.value ?: false
                if (!isRefreshing) {
                    binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                }
            }
        }

        // Observe refreshing state
        deviceViewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            isRefreshing?.let {
                binding.swipeRefreshLayout.isRefreshing = it
            }
        }

        // Observe error messages
        deviceViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
                deviceViewModel.clearMessages()
            }
        }

        // Observe success messages
        deviceViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                deviceViewModel.clearMessages()
            }
        }

        // Observe auth state for logout
        authViewModel.loginState.observe(viewLifecycleOwner) { state ->
            if (state is com.electricmonitor.mobile.ui.viewmodels.AuthState.LoggedOut) {
                try {
                    findNavController().navigate(R.id.action_devicesFragment_to_loginFragment)
                } catch (e: Exception) {
                    try {
                        findNavController().navigate(R.id.loginFragment)
                    } catch (ex: Exception) {
                        // If navigation fails, restart the activity
                        requireActivity().finish()
                        requireActivity().startActivity(requireActivity().intent)
                    }
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            // Use safe null checks instead of !!
            val isLoading = deviceViewModel.isLoading.value ?: false
            val isRefreshing = deviceViewModel.isRefreshing.value ?: false

            if (isEmpty && !isLoading && !isRefreshing) {
                emptyStateContainer.visibility = View.VISIBLE
                recyclerViewDevices.visibility = View.GONE
            } else {
                emptyStateContainer.visibility = View.GONE
                recyclerViewDevices.visibility = View.VISIBLE
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh devices when returning to this fragment
        deviceViewModel.loadDevices(refresh = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}