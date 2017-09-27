package za.co.riggaroo.motioncamera

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

/**
 * @author rebeccafranks
 * @since 2017/09/27.
 */
class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}