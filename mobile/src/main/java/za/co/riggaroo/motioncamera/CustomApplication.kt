package za.co.riggaroo.motioncamera

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging

/**
 * @author rebeccafranks
 * @since 2017/09/22.
 */
class CustomApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/intruders")
    }
}