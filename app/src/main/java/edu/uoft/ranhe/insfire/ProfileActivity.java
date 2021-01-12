package edu.uoft.ranhe.insfire;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private String userID;

    private TextView txtUsername, txtBio;
    private ImageView imgProfile;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        displayUserInfo();

    }


    private void displayUserInfo() {


        userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        System.out.println("!!!!!!!!!!!!!!!");
        System.out.println(userID);

        System.out.println("*******************************");

//        String docID = db.collection("users").document().get().;
//        System.out.println(docID);
        DocumentReference userDocRef = db.collection("users").document(userID);

        userDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        System.out.println("2222222222222222222");
                        System.out.println(documentSnapshot.getString("username"));
                        txtUsername.setText(documentSnapshot.getString("username"));
                        txtBio.setText(documentSnapshot.getString("bio"));
//                        edtTxtUsername.setText(documentSnapshot.getString("profileImgUrl"));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                    }
                });

//        documentReference.addSnapshotListener(ProfileActivity.this, new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                System.out.println("2222222222222222222");
//                System.out.println(documentSnapshot.getString("username"));
//                txtUsername.setText(documentSnapshot.getString("username"));
//                txtBio.setText(documentSnapshot.getString("bio"));
////
//            }
//        });
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