package com.example.applicationname;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = null;
    EditText email, pwd, name, phone, id_card;
    ImageView profileImage;
    Button register;
    TextView signIn;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;
    FirebaseFirestore firestore;
    String userID;
    String imageURL;
    public Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.txt_email);
        pwd = findViewById(R.id.txt_password);
        register = findViewById(R.id.btn_register);
        name = findViewById(R.id.txt_name);
        phone = findViewById(R.id.txt_phone);
        id_card = findViewById(R.id.txt_idCard);
        profileImage = findViewById(R.id.profile_Image);

        signIn = findViewById(R.id.signIn);
        progressBar = findViewById(R.id.progressbar);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        //to sign In activity
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });

        //registering the user
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        final String userEmail = email.getText().toString().trim();
        String userPassword = pwd.getText().toString().trim();
        final String userName = name.getText().toString();
        final String userPhone = phone.getText().toString();
        final String userId_card = id_card.getText().toString();

        if (userEmail.isEmpty()){
            email.setError("Email Required");
            email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
            email.setError("Enter a valid Email");
            email.requestFocus();
        }
        if (userPassword.isEmpty()){
            pwd.setError("Password Required");
            pwd.requestFocus();
            return;
        }
        if (userPassword.length()<6){
            pwd.setError("Strong Password Required");
            pwd.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()){

                    finish();
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));

                    //saving user data with user id
                    userID = mAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = firestore.collection("owner").document(userID);
                    Map<String,Object> ownerData = new HashMap<>();
                    ownerData.put("fName", userName);
                    ownerData.put("uPhone", userPhone);
                    ownerData.put("uIDCard", userId_card);
                    ownerData.put("uEmail", userEmail);
                    ownerData.put("uProfileImage", imageUri);

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && imageUri != null){
                        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName)
                                .setPhotoUri(Uri.parse(String.valueOf(imageUri)))
                                .build();
                        user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(RegisterActivity.this, "Profile Setup", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    documentReference.set(ownerData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: user profile is created for " + userID );
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: " + e.toString());
                        }
                    });

                }else{
                    if (task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(getApplicationContext(), "User Already Exist", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data!= null && data.getData() != null){
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);

                //Uploading Image
                StorageReference storageReference =
                        FirebaseStorage.getInstance().getReference("profilepics/" +System.currentTimeMillis() + ".jpg");
                if (imageUri != null){
                    storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageURL = taskSnapshot.getMetadata().toString();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            //profileImage.setImageURI(imageUri);
        }
    }
}
