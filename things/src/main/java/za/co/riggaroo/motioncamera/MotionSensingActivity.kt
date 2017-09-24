package za.co.riggaroo.motioncamera

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import za.co.riggaroo.motioncamera.camera.CustomCamera


class MotionSensingActivity : AppCompatActivity(), MotionSensor.MotionListener {

    private lateinit var ledGpio: Gpio
    private lateinit var camera: CustomCamera
    private lateinit var motionImageView: ImageView
    private lateinit var buttonArmSystem: Button
    private lateinit var motionViewModel: MotionSensingViewModel
    private lateinit var motionSensor: MotionSensor
    private var armed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motion_sensing)
        setTitle(R.string.app_name)
        setupUIElements()
        setupCamera()
        setupActuators()
        setupSensors()
        setupViewModel()
    }

    private fun setupViewModel() {
        motionViewModel = ViewModelProviders.of(this).get(MotionSensingViewModel::class.java)
    }

    private fun setupSensors() {
        motionSensor = MotionSensor(this, MOTION_SENSOR_GPIO_PIN)
        lifecycle.addObserver(motionSensor)
    }

    private fun setupActuators() {
        val peripheralManagerService = PeripheralManagerService()
        ledGpio = peripheralManagerService.openGpio(LED_GPIO_PIN)
        ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
    }


    private fun setupUIElements() {
        motionImageView = findViewById(R.id.image_view_motion)

        buttonArmSystem = findViewById(R.id.button_arm_disarm)
        buttonArmSystem.setOnClickListener {
            armed = !armed
            FirebaseDatabase.getInstance().getReference("system-armed").setValue(armed)
        }
        val systemArmed = FirebaseDatabase.getInstance().getReference("system-armed")
        systemArmed.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                armed = dataSnapshot.value as Boolean
                buttonArmSystem.text = if (armed) {
                    getString(R.string.disarm_system)
                } else {
                    getString(R.string.arm_system)
                }
            }
        })
    }

    private val imageAvailableListener = object : CustomCamera.ImageCapturedListener {
        override fun onImageCaptured(bitmap: Bitmap) {
            motionImageView.setImageBitmap(bitmap)
            motionViewModel.uploadMotionImage(bitmap)
        }
    }


    private fun setupCamera() {
        camera = CustomCamera.getInstance()
        camera.initializeCamera(this, Handler(), imageAvailableListener)
    }

    override fun onMotionDetected() {
        Log.d(ACT_TAG, "onMotionDetected")

        ledGpio.value = true

        if (armed) {
            camera.takePicture()
        }
    }

    override fun onMotionStopped() {
        Log.d(ACT_TAG, "onMotionStopped")
        ledGpio.value = false
    }


    companion object {
        val ACT_TAG: String = "MotionSensingActivity"
        val LED_GPIO_PIN = "GPIO_174"
        val MOTION_SENSOR_GPIO_PIN = "GPIO_35"
    }
}
