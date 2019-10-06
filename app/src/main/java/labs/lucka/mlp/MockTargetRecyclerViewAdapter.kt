package labs.lucka.mlp

import android.content.Context
import android.graphics.Canvas
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class MockTargetRecyclerViewAdapter(
    private val context: Context,
    private val adapterListener: MainRecyclerViewListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface MainRecyclerViewListener {
        fun onRemove(mockTarget: MockTarget, onConfirmed: () -> Unit, onCanceled: () -> Unit)
        fun onEdit(mockTarget: MockTarget, onSave: () -> Unit)
    }

    interface OnItemClickListener {
        fun onClickAt(position: Int)
        fun onLongClickAt(position: Int)
    }

    class ViewHolderCardMockTarget(
        itemView: View,
        private val onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_view_mock_target)
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val coordinateText: TextView = itemView.findViewById(R.id.coordinateText)
        private val altitudeText: TextView = itemView.findViewById(R.id.altitudeText)
        private val accuracyText: TextView = itemView.findViewById(R.id.accuracyText)
        private val intervalText: TextView = itemView.findViewById(R.id.intervalText)

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

        fun setFrom(context: Context, mockTarget: MockTarget) {
            cardView.isSelected = mockTarget.enabled
            titleText.text = if (mockTarget.title.isBlank()) {
                context.getString(R.string.target_title_default, adapterPosition)
            } else {
                mockTarget.title
            }
            coordinateText.text = context
                .getString(
                    R.string.coordinate_text,
                    Location.convert(mockTarget.location.longitude, Location.FORMAT_SECONDS),
                    Location.convert(mockTarget.location.latitude, Location.FORMAT_SECONDS)
                )
            altitudeText.text = context.getString(R.string.altitude_text, mockTarget.altitude)
            accuracyText.text = context.getString(R.string.accuracy_text, mockTarget.accuracy)
            intervalText.text = context
                .getString(R.string.interval_text, mockTarget.interval / 1000.0)
        }
    }

    private val mockTargetList: ArrayList<MockTarget> = arrayListOf()

    private val onItemClickListener: OnItemClickListener = object : OnItemClickListener {

        override fun onClickAt(position: Int) {
            mockTargetList[position].enabled = !mockTargetList[position].enabled
            notifyItemChanged(position)
        }

        override fun onLongClickAt(position: Int) {
            adapterListener.onEdit(mockTargetList[position]) {
                notifyItemChanged(position)
            }
        }

    }

    private val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.ACTION_STATE_IDLE,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                when (direction) {

                    ItemTouchHelper.LEFT -> {
                        adapterListener
                            .onRemove(
                                mockTargetList[position],
                                {
                                    mockTargetList.removeAt(position)
                                    notifyItemRemoved(position)
                                },
                                { notifyItemChanged(position) }
                            )
                    }

                    ItemTouchHelper.RIGHT -> {
                        notifyItemChanged(position)
                        adapterListener.onEdit(mockTargetList[position]) {
                            notifyItemChanged(position)
                        }
                    }

                }
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
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

    init {
        mockTargetList.addAll(DataKit.loadData(context))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.card_mock_target, parent, false)
        return ViewHolderCardMockTarget(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderCardMockTarget) holder.setFrom(context, mockTargetList[position])
    }

    override fun getItemCount() = mockTargetList.size

    fun attachItemTouchHelperTo(recyclerView: RecyclerView?) {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun add(mockTarget: MockTarget) {
        mockTargetList.add(mockTarget)
        notifyItemInserted(itemCount - 1)
    }

    fun clear() : ArrayList<MockTarget> {
        val oldList = arrayListOf<MockTarget>()
        mockTargetList.forEach { mockTarget ->
            oldList.add(mockTarget)
        }
        mockTargetList.clear()
        notifyItemRangeRemoved(0, oldList.size)
        return oldList
    }

    fun saveData() {
        DataKit.saveData(context, mockTargetList)
    }

    fun isEmpty() = mockTargetList.isEmpty()

}