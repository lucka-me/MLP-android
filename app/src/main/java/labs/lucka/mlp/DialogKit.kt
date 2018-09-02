package labs.lucka.mlp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.view.View
import kotlinx.android.synthetic.main.dialog_add_mock_target.view.*

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
         *
         * @param [context] The context
         * @param [mockTargetList] The mock target list
         * @param [onCoordinateWrong] Callback fired when the coordinate input is wrong
         * @param [onAdded] Callback fired when the new mock target added to [mockTargetList]
         *
         * @author lucka-me
         * @since 0.1
         */
        fun showAddMockTargetDialog(
            context: Context,
            mockTargetList: ArrayList<MockTarget>,
            onCoordinateWrong: (() -> (Unit)),
            onAdded: (() -> (Unit))
        ) {
            val dialogLayout = View.inflate(context, R.layout.dialog_add_mock_target, null)
            android.support.v7.app.AlertDialog.Builder(context)
                .setTitle(R.string.add_mock_target_title)
                .setView(dialogLayout)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    val longitude = dialogLayout.longitudeEdit.text.toString().toDoubleOrNull()
                    val latitude = dialogLayout.latitudeEdit.text.toString().toDoubleOrNull()
                    val title = dialogLayout.titleEdit.text.toString()
                    if (longitude == null || latitude == null ||
                        longitude < -180 || longitude > 180||
                        latitude < -90 || latitude > 90
                    ) {
                        onCoordinateWrong()
                    } else {
                        mockTargetList.add(MockTarget(longitude, latitude, title = title))
                        try {
                            DataKit.saveData(context, mockTargetList)
                        } catch (error: Exception) {
                            DialogKit.showSimpleAlert(context, error.message)
                        }
                        onAdded()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .show()
        }
    }
}