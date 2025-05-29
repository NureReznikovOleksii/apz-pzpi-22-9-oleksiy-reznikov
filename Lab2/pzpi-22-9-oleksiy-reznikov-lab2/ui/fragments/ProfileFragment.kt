package com.electricmonitor.mobile.ui.fragments

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
// import com.electricmonitor.mobile.BuildConfig
import com.electricmonitor.mobile.R
import com.electricmonitor.mobile.data.models.Device
import com.electricmonitor.mobile.data.models.User
import com.electricmonitor.mobile.databinding.FragmentProfileBinding
import com.electricmonitor.mobile.ui.viewmodels.AuthState
import com.electricmonitor.mobile.ui.viewmodels.AuthViewModel
import com.electricmonitor.mobile.ui.viewmodels.DeviceViewModel
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private val deviceViewModel: DeviceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
        loadUserProfile()
        loadDevices()
    }

    private fun setupUI() {
        binding.apply {
            // Toolbar
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            layoutLogout.setOnClickListener {
                showLogoutConfirmation()
            }

            // App version
            try {
                val packageInfo = requireContext().packageManager.getPackageInfo(
                    requireContext().packageName, 0
                )
                textViewAppVersion.text = "Version: ${packageInfo.versionName}"
            } catch (e: PackageManager.NameNotFoundException) {
                textViewAppVersion.text = "Version: 1.0.0"
            }
        }
    }

    private fun observeViewModel() {
        // Observe user profile
        authViewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let { updateProfileUI(it) }
        }

        // Observe auth state for logout
        authViewModel.loginState.observe(viewLifecycleOwner) { state ->
            if (state is AuthState.LoggedOut) {
                // Navigate back to login - clear back stack and restart activity
                requireActivity().finish()
                requireActivity().startActivity(requireActivity().intent)
            }
        }

        // Observe devices for statistics
        deviceViewModel.devices.observe(viewLifecycleOwner) { devices ->
            updateStatistics(devices?.size ?: 0)
        }

        // Observe loading state
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        authViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                authViewModel.clearError()
            }
        }
    }

    private fun loadUserProfile() {
        authViewModel.loadUserProfile()
    }

    private fun loadDevices() {
        deviceViewModel.loadDevices()
    }

    private fun updateProfileUI(user: User) {
        binding.apply {
            // User info
            textViewUserName.text = "${user.firstName} ${user.lastName}"
            textViewUserEmail.text = user.email

            // Member since
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = dateFormat.parse(user.createdAt)
                val memberSinceFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                textViewMemberSince.text = "Member since ${memberSinceFormat.format(date)}"
            } catch (e: Exception) {
                textViewMemberSince.text = "Member since ${user.createdAt.substring(0, 10)}"
            }
        }
    }

    private fun updateStatistics(devicesCount: Int) {
        binding.apply {
            textViewDevicesCount.text = devicesCount.toString()

            // TODO: Load actual alerts count and consumption data
            textViewAlertsCount.text = "0"
            textViewTotalConsumption.text = "0 kWh"
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                authViewModel.logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showAboutDialog() {
        val versionName = try {
            requireContext().packageManager.getPackageInfo(
                requireContext().packageName, 0
            ).versionName
        } catch (e: Exception) {
            "1.0.0"
        }

        val aboutMessage = """
            Electric Monitor Mobile App
            Version: $versionName
            
            Monitor and control your electricity consumption with ease.
            
            Features:
            • Real-time power monitoring
            • Device control and management
            • Power consumption analytics
            • Smart alerts and notifications
            
            Developed with ❤️ for efficient energy management.
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("About")
            .setMessage(aboutMessage)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh profile data when returning to this fragment
        loadUserProfile()
        loadDevices()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}