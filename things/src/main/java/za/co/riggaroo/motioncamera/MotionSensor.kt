package za.co.riggaroo.motioncamera

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService

/**
 * @author rebeccafranks
 * @since 2017/09/15.
 */
class MotionSensor(private val motionListener: MotionListener,
                   motionSensorPinNumber: String) : LifecycleObserver {

    private val motionSensorGpioPin: Gpio = PeripheralManagerService().openGpio(motionSensorPinNumber)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun start() {
        //Receive data from the sensor - DIRECTION_IN
        motionSensorGpioPin.setDirection(Gpio.DIRECTION_IN)
        //High voltage means movement has been detected
        motionSensorGpioPin.setActiveType(Gpio.ACTIVE_HIGH)
        //The trigger we want to receive both low and high triggers so EDGE_BOTH
        motionSensorGpioPin.setEdgeTriggerType(Gpio.EDGE_BOTH)
        motionSensorGpioPin.registerGpioCallback(object : GpioCallback() {
            override fun onGpioEdge(gpio: Gpio?): Boolean {
                gpio?.let {
                    if (gpio.value) {
                        motionListener.onMotionDetected()
                    } else {
                        motionListener.onMotionStopped()
                    }
                }
                return true
            }
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun stop() {
        motionSensorGpioPin.close()
    }

    interface MotionListener {
        fun onMotionDetected()
        fun onMotionStopped()
    }

}