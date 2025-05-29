// AddExistingDeviceFragment.kt
package com.electricmonitor.mobile.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.electricmonitor.mobile.R
import com.electricmonitor.mobile.databinding.FragmentAddExistingDeviceBinding
import com.electricmonitor.mobile.ui.viewmodels.DeviceViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddExistingDeviceFragment : Fragment() {

    private var _binding: FragmentAddExistingDeviceBinding? = null
    private val binding get() = _binding!!

    private val deviceViewModel: DeviceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExistingDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.buttonAddDevice.setOnClickListener {
            addExistingDevice()
        }

        binding.buttonScanQR.setOnClickListener {
            // TODO: Implement QR code scanning
            Toast.makeText(requireContext(), "QR Scanner not implemented yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addExistingDevice() {
        val deviceId = binding.editTextDeviceId.text.toString().trim()
        val name = binding.editTextDeviceName.text.toString().trim().takeIf { it.isNotEmpty() }
        val description = binding.editTextDescription.text.toString().trim().takeIf { it.isNotEmpty() }

        // Validation
        clearErrors()
        var hasError = false

        if (deviceId.isEmpty()) {
            binding.textInputLayoutDeviceId.error = "Device ID is required"
            hasError = true
        } else if (!isValidDeviceId(deviceId)) {
            binding.textInputLayoutDeviceId.error = "Invalid Device ID format"
            hasError = true
        }

        if (hasError) return

        deviceViewModel.addExistingDevice(deviceId, name, description)
    }

    private fun isValidDeviceId(deviceId: String): Boolean {
        // Validate format: YYYYmmdd_UserID
        val regex = Regex("^\\d{8}_[0-9a-fA-F]{24}$")
        return regex.matches(deviceId)
    }

    private fun clearErrors() {
        binding.textInputLayoutDeviceId.error = null
        binding.textInputLayoutDeviceName.error = null
        binding.textInputLayoutDescription.error = null
    }

    private fun observeViewModel() {
        deviceViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonAddDevice.isEnabled = !isLoading
            binding.buttonScanQR.isEnabled = !isLoading
            setFieldsEnabled(!isLoading)
        }

        deviceViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                deviceViewModel.clearMessages()
            }
        }

        deviceViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                deviceViewModel.clearMessages()
                // Navigate back or refresh
                parentFragmentManager.popBackStack()
            }
        }

        // Observe password verification requirement
        deviceViewModel.needPasswordVerification.observe(viewLifecycleOwner) { verificationData ->
            verificationData?.let { (deviceId, name, description) ->
                showPasswordVerificationDialog(deviceId, name, description)
            }
        }
    }

    private fun showPasswordVerificationDialog(deviceId: String, name: String?, description: String?) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_password_verification, null)

        val deviceIdText = dialogView.findViewById<TextInputEditText>(R.id.editTextDeviceIdDisplay)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.editTextOwnerPassword)
        val passwordLayout = dialogView.findViewById<TextInputLayout>(R.id.textInputLayoutOwnerPassword)

        deviceIdText.setText(deviceId)
        deviceIdText.isEnabled = false

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Verify Device Access")
            .setMessage("This device belongs to another user. Please enter the device owner's password to gain access.")
            .setView(dialogView)
            .setPositiveButton("Add Device") { _, _ ->
                val ownerPassword = passwordInput.text.toString().trim()
                if (ownerPassword.isEmpty()) {
                    passwordLayout.error = "Owner password is required"
                    return@setPositiveButton
                }
                deviceViewModel.addSharedDevice(deviceId, name, description, ownerPassword)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                deviceViewModel.clearMessages()
            }
            .setCancelable(false)
            .create()

        dialog.show()

        // Enable positive button only when password is entered
        passwordInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    s.toString().trim().isNotEmpty()
                passwordLayout.error = null
            }
        })

        // Initially disable positive button
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        binding.editTextDeviceId.isEnabled = enabled
        binding.editTextDeviceName.isEnabled = enabled
        binding.editTextDescription.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}