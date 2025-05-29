// CreateDeviceFragment.kt
package com.electricmonitor.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.electricmonitor.mobile.data.models.DeviceLocation
import com.electricmonitor.mobile.databinding.FragmentCreateDeviceBinding
import com.electricmonitor.mobile.ui.viewmodels.DeviceViewModel

class CreateDeviceFragment : Fragment() {

    private var _binding: FragmentCreateDeviceBinding? = null
    private val binding get() = _binding!!

    private val deviceViewModel: DeviceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.buttonCreateDevice.setOnClickListener {
            createDevice()
        }
    }

    private fun createDevice() {
        val name = binding.editTextDeviceName.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim().takeIf { it.isNotEmpty() }
        val maxPower = binding.editTextMaxPower.text.toString().toDoubleOrNull() ?: 500.0
        val city = binding.editTextCity.text.toString().trim().takeIf { it.isNotEmpty() }
        val country = binding.editTextCountry.text.toString().trim().takeIf { it.isNotEmpty() }

        // Validation
        clearErrors()
        var hasError = false

        if (name.isEmpty()) {
            binding.textInputLayoutDeviceName.error = "Device name is required"
            hasError = true
        }

        if (maxPower <= 0) {
            binding.textInputLayoutMaxPower.error = "Power limit must be greater than 0"
            hasError = true
        }

        if (hasError) return

        val location = if (city != null || country != null) {
            DeviceLocation(city = city, country = country)
        } else null

        deviceViewModel.createDevice(name, description, maxPower, location)
    }

    private fun clearErrors() {
        binding.textInputLayoutDeviceName.error = null
        binding.textInputLayoutDescription.error = null
        binding.textInputLayoutMaxPower.error = null
        binding.textInputLayoutCity.error = null
        binding.textInputLayoutCountry.error = null
    }

    private fun observeViewModel() {
        deviceViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonCreateDevice.isEnabled = !isLoading
            setFieldsEnabled(!isLoading)
        }

        deviceViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        binding.editTextDeviceName.isEnabled = enabled
        binding.editTextDescription.isEnabled = enabled
        binding.editTextMaxPower.isEnabled = enabled
        binding.editTextCity.isEnabled = enabled
        binding.editTextCountry.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}