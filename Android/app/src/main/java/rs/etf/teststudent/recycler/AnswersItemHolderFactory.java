package rs.etf.teststudent.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import rs.etf.teststudent.R;


public class AnswersItemHolderFactory implements ItemHolderFactory {

    @Override
    public RecyclerView.ViewHolder createItemViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case RecyclerItemType.ITEM:
                //inflate item row view
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .item_answer, parent, false);

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

        public final TextView letter;
        public final TextView answer;
        public final RadioButton radioButton;

        /**
         * Instantiates a new Item view holder.
         *
         * @param view the view
         */
        public ItemViewHolder(View view) {
            super(view);

            letter = view.findViewById(R.id.letter);
            answer = view.findViewById(R.id.answer);
            radioButton = view.findViewById(R.id.radiobutton);
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
