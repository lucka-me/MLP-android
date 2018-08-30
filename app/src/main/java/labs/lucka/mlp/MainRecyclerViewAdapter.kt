package labs.lucka.mlp

import android.content.Context
import android.location.Location
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView

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
            DialogKit.showDialog(
                context,
                R.string.delete_mock_target_confirm_title,
                String.format(
                    context.getString(R.string.delete_mock_target_confirm_message),
                    Location.convert(mockTargetList[position].longitude, Location.FORMAT_SECONDS),
                    Location.convert(mockTargetList[position].latitude, Location.FORMAT_SECONDS)
                ),
                positiveButtonListener = { _, _ ->
                    mockTargetList.removeAt(position)
                    notifyRemoveMockTarget(position)
                    mainRecyclerViewListener.onRemovedAt(position)
                },
                negativeButtonTextId = R.string.cancel,
                cancelable = false
            )
        }
    }

    interface MainRecyclerViewListener {
        fun onRemovedAt(index: Int)
    }

    interface OnItemClickListener {
        fun onClickAt(position: Int)
        fun onLongClickAt(position: Int)
    }

    class ViewHolderCardLocation(
        itemView: View,
        private val onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        var longitudeText: TextView = itemView.findViewById(R.id.longitudeText)
        var latitudeText: TextView = itemView.findViewById(R.id.latitudeText)
        var enableCheckBox: CheckBox = itemView.findViewById(R.id.enableCheckBox)

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
        return ViewHolderCardLocation(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderCardLocation) {
            holder.enableCheckBox.isChecked = mockTargetList[position].enabled
            holder.longitudeText.text = Location.convert(
                mockTargetList[position].location.longitude,
                Location.FORMAT_SECONDS
            )
            holder.latitudeText.text = Location.convert(
                mockTargetList[position].location.latitude,
                Location.FORMAT_SECONDS
            )
        }
    }

    override fun getItemCount(): Int {
        return mockTargetList.size
    }

    fun notifyAddMockTarget(count: Int = 1) {
        notifyItemRangeInserted(mockTargetList.size - count, count)
    }

    fun notifyRemoveMockTarget(position: Int) {
        notifyItemRemoved(position)

    }
}