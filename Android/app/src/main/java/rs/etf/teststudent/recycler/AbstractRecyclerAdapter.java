package rs.etf.teststudent.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Base recycler adapter for all item/data implementations. Provides convenience set of
 * methods to implement recycler adapter. Adapter is relying on {@link RecyclerViewBinder}
 * implementation for view binding and binder should be specific to fragment or activity.
 *
 * @param <T> the item type
 */
public abstract class AbstractRecyclerAdapter<T> extends RecyclerView
        .Adapter<RecyclerView.ViewHolder> {
    private final RecyclerViewBinder<T> viewBinder;
    //item holder factory
    private ItemHolderFactory itemHolderFactory;

    /**
     * Creates a new recycler adapter instance.
     *
     * @param itemHolderFactory item holder factory
     * @param viewBinder        the view binder
     */
    public AbstractRecyclerAdapter(ItemHolderFactory itemHolderFactory, RecyclerViewBinder<T> viewBinder) {
        this.itemHolderFactory = itemHolderFactory;
        this.viewBinder = viewBinder;
    }

    /**
     * Gets item at given position.
     *
     * @param position the position
     * @return the item
     */
    public abstract T getItem(int position);

    /**
     * Returns true if underlying data is empty.
     *
     * @return true if data is empty
     */
    public abstract boolean isEmpty();

    /**
     * Returns current size of underlying data.
     *
     * @return the size of data
     */
    public abstract int size();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return itemHolderFactory.createItemViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        if (RecyclerItemType.EMPTY == itemViewType) {
            //if header pass no data to view binder
            viewBinder.onBindViewHolder(holder, null, position);
        } else if (RecyclerItemType.ITEM == itemViewType) {

            //fire view binder with the item on clicked position
            viewBinder.onBindViewHolder(holder, getItem(position), position);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && isEmpty()) {
            return RecyclerItemType.EMPTY;
        } else {
            return RecyclerItemType.ITEM;
        }
    }

    @Override
    public int getItemCount() {
        //if data is empty assume 1 count (empty item)
        if (isEmpty()) {
            return 1;
        }

        //item count + 1 (for header item)
        return size();
    }
}
