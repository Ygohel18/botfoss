package `in`.planckstudio.foss.bot.adapter.instagram

import `in`.planckstudio.foss.bot.R
import `in`.planckstudio.foss.bot.model.instagram.SearchHistory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class SearchHistoryAdapter(
    private val dataSet: ArrayList<SearchHistory>,
    val clickListner: OnSearchHistoryItemClickListner
) :
    RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageHolder: ImageView = view.findViewById(R.id.history_image_holder)
        val searchKey: AppCompatTextView = view.findViewById(R.id.history_key)

        fun initialize(item: SearchHistory, action: OnSearchHistoryItemClickListner) {
            itemView.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }

        init {
            // Define click listener for the ViewHolder's View.
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_search_history, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.searchKey.text = dataSet[position].getSearchKey()
        Glide.with(viewHolder.imageHolder.context)
            .load(dataSet[position].getSearchValue())
            .placeholder(R.drawable.default_imageholder_ig)
            .into(viewHolder.imageHolder)
        viewHolder.initialize(dataSet[position], clickListner)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}

interface OnSearchHistoryItemClickListner {
    fun onItemClick(item: SearchHistory, position: Int)
}