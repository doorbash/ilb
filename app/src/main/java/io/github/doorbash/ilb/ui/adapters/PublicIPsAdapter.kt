import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.doorbash.ilb.databinding.ItemPublicIpBinding
import io.github.doorbash.ilb.model.PublicIP

class PublicIPsAdapter : ListAdapter<PublicIP, PublicIPsAdapter.PublicIpViewHolder>(Companion) {

    inner class PublicIpViewHolder(val binding: ItemPublicIpBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object : DiffUtil.ItemCallback<PublicIP>() {
        override fun areItemsTheSame(oldItem: PublicIP, newItem: PublicIP): Boolean =
            oldItem.publicIPv4 == newItem.publicIPv4

        override fun areContentsTheSame(oldItem: PublicIP, newItem: PublicIP): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicIpViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPublicIpBinding.inflate(layoutInflater)

        return PublicIpViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PublicIpViewHolder, position: Int) {
        val currentUser = getItem(position)
        holder.binding.item = currentUser
        holder.binding.executePendingBindings()
    }
}