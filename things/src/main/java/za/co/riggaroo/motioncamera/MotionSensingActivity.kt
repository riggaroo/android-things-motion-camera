package za.co.riggaroo.motioncamera

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import za.co.riggaroo.motioncamera.camera.CustomCamera


class MotionSensingActivity : AppCompatActivity(), MotionSensor.MotionListener {

    private lateinit var ledGpio: Gpio
    private lateinit var camera: CustomCamera
    private lateinit var motionImageView: ImageView
    private lateinit var motionViewModel: MotionSensingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motion_sensing)

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
        lifecycle.addObserver(MotionSensor(this, MOTION_SENSOR_GPIO_PIN))
    }

    private fun setupActuators() {
        val peripheralManagerService = PeripheralManagerService()
        ledGpio = peripheralManagerService.openGpio(LED_GPIO_PIN)
        ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
    }

    private fun setupUIElements() {
        motionImageView = findViewById(R.id.image_view_motion)
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage()
        val imageBuffer = image.planes[0].buffer
        val imageBytes = ByteArray(imageBuffer.remaining())
        imageBuffer.get(imageBytes)
        image.close()
        val bitmap = getBitmapFromByteArray(imageBytes)
        motionImageView.setImageBitmap(bitmap)
        motionViewModel.uploadMotionImage(imageBytes)
    }


    private fun setupCamera() {
        camera = CustomCamera.getInstance()
        camera.initializeCamera(this, Handler(), onImageAvailableListener)
    }

    override fun onMotionDetected() {
        Log.d(ACT_TAG, "onMotionDetected")

        camera.takePicture()
        ledGpio.value = true
    }

    override fun onMotionStopped() {
        Log.d(ACT_TAG, "onMotionStopped")
        ledGpio.value = false
    }


    private fun getBitmapFromByteArray(imageBytes: ByteArray): Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        matrix.postRotate(180f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    companion object {
        val ACT_TAG: String = "MotionSensingActivity"
        val LED_GPIO_PIN = "GPIO_174"
        val MOTION_SENSOR_GPIO_PIN = "GPIO_35"
    }
}
