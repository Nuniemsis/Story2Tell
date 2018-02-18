package com.example.ncarvalho.story2tell;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    private final static int RC_SIGN_IN = 2;
    SignInButton button;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    private EditText editTextPassword;
    private EditText editTextName;

    ProgressDialog progressDialog;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference usersRef;

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeBar();
        setContentView(R.layout.activity_login);

        button = (SignInButton) findViewById(R.id.googleBtn);
        mAuth = FirebaseAuth.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = mFirebaseDatabase.getReference();

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                // TODO: Comprobar si el nombre ya existe. En ese caso, hacer un toast que ponga que el nombre para esa cuenta es incorrecto
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Trying to login in");
                progressDialog.show();
                signIn();

            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    FirebaseUser user = mAuth.getCurrentUser();
                    DatabaseReference mDatabase;

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
                        Toast.makeText(LoginActivity.this, "Something went wrong",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        ).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();


    }

    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void saveUserInformation(String name, String password){
        UserInformation userInformation = new UserInformation(name, password);
        FirebaseUser user = mAuth.getCurrentUser();


        // Save username and passwprd separately. Key is getUid()
        databaseReference.child("users").child(user.getUid()).setValue(userInformation);

        // Save initial user qualities. Key is username, then a random String(date ordered)
        String date = new SimpleDateFormat("dd-MM-yyyy-HH-MM-SS").format(new Date());

        UserQualities UserQualities = new UserQualities(name, date);

        databaseReference.child("user").child(name).push().setValue(UserQualities);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(LoginActivity.this, "Algo va mal",
                        Toast.LENGTH_SHORT).show();

            }
        }
    }

    //Remove the titles
    public void removeBar(){
        try{
            getSupportActionBar().hide();}
        catch (Exception e){
            e.printStackTrace();
        }
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
        progressDialog.dismiss();
        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        //put something you want to happen here eg.

                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");

                            // Get the user
                            FirebaseUser user = mAuth.getCurrentUser();


                            usersRef = FirebaseDatabase.getInstance().getReference().child("users");


                            usersRef.child(user.getUid()).
                                    addValueEventListener(new ValueEventListener() {
                                        @Override public void onDataChange (DataSnapshot dataSnapshot){
                                            // code here does not get executed straight away,
                                            // it gets executed whenever data is received back from the remote database

                                            try{
                                            UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);


                                                if (userInformation.getUsername() != null) {

                                                    editTextName.setText(userInformation.getUsername());
                                                    editTextPassword.setText(userInformation.getPassword());
                                                    progressDialog.dismiss();
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                }
                                            }catch (Exception e){

                                                e.printStackTrace();
                                                String name = (String) editTextName.getText().toString();
                                                String password = (String) editTextPassword.getText().toString();

                                                if (name.equals("")) {
                                                    Toast.makeText(LoginActivity.this, "Pick a password and an username please",
                                                            Toast.LENGTH_SHORT).show();
                                                    signOut();

                                                }
                                                else{

                                                    if(password.equals("")){
                                                        Toast.makeText(LoginActivity.this, "Empty password",
                                                                Toast.LENGTH_SHORT).show();
                                                        signOut();


                                                    }else {
                                                        // Starting for the first time
                                                        saveUserInformation(name, password);

                                                        Intent mainIntent = new Intent(LoginActivity.this,
                                                                MainActivity.class);

                                                        progressDialog.dismiss();
                                                        startActivity(mainIntent);


                                                    }
                                                }

                                            }

                                        }

                                        @Override public void onCancelled (DatabaseError databaseError){

                                        }
                                    });

                        }
                        }

                        // ...
                    }
                );
    }


}

