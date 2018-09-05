package labs.lucka.mlp

import android.content.Context
import android.graphics.Canvas
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Adapter for customizedProviderRecyclerView.
 *
 * ## Private Attributes
 * - [itemTouchHelper]
 *
 * ## Nested Classes
 * - [CustomizedProviderRecyclerViewListener]
 * - [ViewHolderProviderItem]
 *
 * ## Methods
 * ### Overridden
 * - [onCreateViewHolder]
 * - [onBindViewHolder]
 * - [getItemCount]
 * ### Public
 * - [attachItemTouchHelperTo]
 *
 * @param [context] The context
 * @param [providerList] ArrayList for customized providers
 * @param [customizedProviderRecyclerViewListener] Message listener
 *
 * @author lucka-me
 * @since 0.2.7
 *
 * @property [itemTouchHelper] Used to handle swipe
 */
class CustomizedProviderRecyclerViewAdapter(
    private val context: Context,
    private val providerList: ArrayList<String>,
    private val customizedProviderRecyclerViewListener: CustomizedProviderRecyclerViewListener
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.ACTION_STATE_IDLE,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder?,
                target: RecyclerView.ViewHolder?
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                if (viewHolder == null) return
                val position = viewHolder.adapterPosition
                when (direction) {

                    ItemTouchHelper.LEFT -> {
                        val removedProvider = providerList[position]
                        providerList.removeAt(position)
                        notifyItemRemoved(position)
                        customizedProviderRecyclerViewListener.onRemovedAt(position, removedProvider)

                    }

                    ItemTouchHelper.RIGHT -> {
                        notifyItemChanged(position)
                        customizedProviderRecyclerViewListener.onEditAt(position)
                    }

                }

            }

            override fun onChildDraw(
                c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?,
                dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                if (viewHolder == null || c == null) return
                val icon = ContextCompat.getDrawable(
                    context, if (dX < 0) R.drawable.ic_remove else R.drawable.ic_edit
                ) ?: return
                val iconSize = icon.intrinsicWidth
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val iconTop = itemView.top + (itemHeight - iconSize) / 2
                val iconBottom = iconTop + iconSize
                val iconMargin = (itemHeight - iconSize) / 2
                if (dX < 0) {
                    // Remove
                    val iconLeft = itemView.right - iconMargin - iconSize
                    val iconRight = itemView.right - iconMargin
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    icon.draw(c)
                } else {
                    // Edit
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = iconLeft + iconSize
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    icon.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
    )

    /**
     * Interface used to receive message from [CustomizedProviderRecyclerViewAdapter].
     *
     * ## Public Methods
     * - [onRemovedAt]
     * - [onEditAt]
     *
     * @author lucka-me
     * @since 0.2.7
     */
    interface CustomizedProviderRecyclerViewListener {
        /**
         * Called when provider removed from the [providerList]
         *
         * @param [position] The position of removed target
         * @param [removedProvider] Removed target
         *
         * @author lucka-me
         * @since 0.2.7
         */
        fun onRemovedAt(position: Int, removedProvider: String)

        /**
         * Called when swiped to right
         *
         * @param [position] Position of the card (provider)
         *
         * @author lucka-me
         * @since 0.2.7
         */
        fun onEditAt(position: Int)
    }

    class ViewHolderProviderItem(itemView: View): RecyclerView.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.findViewById(R.id.itemTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.list_item_cusomized_provider, parent, false)
        return ViewHolderProviderItem(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderProviderItem) {
            holder.itemTitle.text = providerList[position]
        }
    }

    override fun getItemCount(): Int {
        return providerList.size
    }

    /**
     * Attach [itemTouchHelper] to the recycler view
     *
     * @author lucka-me
     * @since 0.2.3
     */
    fun attachItemTouchHelperTo(recyclerView: RecyclerView?) {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}