package labs.lucka.mlp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TabHost

/**
 * Display dialogs in a bit better way.
 *
 * ## Public Methods
 * - [showDialog]
 * - [showSimpleAlert]
 * - [showDeveloperOptionsDialog]
 * - [showAddMockTargetDialog]
 * - [showEditMockTargetDialog]
 * - [showImportExportMenuDialog]
 * - [showAddProviderDialog]
 * - [showEditProviderDialog]
 *
 * @author lucka-me
 * @since 0.1
 */
class DialogKit {

    companion object {

        /**
         * Display a dialog
         *
         * @param [context] The context
         * @param [titleId] Resource ID for Title
         * @param [message] String for message
         * @param [positiveButtonTextId] Resource ID for PositiveButton text, CONFIRM for default
         * @param [positiveButtonListener] OnClickListener for PositiveButton, nullable
         * @param [negativeButtonTextId] Resource ID for NegativeButton text, nullable
         * @param [negativeButtonListener] OnClickListener for NegativeButton, nullable
         * @param [icon] Icon for dialog, nullable
         * @param [cancelable] Could dialog canceled by tapping outside or back button, nullable
         *
         * @see <a href="https://www.jianshu.com/p/6bd7dd1cd491">使用着色器修改 Drawable 颜色 | 简书</a>
         *
         * @author lucka-me
         * @since 0.1
         */
        fun showDialog(
            context: Context, titleId: Int, message: String?,
            positiveButtonTextId: Int = R.string.confirm,
            positiveButtonListener: ((DialogInterface, Int) -> (Unit))? = null,
            negativeButtonTextId: Int? = null,
            negativeButtonListener: ((DialogInterface, Int) -> (Unit))? = null,
            icon: Drawable? = null,
            cancelable: Boolean? = null
        ) {

            val builder = AlertDialog.Builder(context)
                .setTitle(titleId)
                .setIcon(icon)
                .setMessage(message)
                .setPositiveButton(positiveButtonTextId, positiveButtonListener)

            if (negativeButtonTextId != null)
                builder.setNegativeButton(negativeButtonTextId, negativeButtonListener)
            if (cancelable != null) builder.setCancelable(cancelable)

            builder.show()

        }

        /**
         * Display a dialog
         *
         * @param [context] The context
         * @param [titleId] Resource ID for Title
         * @param [messageId] Resource ID for message
         * @param [positiveButtonTextId] Resource ID for PositiveButton text, CONFIRM for default
         * @param [positiveButtonListener] OnClickListener for PositiveButton, nullable
         * @param [negativeButtonTextId] Resource ID for NegativeButton text, nullable
         * @param [negativeButtonListener] OnClickListener for NegativeButton, nullable
         * @param [icon] Icon for dialog, nullable
         * @param [cancelable] Could dialog canceled by tapping outside or back button, nullable
         *
         * @author lucka-me
         * @since 0.1
         */
        fun showDialog(
            context: Context, titleId: Int, messageId: Int,
            positiveButtonTextId: Int = R.string.confirm,
            positiveButtonListener: ((DialogInterface, Int) -> (Unit))? = null,
            negativeButtonTextId: Int? = null,
            negativeButtonListener: ((DialogInterface, Int) -> (Unit))? = null,
            icon: Drawable? = null,
            cancelable: Boolean? = null
        ) {

            showDialog(
                context, titleId, context.getString(messageId), positiveButtonTextId,
                positiveButtonListener,
                negativeButtonTextId, negativeButtonListener,
                icon,
                cancelable)

        }

        /**
         * Display a simple alert with a CONFIRM button and un-cancelable
         *
         * @param [context] The context
         * @param [message] String for message to display
         *
         * @author lucka-me
         * @since 0.1
         */
        fun showSimpleAlert(context: Context, message: String?) {
            showDialog(context, R.string.alert_title, message, cancelable = false)
        }

        /**
         * Display a simple alert with a CONFIRM button and un-cancelable
         *
         * @param [context] The context
         * @param [messageId] Resource ID for message to display
         *
         * @author lucka-me
         * @since 0.1
         */
        fun showSimpleAlert(context: Context, messageId: Int) {
            showSimpleAlert(context, context.getString(messageId))
        }

        /**
         * Show a dialog to explain the Enable mock location option
         * and provide a button to open the Developer Options.
         *
         * ## Changelog
         * ### 0.2
         * - Migrated from [MainActivity]
         *
         * @param [context] The context
         *
         * @author lucka-me
         * @since 0.1.2
         */
        fun showDeveloperOptionsDialog(context: Context) {
            showDialog(
                context,
                R.string.mock_location_option_title,
                R.string.mock_location_option_text,
                positiveButtonTextId = R.string.settings,
                positiveButtonListener = { _, _ ->
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                        )
                    } catch (error: Exception) {
                        showSimpleAlert(
                            context,
                            R.string.err_developer_options_failed
                        )
                    }
                },
                negativeButtonTextId = R.string.cancel,
                cancelable = false
            )
        }

        /**
         * Show a dialog to add a new mock target.
         *
         * ## Changelog
         * ### 0.2
         * - Migrated to [DialogKit]
         * ### 0.2.2
         * - Support TabHost
         * ### 0.2.3
         * - Check value immediately after input
         * - Add button enable only when longitude and latitude are both valid and not being edited
         * ### 0.2.7
         * - Call [onAddClickListener] instead of process adding to list and saving
         * ### 0.2.9
         * - Support edit interval
         *
         * @param [context] The context
         * @param [onAddClickListener] Callback fired when the add button is clicked
         *
         * @author lucka-me
         * @since 0.1
         *
         * @see <a href="https://www.viralandroid.com/2015/09/simple-android-tabhost-and-tabwidget-example.html">Simple Android TabHost and TabWidget Example | Viral Android</a>
         */
        fun showAddMockTargetDialog(
            context: Context,
            onAddClickListener: ((newTarget: MockTarget) -> (Unit))
        ) {
            val dialogLayout = View.inflate(context, R.layout.dialog_edit_mock_target, null)
            val tabHost: TabHost = dialogLayout.findViewById(R.id.tabHost)
            tabHost.setup()
            tabHost.addTab(
                tabHost
                    .newTabSpec(context.getString(R.string.tab_basic_title))
                    .setContent(R.id.tab_basic)
                    .setIndicator(context.getString(R.string.tab_basic_title))
            )
            tabHost.addTab(
                tabHost
                    .newTabSpec(context.getString(R.string.tab_advanced_title))
                    .setContent(R.id.tab_advanced)
                    .setIndicator(context.getString(R.string.tab_advanced_title))
            )
            val longitudeEdit: EditText = dialogLayout.findViewById(R.id.longitudeEdit)
            val latitudeEdit: EditText = dialogLayout.findViewById(R.id.latitudeEdit)

            val dialog = AlertDialog.Builder(context)
                .setTitle(R.string.add_mock_target_title)
                .setView(dialogLayout)
                .setPositiveButton(R.string.add) { _, _ ->
                    val longitude = longitudeEdit.text.toString().toDouble()
                    val latitude = latitudeEdit.text.toString().toDouble()
                    val title = dialogLayout.findViewById<EditText>(R.id.titleEdit).text.toString()
                    val altitude = dialogLayout.findViewById<EditText>(R.id.altitudeEdit).text
                        .toString().toDoubleOrNull()
                    val accuracy = dialogLayout.findViewById<EditText>(R.id.accuracyEdit).text
                        .toString().toFloatOrNull()
                    val interval = dialogLayout.findViewById<EditText>(R.id.intervalEdit).text
                        .toString().toLongOrNull()
                    onAddClickListener(MockTarget(
                        longitude, latitude, title = title,
                        altitude = altitude, accuracy = accuracy ?: 5.0F,
                        interval = interval ?: 5000
                    ))
                }
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .show()
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.isEnabled = false
            var isLongitudeReady = false
            var isLatitudeReady = false
            longitudeEdit.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    addButton.isEnabled = false
                    return@setOnFocusChangeListener
                }
                val longitude = longitudeEdit.text.toString().toDoubleOrNull()
                if (longitude == null) {
                    isLongitudeReady = false
                } else if (longitude < -180 || longitude > 180) {
                    isLongitudeReady = false
                    longitudeEdit.text.clear()
                } else {
                    isLongitudeReady = true
                }
                addButton.isEnabled = isLongitudeReady && isLatitudeReady
            }
            latitudeEdit.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    addButton.isEnabled = false
                    return@setOnFocusChangeListener
                }
                val latitude = latitudeEdit.text.toString().toDoubleOrNull()
                if (latitude == null) {
                    isLatitudeReady = false
                } else if (latitude < -90 || latitude > 90) {
                    isLatitudeReady = false
                    latitudeEdit.text.clear()
                } else {
                    isLatitudeReady = true
                }
                addButton.isEnabled = isLongitudeReady && isLatitudeReady
            }
        }

        /**
         * Show a dialog to edit a mock target.
         *
         * @param [context] The context
         * @param [mockTarget] The mock target to be edit
         * @param [onEdited] Callback fired when the new mock target edited and saved
         *
         * @see [showAddMockTargetDialog]
         *
         * @author lucka-me
         * @since 0.2.3
         */
        fun showEditMockTargetDialog(
            context: Context,
            mockTarget: MockTarget,
            onEdited: (() -> (Unit))
        ) {
            val dialogLayout = View.inflate(context, R.layout.dialog_edit_mock_target, null)
            val tabHost: TabHost = dialogLayout.findViewById(R.id.tabHost)
            tabHost.setup()
            tabHost.addTab(
                tabHost
                    .newTabSpec(context.getString(R.string.tab_basic_title))
                    .setContent(R.id.tab_basic)
                    .setIndicator(context.getString(R.string.tab_basic_title))
            )
            tabHost.addTab(
                tabHost
                    .newTabSpec(context.getString(R.string.tab_advanced_title))
                    .setContent(R.id.tab_advanced)
                    .setIndicator(context.getString(R.string.tab_advanced_title))
            )

            val longitudeEdit: EditText = dialogLayout.findViewById(R.id.longitudeEdit)
            val latitudeEdit: EditText = dialogLayout.findViewById(R.id.latitudeEdit)
            val titleEdit: EditText = dialogLayout.findViewById(R.id.titleEdit)
            val altitudeEdit: EditText = dialogLayout.findViewById(R.id.altitudeEdit)
            val accuracyEdit: EditText = dialogLayout.findViewById(R.id.accuracyEdit)
            val intervalEdit: EditText = dialogLayout.findViewById(R.id.intervalEdit)
            longitudeEdit.setText(mockTarget.longitude.toString())
            latitudeEdit.setText(mockTarget.latitude.toString())
            titleEdit.setText(mockTarget.title)
            if (mockTarget.altitude != null)
                altitudeEdit.setText(mockTarget.altitude.toString())
            accuracyEdit.setText(mockTarget.accuracy.toString())
            intervalEdit.setText(mockTarget.interval.toString())

            val dialog = AlertDialog.Builder(context)
                .setTitle(R.string.edit_mock_target_title)
                .setView(dialogLayout)
                .setPositiveButton(R.string.save) { _, _ ->
                    val longitude = longitudeEdit.text.toString().toDouble()
                    val latitude = latitudeEdit.text.toString().toDouble()
                    val title = titleEdit.text.toString()
                    val altitude = altitudeEdit.text.toString().toDoubleOrNull()
                    val accuracy = accuracyEdit.text.toString().toFloatOrNull()
                    val interval = intervalEdit.text.toString().toLongOrNull()
                    mockTarget.longitude = longitude
                    mockTarget.latitude = latitude
                    mockTarget.title = title
                    mockTarget.altitude = altitude
                    mockTarget.accuracy = accuracy ?: 5.0F
                    mockTarget.interval = interval ?: 5000
                    onEdited()
                }
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .show()
            var isLongitudeReady = true
            var isLatitudeReady = true
            longitudeEdit.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    return@setOnFocusChangeListener
                }
                val longitude = longitudeEdit.text.toString().toDoubleOrNull()
                if (longitude == null) {
                    isLongitudeReady = false
                } else if (longitude < -180 || longitude > 180) {
                    isLongitudeReady = false
                    longitudeEdit.text.clear()
                } else {
                    isLongitudeReady = true
                }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    isLongitudeReady && isLatitudeReady
            }
            latitudeEdit.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    return@setOnFocusChangeListener
                }
                val latitude = latitudeEdit.text.toString().toDoubleOrNull()
                if (latitude == null) {
                    isLatitudeReady = false
                } else if (latitude < -90 || latitude > 90) {
                    isLatitudeReady = false
                    latitudeEdit.text.clear()
                } else {
                    isLatitudeReady = true
                }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    isLongitudeReady && isLatitudeReady
            }
        }

        /**
         * Show the import / export dialog.
         *
         * @param [context] The context
         * @param [titleId] Resource ID of title
         * @param [onSelected] Callback fired when the item is selected
         *
         * @author lucka-me
         * @since 0.2.4
         */
        fun showImportExportMenuDialog(
            context: Context,
            titleId: Int,
            onSelected: ((fileType: DataKit.FileType) -> Unit)
        ) {
            val arrayAdapter =
                ArrayAdapter<String>(context, R.layout.select_dialog_item_material)
            arrayAdapter.addAll(
                context.getString(DataKit.FileType.JSON.menuTitle),
                context.getString(DataKit.FileType.GPX.menuTitle)
            )
            AlertDialog
                .Builder(context)
                .setTitle(titleId)
                .setAdapter(arrayAdapter) { _, which ->
                    val fileType = when (which) {
                        DataKit.FileType.GPX.menuIndex -> DataKit.FileType.GPX
                        DataKit.FileType.JSON.menuIndex -> DataKit.FileType.JSON
                        else -> null
                    } ?: return@setAdapter
                    onSelected(fileType)
                }
                .show()
        }

        /**
         * Show add customized provider dialog.
         *
         * @param [context] The context
         * @param [onAddClickListener] Callback fired when the add button clicked
         *
         * @author lucka-me
         * @since 0.2.7
         */
        fun showAddProviderDialog(
            context: Context, onAddClickListener: (provider: String) -> Unit
        ) {
            val dialogView = View.inflate(context, R.layout.edit_dialog_item, null)
            val editText: EditText = dialogView.findViewById(R.id.editText)
            val dialog =
                AlertDialog
                    .Builder(context)
                    .setView(dialogView)
                    .setTitle(R.string.customized_provider_add_title)
                    .setPositiveButton(R.string.add) { _, _ ->
                        onAddClickListener(editText.text.toString())
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.isEnabled = false
            editText.setOnKeyListener { _, _, _ ->
                addButton.isEnabled = !editText.text.isBlank()
                return@setOnKeyListener true
            }
        }

        /**
         * Show add customized provider dialog.
         *
         * @param [context] The context
         * @param [provider] The provider to be edited
         * @param [onEdited] Callback fired when the confirm button clicked
         *
         * @author lucka-me
         * @since 0.2.7
         */
        fun showEditProviderDialog(
            context: Context, provider: String, onEdited: (provider: String) -> Unit
        ) {
            val dialogView = View.inflate(context, R.layout.edit_dialog_item, null)
            val editText: EditText = dialogView.findViewById(R.id.editText)
            editText.setText(provider)
            val dialog =
                AlertDialog
                    .Builder(context)
                    .setView(dialogView)
                    .setTitle(R.string.customized_provider_edit_title)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        onEdited(editText.text.toString())
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            val confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            confirmButton.isEnabled = false
            editText.setOnKeyListener { _, _, _ ->
                confirmButton.isEnabled = !editText.text.isBlank()
                return@setOnKeyListener true
            }
        }

    }
}