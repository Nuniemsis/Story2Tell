package com.example.ncarvalho.story2tell;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.widget.LinearLayout.VERTICAL;

public class MainActivity extends AppCompatActivity {


    private Uri mCropImageUri;
    FirebaseAuth.AuthStateListener mAuthListener;
    Button messageButton;
    Button mPhotoPickerButton;
    FirebaseAuth mAuth;
    GoogleApiClient mGoogleApiClient;
    EditText messageEditText;
    RecyclerView messageRecycler;
    int resAnimId;
    SwipeRefreshLayout mSwipeRefreshLayout;
    boolean firstTime;

    DatabaseReference databaseReferenceMessages;
    ProgressDialog progressDialog;
    RecyclerView.Adapter adapter ;
    UserInformation information;
    DatabaseReference databaseReferenceUsers;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;
    FirebaseDatabase database;
    DatabaseReference usersRefMessages;
    DateFormat simpleDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set recyclerView for the first time
        firstTime = true;

        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-MM-SS");
        mAuth = FirebaseAuth.getInstance();
        mPhotoPickerButton = findViewById(R.id.send_photo);
        mSwipeRefreshLayout = findViewById(R.id.activity_main_swipe_refresh_layout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
             @Override
             public void onRefresh() {
                 refreshContent();
                 mSwipeRefreshLayout.setRefreshing(false);
             }
         });
        // Listen to the user name

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference("users");

        database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        usersRefMessages = ref.child("messages");

        mFirebaseStorage = FirebaseStorage.getInstance();
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        FirebaseUser user = mAuth.getCurrentUser();
        databaseReferenceUsers.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                information = snapshot.getValue(UserInformation.class);

            }
            @Override
            public void onCancelled(DatabaseError databaseError){
            }
        });

        messageButton = findViewById(R.id.send_message);
        messageEditText = findViewById(R.id.messageEditText);
        messageButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                if(!messageEditText.getText().toString().trim().equals("") &
                        messageEditText.getText().toString().trim().length() <
                                getApplicationContext().getResources().getInteger(R.integer.max_characters)) {
                    saveMessage(information.getUsername(), messageEditText.getText().toString().trim());

                }
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,
                new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this, "Something went wrong",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        ).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();


        // Toolbar
        Toolbar myToolbar = findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(myToolbar);

        // Recycler View

        messageRecycler = findViewById(R.id.messageRecycler);
        messageRecycler.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this,
                VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        messageRecycler.setLayoutManager(linearLayoutManager);
        messageRecycler.scrollToPosition(50);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading Data");
        progressDialog.show();

        databaseReferenceMessages = FirebaseDatabase.getInstance().getReference("messages");
        databaseReferenceMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                ArrayList<Message> mlist = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    mlist.add(message);
                }
                
                adapter = new MessagesAdapter(MainActivity.this, mlist) {
                };

                // Setting recycler for the first time
                if(firstTime) {
                    messageRecycler.setAdapter(adapter);
                    progressDialog.dismiss();
                    firstTime = false;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });


        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                onSelectImage(view);
            }
        });
    }
    public void onSelectImage(View view){
        CropImage.startPickImageActivity(this);

    }

    private void refreshContent(){
        messageRecycler.setAdapter(adapter);

        // Set the animation
        resAnimId = R.anim.layout_animation_fall_down;
        LayoutAnimationController animation = AnimationUtils
                .loadLayoutAnimation(getApplicationContext(), resAnimId);

        messageRecycler.setLayoutAnimation(animation);
        progressDialog.dismiss();
    }


    private void saveMessage(String username, String message){

        DatabaseReference messagePush = usersRefMessages.push();
        String pushKey = messagePush.getKey();

        // Save the message in firebase
        String date = simpleDateFormat.format(new Date());
        Message mmessage = new Message(username, message, date, null, pushKey);

        // Clear editText
        messageEditText.setText("");
        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT)
            .show();

        // Store message in database
        messagePush.setValue(mmessage);
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        //put something you want to happen here eg.
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater_settings = getMenuInflater();
        inflater_settings.inflate(R.menu.options, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected

            // TODO : Link this with settings and profile page
            case R.id.Profile:
                Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT)
                        .show();

                Intent profileIntent = new Intent(new Intent(MainActivity.this,
                        ProfileActivity.class));
                profileIntent.putExtra("EXTRA_SESSION_ID", information.getUsername());
                startActivity(profileIntent);

                break;

            // action with ID action_settings was selected
            case R.id.logout:
                signOut();

                break;

            // action with ID action_settings was selected
            case R.id.search:
                Toast.makeText(this, "Search selected", Toast.LENGTH_SHORT)
                        .show();
                Intent searchIntent = new Intent(new Intent(MainActivity.this,
                        SearchActivity.class));
                searchIntent.putExtra("EXTRA_SESSION_ID", information.getUsername());
                startActivity(searchIntent);

                break;
            default:
                break;
        }

        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // required permissions granted, start crop image activity
            startCropImageActivity(mCropImageUri);
        } else {
            Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);

            }
        }



        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri selectedImageUri = result.getUri();

            if (resultCode == RESULT_OK) {

                try {

                    StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

                    // Store photo
                    photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String date = new SimpleDateFormat("dd-MM-yyyy-HH-MM-SS").format(new Date());
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            DatabaseReference usersRefPushMessages= usersRefMessages.push();
                            String pushKey = usersRefPushMessages.getKey();

                            Message message = new Message(information.getUsername(), null , date , downloadUrl.toString(), pushKey);
                            usersRefPushMessages.setValue(message);
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(MainActivity.this, "Something went wrong",
                                    Toast.LENGTH_SHORT).show(); // Handle any errors
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                }

                Toast.makeText(
                        this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG)
                        .show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(imageUri.getPath()).getAbsolutePath(), options);

        double realHeight = options.outHeight;
        double realWidth = options.outWidth;

        int maxImageHeight = (int) realHeight;
        int maxImageWidth = (int) realWidth;
        int minImageWidth = (int) realHeight/2;
        int minImageHeight = (int) realWidth/2;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        CropImage.activity(imageUri)
                .setMultiTouchEnabled(true)
                .setFixAspectRatio(true)
                .setMinCropResultSize(minImageWidth, minImageHeight)
                .setMaxCropResultSize(maxImageWidth, maxImageHeight)
                .setAutoZoomEnabled(true)
                .start(this);
    }
}
