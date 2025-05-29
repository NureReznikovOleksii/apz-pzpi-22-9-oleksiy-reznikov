package com.electricmonitor.mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.electricmonitor.mobile.data.network.NetworkModule
import com.electricmonitor.mobile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize NetworkModule
        NetworkModule.initialize(this)

        // Setup ActionBar first
        setupActionBar()

        // Then setup navigation
        setupNavigation()
    }

    private fun setupActionBar() {
        // Find toolbar in your layout and set it as action bar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
        } else {
            // If no toolbar in layout, enable default action bar
            supportActionBar?.show()
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Top-level destinations where the back button should not appear
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.devicesFragment,
                R.id.loginFragment
            )
        )

        // Only setup ActionBar with NavController if ActionBar exists
        supportActionBar?.let {
            setupActionBarWithNavController(navController, appBarConfiguration)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkModule.clearAuthToken(this)
    }
}