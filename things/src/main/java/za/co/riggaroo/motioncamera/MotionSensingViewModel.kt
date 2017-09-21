package za.co.riggaroo.motioncamera

import android.arch.lifecycle.ViewModel
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

/**
 * @author rebeccafranks
 * @since 2017/09/21.
 */
class MotionSensingViewModel : ViewModel() {


    fun uploadMotionImage(imageBytes: ByteArray) {
        val storageRef = FirebaseStorage.getInstance().getReference(FIREBASE_MOTION_REF)
        val riversRef = storageRef.child(FIREBASE_IMAGE_PREFIX + System.currentTimeMillis() + ".jpg")
        val uploadTask = riversRef.putBytes(imageBytes)

        uploadTask.addOnFailureListener {
            Log.d(TAG, "onFailure uploadMotionImage")
        }.addOnSuccessListener { taskSnapshot ->
            Log.d(TAG, "onSuccess uploadMotionImage")
            val downloadUrl = taskSnapshot.downloadUrl
            val ref = FirebaseDatabase.getInstance().getReference(FIREBASE_MOTION_LOGS).push()
            ref.setValue(FirebaseImageLog(System.currentTimeMillis(), downloadUrl.toString()))
        }

    }

    companion object {
        private val FIREBASE_MOTION_REF = "motion"
        private val FIREBASE_MOTION_LOGS = "motion-logs"
        private val FIREBASE_IMAGE_PREFIX = "images/motion_img_"
        private val TAG = "MotionSensingViewModel"
    }
}