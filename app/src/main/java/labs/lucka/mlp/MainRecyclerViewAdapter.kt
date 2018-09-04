package labs.lucka.mlp

import android.content.Context
import android.location.Location
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView

/**
 * Adapter for mainRecyclerView.
 *
 * ## Changelog
 * ### 0.2.3
 * - Long press to delete -> swipe left to delete
 *
 * ## Private Attributes
 * - [onItemClickListener]
 * - [itemTouchHelper]
 *
 * ## Nested Classes
 * - [MainRecyclerViewListener]
 * - [OnItemClickListener]
 * - [ViewHolderCardMockTarget]
 *
 * ## Methods
 * ### Overridden
 * - [onCreateViewHolder]
 * - [onBindViewHolder]
 * - [getItemCount]
 * ### Public
 * - [attachItemTouchHelperTo]
 * - [notifyAddMockTarget]
 * - [notifyRemoveMockTarget]
 *
 * @param [context] The context
 * @param [mockTargetList] ArrayList for mock targets from [MainActivity]
 * @param [mainRecyclerViewListener] Message listener from [MainActivity]
 *
 * @see <a href="https://medium.com/@kitek/recyclerview-swipe-to-delete-easier-than-you-thought-cff67ff5e5f6">RecyclerView swipe to delete easier than you thought | Medium</a>
 *
 * @author lucka-me
 * @since 0.1
 *
 * @property [onItemClickListener] Listener for click event on cards
 * @property [itemTouchHelper] Used to handle swipe
 */
class MainRecyclerViewAdapter(
    private val context: Context,
    private var mockTargetList: ArrayList<MockTarget>,
    private val mainRecyclerViewListener: MainRecyclerViewListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val onItemClickListener: OnItemClickListener = object : OnItemClickListener {

        override fun onClickAt(position: Int) {
            mockTargetList[position].enabled = !mockTargetList[position].enabled
            notifyItemChanged(position)
        }

        override fun onLongClickAt(position: Int) {
            mainRecyclerViewListener.onEditAt(position)
        }
    }

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
                        DialogKit.showDialog(
                            context,
                            R.string.delete_mock_target_confirm_title,
                            String.format(
                                context.getString(R.string.delete_mock_target_confirm_message),
                                mockTargetList[position].title,
                                Location.convert(
                                    mockTargetList[position].longitude,
                                    Location.FORMAT_SECONDS
                                ),
                                Location.convert(
                                    mockTargetList[position].latitude,
                                    Location.FORMAT_SECONDS
                                )
                            ),
                            positiveButtonListener = { _, _ ->
                                mockTargetList.removeAt(position)
                                notifyRemoveMockTarget(position)
                                mainRecyclerViewListener.onRemovedAt(position)
                            },
                            negativeButtonTextId = R.string.cancel,
                            negativeButtonListener = { _, _ ->
                                notifyItemChanged(position)
                            },
                            cancelable = false
                        )
                    }

                    ItemTouchHelper.RIGHT -> {
                        notifyItemChanged(position)
                        mainRecyclerViewListener.onEditAt(position)
                    }

                }

            }
        }
    )


    /**
     * Interface used to receive message from [MainRecyclerViewListener].
     *
     * ## Public Methods
     * - [onRemovedAt]
     * - [onEditAt]
     *
     * @author lucka-me
     * @since 0.1
     */
    interface MainRecyclerViewListener {
        /**
         * Called when mock target removed from the [mockTargetList]
         *
         * @param [position] The position of removed target
         *
         * @author lucka-me
         * @since 0.1
         */
        fun onRemovedAt(position: Int)

        /**
         * Called when long pressed or swiped to right
         *
         * @param [position] Position of the card (target)
         *
         * @author lucka-me
         * @since 0.1
         */
        fun onEditAt(position: Int)
    }

    /**
     * Interface to receive message of click event on items (cards), with the position of the
     * clicked card.
     *
     * ## Public Methods
     * - [onClickAt]
     * - [onLongClickAt]
     *
     * @author lucka-me
     * @since 0.1.1
     */
    interface OnItemClickListener {
        /**
         * Called when item been clicked.
         *
         * @param [position] Position of clicked item
         *
         * @author lucka-me
         * @since 0.1.1
         */
        fun onClickAt(position: Int)
        /**
         * Called when item been long clicked
         *
         * @param [position] Position of clicked item
         *
         * @author lucka-me
         * @since 0.1.1
         */
        fun onLongClickAt(position: Int)
    }

    /**
     * ViewHolder of card to present [MockTarget].
     *
     * ## Public Attributes
     * - [titleText]
     * - [longitudeText]
     * - [latitudeText]
     * - [enableCheckBox]
     *
     * @param [itemView] View of the item
     * @param [onItemClickListener] See [OnItemClickListener]
     *
     * @author lucka-me
     * @since 0.1
     *
     * @property [titleText] [TextView] to present [MockTarget.title]
     * @property [longitudeText] [TextView] to present [MockTarget.longitude]
     * @property [latitudeText] [TextView] to present [MockTarget.latitude]
     * @property [enableCheckBox] [CheckBox] to present [MockTarget.enabled]
     */
    class ViewHolderCardMockTarget(
        itemView: View,
        private val onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val longitudeText: TextView = itemView.findViewById(R.id.longitudeText)
        val latitudeText: TextView = itemView.findViewById(R.id.latitudeText)
        val enableCheckBox: CheckBox = itemView.findViewById(R.id.enableCheckBox)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            onItemClickListener.onClickAt(adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            onItemClickListener.onLongClickAt(adapterPosition)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.card_mock_target, parent, false)
        return ViewHolderCardMockTarget(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderCardMockTarget) {
            holder.titleText.text = if (mockTargetList[position].title.isBlank()) {
                String.format(context.getString(R.string.target_title_default), position)
            } else {
                mockTargetList[position].title
            }
            holder.longitudeText.text = Location.convert(
                mockTargetList[position].location.longitude,
                Location.FORMAT_SECONDS
            )
            holder.latitudeText.text = Location.convert(
                mockTargetList[position].location.latitude,
                Location.FORMAT_SECONDS
            )
            holder.enableCheckBox.isChecked = mockTargetList[position].enabled
        }
    }

    override fun getItemCount(): Int {
        return mockTargetList.size
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

    /**
     * Should called when new target added to [mockTargetList].
     *
     * @param [count] Quantity of added mock targets, 1 for default
     *
     * @author lucka-me
     * @since 0.1
     */
    fun notifyAddMockTarget(count: Int = 1) {
        notifyItemRangeInserted(mockTargetList.size - count, count)
    }

    /**
     * Should called when target removed from [mockTargetList].
     *
     * @param [position] The position of removed target
     *
     * @author lucka-me
     * @since 0.1
     */
    fun notifyRemoveMockTarget(position: Int) {
        notifyItemRemoved(position)
    }
}