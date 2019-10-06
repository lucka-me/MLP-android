package labs.lucka.mlp

import android.content.Context
import android.net.Uri
import android.util.Xml
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.util.ISO8601Utils
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import org.xmlpull.v1.XmlSerializer
import java.io.*
import java.text.ParsePosition
import java.util.*
import javax.xml.parsers.SAXParserFactory

class DataKit {

    enum class FileType(
        val menuIndex: Int, val menuTitle: Int, val mime: Int,
        val importRequestCode: Int, val exportRequestCode: Int
    ) {
        JSON(
            0, R.string.ie_json, R.string.ie_json_mime,
            MainActivity.Request.IMPORT_JSON.code, MainActivity.Request.EXPORT_JSON.code
        ),
        GPX(
            1, R.string.ie_gpx, R.string.ie_gpx_mime,
            MainActivity.Request.IMPORT_GPX.code, MainActivity.Request.EXPORT_GPX.code
        )
    }

    /**
     * ParserHandler for GPX, convert to [MockTarget] list
     *
     * ## Supported Element Types
     * - `wpt`, `trkpt`
     * - `ele`, `desc`, `time`
     *
     * @param [automaticInterval] Should calculate interval or not
     *
     * @see <a href="https://www.jianshu.com/p/e99f061ce67c">Kotlin/Java解析XMl文件的四种方式 | 简书</a>
     *
     * @author lucka-me
     * @since 0.2.8
     *
     */
    class GPXParserHandler(private val automaticInterval: Boolean) : DefaultHandler() {

        val mockTargetList: ArrayList<MockTarget> = arrayListOf()
        private var longitude: Double? = null
        private var latitude: Double? = null
        private var title: String? = null
        private var altitude: Double? = null
        private var lastTime: Date? = null
        private var interval: Long? = null
        private var elementValue: String? = null
        private var isInTargetElement: Boolean = false

        override fun startDocument() {
            isInTargetElement = false
            mockTargetList.clear()
            super.startDocument()
        }

        override fun startElement(
            uri: String?, localName: String?, qName: String?, attributes: Attributes?
        ) {
            super.startElement(uri, localName, qName, attributes)
            when (qName) {
                E_WPT, E_TRKPT -> {
                    longitude = null
                    latitude = null
                    title = null
                    altitude = null
                    interval = null
                    if (attributes != null) {
                        longitude = attributes.getValue(A_LON).toDoubleOrNull()
                        latitude = attributes.getValue(A_LAT).toDoubleOrNull()
                    }
                    isInTargetElement = true
                }

                E_DESC, E_ELE -> {
                    elementValue = null
                }
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            super.endElement(uri, localName, qName)
            if (!isInTargetElement) return
            when (qName) {
                E_WPT, E_TRKPT -> {
                    mockTargetList.add(MockTarget(
                        longitude ?: return,
                        latitude ?: return,
                        title = title ?: "",
                        altitude = altitude,
                        interval = interval ?: DEFAULT_INTERVAL
                    ))
                    isInTargetElement = false
                }

                E_DESC -> {
                    title = elementValue
                }

                E_ELE -> {
                    altitude = elementValue?.toDoubleOrNull()
                }

                E_TIME -> {
                    if (automaticInterval) {
                        if (elementValue != null) {
                            val currentTime =
                                ISO8601Utils.parse(elementValue, ParsePosition(0))
                            if (lastTime == null) {
                                lastTime = currentTime
                                interval = null
                            } else {
                                interval = currentTime.time - lastTime!!.time
                                lastTime = currentTime
                            }

                        }
                    }
                }
            }
        }

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            super.characters(ch, start, length)
            if (ch == null) return
            elementValue = String(ch, start, length).trim()
        }

        companion object {
            private const val DEFAULT_INTERVAL: Long = 5000
        }
    }

    companion object {

        private const val E_TRK = "trk"
        private const val E_TRKSEG = "trkseg"
        private const val E_WPT = "wpt"
        private const val E_TRKPT = "trkpt"
        private const val E_ELE = "ele"
        private const val E_DESC = "desc"
        private const val E_TIME = "time"
        private const val A_LON = "lon"
        private const val A_LAT = "lat"
        private const val ENCODING = "UTF-8"
        private const val E_GPX = "gpx"
        private const val E_METADATA = "metadata"

        fun saveData(context: Context, mockTargetList: ArrayList<MockTarget>) {
            val filename = context.getString(R.string.data_filename)
            val file = File(context.filesDir, filename)

            file.writeText(Gson().toJson(mockTargetList.toArray()))
        }

        fun loadData(context: Context): ArrayList<MockTarget> {
            val file = File(context.filesDir, context.getString(R.string.data_filename))
            var list = arrayListOf<MockTarget>()

            if (file.exists()) {
                try {
                    list = Gson().fromJson(file.readText(), Array<MockTarget>::class.java)
                        .toCollection(ArrayList())
                } catch (error: Exception) {
                    return list
                }
            }

            return list
        }

        fun writeFile(context: Context, content: String, uri: Uri?) {
            if (uri == null) return
            val parcelFileDescriptor =
                context.contentResolver.openFileDescriptor(uri, "w") ?: return
            val fileOutputStream = FileOutputStream(parcelFileDescriptor.fileDescriptor)
            fileOutputStream.write(content.toByteArray())
            fileOutputStream.close()
            parcelFileDescriptor.close()
        }

        fun readFile(context: Context, uri: Uri?): String {
            var result = ""
            if (uri == null) return  result
            val inputStream = context.contentResolver.openInputStream(uri) ?: return result
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                result += line + "\n"
                line = bufferedReader.readLine()
            }
            return result
        }

        fun importFromJSON(source: String): ArrayList<MockTarget> {
            var list = arrayListOf<MockTarget>()

            try {
                list = Gson().fromJson(source, Array<MockTarget>::class.java)
                    .toCollection(ArrayList())
            } catch (error: Exception) {
                return list
            }

            return list
        }

        fun exportToJSON(mockTargetList: ArrayList<MockTarget>): String {
            return GsonBuilder().setPrettyPrinting().create().toJson(mockTargetList.toArray())
        }

        fun importFromGPX(source: String, automaticInterval: Boolean): ArrayList<MockTarget> {
            var list = arrayListOf<MockTarget>()

            try {
                val saxParser = SAXParserFactory.newInstance().newSAXParser()
                val gpxParserHandler = GPXParserHandler(automaticInterval)
                saxParser.parse(source.byteInputStream(), gpxParserHandler)
                list = gpxParserHandler.mockTargetList
            } catch (error: Exception) {
                return list
            }

            return list
        }

        /**
         * Convert mock target ArrayList to GPX string.
         *
         * @param [mockTargetList] The mock target ArrayList to be converted
         *
         * @return GPX string converted from [mockTargetList]
         *
         * @see <a href="https://stackoverflow.com/a/13631894/10276204">Read/write to external XML file in Android | Stack Overflow</a>
         * @see <a href="https://android.jlelse.eu/how-to-generate-xml-with-kotlin-extension-functions-and-lambdas-in-android-app-976229f1e4d8">How to generate XML with Kotlin Extension functions and Lambdas in Android app | AndroidPub</a>
         *
         * @author lucka-me
         * @since 0.2.4
         */
        fun exportToGPX(mockTargetList: ArrayList<MockTarget>): String {

            // Extend XmlSerializer
            fun XmlSerializer.document(content: XmlSerializer.() -> Unit): String {
                startDocument(ENCODING, true)
                val writer = StringWriter()
                setOutput(writer)
                content()
                endDocument()
                return writer.toString()
            }

            fun XmlSerializer.element(name: String, content: XmlSerializer.() -> Unit) {
                startTag(null, name)
                content()
                endTag(null, name)
            }

            fun XmlSerializer.attribute(name: String, value: String) =
                attribute(null, name, value)

            val xml = Xml.newSerializer()
            var currentTime = Date().time

            return xml.document {

                element(E_GPX) {
                    attribute("xmlns", "http://www.topografix.com/GPX/1/1")

                    element(E_METADATA) { text(ISO8601Utils.format(Date(currentTime))) }

                    element(E_TRK) {

                        element(E_TRKSEG) {

                            for (mockTarget in mockTargetList) {
                                element(E_TRKPT) {
                                    attribute(A_LAT, mockTarget.latitude.toString())
                                    attribute(A_LON, mockTarget.longitude.toString())

                                    if (mockTarget.title.isNotBlank())
                                        element(E_DESC) { text(mockTarget.title) }
                                    if (mockTarget.altitude != null)
                                        element(E_ELE) { text(mockTarget.altitude.toString()) }

                                    currentTime += mockTarget.interval
                                    element(E_TIME) { text(ISO8601Utils.format(Date(currentTime))) }
                                }
                            }

                        }

                    }
                }

            }
        }
    }
}