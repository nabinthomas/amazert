package com.example.fireamaze

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    companion object {
        private const val TAG = "SANS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance();
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.i("SANS", user.toString() + " logged IN........")
    }

    fun launchUserSignin(view: View) {
        val providers = arrayListOf(
            //AuthUI.IdpConfig.EmailBuilder().build(),
            //AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in.
                Log.i("MSG", "Successfully signed in user DisplayName: ${FirebaseAuth.getInstance().currentUser?.displayName}!")
                Log.i("MSG", "                                  Email: ${FirebaseAuth.getInstance().currentUser?.email}!")
                Log.i("MSG", "                                    UID: ${FirebaseAuth.getInstance().currentUser?.uid}!")
                Log.i("MSG", "                               TenantId: ${FirebaseAuth.getInstance().currentUser?.tenantId}!")
                Log.i("MSG", "                             ProviderId: ${FirebaseAuth.getInstance().currentUser?.providerId}!")
                Log.i("MSG", "                            PhoneNumber: ${FirebaseAuth.getInstance().currentUser?.phoneNumber}!")

                Toast.makeText(this,
                    "Successfully signed in customer ${FirebaseAuth.getInstance().currentUser?.displayName}!",
                    Toast.LENGTH_SHORT).show()
                launchFirebaseIO()
            } else {
                // Sign in failed. If response is null, the user canceled the
                // sign-in flow using the back button. Otherwise, check
                // the error code and handle the error.
                Log.i("MSG", "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    fun launchFirebaseIO(){
        val intent = Intent(this, firebaseio::class.java)
        startActivity(intent)
    }

    fun logoutUser(view: View) {
        val userName = FirebaseAuth.getInstance().currentUser?.displayName
        Log.i("SANS", "LOGGING OUT User: $userName")
        AuthUI.getInstance().signOut(this)
    }
}