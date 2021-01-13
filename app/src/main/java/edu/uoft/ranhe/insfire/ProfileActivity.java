package edu.uoft.ranhe.insfire;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private String userID;
    private String profileImgUrl;

    private TextView txtUsername, txtBio;
    private ImageView imgProfile;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        displayUserInfo();

    }


    private void displayUserInfo() {


        userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        DocumentReference userDocRef = db.collection("users").document(userID);

        userDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        txtUsername.setText(documentSnapshot.getString("username"));
                        txtBio.setText(documentSnapshot.getString("bio"));
                        profileImgUrl = documentSnapshot.getString("profileImgUrl");

                        //load profile image
//                        StorageReference storageRef = storage.getReference();
//                        StorageReference gsReference = storage.getReferenceFromUrl(profileImgUrl);
                        try {
//                            System.out.println("!!!!!!!!!!!!!!!!!!00000000000000000000000#########000000000000000000");
//                            System.out.println(profileImgUrl);

                            RequestOptions options = new RequestOptions()
                                    .placeholder(R.drawable.profile2)
                                    .error(R.drawable.profile);

                            Glide.with(ProfileActivity.this).load(profileImgUrl).apply(options).into(imgProfile);

//                            Glide.with( ProfileActivity.this)
//                                    .load(profileImgUrl)
//                                    .into(imgProfile);

//                            Glide.with(getApplicationContext()).asBitmap().load(downloadUri).into(new CustomTarget<Bitmap>() {
//
//                                @Override
//                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                                    System.out.println("@@@@@@@@@@@@@@@@@@@@@@");
//                                    imgProfile.setImageBitmap(resource);
//
//
//                                }
//
//                                @Override
//                                public void onLoadCleared(@Nullable Drawable placeholder) {
//
//
//                                }
//                            });


//                            Glide.with(ProfileActivity.this)
//                                    .load(profileImgUrl)
//                                    .into(imgProfile);
                        } catch (Exception e) {
                            //TODO
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                    }
                });


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menuLogout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        }
    }


    private void initViews() {
        Log.d(TAG, "initViews: Started");
        toolbar = ActivityCompat.requireViewById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        imgProfile = findViewById(R.id.imgProfile);
        txtUsername = findViewById(R.id.txtUsername);
        txtBio = findViewById(R.id.txtBio);
    }

}