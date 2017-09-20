package za.co.riggaroo.motioncamera

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: LogsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val recyclerViewImages = findViewById<RecyclerView>(R.id.recyclerViewImages)

        recyclerViewImages.layoutManager = LinearLayoutManager(this)

        val databaseRef = FirebaseDatabase.getInstance().getReference("motion-logs")

        adapter = LogsAdapter(databaseRef)
        recyclerViewImages.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.cleanup()
    }
}


