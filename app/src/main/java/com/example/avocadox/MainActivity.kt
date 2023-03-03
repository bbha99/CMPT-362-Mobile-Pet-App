package com.example.avocadox

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.example.avocadox.Util.checkPermissions
import com.example.avocadox.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.appBarStart.toolbar)

        val navController = this.findNavController(R.id.navHostFragment)
        drawerLayout = binding.drawerLayout
        toolbar = binding.appBarStart.toolbar
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_chats, R.id.nav_personal_moments, R.id.nav_map, R.id.nav_bookmark_history, R.id.nav_profile_user
            ), drawerLayout
        )
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(binding.navView, navController)

        checkPermissions(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            mAuth.signOut()
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, LogInActivity::class.java)
            finish()
            startActivity(intent)
        }
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHostFragment)
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    //Functions to lock/unlock drawer when navigating to the Detail Fragment (where drawer should not be accessible)
    fun lockDrawer(){
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toolbar.navigationIcon = null
    }

    fun unlockDrawer(){
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        val hamburgerIcon = DrawerArrowDrawable(this)
        hamburgerIcon.color = Color.BLACK
        toolbar.navigationIcon = hamburgerIcon
    }
}