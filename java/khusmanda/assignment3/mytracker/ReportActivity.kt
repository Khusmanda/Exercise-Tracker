package khusmanda.assignment3.mytracker

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import khusmanda.assignment3.mytracker.MainActivity.Companion.PermissionCheck
import java.util.concurrent.TimeUnit

class ReportActivity : AppCompatActivity() {
    private lateinit var _distance: TextView
    private lateinit var _speed: TextView
    private lateinit var _time: TextView
    private lateinit var _maxAltitude: TextView
    private lateinit var _minAltitude: TextView
    private lateinit var _button: Button
    private var mylocations: Calculate? = null
    private var myFileWriter: GPSFile? = null
    private val filePath = "GPSTracks"
    var arrayPermission = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        _distance = findViewById<TextView>(R.id.distance)
        _speed = findViewById<TextView>(R.id.speed)
        _time = findViewById<TextView>(R.id.time)
        _maxAltitude = findViewById<TextView>(R.id.max_altitude)
        _minAltitude = findViewById<TextView>(R.id.min_altitude)
        myFileWriter = GPSFile()
        mylocations = Calculate(this, this)
        _button = findViewById<Button>(R.id.reset)

        // add a listener to the button that will add a location listener to the activity
        _button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                startAgain()
            }
        })

        if (!PermissionCheck(this, *arrayPermission)) {
            ActivityCompat.requestPermissions(this, arrayPermission, 1)
        } else {
            getValues()
        }

    }

    fun getValues(){
        val mypath = getExternalFilesDir(filePath)?.absolutePath
        val values = myFileWriter!!.readerGPX(mypath.toString())
        _distance.setText("Distance: " + values.overallDistance)
        _maxAltitude.setText("Max Altitude: " + values.altitudeMax() + " m")
        _minAltitude.setText("Min Altitude: "+ values.altitudeMin() + " m")
        _time.setText("Time: " + convert(MainActivity.myrunTime))
        _speed.setText("Avg: " + values.overallSpeed + " Km/h")
        // plot graph
        val mygraph: MyGraph = findViewById(R.id.graph)
        val graphPoints = values.graphPoints()
        for (i in graphPoints.indices) {
            graphPoints[i] = graphPoints[i] / 10
        }
        mygraph.graphDetail(graphPoints)

    }

    fun startAgain(){
        var intent: Intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    fun convert(miliSeconds: Long): String {
        val hrs = TimeUnit.MILLISECONDS.toHours(miliSeconds).toInt() % 24
        val min = TimeUnit.MILLISECONDS.toMinutes(miliSeconds).toInt() % 60
        val sec = TimeUnit.MILLISECONDS.toSeconds(miliSeconds).toInt() % 60
        return String.format("%02d:%02d:%02d", hrs, min, sec)
    }



}