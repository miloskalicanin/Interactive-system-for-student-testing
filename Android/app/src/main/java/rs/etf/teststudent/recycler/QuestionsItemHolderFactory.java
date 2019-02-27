package rs.etf.teststudent.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import rs.etf.teststudent.R;


public class QuestionsItemHolderFactory implements ItemHolderFactory {

    @Override
    public RecyclerView.ViewHolder createItemViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case RecyclerItemType.ITEM:
                //inflate item row view
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .item_question, parent, false);

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

        public final LinearLayout questionInfoHolder;
        public final TextView testName;
        public final TextView question;
        public final TextView type;
        public final ImageView deleteQuestionButton;

        /**
         * Instantiates a new Item view holder.
         *
         * @param view the view
         */
        public ItemViewHolder(View view) {
            super(view);

            questionInfoHolder = view.findViewById(R.id.question_info_holder);
            testName = view.findViewById(R.id.test_name);
            question = view.findViewById(R.id.question);
            type = view.findViewById(R.id.type);
            deleteQuestionButton = view.findViewById(R.id.delete_question);
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
