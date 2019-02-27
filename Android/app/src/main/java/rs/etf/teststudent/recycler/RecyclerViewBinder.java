package rs.etf.teststudent.recycler;

import android.support.v7.widget.RecyclerView;

public interface RecyclerViewBinder<T> {
    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder the view holder which should be updated to represent the contents of the item
     *               at the given position in the data set
     * @param item   the item data content
     */
    void onBindViewHolder(RecyclerView.ViewHolder holder, T item, int position);
}
