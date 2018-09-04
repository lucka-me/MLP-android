package labs.lucka.mlp

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.view.View
import kotlinx.android.synthetic.main.dialog_edit_mock_target.view.*

/**
 * Display dialogs in a bit better way.
 *
 * ## Public Methods
 * - [showDialog]
 * - [showSimpleAlert]
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

            val builder = android.support.v7.app.AlertDialog.Builder(context)
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
         *
         * @param [context] The context
         * @param [mockTargetList] The mock target list
         * @param [onAdded] Callback fired when the new mock target added to [mockTargetList]
         *
         * @author lucka-me
         * @since 0.1
         *
         * @see <a href="https://www.viralandroid.com/2015/09/simple-android-tabhost-and-tabwidget-example.html">Simple Android TabHost and TabWidget Example | Viral Android</a>
         */
        fun showAddMockTargetDialog(
            context: Context,
            mockTargetList: ArrayList<MockTarget>,
            onAdded: (() -> (Unit))
        ) {
            val dialogLayout = View.inflate(context, R.layout.dialog_edit_mock_target, null)
            val tabHost = dialogLayout.tabHost
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

            val dialog = android.support.v7.app.AlertDialog.Builder(context)
                .setTitle(R.string.add_mock_target_title)
                .setView(dialogLayout)
                .setPositiveButton(R.string.add) { _, _ ->
                    val longitude = dialogLayout.longitudeEdit.text.toString().toDouble()
                    val latitude = dialogLayout.latitudeEdit.text.toString().toDouble()
                    val title = dialogLayout.titleEdit.text.toString()
                    val altitude = dialogLayout.altitudeEdit.text.toString().toDoubleOrNull()
                    val accuracy = dialogLayout.accuracyEdit.text.toString().toFloatOrNull()
                    mockTargetList.add(MockTarget(
                        longitude, latitude, title = title,
                        altitude = altitude, accuracy = accuracy ?: 5.0F
                    ))
                    try {
                        DataKit.saveData(context, mockTargetList)
                    } catch (error: Exception) {
                        DialogKit.showSimpleAlert(context, error.message)
                    }
                    onAdded()
                }
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .show()
            dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = false
            var isLongitudeReady = false
            var isLatitudeReady = false
            dialogLayout.longitudeEdit.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = false
                    return@setOnFocusChangeListener
                }
                val longitude = dialogLayout.longitudeEdit.text.toString().toDoubleOrNull()
                if (longitude == null) {
                    isLongitudeReady = false
                } else if (longitude < -180 || longitude > 180) {
                    isLongitudeReady = false
                    dialogLayout.longitudeEdit.text.clear()
                } else {
                    isLongitudeReady = true
                }
                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled =
                    isLongitudeReady && isLatitudeReady
            }
            dialogLayout.latitudeEdit.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = false
                    return@setOnFocusChangeListener
                }
                val latitude = dialogLayout.latitudeEdit.text.toString().toDoubleOrNull()
                if (latitude == null) {
                    isLatitudeReady = false
                } else if (latitude < -90 || latitude > 90) {
                    isLatitudeReady = false
                    dialogLayout.latitudeEdit.text.clear()
                } else {
                    isLatitudeReady = true
                }
                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled =
                    isLongitudeReady && isLatitudeReady
            }
        }

        /**
         * Show a dialog to edit a mock target.
         *
         * @param [context] The context
         * @param [mockTargetList] The mock target list
         * @param [onEdited] Callback fired when the new mock target edited and saved
         *
         * @author lucka-me
         * @since 0.2.3
         */
        fun showEditMockTargetDialog(
            context: Context,
            mockTargetList: ArrayList<MockTarget>,
            index: Int,
            onEdited: (() -> (Unit))
        ) {
            val mockTarget = mockTargetList[index]
            val dialogLayout = View.inflate(context, R.layout.dialog_edit_mock_target, null)
            val tabHost = dialogLayout.tabHost
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

            dialogLayout.longitudeEdit.setText(mockTarget.longitude.toString())
            dialogLayout.latitudeEdit.setText(mockTarget.latitude.toString())
            dialogLayout.titleEdit.setText(mockTarget.title)
            if (mockTarget.altitude != null)
                dialogLayout.altitudeEdit.setText(mockTarget.altitude.toString())
            dialogLayout.accuracyEdit.setText(mockTarget.accuracy.toString())

            val dialog = android.support.v7.app.AlertDialog.Builder(context)
                .setTitle(R.string.edit_mock_target_title)
                .setView(dialogLayout)
                .setPositiveButton(R.string.save) { _, _ ->
                    val longitude = dialogLayout.longitudeEdit.text.toString().toDouble()
                    val latitude = dialogLayout.latitudeEdit.text.toString().toDouble()
                    val title = dialogLayout.titleEdit.text.toString()
                    val altitude = dialogLayout.altitudeEdit.text.toString().toDoubleOrNull()
                    val accuracy = dialogLayout.accuracyEdit.text.toString().toFloatOrNull()
                    mockTarget.longitude = longitude
                    mockTarget.latitude = latitude
                    mockTarget.title = title
                    mockTarget.altitude = altitude
                    mockTarget.accuracy = accuracy ?: 5.0F
                    try {
                        DataKit.saveData(context, mockTargetList)
                    } catch (error: Exception) {
                        DialogKit.showSimpleAlert(context, error.message)
                    }
                    onEdited()
                }
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .show()
            var isLongitudeReady = true
            var isLatitudeReady = true
            dialogLayout.longitudeEdit.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = false
                    return@setOnFocusChangeListener
                }
                val longitude = dialogLayout.longitudeEdit.text.toString().toDoubleOrNull()
                if (longitude == null) {
                    isLongitudeReady = false
                } else if (longitude < -180 || longitude > 180) {
                    isLongitudeReady = false
                    dialogLayout.longitudeEdit.text.clear()
                } else {
                    isLongitudeReady = true
                }
                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled =
                    isLongitudeReady && isLatitudeReady
            }
            dialogLayout.latitudeEdit.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = false
                    return@setOnFocusChangeListener
                }
                val latitude = dialogLayout.latitudeEdit.text.toString().toDoubleOrNull()
                if (latitude == null) {
                    isLatitudeReady = false
                } else if (latitude < -90 || latitude > 90) {
                    isLatitudeReady = false
                    dialogLayout.latitudeEdit.text.clear()
                } else {
                    isLatitudeReady = true
                }
                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled =
                    isLongitudeReady && isLatitudeReady
            }
        }
    }
}