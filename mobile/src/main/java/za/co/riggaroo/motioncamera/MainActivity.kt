package za.co.riggaroo.motioncamera

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: LogsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setupMotionLogsRecyclerView()
        setupArmSystemToggle()
    }

    private val ACT_TAG: String = "MainActivity"

    private fun setupArmSystemToggle() {
        val armSystemToggleButton = findViewById<SwitchCompat>(R.id.switch_arm_system)
        armSystemToggleButton.setOnCheckedChangeListener { _, checkedValue ->
            FirebaseDatabase.getInstance().getReference("system-armed").setValue(checkedValue)
        }
        val armedValue = FirebaseDatabase.getInstance().getReference("system-armed")
        armedValue.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(ACT_TAG, "onDataChange:" + dataSnapshot.toString())
                val isArmed = dataSnapshot.value as Boolean
                armSystemToggleButton.isChecked = isArmed
                armSystemToggleButton.text = if (isArmed) {
                    getString(R.string.system_armed)
                } else {
                    getString(R.string.system_unarmed)
                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }

        })
    }

    private fun setupMotionLogsRecyclerView() {
        val recyclerViewImages = findViewById<RecyclerView>(R.id.recyclerViewImages)

        val databaseRef = FirebaseDatabase.getInstance().getReference("motion-logs")

        adapter = LogsAdapter(databaseRef.orderByChild("timestamp").ref)
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
}


