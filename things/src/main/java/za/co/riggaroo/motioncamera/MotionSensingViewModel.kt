package za.co.riggaroo.motioncamera

import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class MotionSensingViewModel : ViewModel() {

    var armed = SingleLiveEvent<Boolean>()
    private var systemArmedFirebaseReference = FirebaseDatabase.getInstance().getReference(FIREBASE_ARM_SYSTEM_NODE)

    init {
        systemArmedFirebaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val systemArmed = dataSnapshot.value as Boolean
                armed.value = systemArmed
            }
        })
    }

    fun uploadMotionImage(imageBytes: Bitmap) {
        armed.value?.let { isSystemArmed ->
            if (isSystemArmed) {
                val storageRef = FirebaseStorage.getInstance().getReference(FIREBASE_MOTION_REF)
                val imageStorageRef = storageRef.child(FIREBASE_IMAGE_PREFIX + System.currentTimeMillis() + ".jpg")
                val stream = ByteArrayOutputStream()
                imageBytes.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val uploadTask = imageStorageRef.putBytes(stream.toByteArray())

                uploadTask.addOnFailureListener {
                    Log.d(TAG, "onFailure uploadMotionImage")
                }.addOnSuccessListener {
                    Log.d(TAG, "onSuccess uploadMotionImage")
                    val downloadUrl = imageStorageRef.path
                    val ref = FirebaseDatabase.getInstance().getReference(FIREBASE_MOTION_LOGS).push()
                    ref.setValue(FirebaseImageLog(System.currentTimeMillis(), downloadUrl))
                }
            }
        }


    }

    companion object {
        private val FIREBASE_MOTION_REF = "motion"
        private val FIREBASE_MOTION_LOGS = "motion-logs"
        private val FIREBASE_IMAGE_PREFIX = "images/motion_img_"
        private val TAG = "MotionSensingViewModel"
        private val FIREBASE_ARM_SYSTEM_NODE = "system-armed"
    }

    fun toggleSystemArmedStatus() {

    }
}