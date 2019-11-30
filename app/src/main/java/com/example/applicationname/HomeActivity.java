package com.example.applicationname;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "Tag";
    FirebaseAuth mAuth;
    TextView email, name, phone, id_card;
    ImageView profileImage;
    Button logout;
    private Object Tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        name = findViewById(R.id.txt_name);
        email = findViewById(R.id.txt_email);
        phone = findViewById(R.id.txt_phone);
        profileImage = findViewById(R.id.profile_Image);
        logout = findViewById(R.id.log_out);

        mAuth = FirebaseAuth.getInstance();

        //Hello

        loadProfile();

        logout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
            }
        });

    }

    private void loadProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            if (user.getPhotoUrl() != null){

                Glide.with(this).load(user.getPhotoUrl().toString()).into(profileImage);
                Log.d(TAG, "onSuccess: user profile Image Loaded " + profileImage );

            }
            if (user.getDisplayName() != null){
               name.setText(user.getDisplayName());
            }
            if (user.getEmail() != null){
                email.setText(user.getEmail());
            }
            if (user.getPhoneNumber() != null){
                phone.setText(user.getPhoneNumber());
                Log.d("On Success: phone",""+ user.getPhoneNumber());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth == null){
            finish();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        }
    }
}
