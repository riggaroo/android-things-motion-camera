package za.co.riggaroo.motioncamera

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.widget.ImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: LogsAdapter
    private lateinit var armSystemToggleButton: SwitchCompat
    private lateinit var armSystemImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupMotionLogsRecyclerView()
        setupArmSystemToggle()
    }

    private fun setupArmSystemToggle() {
        armSystemToggleButton = findViewById(R.id.switch_arm_system)
        armSystemImageView = findViewById(R.id.image_view_arm_system)
        val armedValue = FirebaseDatabase.getInstance().getReference(SYSTEM_ARMED_STATUS_FIREBASE_REF)

        armSystemToggleButton.setOnCheckedChangeListener { _, checkedValue ->
            armedValue.setValue(checkedValue)
        }

        armedValue.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(ACT_TAG, "onDataChange:" + dataSnapshot.toString())
                val isArmed = dataSnapshot.value as Boolean
                toggleUIState(isArmed)
            }

            override fun onCancelled(p0: DatabaseError?) {

            }

        })
    }

    private fun toggleUIState(isArmed: Boolean) {
        armSystemToggleButton.isChecked = isArmed
        armSystemToggleButton.text = if (isArmed) {
            getString(R.string.system_armed)
        } else {
            getString(R.string.system_unarmed)
        }
        val armedImageResource = if (isArmed) {
            R.drawable.ic_armed
        } else {
            R.drawable.ic_not_armed
        }
        armSystemImageView.setImageResource(armedImageResource)

    }

    private fun setupMotionLogsRecyclerView() {
        val recyclerViewImages = findViewById<RecyclerView>(R.id.recyclerViewImages)
        recyclerViewImages.isNestedScrollingEnabled = false
        val databaseRef = FirebaseDatabase.getInstance().getReference(MOTION_LOGS_FIREBASE_REF)

        adapter = LogsAdapter(databaseRef.orderByChild(ORDER_BY_TIMESTAMP).ref)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerViewImages.layoutManager = linearLayoutManager
        recyclerViewImages.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.cleanup()
    }

    companion object {
        private val ORDER_BY_TIMESTAMP = "timestamp"
        private val ACT_TAG: String = "MainActivity"
        private val MOTION_LOGS_FIREBASE_REF = "motion-logs"
        private val SYSTEM_ARMED_STATUS_FIREBASE_REF = "system-armed"
    }


}


