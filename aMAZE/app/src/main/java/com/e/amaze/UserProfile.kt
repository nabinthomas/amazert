package com.e.amaze

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.user_profile.*

class UserProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val uName = findViewById<TextView>(R.id.textView13)
        val eMail = findViewById<TextView>(R.id.textView15)
        val profileImage = findViewById<ImageView>(R.id.imageView)

        uName.text = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        eMail.text = FirebaseAuth.getInstance().currentUser?.email.toString()

        imageView.setImageDrawable(BitmapDrawable(resources, MyApplication.Companion.bitmap))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)

        val userProf = menu.findItem(R.id.miProfile)

        userProf.setIcon(BitmapDrawable(resources, MyApplication.Companion.bitmap))
        return true
    }

    fun onProfileAction(item: MenuItem) {

    }

    fun logoutCurrentUser(view: View) {
        val userName = FirebaseAuth.getInstance().currentUser
        Log.i("UP", "LOGGING OUT User: $userName")
        AuthUI.getInstance().signOut(this)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}