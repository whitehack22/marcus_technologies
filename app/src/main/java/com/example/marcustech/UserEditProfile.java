package com.example.marcustech;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class UserEditProfile extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText profileFullName, profileEmail, profilePhone;
    ImageView profileImageView;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fStore;
    Button saveBtn;
    FirebaseUser user;
    StorageReference storageReference;
    Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    //profileImage.setImageURI(selectedImageUri);
                    uploadImageToFirebaseStorage(selectedImageUri); // Proceed to upload
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_edit_profile);

        Intent data = getIntent();
        String fullName = data.getStringExtra("fullName");
        String email = data.getStringExtra("email");
        String phone = data.getStringExtra("phone");

        firebaseAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        profileFullName = findViewById(R.id.editName);
        profileEmail = findViewById(R.id.editEmailAddress);
        profilePhone = findViewById(R.id.editPhone);
        profileImageView = findViewById(R.id.profile_photo);
        saveBtn = findViewById(R.id.save_button);

        StorageReference profileRef = storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImageView);
            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                selectImageLauncher.launch(intent);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (profileFullName.getText().toString().isEmpty() || profileEmail.getText().toString().isEmpty() ||profilePhone.getText().toString().isEmpty()){
                   Toast.makeText(UserEditProfile.this, "One or Many fields are empty.", Toast.LENGTH_SHORT).show();
               }

               String email = profileEmail.getText().toString();
               user.verifyBeforeUpdateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void unused) {
                       DocumentReference docRef = fStore.collection("users").document(user.getUid());
                       Map<String,Object> edited = new HashMap<>();
                       edited.put("email", email);
                       edited.put("fName", profileFullName.getText().toString());
                       edited.put("phone", profilePhone.getText().toString());
                       docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void unused) {
                               Toast.makeText(UserEditProfile.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                               startActivity(new Intent(getApplicationContext(), UserProfile.class));
                               finish();
                           }
                       });
                       Toast.makeText(UserEditProfile.this, "Email is changed", Toast.LENGTH_SHORT).show();
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(UserEditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               });
            }
        });

        profileEmail.setText(email);
        profileFullName.setText(fullName);
        profilePhone.setText(phone);

        Log.d(TAG,"onCreate: " + fullName + " " + email + " " + phone);
    }


    private void uploadImageToFirebaseStorage(Uri selectedImageUri) {
        //upload image to firebase storage
        StorageReference fileRef = storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImageView);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserEditProfile.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}