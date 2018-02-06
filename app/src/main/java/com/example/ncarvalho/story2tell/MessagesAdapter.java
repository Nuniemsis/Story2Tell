package com.example.ncarvalho.story2tell;

import android.content.Context;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ncarvalho on 1/01/18.
 */



public class MessagesAdapter extends
        RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private ArrayList<Message> mMessages;
    private Context mContext;
    private DateFormat simpleDateFormat;

    public MessagesAdapter(Context context, ArrayList<Message> messages) {

        mMessages = messages;
        mContext = context;

    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View messageView = inflater.inflate(R.layout.custom_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(context, messageView);
        return viewHolder;
    }


    public void onBindViewHolder(MessagesAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final Message message = mMessages.get(position);

        final String hour;
        final String minute;
        final String day;
        final String month;
        final String year;
        String messageDateString;
        final ImageView photoImageView = viewHolder.photoImageView;
        final TextView messageTextView = viewHolder.messageTextView;
        final TextView nameTextView = viewHolder.nameTextView;
        final ProgressBar progressBarImage = viewHolder.progressBarImage;
        final RatingBar ratingBar = viewHolder.ratingBar;

        ratingBar.setRating(message.getRating());
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference();
                DatabaseReference usersRefMessages = ref.child("messages");

                DatabaseReference thisMessage = usersRefMessages.child(message.getPushKey());
                thisMessage.child("rating").setValue(rating);

                message.setRating(rating);
                ratingBar.setRating(rating);
            }
        });



        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-MM-SS");

        try {
            final Date date = simpleDateFormat.parse(message.getDate());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            hour = Integer.toString(calendar.get(Calendar.HOUR));
            day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
            minute = Integer.toString(calendar.get(Calendar.MINUTE));
            month = getMonth(calendar.get(Calendar.MONTH));
            year = Integer.toString(calendar.get(Calendar.YEAR));

            messageDateString = "\n" + day + " de " + month + " de " + year + " a las " + hour +
                    " horas " + minute + " minutos";

        } catch (Exception e) {
            e.printStackTrace();

            messageDateString = "";
        }

        boolean isPhoto = message.getPhotoUrl() != null;

        if(isPhoto) {
            messageTextView.setVisibility(View.GONE);
            progressBarImage.setVisibility(View.VISIBLE);
            Picasso.with(getContext())
                    .load(message.getPhotoUrl())
                    .fit()
                    .into(photoImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            progressBarImage.setVisibility(View.GONE);
                            photoImageView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError() {
                            progressBarImage.setVisibility(View.GONE);

                        }
                    });


        } else{
            progressBarImage.setVisibility(View.GONE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setVisibility(View.VISIBLE);
            messageTextView.setTextColor(Color.BLACK);
            messageTextView.setText(String.format(Locale.US, "%s", message.getMessage()));
        }

        nameTextView.setText(message.getName());
        nameTextView.setVisibility(View.VISIBLE);

        String displayName = String.format(Locale.US, "%s", message.getName());
        nameTextView.setText(displayName + messageDateString);

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }


    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row

        public TextView messageTextView;
        public TextView nameTextView;
        public ImageView photoImageView;
        public LinearLayout linearLayout;
        public ProgressBar progressBarImage;
        public RatingBar ratingBar;

        private Context context;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(Context context, View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = itemView.findViewById(R.id.nameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            photoImageView = itemView.findViewById(R.id.photoImageView);


            progressBarImage = itemView.findViewById(R.id.progressBarImage);
            ratingBar = itemView.findViewById(R.id.ratingBar);

            linearLayout = itemView.findViewById(R.id.linearLayout);
            // Store the context
            this.context = context;

        }
    }


    private String getMonth(Integer month) {

        String stringMonth;

        switch (month) {
            case 0:
                stringMonth = "Enero";
                break;

            case 1:
                stringMonth = "Febrero";
                break;

            case 2:
                stringMonth = "Enero";
                break;

            case 3:
                stringMonth = "Febrero";
                break;

            case 4:
                stringMonth = "Enero";
                break;

            case 5:
                stringMonth = "Febrero";
                break;

            case 6:
                stringMonth = "Enero";
                break;

            case 7:
                stringMonth = "Febrero";
                break;

            case 8:
                stringMonth = "Enero";
                break;

            case 9:
                stringMonth = "Febrero";
                break;

            case 10:
                stringMonth = "Enero";
                break;

            case 11:
                stringMonth = "Febrero";
                break;
            default:
                stringMonth = "";
                break;
        }
        return stringMonth;
    }
}