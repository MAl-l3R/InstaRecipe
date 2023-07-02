package com.example.instarecipe.ui.recipe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.instarecipe.Database;
import com.example.instarecipe.R;
import com.example.instarecipe.ui.explorePage.ExploreFragment;
import com.example.instarecipe.ui.explorePage.accountsTab.UserProfileFragment;
import com.example.instarecipe.ui.profilePage.ProfileFragment;
import com.example.instarecipe.ui.recipe.comments.CommentsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ortiz.touchview.TouchImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecipeFragment extends Fragment {

    String recipeID, searchQuery;
    final long TIME_THRESHOLD = 300;
    long lastClickTime = Long.valueOf(0);
    ImageView likeBtn, bigLike;
    TextView likeCount;
    Boolean alreadyLiked = false;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_recipe, container, false);

        // Retrieve the recipe UID and search query from the arguments
        recipeID = getArguments().getString("ID");
        searchQuery = getArguments().getString("searchQuery");

        // Get the user's file
        DocumentReference recipeFile = db.collection("recipes").document(recipeID);
        recipeFile.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        // Get all UI items
                        ScrollView recipeFragment = view.findViewById(R.id.recipeFragment);
                        ConstraintLayout recipeView = view.findViewById(R.id.recipe_view);
                        TouchImageView foodPicIV = view.findViewById(R.id.food_pic);
                        TextView foodNameTV = view.findViewById(R.id.food_name);
                        TextView timestampTV = view.findViewById(R.id.timestamp);
                        bigLike = view.findViewById(R.id.big_like);
                        likeBtn = view.findViewById(R.id.like_button);
                        likeCount = view.findViewById(R.id.like_count);
                        ImageView commentBtn = view.findViewById(R.id.comment_button);
                        ImageView shareBtn = view.findViewById(R.id.share_button);
                        CircleImageView profilePicIV = view.findViewById(R.id.profile_pic);
                        TextView usernameTV = view.findViewById(R.id.comment);
                        TextView cookTimeTV = view.findViewById(R.id.cook_time);
                        TextView ingredientsTV = view.findViewById(R.id.ingredients);
                        TextView recipeTV = view.findViewById(R.id.recipe_text);
                        TextView invisibleTV = view.findViewById(R.id.invisible_recipe_text);

                        // Check if user already liked this recipe
                        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DocumentReference followers = db.collection("users").document(currentUserUid).collection("likedRecipes").document(recipeID);
                        followers.get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();

                                            // Check if user already liked this recipe
                                            if (document.exists()) {
                                                // The document exists in the collection, i.e. user already liked this recipe
                                                alreadyLiked = true;

                                            } else {
                                                // The document does not exist in the collection
                                                alreadyLiked = false;
                                            }

                                            // Retrieve recipe info
                                            String foodPic = documentSnapshot.getString("imageURL");
                                            String name = documentSnapshot.getString("name");
                                            Timestamp timestamp = documentSnapshot.getTimestamp("timestamp");
                                            String createdByUid  = documentSnapshot.getString("uid");
                                            String cookTime = documentSnapshot.getString("cookTime");
                                            String ingredients = documentSnapshot.getString("ingredients");
                                            String recipe = documentSnapshot.getString("recipe");
                                            int likes = documentSnapshot.getLong("likes").intValue();

                                            // Fetch the username and profile pic as well and then set and display everything
                                            db.collection("users").document(createdByUid)
                                                    .get()
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            String username = documentSnapshot.getString("username");
                                                            String profilePic = documentSnapshot.getString("profilePic");

                                                            // Display the retrieved info
                                                            Glide.with(getActivity()).load(foodPic).into(foodPicIV);
                                                            foodNameTV.setText(name);
                                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                                                            timestampTV.setText(dateFormat.format(timestamp.toDate()));
                                                            Glide.with(getActivity()).load(profilePic).into(profilePicIV);
                                                            usernameTV.setText(username);
                                                            cookTimeTV.setText(cookTime);
                                                            ingredientsTV.setText(ingredients);
                                                            recipeTV.setText(recipe);
                                                            if (alreadyLiked) {
                                                                likeBtn.setImageResource(R.drawable.liked_icon);
                                                            } else {
                                                                likeBtn.setImageResource(R.drawable.like_icon);
                                                            }
                                                            if (likes > 100000000) {
                                                                likeCount.setText("100000000+");
                                                            } else {
                                                                likeCount.setText(String.valueOf(likes));
                                                            }
                                                            recipeFragment.setVisibility(View.VISIBLE);

                                                            // Go to user's profile if their username is clicked
                                                            usernameTV.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    launchUserProfile(createdByUid);
                                                                }
                                                            });

                                                            // Go to user's profile if their profile pic is clicked
                                                            profilePicIV.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    launchUserProfile(createdByUid);
                                                                }
                                                            });

                                                            // Double tap to like feature
                                                            recipeView.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    // Get current time
                                                                    long currentTime = System.currentTimeMillis();
                                                                    // If double tapped
                                                                    if (currentTime - lastClickTime < TIME_THRESHOLD) {
                                                                        incrementLike(currentUserUid, recipeID, "DoubleTap");
                                                                    }
                                                                    // Reset last click time to current time
                                                                    lastClickTime = currentTime;

                                                                }
                                                            });

                                                            // Single tap on like button to like feature
                                                            likeBtn.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    if (alreadyLiked) {
                                                                        decrementLike(currentUserUid, recipeID);
                                                                    } else {
                                                                        incrementLike(currentUserUid, recipeID, "SingleTap");
                                                                    }
                                                                }
                                                            });

                                                            // Comment button
                                                            commentBtn.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    // Create an instance of the destination fragment
                                                                    CommentsFragment commentsFragment = new CommentsFragment();
                                                                    // Pass the recipe ID as an argument to the CommentsFragment
                                                                    Bundle args = new Bundle();
                                                                    args.putString("recipeID", recipeID);
                                                                    args.putString("uid", currentUserUid);
                                                                    commentsFragment.setArguments(args);
                                                                    // Get the FragmentManager
                                                                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                                                    // Start a new FragmentTransaction
                                                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                                    // Replace the current fragment with the destination fragment
                                                                    fragmentTransaction.replace(R.id.frameLayout, commentsFragment);
                                                                    // Come back to current fragment on back pressed
                                                                    fragmentTransaction.addToBackStack(null);
                                                                    // Commit the transaction
                                                                    fragmentTransaction.commit();
                                                                }
                                                            });

                                                            // Share Button
                                                            shareBtn.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    // Setup bitmap for sharing the recipe image
                                                                    BitmapDrawable bitmapDrawable = (BitmapDrawable)foodPicIV.getDrawable();
                                                                    if (bitmapDrawable != null) {
                                                                        // Create an image containing the Recipe Image and the Recipe
                                                                        String recipeText = "Recipe Name: \n" +
                                                                                "\t" + name + "\n" +
                                                                                "\n" +
                                                                                "Created by: \n" +
                                                                                "\t" + username + "\n" +
                                                                                "\n" +
                                                                                "Cook Time: \n" +
                                                                                "\t" + cookTime + "\n" +
                                                                                "\n" +
                                                                                "Ingredients: \n" +
                                                                                "\t" + ingredients + "\n" +
                                                                                "\n" +
                                                                                "Recipe:\n" +
                                                                                "\t" + recipe;
                                                                        invisibleTV.setText(recipeText);

                                                                        // Measure the dimensions of the text view and calculate the required height
                                                                        invisibleTV.measure(View.MeasureSpec.makeMeasureSpec(invisibleTV.getWidth(), View.MeasureSpec.EXACTLY),
                                                                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                                                                        int width = invisibleTV.getMeasuredWidth();
                                                                        int height = invisibleTV.getMeasuredHeight();

                                                                        // Create a bitmap with the calculated dimensions
                                                                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                                                        Canvas canvas = new Canvas(bitmap);
                                                                        invisibleTV.layout(0, 0, width, height);
                                                                        invisibleTV.draw(canvas);

                                                                        // Load the recipe image bitmap
                                                                        Bitmap recipeImage = bitmapDrawable.getBitmap();

                                                                        // Create a new combined bitmap with the recipe image and the recipe
                                                                        Bitmap combinedBitmap = Bitmap.createBitmap(recipeImage.getWidth(), recipeImage.getHeight() + height, Bitmap.Config.ARGB_8888);
                                                                        Canvas combinedCanvas = new Canvas(combinedBitmap);
                                                                        combinedCanvas.drawBitmap(recipeImage, 0, 0, null);
                                                                        combinedCanvas.drawBitmap(bitmap, 0, recipeImage.getHeight(), null);

                                                                        // Get the combined image uri
                                                                        Uri bmpUri = saveImage(combinedBitmap, recipeID+".jpg", getContext());

                                                                        // Share Intent
                                                                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                                        shareIntent.setType("image/jpeg");
                                                                        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                                        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                                                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, name);
                                                                        startActivity(Intent.createChooser(shareIntent, "Share Recipe"));
                                                                    }
                                                                }
                                                            });

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            System.out.println(e.getMessage());
                                                        }
                                                    });

                                        } else {
                                            // An error occurred while retrieving the document
                                            // Handle the error appropriately
                                            System.out.println(task.getException().getMessage());
                                        }
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handle the back button
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!searchQuery.equals("N/A")) {
                    // Go back to Explore Fragment's Accounts tab and display previous search query
                    ExploreFragment exploreFragment = new ExploreFragment();
                    // Send the search query
                    Bundle args = new Bundle();
                    args.putString("searchQuery", searchQuery);
                    args.putString("backFrom", "recipeCard");
                    exploreFragment.setArguments(args);
                    // Perform the fragment transaction
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayout, exploreFragment);
                    fragmentTransaction.commit();
                } else {
                    // Default back functionality (go back as normal to the previous page)
                    this.setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    private void incrementLike(String uid, String recipeID, String likeMethod) {
        // Start animation for like button
        Animation likeZoomIn = AnimationUtils.loadAnimation(getContext(), R.anim.like_zoom_in);
        likeBtn.setImageResource(R.drawable.liked_icon);
        likeBtn.startAnimation(likeZoomIn);

        // Start animation for big central like button if double tapped
        Animation bigLikeZoomIn = AnimationUtils.loadAnimation(getContext(), R.anim.big_like_zoom_in);
        Animation bigLikeZoomHold = AnimationUtils.loadAnimation(getContext(), R.anim.big_like_zoom_hold);
        Animation bigLikeZoomOut = AnimationUtils.loadAnimation(getContext(), R.anim.big_like_zoom_out);
        if (likeMethod.equals("DoubleTap")) {
            bigLike.startAnimation(bigLikeZoomIn);
            bigLike.startAnimation(bigLikeZoomHold);
            bigLike.startAnimation(bigLikeZoomOut);
        }

        // Increment likes
        if (!alreadyLiked) {
            likeCount.setText(String.valueOf(Integer.parseInt(likeCount.getText().toString())+1));
            Database.incrementLikes(uid, recipeID, Timestamp.now());
            alreadyLiked = true;
        }
    }

    private void decrementLike(String uid, String recipeID) {
        // Start animation for like button
        Animation likeZoomIn = AnimationUtils.loadAnimation(getContext(), R.anim.like_zoom_in);
        likeBtn.setImageResource(R.drawable.like_icon);
        likeBtn.startAnimation(likeZoomIn);

        // Decrement likes
        alreadyLiked = false;
        likeCount.setText(String.valueOf(Integer.parseInt(likeCount.getText().toString())-1));
        Database.decrementLikes(uid, recipeID);
    }

    private void launchUserProfile(String uid) {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUserUid.equals(uid)) {
            // Create an instance of the destination fragment
            ProfileFragment profileFragment = new ProfileFragment();
            // Get the FragmentManager
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            // Start a new FragmentTransaction
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Replace the current fragment with the destination fragment
            fragmentTransaction.replace(R.id.frameLayout, profileFragment);
            // Come back to current fragment on back pressed
            fragmentTransaction.addToBackStack(null);
            // Commit the transaction
            fragmentTransaction.commit();

        } else {
            // Create an instance of the destination fragment
            UserProfileFragment userProfileFragment = new UserProfileFragment();
            // Pass the user ID and the search query as an argument to the UserProfileFragment
            Bundle args = new Bundle();
            args.putString("ID", uid);
            args.putString("searchQuery", "N/A");
            args.putString("comingFrom", "RecipeFragment");
            userProfileFragment.setArguments(args);
            // Get the FragmentManager
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            // Start a new FragmentTransaction
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Replace the current fragment with the destination fragment
            fragmentTransaction.replace(R.id.frameLayout, userProfileFragment);
            // Come back to current fragment on back pressed
            fragmentTransaction.addToBackStack(null);
            // Commit the transaction
            fragmentTransaction.commit();
        }
    }

    private Uri saveImage(Bitmap image, String fileName, Context context) {
        File imagesFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;

        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, fileName);
            FileOutputStream stream = new FileOutputStream(file);

            // Compress image
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();

            // Get the uri now
            uri = FileProvider.getUriForFile(Objects.requireNonNull(context.getApplicationContext()),
                    "com.example.instarecipe.fileprovider", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return uri;
    }
}
