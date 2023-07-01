package com.example.instarecipe.ui.profilePage;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.instarecipe.Database;
import com.example.instarecipe.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AppSettingsDialog;

public class ProfileSetupActivity extends AppCompatActivity {

    private static final int CHOOSE_PHOTO = 101;
    public boolean CameraPermission = false;
    public boolean ReadMediaPermission = false;
    public boolean ReadStoragePermission = false;
    String[] PERMISSIONS;
    EditText nameET, usernameET, emailET;
    CircleImageView profilePic;
    ProgressBar progressBar;
    MaterialButton saveBtn;
    Uri imageUri, finalUri;
    String currentPhotoPath;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profilePic = findViewById(R.id.profile_pic);
        nameET = findViewById(R.id.name);
        usernameET = findViewById(R.id.comment);
        emailET = findViewById(R.id.email);
        progressBar = findViewById(R.id.progressBar);
        saveBtn = findViewById(R.id.save_btn);

        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get saved name, username, and email of the user
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)  {
            if (bundle.getString("name") != null && bundle.getString("username") != null && bundle.getString("profilePic") != null) {
                Glide.with(ProfileSetupActivity.this).load(bundle.getString("profilePic")).into(profilePic);
                nameET.setText(bundle.getString("name"));
                usernameET.setText(bundle.getString("username"));
            }

            if (bundle.getString("email") != null) {
                emailET.setText(bundle.getString("email"));
            }
        }

        // Get permissions
        PERMISSIONS = new String[] { CAMERA, READ_MEDIA_IMAGES, READ_EXTERNAL_STORAGE };
        if (!hasPermissions(ProfileSetupActivity.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(ProfileSetupActivity.this, PERMISSIONS, 1);
        }

        // Add profile picture
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });

        // Save Button
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the data
                String name = nameET.getText().toString().trim();
                String username = usernameET.getText().toString().toLowerCase().trim();
                String email = emailET.getText().toString().trim();

                // Validate the data
                if (name.isEmpty()) {
                    nameET.setError("Please enter your name");
                } else if (username.isEmpty()) {
                    usernameET.setError("Please enter your username");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailET.setError("Please enter a valid email address");
                } else {
                    // Check if username is unique
                    db.collection("users").get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    boolean isUsernameUnique = true;

                                    // Scan through all users' files to see if username is already in use
                                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                        String uid = document.getId();
                                        String takenUsername = document.getString("username");

                                        if (!uid.equals(currentUser.getUid()) && takenUsername.equals(username)) {
                                            // Username already exists
                                            isUsernameUnique = false;
                                            break;
                                        }
                                    }

                                    if (isUsernameUnique) {
                                        // Username is unique, you can proceed with saving it
                                        Database.saveUserInfo(ProfileSetupActivity.this, currentUser, finalUri, name, username, email, progressBar);
                                    } else {
                                        // Username already exists, show an error message
                                        usernameET.setError("Username is already in use");
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println(e.getMessage());
                                }
                            });
                }
            }
        });
    }















    // ############################## //
    // ############################## //
    // IMAGE PICKER CODE STARTS BELOW //
    // ############################## //
    // ############################## //
    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void pickImage() {
        CameraPermission = ContextCompat.checkSelfPermission(ProfileSetupActivity.this, CAMERA) == PERMISSION_GRANTED;
        ReadMediaPermission = ContextCompat.checkSelfPermission(ProfileSetupActivity.this, READ_MEDIA_IMAGES) == PERMISSION_GRANTED;
        ReadStoragePermission = ContextCompat.checkSelfPermission(ProfileSetupActivity.this, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED;

        // Check android version first
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            // This code will be executed on devices running android version Tiramisu or later

            // Check camera and storage permissions
            if (CameraPermission && ReadMediaPermission) {
                // Start Image Picker if all permissions granted
                launchIntent();
            } else {
                // Open App Settings if all permissions were not granted
                new AppSettingsDialog.Builder(ProfileSetupActivity.this).build().show();
            }

        } else {
            // This code will be executed on older devices

            // Check camera and storage permissions
            if (CameraPermission && ReadStoragePermission) {
                // Start Image Picker if all permissions granted
                launchIntent();
            } else {
                // Open App Settings if all permissions were not granted
                new AppSettingsDialog.Builder(ProfileSetupActivity.this).build().show();
            }

        }
    }

    private void launchIntent() {
        // Preserve image quality by saving the image first
        String fileName = "InstaRecipePhoto";
        File storageDirectory = ProfileSetupActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            // Save the image in external directory and then get the uri (needed  for good camera image quality)
            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();

            // Now get the uri (needed  for good camera image quality)
            Uri uri = FileProvider.getUriForFile(ProfileSetupActivity.this.getApplicationContext(), "com.example.instarecipe.fileprovider", imageFile);

            // Gallery Intent
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");

            // Camera Intent
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            // Choose between Camera or Gallery
            Intent chooser = Intent.createChooser(galleryIntent, "Select source");
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent });

            startActivityForResult(chooser, CHOOSE_PHOTO);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void cropPhoto(Uri sourceUri) {
        // Create destination uri, where cropped image will be saved
        String destinationFileName = UUID.randomUUID().toString() + ".jpg";
        Uri destinationUri = Uri.fromFile(new File(ProfileSetupActivity.this.getCacheDir(), destinationFileName));

        // Crop Photo page customization
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(75);
        options.setMaxBitmapSize(10000);
        options.setToolbarColor(Color.parseColor("#20242F"));
        options.setStatusBarColor(Color.parseColor("#20242F"));
        options.setToolbarWidgetColor(Color.parseColor("#D5DBE4"));
        options.setCircleDimmedLayer(true);

        // Start Crop Photo page
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(1000, 1000)
                .withOptions(options)
                .start(ProfileSetupActivity.this, UCrop.REQUEST_CROP);
    }

    private Uri bitmapToUri(Bitmap image, Context context) {
        File imagesFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;

        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "captured_image.jpg");
            FileOutputStream stream = new FileOutputStream(file);

            // Rotate the image left by 90 degrees
            Matrix matrix = new Matrix();
            matrix.postRotate(-90); // Specify the desired rotation angle (-90 for left rotation)
            image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

            // Compress image
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();

            // Get the uri now
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.example.instarecipe.fileprovider", file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return uri;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Catch image
        if (requestCode == CHOOSE_PHOTO && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                // Image from gallery
                imageUri = data.getData();

                // Crop image
                cropPhoto(imageUri);

            } else {
                // Image from camera
                Bitmap photo = BitmapFactory.decodeFile(currentPhotoPath);

                // Convert bitmap to uri
                imageUri = bitmapToUri(photo, ProfileSetupActivity.this);

                // Crop image
                cropPhoto(imageUri);
            }

        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            // Use cropped image
            finalUri = UCrop.getOutput(data);
            profilePic.setImageURI(finalUri);

        } else if (resultCode == UCrop.RESULT_ERROR) {
            // If there was an error in the Crop Photo page, show error
            Toast.makeText(ProfileSetupActivity.this, ""+UCrop.getError(data).getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}
