package labs.lucka.mlp

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.util.ISO8601Utils
import org.jetbrains.anko.defaultSharedPreferences
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.*
import java.text.ParsePosition
import java.util.*
import javax.xml.parsers.SAXParserFactory

/**
 * A class used to save and load data to / from file
 *
 * ## Nested Classes
 * - [FileType]
 * - [GPXParserHandler]
 *
 * ## Static Methods
 * - [saveData]
 * - [loadData]
 * - [readFile]
 * - [writeFile]
 * - [importFromGPX]
 * - [importFromJSON]
 * - [exportToGPX]
 * - [exportToJSON]
 *
 * @author lucka-me
 * @since 0.2
 */
class DataKit {

    enum class FileType(
        val menuIndex: Int, val menuTitle: Int, val mime: Int,
        val importRequestCode: Int, val exportRequestCode: Int
    ) {
        JSON(
            0, R.string.ie_json, R.string.ie_json_mime,
            MainActivity.AppRequest.IMPORT_JSON.code, MainActivity.AppRequest.EXPORT_JSON.code
        ),
        GPX(
            1, R.string.ie_gpx, R.string.ie_gpx_mime,
            MainActivity.AppRequest.IMPORT_GPX.code, MainActivity.AppRequest.EXPORT_GPX.code
        )
    }

    /**
     * ParserHandler for GPX, convert to mock target list
     *
     * ## Changelog
     * ### 0.2.10
     * - Support `time` and automatic interval
     * - Requires context
     *
     * ## Supported Element Types
     * - `wpt`, `trkpt`
     * - `ele`, `desc`, `time`
     *
     * @param [context] The context
     *
     * @see <a href="https://www.jianshu.com/p/e99f061ce67c">Kotlin/Java解析XMl文件的四种方式 | 简书</a>
     *
     * @author lucka-me
     * @since 0.2.8
     */
    class GPXParserHandler(context: Context) : DefaultHandler() {

        var mockTargetList: ArrayList<MockTarget> = ArrayList(0)
        private var longitude: Double? = null
        private var latitude: Double? = null
        private var title: String? = null
        private var altitude: Double? = null
        private var lastTime: Date? = null
        private var interval: Long? = null
        private var elementValue: String? = null
        private val automaticInterval: Boolean = context.defaultSharedPreferences.getBoolean(
            context.getString(R.string.pref_ie_automatic_interval_key), false
        )

        override fun startDocument() {
            mockTargetList = ArrayList(0)
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
                }

                E_DESC, E_ELE -> {
                    elementValue = null
                }
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            super.endElement(uri, localName, qName)
            when (qName) {
                E_WPT, E_TRKPT -> {
                    mockTargetList.add(MockTarget(
                        longitude ?: return,
                        latitude ?: return,
                        title = title ?: "",
                        altitude = altitude,
                        interval = interval ?: DEFAULT_INTERVAL
                    ))
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
            private const val E_WPT = "wpt"
            private const val E_TRKPT = "trkpt"
            private const val E_ELE = "ele"
            private const val E_DESC = "desc"
            private const val E_TIME = "time"
            private const val A_LON = "lon"
            private const val A_LAT = "lat"
            private const val DEFAULT_INTERVAL: Long = 5000
        }
    }

    companion object {
        /**
         * Save data to JSON file.
         *
         * @param [context] The context
         * @param [mockTargetList] Mock target list to save
         *
         * @throws [Exception] Whatever exception occurred during the process
         *
         * @author lucka-me
         * @since 0.2
         */
        fun saveData(context: Context, mockTargetList: ArrayList<MockTarget>) {
            val filename = context.getString(R.string.data_filename)
            val file = File(context.filesDir, filename)

            try {
                file.writeText(Gson().toJson(mockTargetList.toArray()))
            } catch (error: Exception) {
                throw error
            }
        }

        /**
         * Load data from JSON file.
         *
         * @param [context] The context
         *
         * @return Mock target list loaded from file
         *
         * @throws [Exception] Whatever exception occurred during the process
         *
         * @author lucka-me
         * @since 0.2
         */
        fun loadData(context: Context): ArrayList<MockTarget> {
            val filename = context.getString(R.string.data_filename)
            val file = File(context.filesDir, filename)
            var mockTargetArray: Array<MockTarget> = arrayOf()

            if (file.exists()) {
                try {
                    mockTargetArray = Gson().fromJson(file.readText(), Array<MockTarget>::class.java)
                } catch (error: Exception) {
                    throw error
                }
            }

            return mockTargetArray.toCollection(ArrayList())
        }

        fun writeFile(context: Context, content: String, uri: Uri?) {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "w")
            val fileOutputStream = FileOutputStream(parcelFileDescriptor.fileDescriptor)
            fileOutputStream.write(content.toByteArray())
            fileOutputStream.close()
            parcelFileDescriptor.close()
        }

        fun readFile(context: Context, uri: Uri?): String {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var result = ""
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                result += line + "\n"
                line = bufferedReader.readLine()
            }
            return result
        }

        /**
         * Convert JSON string to mock target array.
         *
         * @param [source] The JSON string to be converted
         *
         * @return Array of MockTarget converted from the source
         *
         * @author lucka-me
         * @since 0.2.4
         */
        fun importFromJSON(source: String): Array<MockTarget> {
            return Gson().fromJson(source, Array<MockTarget>::class.java)
        }

        /**
         * Convert mock target ArrayList to JSON string.
         *
         * ## Changelog
         * ### 0.2.8
         * - Export pretty printing JSON
         *
         * @param [mockTargetList] The mock target ArrayList to be converted
         *
         * @return JSON string converted from [mockTargetList]
         *
         * @author lucka-me
         * @since 0.2.4
         */
        fun exportToJSON(mockTargetList: ArrayList<MockTarget>): String {
            return GsonBuilder().setPrettyPrinting().create().toJson(mockTargetList.toArray())
        }

        /**
         * Convert GPX string to mock target array.
         *
         * ## Changelog
         * ### 0.2.8
         * - Finished, parse with SAX
         *
         * @param [source] The GPX string to be converted
         *
         * @return Array of MockTarget converted from the source
         *
         * @author lucka-me
         * @since 0.2.4
         */
        fun importFromGPX(context: Context, source: String): Array<MockTarget> {
            val saxParser = SAXParserFactory.newInstance().newSAXParser()
            val gpxParserHandler = GPXParserHandler(context)
            saxParser.parse(source.byteInputStream(), gpxParserHandler)
            return gpxParserHandler.mockTargetList.toTypedArray()
        }

        /**
         * Convert mock target ArrayList to GPX string.
         *
         * @param [mockTargetList] The mock target ArrayList to be converted
         *
         * @return GPX string converted from [mockTargetList]
         *
         * @author lucka-me
         * @since 0.2.4
         */
        fun exportToGPX(mockTargetList: ArrayList<MockTarget>): String {
            return ""
        }
    }
}