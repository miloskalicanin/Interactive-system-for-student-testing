package rs.etf.teststudent.recycler;

import java.util.List;

/**
 * List based recycler adapter.
 */
public class ListRecyclerAdapter<T> extends AbstractRecyclerAdapter<T> {
    private List<T> list;

    /**
     * Instantiates a new List recycler adapter.
     *
     * @param itemHolderFactory the item holder factory
     * @param viewBinder        the view binder
     */
    public ListRecyclerAdapter(ItemHolderFactory itemHolderFactory, RecyclerViewBinder<T> viewBinder) {
        super(itemHolderFactory, viewBinder);
    }

    @Override
    public T getItem(int position) {
        if (position >= 0 && position < list.size()) {
            return list.get(position);
        } else {
            return null;
        }
    }

    @Override
    public boolean isEmpty() {
        return list == null || list.size() == 0;
    }

    @Override
    public int size() {
        return list == null ? 0 : list.size();
    }

    /**
     * Gets list.
     *
     * @return the list
     */
    public List<T> getList() {
        return list;
    }

    /**
     * Sets list.
     *
     * @param list the list
     */
    public void setList(List<T> list) {
        this.list = list;
    }

    public int getCount() {
        return size();
    }


    public void removeData(int position) {
        list.remove(position);
    }
}
