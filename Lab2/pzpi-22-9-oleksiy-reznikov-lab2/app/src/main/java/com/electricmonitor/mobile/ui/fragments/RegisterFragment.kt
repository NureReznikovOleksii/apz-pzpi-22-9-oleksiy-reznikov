package com.electricmonitor.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.electricmonitor.mobile.R
import com.electricmonitor.mobile.databinding.FragmentRegisterBinding
import com.electricmonitor.mobile.ui.viewmodels.AuthState
import com.electricmonitor.mobile.ui.viewmodels.AuthViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.apply {
            buttonRegister.setOnClickListener {
                register()
            }

            textViewLogin.setOnClickListener {
                findNavController().popBackStack()
            }

            imageViewBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun observeViewModel() {
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.apply {
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                buttonRegister.isEnabled = !isLoading
                setFieldsEnabled(!isLoading)
            }
        }

        authViewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Success -> {
                    Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is AuthState.Error -> {
                    showError(state.message)
                }
                else -> {}
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
                authViewModel.clearError()
            }
        }
    }

    private fun register() {
        clearErrors()

        val username = binding.editTextUsername.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()
        val firstName = binding.editTextFirstName.text.toString().trim()
        val lastName = binding.editTextLastName.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim().takeIf { it.isNotEmpty() }

        // Basic validation
        var hasError = false

        if (username.isEmpty()) {
            binding.textInputLayoutUsername.error = "Username is required"
            hasError = true
        } else if (username.length < 3) {
            binding.textInputLayoutUsername.error = "Username must be at least 3 characters"
            hasError = true
        }

        if (email.isEmpty()) {
            binding.textInputLayoutEmail.error = "Email is required"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayoutEmail.error = "Please enter a valid email"
            hasError = true
        }

        if (password.isEmpty()) {
            binding.textInputLayoutPassword.error = "Password is required"
            hasError = true
        } else if (password.length < 6) {
            binding.textInputLayoutPassword.error = "Password must be at least 6 characters"
            hasError = true
        }

        if (confirmPassword.isEmpty()) {
            binding.textInputLayoutConfirmPassword.error = "Please confirm your password"
            hasError = true
        } else if (password != confirmPassword) {
            binding.textInputLayoutConfirmPassword.error = "Passwords do not match"
            hasError = true
        }

        if (firstName.isEmpty()) {
            binding.textInputLayoutFirstName.error = "First name is required"
            hasError = true
        }

        if (lastName.isEmpty()) {
            binding.textInputLayoutLastName.error = "Last name is required"
            hasError = true
        }

        if (hasError) return

        authViewModel.register(username, email, password, confirmPassword, firstName, lastName, phone)
    }

    private fun clearErrors() {
        binding.apply {
            textInputLayoutUsername.error = null
            textInputLayoutEmail.error = null
            textInputLayoutPassword.error = null
            textInputLayoutConfirmPassword.error = null
            textInputLayoutFirstName.error = null
            textInputLayoutLastName.error = null
            textInputLayoutPhone.error = null
        }
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        binding.apply {
            editTextUsername.isEnabled = enabled
            editTextEmail.isEnabled = enabled
            editTextPassword.isEnabled = enabled
            editTextConfirmPassword.isEnabled = enabled
            editTextFirstName.isEnabled = enabled
            editTextLastName.isEnabled = enabled
            editTextPhone.isEnabled = enabled
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        // For now, just show a success message and go back to login
        Toast.makeText(requireContext(), "Registration successful! Please log in.", Toast.LENGTH_LONG).show()
        findNavController().popBackStack()

        // You can add proper navigation here once you set up your navigation graph:
        // findNavController().navigate(R.id.mainFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}