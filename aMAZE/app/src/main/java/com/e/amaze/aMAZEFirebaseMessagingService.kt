package com.e.amaze
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.logging.Handler


class aMAZEFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        //handleMessage(remoteMessage)

        Toast.makeText(baseContext, remoteMessage.toString(), Toast.LENGTH_LONG).show()
    }

}