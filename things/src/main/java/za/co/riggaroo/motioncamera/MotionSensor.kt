package za.co.riggaroo.motioncamera

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback

/**
 * @author rebeccafranks
 * @since 2017/09/15.
 */
class MotionSensor(private val motionListener: MotionListener,
                   private val motionSensorGpioPin: Gpio) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun start() {
        motionSensorGpioPin.setDirection(Gpio.DIRECTION_IN)
        motionSensorGpioPin.setActiveType(Gpio.ACTIVE_HIGH)
        motionSensorGpioPin.setEdgeTriggerType(Gpio.EDGE_RISING)
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