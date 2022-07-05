package khusmanda.assignment3.mytracker
// need to call file writing
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    // private fields of the class
    private lateinit var _button: Button
    private lateinit var _stop_button: Button
    private lateinit var startJour: TextView
    private lateinit var stopJour: TextView
    private lateinit var startIns: TextView
    private lateinit var stopIns: TextView
    private lateinit var _linear_layout: LinearLayout
    private lateinit var _lm: LocationManager


    var arrayPermission = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private var mylocations: Calculate? = null
    private var myfileWriter: GPSFile? = null
    private val filePath = "GPSTracks"
    private var mygpsFile: File? = null
    private val context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _linear_layout = findViewById<LinearLayout>(R.id.linear_layout)
        _lm = getSystemService(LOCATION_SERVICE) as LocationManager
        _button = findViewById<Button>(R.id.button)
        _stop_button = findViewById<Button>(R.id.stop_button)
        // get access to all of the views in our UI
        startJour = findViewById<TextView>(R.id.startjourney)
        startIns = findViewById<TextView>(R.id.startInstruction)
        stopJour = findViewById<TextView>(R.id.stopJorney)
        stopIns = findViewById<TextView>(R.id.stopInstruction)
        startIns.setText("To start recording, click on the start button.")
        stopIns.setText("To stop recording, click on the stop button.")

        myfileWriter = GPSFile()
        mylocations = Calculate(this, this)

        // add a listener to the button that will add a location listener to the activity
        _button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                startRecording()
            }
        })

        _stop_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                stopRecording()
            }
        })
        //ask for permissions required
        if (!PermissionCheck(this, *arrayPermission)) {
            checkPermissionRequest()
        }
    }

    //stop location listener
    private fun stopRecording() {
        mylocations!!.stopLocationListener()
        myrunTime = System.currentTimeMillis() - mystartTime
        val mydate: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        mygpsFile = File(getExternalFilesDir(filePath), mydate.format(Date()) + ".xml")
        myfileWriter!!.writePath(mygpsFile!!, mylocations!!, context, this)
        var intent: Intent = Intent(this, ReportActivity::class.java)
        startActivity(intent)

    }

    fun startRecording() {

        mylocations!!.addLocationListener()
        myrunTime = 0
        mystartTime = System.currentTimeMillis()
        Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show()
    }


    // ---------------------------Request permission -------------------------------------
    fun checkPermissionRequest() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    + ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    + ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            ))
            != PackageManager.PERMISSION_GRANTED
        ) {

            // permissions are not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
            ) {

                val builder = AlertDialog.Builder(
                    this
                )
                builder.setMessage("App will not work without this permissions.")
                builder.setTitle("Permissions")
                builder.setPositiveButton("Allow") { dialogInterface, i ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayPermission,
                        request_all
                    )
                }
                builder.setNeutralButton("Cancel", null)
                val dialog = builder.create()
                dialog.show()
            } else {
                // request for permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayPermission,
                    request_all
                )
            }
        } else {
            // permission granted statement
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {

        var mystartTime: Long = 0
        var myrunTime: Long = 0
        const val request_all = 1
        fun PermissionCheck(context: Context?, vararg permissions: String?): Boolean {
            if (context != null && permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission!!) != PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                }
            }
            return true
        }

    }

}