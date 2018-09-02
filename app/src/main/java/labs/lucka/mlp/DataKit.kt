package labs.lucka.mlp

import android.content.Context
import com.google.gson.Gson
import java.io.File

/**
 * A class used to save and load data to / from file
 *
 * ## Static Methods
 * - [saveData]
 * - [loadData]
 *
 * @author lucka-me
 * @since 0.2
 */
class DataKit {
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
    }
}