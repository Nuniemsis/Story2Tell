package com.example.ncarvalho.story2tell;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    RecyclerView searchRecycler;
    DatabaseReference databaseReferenceSearch;
    RecyclerView.Adapter adapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        removeBar();

        // Set profile Name
        searchRecycler = findViewById(R.id.userSearchRecycler);
        searchRecycler.setHasFixedSize(true);
        searchRecycler.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        listenToUsers();

    }

    public void listenToUsers(){

        databaseReferenceSearch = FirebaseDatabase.getInstance()
                .getReference("user");

        databaseReferenceSearch.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                ArrayList<UserQualities> mlist = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Take the first element
                    Object obj =  ((HashMap) dataSnapshot.getValue()).entrySet().iterator().next();


                    UserQualities userQualities = new UserQualities(obj);

                    Log.d("User key", dataSnapshot.getKey());
                    Log.d("User ref", dataSnapshot.getRef().toString());
                    Log.d("User val", dataSnapshot.getValue().toString());

                    mlist.add(userQualities);

                }

                adapter = new SearchAdapter(SearchActivity.this, mlist) {
                };

                searchRecycler.setAdapter(adapter);

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
