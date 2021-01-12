package edu.uoft.ranhe.insfire;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int CAMERA_CODE = 501;
    private static final int GALLERY_CODE = 502;

    private EditText edtTxtEmail, edtTxtPwd, edtTxtConfirmPsw, edtTxtUsername, edtTxtBio;
    private Button btnRegister;
    private TextView txtSignin;
    private ProgressBar progressBarRegister;
    private ImageView imgProfile, imgPlus;

    private Uri uriProfileImg = null;
    private Bitmap profileImgBitmap = null;

    private String profileImgUrl;
    private String userID;
    private String pathToFile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;
    private DocumentReference documentReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        txtSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });

        imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CAMERA_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    profileImgBitmap = (Bitmap) data.getExtras().get("data");
                    imgProfile.setImageBitmap(profileImgBitmap);

//                    uploadImgToFirebaseStorage(profileImgBitmap);
                }

            case GALLERY_CODE:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    uriProfileImg = data.getData();
                    try {
                        profileImgBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImg);
                        imgProfile.setImageBitmap(profileImgBitmap);

//                        uploadImgToFirebaseStorage_ori();
//                        uploadImgToFirebaseStorage(profileImgBitmap);
                        Toast.makeText(getApplicationContext(),
                                "Profile image selected", Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
//                e.printStackTrace();
                        Log.w(TAG, "Error setting profile image", e);
                    }
                }
        }
    }


    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_CODE);
    }

    private void launchGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), GALLERY_CODE);
    }

    private void showImageChooserOptions() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle("Choose an Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        launchCamera();
                        break;
                    case 1:
                        launchGallery();
                        break;
                }
            }
        });
        builder.create().show();
    }


    private void checkPermissions() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    showImageChooserOptions();
                }
                if (report.isAnyPermissionPermanentlyDenied()) {
                    Toast.makeText(getApplicationContext(), "Permission must be accepted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }


    //    private void useFirestore () {
//        FirebaseUser user = mAuth.getCurrentUser();
//
//        if (username.isEmpty()) {
//            edtTxtUsername.setError("Username is required");
//            edtTxtUsername.requestFocus();
//            return;
//        }
//
//        if (bio.isEmpty()) {
//            edtTxtBio.setError("Bio is required");
//            edtTxtBio.requestFocus();
//            return;
//        }
//
//    }


    private void registerUser() {
        Log.d(TAG, "registerViews: Started");
        String email = edtTxtEmail.getText().toString().trim();
        String pwd = edtTxtPwd.getText().toString().trim();
        String pwdConfirm = edtTxtConfirmPsw.getText().toString().trim();

        String username = edtTxtUsername.getText().toString();
        String bio = edtTxtBio.getText().toString();

        // validate email
        if (email.isEmpty()) {
            edtTxtEmail.setError("Email is required");
            edtTxtEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtTxtEmail.setError("Please enter a valid email");
            edtTxtEmail.requestFocus();
            return;
        }

        // validate password
        if (pwd.isEmpty()) {
            edtTxtPwd.setError("Password is required");
            edtTxtPwd.requestFocus();
            return;
        }
        if (pwd.length() < 6) {
            edtTxtPwd.setError("Password must be at least 6 characters");
            edtTxtPwd.requestFocus();
            return;
        }
        if (!pwdConfirm.equals(pwd)) {
            edtTxtConfirmPsw.setError("Please enter the same password");
            edtTxtConfirmPsw.requestFocus();
            return;
        }

//        // validate username
//        if (pwd.isEmpty()) {
//            edtTxtPwd.setError("Password is required");
//            edtTxtPwd.requestFocus();
//            return;
//        }
//        // validate bio
//        if (pwd.isEmpty()) {
//            edtTxtPwd.setError("Password is required");
//            edtTxtPwd.requestFocus();
//            return;
//        }
//        // validate profile image
//        if (pwd.isEmpty()) {
//            edtTxtPwd.setError("Password is required");
//            edtTxtPwd.requestFocus();
//            return;
//        }


        progressBarRegister.setVisibility(View.VISIBLE);

        // register the user using Firebase
        mAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
//                    uploadProfileImgUrlToFirestore();

//                    uploadImgToFirebaseStorage(profileImgBitmap);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    profileImgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] profileImgBitmap_data = baos.toByteArray();
                    mStorageRef = FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg");

                    mStorageRef.putBytes(profileImgBitmap_data).
                            addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    UploadTask.TaskSnapshot downloadUri = taskSnapshot.getTask().getResult();
                                    profileImgUrl = downloadUri.toString();

                                    progressBarRegister.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(),
                                            "Profile image upload to Firebase Storage successfully", Toast.LENGTH_SHORT).show();


                                    userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();  //TODO:NULL
                                    DocumentReference userRef = db.collection("users").document(userID);

                                    // Create a new user with a username and bio
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("username", username);
                                    user.put("bio", bio);
                                    user.put("profileImgUrl", profileImgUrl);

                                    // Add a new document with a generated ID
                                    userRef.set(user).
                                            addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getApplicationContext(), "User created", Toast.LENGTH_SHORT).show();

                                                    // user created successfully, go to the profile page
                                                    progressBarRegister.setVisibility(View.GONE);
                                                    finish();
                                                    Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Avoid the back button bring user to login again
                                                    startActivity(intent);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                                    Log.w(TAG, "Error adding document", e);
                                                }
                                            });

//                                    db.collection("users")
//                                            .add(user)
//                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                                @Override
//                                                public void onSuccess(DocumentReference documentReference) {
//                                                    Toast.makeText(getApplicationContext(), "User created", Toast.LENGTH_SHORT).show();
//                                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//
//                                                    // user created successfully, go to the profile page
//                                                    progressBarRegister.setVisibility(View.GONE);
//                                                    finish();
//                                                    Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
//                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Avoid the back button bring user to login again
//                                                    startActivity(intent);
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                                    Log.w(TAG, "Error adding document", e);
//                                                }
//                                            });


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBarRegister.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                } else {
                    progressBarRegister.setVisibility(View.GONE);
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "This email is already registered", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


    private void initViews() {
        Log.d(TAG, "initViews: Started");
        edtTxtEmail = findViewById(R.id.edtTxtEmail);
        edtTxtPwd = findViewById(R.id.edtTxtPwd);
        edtTxtConfirmPsw = findViewById(R.id.edtTxtConfirmPsw);
        edtTxtUsername = findViewById(R.id.edtTxtUsername);
        edtTxtBio = findViewById(R.id.edtTxtBio);

        btnRegister = findViewById(R.id.btnRegister);
        txtSignin = findViewById(R.id.txtSignin);
        progressBarRegister = findViewById(R.id.progressBarRegister);
        imgProfile = findViewById(R.id.imgProfile);
        imgPlus = findViewById(R.id.imgPlus);
    }

}