package labs.lucka.mlp

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import java.io.*

/**
 * A class used to save and load data to / from file
 *
 * ## Nested Classes
 * - [FileType]
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
        val menuIndex: Int, val menuTitle: Int, val mime: Int, val importRequestCode: Int, val exportRequestCode: Int
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
         * @param [mockTargetList] The mock target ArrayList to be converted
         *
         * @return JSON string converted from [mockTargetList]
         *
         * @author lucka-me
         * @since 0.2.4
         */
        fun exportToJSON(mockTargetList: ArrayList<MockTarget>): String {
            return Gson().toJson(mockTargetList.toArray())
        }

        /**
         * Convert GPX string to mock target array.
         *
         * @param [source] The GPX string to be converted
         *
         * @return Array of MockTarget converted from the source
         *
         * @author lucka-me
         * @since 0.2.4
         */
        fun importFromGPX(source: String): Array<MockTarget> {
            return arrayOf()
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