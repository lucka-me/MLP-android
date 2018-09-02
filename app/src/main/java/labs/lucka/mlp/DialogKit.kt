package labs.lucka.mlp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable

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
    }
}