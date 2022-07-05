package khusmanda.assignment3.mytracker

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import khusmanda.assignment3.mytracker.MainActivity.Companion.PermissionCheck
import org.xml.sax.SAXException
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class GPSFile {
    var arrayPermission = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val dateFormat = SimpleDateFormat("MM-dd-yyyy HH:mm:ss")
    private val calendar = Calendar.getInstance()
    private val date = dateFormat.format(calendar.time).replace(" ","").trim()
    private val file_name = "$date.gpx"
    private val pathtoSD: File
    private val myfolder: File
    private val gpxFile: File

    fun readerGPX(mypath: String): Calculate {
        val calculate: Calculate
        val list: MutableList<Location> = ArrayList()
        var distance = 0.0
        var speed= 0.0
        var averageSpeed = 0.0
        var minAltitude = 0.0
        var maxAltitude = 0.0

        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        try {
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()
            val fileInputStream = FileInputStream(lastFileModified(mypath))
            val document = documentBuilder.parse(fileInputStream)
            val elementRoot = document.documentElement
            val nodelist_trkpt = elementRoot.getElementsByTagName("trkpt")
            val nodelist_ele = elementRoot.getElementsByTagName("ele")
            distance = elementRoot.getElementsByTagName("distance").item(0).textContent.toFloat()
                .toDouble()
            averageSpeed =
                elementRoot.getElementsByTagName("averagespeed").item(0).textContent.toFloat()
                    .toDouble()
            minAltitude =
                elementRoot.getElementsByTagName("minaltitude").item(0).textContent.toDouble()
            maxAltitude =
                elementRoot.getElementsByTagName("maxaltitude").item(0).textContent.toDouble()

            for (i in 0 until nodelist_trkpt.length) {
                val node = nodelist_trkpt.item(i)
                val attributes = node.attributes
                val latitude = attributes.getNamedItem("lat").textContent.toDouble()
                val longitude = attributes.getNamedItem("lon").textContent.toDouble()
                val myspeed = attributes.getNamedItem("speed").textContent.toDouble()
                val altitude = nodelist_ele.item(i).textContent.toDouble()
                val location = Location("GPX $i")
                location.latitude = latitude
                location.longitude = longitude
                location.altitude = altitude
                location.speed = myspeed.toFloat()
                list.add(location)
            }
            fileInputStream.close()
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        calculate = Calculate(list, distance, speed, averageSpeed, minAltitude, maxAltitude,)
        return calculate
    }

    fun writePath(file: File, calculate: Calculate,  context: Context?, mainActivity: MainActivity?) {
        val header =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n"
        val distance = """
             <distance>${calculate.distance}</distance>
             """.trimIndent()
        val time = """
             <time>${MainActivity.myrunTime}</time>
             """.trimIndent()

        val averageSpeed = """
             <averagespeed>${calculate.averageSpeed}</averagespeed>
             """.trimIndent()

        val minAltitude = """
             <minaltitude>${calculate.altitudeMin()}</minaltitude> 
             """.trimIndent()

        val maxAltitude = """
             <maxaltitude>${calculate.altitudeMax()}</maxaltitude>
             """.trimIndent()

        var myPoints = "<trkseg>\n"
        val simpleDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        for (location in calculate.locationList) {
            myPoints += """<trkpt lat="${location.latitude}" lon="${location.longitude}" speed= "${location.speed}"><ele>${location.altitude}</ele><time>${
                simpleDateFormat.format(
                    Date(location.time)
                )
            }</time></trkpt>
"""
        }
        val footer = "</trkseg>\n</trk>\n</gpx>\n"
        try {
            if (!PermissionCheck(context, *arrayPermission)) {
                ActivityCompat.requestPermissions(mainActivity!!, arrayPermission, 1)
            } else {
                val fileData = header + distance + time + averageSpeed + minAltitude + maxAltitude +
                         myPoints + footer
                val fileOutPutStream = FileOutputStream(file)
                fileOutPutStream.write(fileData.toByteArray())
                fileOutPutStream.close()
                Toast.makeText(mainActivity, "GPX recorded", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }




    companion object {
        fun lastFileModified(dir: String?): File? {
            val myDirectory = File(dir)
            var myFile: File? = null
            if (!myDirectory.listFiles().isNullOrEmpty()) {
                val files = myDirectory.listFiles { file -> file.isFile }
                var lastMod = Long.MIN_VALUE
                if (!files.isNullOrEmpty()) {
                    for (file in files) {
                        if (file.lastModified() > lastMod) {
                            myFile = file
                            lastMod = file.lastModified()
                        }
                    }
                }
            }


            return myFile
        }
    }

    init {
        pathtoSD = Environment.getExternalStorageDirectory()
        myfolder = File("$pathtoSD/GPStracks")
        myfolder.mkdirs()
        gpxFile = File(myfolder, file_name)
    }
}