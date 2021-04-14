package io.github.doorbash.ilb

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import io.github.doorbash.ilb.theme.Theme


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Theme.selected.themeRes)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        navController = findNavController(R.id.navhost_controller)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }
}