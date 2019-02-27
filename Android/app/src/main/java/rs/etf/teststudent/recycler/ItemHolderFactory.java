package rs.etf.teststudent.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Interface for all recycler view's item holder factory implementations.
 *
 * @see AbstractRecyclerAdapter
 */
public interface ItemHolderFactory {
    /**
     * Creates the new item view holder
     *
     * @param parent   the view parent
     * @param viewType the view type
     * @return the new item view holder instance
     */
    RecyclerView.ViewHolder createItemViewHolder(ViewGroup parent, int viewType);
}
