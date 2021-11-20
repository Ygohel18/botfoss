package `in`.planckstudio.foss.bot.adapter

import `in`.planckstudio.foss.bot.R
import `in`.planckstudio.foss.bot.model.SocialPostModel
import `in`.planckstudio.foss.bot.ui.instagram.InstagramProfileActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SocialPostAdapter(
    private val dataSet: ArrayList<SocialPostModel>,
    private val clickListner: InstagramProfileActivity
) :
    RecyclerView.Adapter<SocialPostAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageHolder: ImageView = view.findViewById(R.id.grid_image_item)

        fun initialize(item: SocialPostModel, action: OnGridImageItemClickListner) {

            imageHolder.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(2000).setListener(null)
            }

            imageHolder.setOnClickListener {
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
            .inflate(R.layout.item_grid_imageview, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        Glide.with(viewHolder.imageHolder.context)
            .load(dataSet[position].getPostThumbUrl())
            .placeholder(R.drawable.placeholder_thumb)
            .into(viewHolder.imageHolder)

        viewHolder.initialize(dataSet[position], clickListner)
    }

    override fun getItemCount() = dataSet.size
}

interface OnGridImageItemClickListner {
    fun onItemClick(item: SocialPostModel, position: Int)
}