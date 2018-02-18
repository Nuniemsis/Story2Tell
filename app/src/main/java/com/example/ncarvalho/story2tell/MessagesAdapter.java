package com.example.ncarvalho.story2tell;

import android.content.Context;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageMetadata;
import com.squareup.picasso.Picasso;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.tasks.Tasks.await;

/**
 * Created by ncarvalho on 1/01/18.
 */



public class MessagesAdapter extends
        RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private ArrayList<Message> mMessages;
    private Context mContext;
    private DateFormat simpleDateFormat;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReferenceUsers;
    private UserInformation information;

    public MessagesAdapter(Context context, ArrayList<Message> messages) {

        mMessages = messages;
        mContext = context;

        // Set the user name
        loadUserName();
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


    private void updateViewHolderElements(ViewHolder viewHolder, Message message) {

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
        final TextView numberRatingTextView = viewHolder.numberRatingTextView;

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


        // Number of ratings
        numberRatingTextView.setText("Total : " + Integer.toString(message.getNumberRatings()));

        nameTextView.setText(message.getName());
        nameTextView.setVisibility(View.VISIBLE);

        String displayName = String.format(Locale.US, "%s", message.getName());
        nameTextView.setText(displayName + messageDateString);
    }

    private void loadUserName() {

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference("users");

        // Get current user
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        databaseReferenceUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                information = dataSnapshot.getValue(UserInformation.class);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }


    private void setRatings(RatingBar ratingBar, final TextView meanRatingTextView, final Message message) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        DatabaseReference usersRefMessages = ref.child("messages");
        final DatabaseReference thisMessageRef = usersRefMessages.child(message.getPushKey());


        ratingBar.setRating(message.getRating());
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {


                // Update total count. Update users that rated the content
                onStarClicked(thisMessageRef, ratingBar, meanRatingTextView, rating);

            }
        });
    }

    private void onStarClicked(DatabaseReference postRef, final RatingBar ratingBar,
                               final TextView meanRatingTextView, final float rating) {

        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {


                float actualRating;

                Message p = mutableData.getValue(Message.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }


                if (!p.getRaters().contains(information.getUsername())) {


                    if (p.numberRatings == 0) {

                        actualRating = rating;

                    } else {

                        actualRating = (p.getNumberRatings() * p.getRating() + rating) / (p.getNumberRatings() + 1);

                    }

                    p.addRater(information.getUsername());
                    int numberRatings = p.getNumberRatings();
                    p.numberRatings = numberRatings + 1;


                    ratingBar.setRating(actualRating);
                    p.setRating(actualRating);

                    // Set value and report transaction success
                    mutableData.setValue(p);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
                ratingBar.setVisibility(View.GONE);
                meanRatingTextView.setText("Mean : " + Float.toString(rating));
                meanRatingTextView.setVisibility(View.VISIBLE);
                meanRatingTextView.setEnabled(false);
            }
        });
    }


    public void onBindViewHolder(MessagesAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final Message message = mMessages.get(position);
        final RatingBar ratingBar = viewHolder.ratingBar;
        final TextView meanRatingTextView = viewHolder.meanRatingTextView;

        setRatings(ratingBar, meanRatingTextView, message);
        // Update viewHolder
        updateViewHolderElements(viewHolder, message);

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
        public TextView numberRatingTextView;
        public ImageView photoImageView;
        public LinearLayout linearLayout;
        public ProgressBar progressBarImage;
        public RatingBar ratingBar;
        public TextView meanRatingTextView;

        private Context context;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(Context context, View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = itemView.findViewById(R.id.nameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            numberRatingTextView = itemView.findViewById(R.id.numberratings);

            photoImageView = itemView.findViewById(R.id.photoImageView);

            progressBarImage = itemView.findViewById(R.id.progressBarImage);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            meanRatingTextView = itemView.findViewById(R.id.meanratings);

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