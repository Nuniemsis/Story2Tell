package com.example.ncarvalho.story2tell;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Locale;

public class SearchAdapter extends
        RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private ArrayList<UserQualities> mQualities;
    private Context mContext;

    public SearchAdapter(Context context, ArrayList<UserQualities> userQualities) {

        mQualities = userQualities;
        mContext = context;

    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View qualitiesView = inflater.inflate(R.layout.custom_row_search, parent, false);

        // Return a new holder instance
        SearchAdapter.ViewHolder viewHolder = new SearchAdapter.ViewHolder(context, qualitiesView);
        return viewHolder;
    }

    public void onBindViewHolder(SearchAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final UserQualities userQualities = mQualities.get(position);

        viewHolder.dateTextView.setText(String.format(Locale.US, "%s", userQualities.getDate()));
        viewHolder.nameTextView.setText(String.format(Locale.US, "%s", userQualities.getUsername()));
        viewHolder.llsearch.addView(new RatingRectangle(getContext(), userQualities));

    }
    @Override
    public int getItemCount() {
        return mQualities.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row

        public TextView dateTextView;
        public TextView nameTextView;

        private Context context;
        public LinearLayout llsearch;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(Context context, View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.

            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateSearchTextView);
            nameTextView = itemView.findViewById(R.id.nameSearchTextView);
            llsearch = itemView.findViewById(R.id.llsearch);


            // Store the context
            this.context = context;

        }
    }
}