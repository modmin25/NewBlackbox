package top.niunaijun.blackboxa.view.store

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import top.niunaijun.blackboxa.R

class StoreAdapter(
    private val items: MutableList<StoreItem>,
    private val onInstallClick: (StoreItem) -> Unit
) : RecyclerView.Adapter<StoreAdapter.StoreViewHolder>(), Filterable {

    private var filteredItems: MutableList<StoreItem> = ArrayList(items)
    private var currentFilter: String = ""

    class StoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.store_item_name)
        val description: TextView = view.findViewById(R.id.store_item_description)
        val category: TextView = view.findViewById(R.id.store_item_category)
        val version: TextView = view.findViewById(R.id.store_item_version)
        val actionButton: Button = view.findViewById(R.id.store_item_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_store, parent, false)
        return StoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        val item = filteredItems[position]
        holder.name.text = item.name
        holder.description.text = item.description
        holder.category.text = item.category
        holder.version.text = item.version

        if (item.isInstalled) {
            holder.actionButton.text = holder.itemView.context.getString(R.string.store_installed)
            holder.actionButton.isEnabled = false
        } else {
            holder.actionButton.text = holder.itemView.context.getString(R.string.store_install)
            holder.actionButton.isEnabled = true
            holder.actionButton.setOnClickListener { onInstallClick(item) }
        }
    }

    override fun getItemCount(): Int = filteredItems.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                currentFilter = constraint?.toString() ?: ""
                val filtered = if (currentFilter.isEmpty()) {
                    ArrayList(items)
                } else {
                    items.filter {
                        it.name.contains(currentFilter, ignoreCase = true) ||
                                it.description.contains(currentFilter, ignoreCase = true) ||
                                it.category.contains(currentFilter, ignoreCase = true) ||
                                it.packageName.contains(currentFilter, ignoreCase = true)
                    }.toMutableList()
                }
                return FilterResults().apply { values = filtered }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems = (results?.values as? MutableList<StoreItem>) ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    fun updateItemInstallStatus(packageName: String, installed: Boolean) {
        val index = filteredItems.indexOfFirst { it.packageName == packageName }
        if (index >= 0) {
            filteredItems[index] = filteredItems[index].copy(isInstalled = installed)
            notifyItemChanged(index)
        }
        val mainIndex = items.indexOfFirst { it.packageName == packageName }
        if (mainIndex >= 0) {
            items[mainIndex] = items[mainIndex].copy(isInstalled = installed)
        }
    }
}
