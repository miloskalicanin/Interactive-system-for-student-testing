package rs.etf.teststudent.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import rs.etf.teststudent.R;


public class ActiveThemesItemHolderFactory implements ItemHolderFactory {

    @Override
    public RecyclerView.ViewHolder createItemViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case RecyclerItemType.ITEM:
                //inflate item row view
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .item_active_theme, parent, false);

                //return view holder
                return new ItemViewHolder(view);
            case RecyclerItemType.EMPTY:
                return new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R
                        .layout.fragment_default_empty_item, parent, false));
            default:
                throw new IllegalArgumentException("View type is not supported, type:" + viewType);
        }
    }

    /**
     * Actual item view holder.
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        public final LinearLayout classInfoHolder;
        public final TextView course;
        public final TextView testName;
        public final TextView date;
        public final ImageView unsubscribeButton;

        /**
         * Instantiates a new Item view holder.
         *
         * @param view the view
         */
        public ItemViewHolder(View view) {
            super(view);
            classInfoHolder = view.findViewById(R.id.class_info_holder);
            course = view.findViewById(R.id.course);
            testName = view.findViewById(R.id.test_name);
            date = view.findViewById(R.id.date);
            unsubscribeButton = view.findViewById(R.id.unsubscribe_button);
        }
    }

    /**
     * Empty label view holder.
     */
    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;

        /**
         * Instantiates a new Item view holder.
         *
         * @param view the view
         */
        public EmptyViewHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.fragment_default_empty_item_title);
        }
    }
}
