package com.example.ncarvalho.story2tell;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    String profileName;
    DatabaseReference databaseReferenceRatings;
    RecyclerView ratingsRecycler;
    RecyclerView.Adapter adapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        removeBar();


        // Set profile Name
        TextView profileNameTextView = findViewById(R.id.profileName);
        profileName = getIntent().getStringExtra("EXTRA_SESSION_ID");
        profileNameTextView.setText("Hola " + profileName + "!");


        ratingsRecycler = findViewById(R.id.userQualitiesRecycler);
        ratingsRecycler.setHasFixedSize(true);
        ratingsRecycler.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
        listenToRatings();

        // Listen to changes in your ratings

    }

    public void listenToRatings(){

        databaseReferenceRatings = FirebaseDatabase.getInstance()
                .getReference("user/" + profileName);

        databaseReferenceRatings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                ArrayList<UserQualities> mlist = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    dataSnapshot.getRef().toString();
                    UserQualities userQualities = dataSnapshot.getValue(UserQualities.class);

                    mlist.add(userQualities);

                }

                adapter = new RatingsAdapter(ProfileActivity.this, mlist) {
                };

                ratingsRecycler.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });

    }



    //Remove the title
    public void removeBar(){
        try{
            getSupportActionBar().hide();}
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
