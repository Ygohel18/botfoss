package `in`.planckstudio.foss.bot.adapter

import `in`.planckstudio.foss.bot.R
import `in`.planckstudio.foss.bot.model.instagram.SearchHistory
import `in`.planckstudio.foss.bot.ui.RecentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class RecentAdapter(
    private val dataSet: ArrayList<SearchHistory>,
    private val clickListner: RecentActivity
) :
    RecyclerView.Adapter<RecentAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageHolder: ImageView = view.findViewById(R.id.recent_image)
        val titleText: AppCompatTextView = view.findViewById(R.id.recent_title)
        val captionText: AppCompatTextView = view.findViewById(R.id.recent_caption)
        val removeBtn: MaterialButton = view.findViewById(R.id.recent_btn)
        val divider: View = view.findViewById(R.id.recent_divider)

        fun initialize(item: SearchHistory, action: OnSearchHistoryItemClickListner) {
            removeBtn.setOnClickListener {
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
            .inflate(R.layout.item_recent, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.titleText.text = dataSet[position].getSearchKey()

        var vType = ""

        vType = if (dataSet[position].getSearchType() == "ig_dp") {
            "Profile"
        } else {
            "Media"
        }

        viewHolder.captionText.text = vType
        Glide.with(viewHolder.imageHolder.context)
            .load(dataSet[position].getSearchValue())
            .placeholder(R.drawable.default_imageholder_ig)
            .into(viewHolder.imageHolder)

        val mypos = position + 1

        if (mypos == this.itemCount) {
            viewHolder.divider.visibility = View.GONE
        }

        viewHolder.initialize(dataSet[position], clickListner)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}

interface OnSearchHistoryItemClickListner {
    fun onItemClick(item: SearchHistory, position: Int)
}