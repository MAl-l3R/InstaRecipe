package com.example.instarecipe.ui.addRecipePage;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.instarecipe.Database;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AppSettingsDialog;
import static android.app.Activity.RESULT_OK;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.Context;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.instarecipe.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class AddRecipeFragment extends Fragment {
    private static final int CHOOSE_PHOTO = 101;
    public boolean CameraPermission = false;
    public boolean ReadMediaPermission = false;
    public boolean ReadStoragePermission = false;

    String[] PERMISSIONS;
    ImageView food_pic;
    EditText foodNameEditText, hoursEditText, minEditText, ingredEditText, recipeEditText;
    ScrollView addRecipeFragment;
    CircleImageView profilePicView;
    TextView usernameView;
    MaterialButton addBtn;
    Uri imageUri, finalUri;
    String currentPhotoPath, uid, username, profilePic;
    ProgressBar progressBar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_addrecipe, container, false);

        // Get current user
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        uid = currentUser.getUid();

        // Get the user's file
        DocumentReference userFile = db.collection("users").document(uid);
        userFile.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        addRecipeFragment = view.findViewById(R.id.addrecipeFragment);
                        foodNameEditText = view.findViewById(R.id.food_name);
                        profilePicView = view.findViewById(R.id.profile_pic);
                        usernameView = view.findViewById(R.id.comment);
                        hoursEditText = view.findViewById(R.id.cook_time);
                        minEditText = view.findViewById(R.id.cook_time_min);
                        ingredEditText = view.findViewById(R.id.ingredients);
                        recipeEditText = view.findViewById(R.id.recipe_text);
                        addBtn = view.findViewById(R.id.add_button);
                        food_pic = view.findViewById(R.id.food_pic);
                        progressBar = view.findViewById(R.id.progressBar);

                        // Retrieve user info
                        profilePic = documentSnapshot.getString("profilePic");
                        username = documentSnapshot.getString("username");

                        // Display the retrieved info
                        Glide.with(getActivity()).load(profilePic).into(profilePicView);
                        usernameView.setText(username);
                        // Display all together after all info has been fetched
                        addRecipeFragment.setVisibility(View.VISIBLE);

                        // Get Permissions
                        PERMISSIONS = new String[] { CAMERA, READ_MEDIA_IMAGES, READ_EXTERNAL_STORAGE };
                        if (!hasPermissions(getContext(), PERMISSIONS)) {
                            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, 1);
                        }

                        // Add food picture
                        food_pic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                pickImage();
                            }
                        });

                        // Add Recipe Button
                        addBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                uploadToFirebase();
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the exception if the retrieval fails
                        // This could occur due to permission issues or network problems
                        System.out.println(e.getMessage());
                    }
                });

        return view;
    }


    private String getCookTime(String hh, String mm) {
        String time = "";
        int hours = Integer.parseInt(hh);
        int min = Integer.parseInt(mm);
        int minToHours;

        // Convert to hours
        if (min >= 60) {
            minToHours = (int) Math.floor(min / 60);
            hours += minToHours;
            min -= minToHours * 60;
        }

        // Create cook time string
        if (hours > 0 && min == 0) {
            time += hours + " h";
        } else if (hours == 0 && min > 0) {
            time += min + " m";
        } else if (hours == 0 && min == 0) {
            time = "Instant";
        } else {
            time += hours + " h " + min + " m";
        }
        return time;
    }

    private void uploadToFirebase() {
        String foodName = foodNameEditText.getText().toString().trim();
        String hours = hoursEditText.getText().toString().trim();
        String minutes = minEditText.getText().toString().trim();
        String ingredients = ingredEditText.getText().toString().trim();
        String recipe = recipeEditText.getText().toString().trim();

        // Validate the data
        if (foodName.isEmpty()) {
            foodNameEditText.setError("Please give your recipe a name");
        } else if (hours.isEmpty()) {
            hoursEditText.setError("Please enter number of hours needed to cook (0 if none)");
        } else if (minutes.isEmpty()) {
            minEditText.setError("Please enter number of minutes needed to cook (0 if none)");
        } else if (ingredients.isEmpty()) {
            ingredEditText.setError("Please list the ingredients");
        } else if (recipe.isEmpty()) {
            recipeEditText.setError("Please type in your recipe");
        } else {
            String cookTime = getCookTime(hours, minutes);

            // Now upload the recipe to firebase
            if (finalUri != null) {
                Database.addRecipe(uid, finalUri, foodName, cookTime, ingredients, recipe, Timestamp.now(), progressBar, getContext());

                // Reset all fields
                foodNameEditText.setText("");
                hoursEditText.setText("");
                minEditText.setText("");
                ingredEditText.setText("");
                recipeEditText.setText("");
                food_pic.setImageResource(R.drawable.add_picture);
                finalUri = null;

            } else {
                Toast.makeText(getContext(), "Please upload an image", Toast.LENGTH_SHORT).show();
            }

        }
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
        CameraPermission = ContextCompat.checkSelfPermission(getContext(), CAMERA) == PERMISSION_GRANTED;
        ReadMediaPermission = ContextCompat.checkSelfPermission(getContext(), READ_MEDIA_IMAGES) == PERMISSION_GRANTED;
        ReadStoragePermission = ContextCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED;

        // Check android version first
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            // This code will be executed on devices running android version Tiramisu or later

            // Check camera and storage permissions
            if (CameraPermission && ReadMediaPermission) {
                // Start Image Picker if all permissions granted
                launchIntent();
            } else {
                // Open App Settings if all permissions were not granted
                new AppSettingsDialog.Builder(getActivity()).build().show();
            }

        } else {
            // This code will be executed on older devices

            // Check camera and storage permissions
            if (CameraPermission && ReadStoragePermission) {
                // Start Image Picker if all permissions granted
                launchIntent();
            } else {
                // Open App Settings if all permissions were not granted
                new AppSettingsDialog.Builder(getActivity()).build().show();
            }

        }
    }

    private void launchIntent() {
        // Preserve image quality by saving the image first
        String fileName = "InstaRecipePhoto";
        File storageDirectory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            // Save the image in external directory and then get the uri (needed  for good camera image quality)
            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();

            // Now get the uri (needed  for good camera image quality)
            Uri uri = FileProvider.getUriForFile(getContext().getApplicationContext(), "com.example.instarecipe.fileprovider", imageFile);

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
        Uri destinationUri = Uri.fromFile(new File(getActivity().getCacheDir(), destinationFileName));

        // Crop Photo page customization
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(75);
        options.setMaxBitmapSize(10000);
        options.setToolbarColor(Color.parseColor("#20242F"));
        options.setStatusBarColor(Color.parseColor("#20242F"));
        options.setToolbarWidgetColor(Color.parseColor("#D5DBE4"));

        // Start Crop Photo page
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(16, 9)
                .withMaxResultSize(1000, 1000)
                .withOptions(options)
                .start(getContext(), this, UCrop.REQUEST_CROP);
    }

    private Uri bitmapToUri(Bitmap image, Context context) {
        File imagesFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;

        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "captured_image.jpg");
            FileOutputStream stream = new FileOutputStream(file);

            // Rotate the image right by 90 degrees
            Matrix matrix = new Matrix();
            matrix.postRotate(90); // Specify the desired rotation angle (90 for right rotation)
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
                imageUri = bitmapToUri(photo, getContext());

                // Crop image
                cropPhoto(imageUri);
            }

        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            // Use cropped image
            finalUri = UCrop.getOutput(data);
            food_pic.setImageURI(finalUri);

        } else if (resultCode == UCrop.RESULT_ERROR) {
            // If there was an error in the Crop Photo page, show error
            Toast.makeText(getContext(), ""+UCrop.getError(data).getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}
