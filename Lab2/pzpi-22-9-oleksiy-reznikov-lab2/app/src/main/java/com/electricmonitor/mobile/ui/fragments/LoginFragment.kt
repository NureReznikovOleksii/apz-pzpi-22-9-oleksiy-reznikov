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
import com.electricmonitor.mobile.databinding.FragmentLoginBinding
import com.electricmonitor.mobile.ui.viewmodels.AuthState
import com.electricmonitor.mobile.ui.viewmodels.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()

        // Check if user is already logged in
        if (authViewModel.isLoggedIn()) {
            navigateToMain()
        }
    }

    private fun setupUI() {
        binding.apply {
            buttonLogin.setOnClickListener {
                login()
            }

            textViewRegister.setOnClickListener {
                // Navigate to register - update navigation action ID as needed
                try {
                    findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
                } catch (e: Exception) {
                    // Fallback navigation
                    try {
                        findNavController().navigate(R.id.registerFragment)
                    } catch (ex: Exception) {
                        Toast.makeText(requireContext(), "Registration feature coming soon", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            textViewForgotPassword.setOnClickListener {
                // Navigate to forgot password - update navigation action ID as needed
                try {
                    findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeViewModel() {
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.apply {
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                buttonLogin.isEnabled = !isLoading
                editTextIdentifier.isEnabled = !isLoading
                editTextPassword.isEnabled = !isLoading
            }
        }

        authViewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Success -> {
                    navigateToMain()
                }
                is AuthState.Error -> {
                    showError(state.message)
                }
                is AuthState.LoggedOut -> {
                    // Stay on login screen
                }
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
                authViewModel.clearError()
            }
        }
    }

    private fun login() {
        val identifier = binding.editTextIdentifier.text.toString().trim()
        val password = binding.editTextPassword.text.toString()

        // Clear any previous errors
        binding.textInputLayoutIdentifier.error = null
        binding.textInputLayoutPassword.error = null

        // Validate input
        if (identifier.isEmpty()) {
            binding.textInputLayoutIdentifier.error = "Email or username is required"
            return
        }

        if (password.isEmpty()) {
            binding.textInputLayoutPassword.error = "Password is required"
            return
        }

        authViewModel.login(identifier, password)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        Toast.makeText(requireContext(), "Login successful! Welcome!", Toast.LENGTH_SHORT).show()

        try {
            // Navigate to the devices fragment after successful login
            findNavController().navigate(R.id.action_loginFragment_to_devicesFragment)
        } catch (e: Exception) {
            // Fallback navigation if the action doesn't exist
            try {
                findNavController().navigate(R.id.devicesFragment)
            } catch (ex: Exception) {
                // If navigation fails, show error but don't crash
                Toast.makeText(requireContext(), "Navigation error: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}