package com.example.marcustech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminRegister extends AppCompatActivity {

    public static final String TAG = "TAG";
    private EditText signupPassword, mFullName, mEmail, mPhone;
    Button signUpBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String adminID;
    private boolean isPasswordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        mFullName = findViewById(R.id.full_name_edit_text);
        mEmail = findViewById(R.id.email_edit_text);
        mPhone = findViewById(R.id.phone_edit_text);
        signupPassword = findViewById(R.id.password_edit_text);
        signUpBtn = findViewById(R.id.sign_up_btn);
        mLoginBtn = findViewById(R.id.redirect_to_login);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);
        final ImageView eyeIcon = findViewById(R.id.eye_icon);

        if(fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), AdminDashboard.class));
            finish();
        }
        isPasswordVisible = false;

        eyeIcon.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Switch to password hidden
                signupPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                eyeIcon.setImageResource(R.drawable.eye_icon);
                isPasswordVisible = false;
            } else {
                // Switch to password visible
                signupPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                eyeIcon.setImageResource(R.drawable.eye_visibility_on);
                isPasswordVisible = true;
            }

            // Move cursor to the end of the text
            signupPassword.setSelection(signupPassword.getText().length());
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();
                String fullName = mFullName.getText().toString();
                String phone = mPhone.getText().toString();

                if(TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required!");
                    return;
                }

                if(TextUtils.isEmpty(password)) {
                    signupPassword.setError("Password is Required!");
                    return;
                }

                if(password.length() < 5){
                    signupPassword.setError("Password should be more than 5 characters!");
                }
                progressBar.setVisibility(View.VISIBLE);

                //register the admin in firebase
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            FirebaseUser fAdmin = fAuth.getCurrentUser();
                            fAdmin.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(AdminRegister.this, "Verification Email has been Sent.", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"onFailure: Email not sent " + e.getMessage());
                                }
                            });

                            Toast.makeText(AdminRegister.this,"Admin Created", Toast.LENGTH_SHORT).show();
                            adminID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("admin").document(adminID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("fName",fullName);
                            user.put("email",email);
                            user.put("phone",phone);
                            documentReference.set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d(TAG, "onSuccess: Admin Profile is created for " + adminID);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "onFailure: Error creating admin profile", e);
                                        }
                                    });
                            startActivity(new Intent(getApplicationContext(), AdminLogin.class));

                        }else {
                            Toast.makeText(AdminRegister.this,"Error!" + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
        }
    });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AdminLogin.class));
            }
        });

    }
}