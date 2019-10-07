package labs.lucka.mlp

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.edit
import org.jetbrains.anko.defaultSharedPreferences

class ProviderManagerRecyclerViewAdapter(
    private val context: Context,
    private val adapterListener: Listener
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Listener {
        fun onRemoved(provider: String, position: Int)
        fun onEdit(provider: String, onSave: (newProvider: String) -> Unit)
    }

    class ViewHolderProviderItem(itemView: View): RecyclerView.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.findViewById(R.id.item_title)
    }

    private val providerList: ArrayList<String> = arrayListOf()

    private val itemTouchHelper: ItemTouchHelper =
        ItemTouchHelper(
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
                            val provider = providerList[position]
                            providerList.removeAt(position)
                            notifyItemRemoved(position)
                            adapterListener.onRemoved(provider, position)

                        }

                        ItemTouchHelper.RIGHT -> {
                            notifyItemChanged(position)
                            adapterListener.onEdit(providerList[position]) {
                                providerList[position] = it
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

                    super.onChildDraw(
                        c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                    )
                }
            }
        )

    init {
        val providerSet = context.defaultSharedPreferences
            .getStringSet(context.getString(R.string.pref_provider_list_key), setOf())
        if (providerSet != null) providerList.addAll(providerSet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.list_item_provider_manager, parent, false)
        return ViewHolderProviderItem(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderProviderItem) {
            holder.itemTitle.text = providerList[position]
        }
    }

    override fun getItemCount() = providerList.size

    fun attachItemTouchHelperTo(recyclerView: RecyclerView?) {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun add(provider: String) {
        providerList.add(provider)
        notifyItemInserted(itemCount - 1)
    }

    fun add(provider: String, position: Int) {
        providerList.add(position, provider)
        notifyItemInserted(position)
    }

    fun saveData() {
        context.defaultSharedPreferences.edit {
            putStringSet(context.getString(R.string.pref_provider_list_key), HashSet(providerList))
        }
    }
}