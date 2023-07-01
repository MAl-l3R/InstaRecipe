package com.example.instarecipe.ui.explorePage.accountsTab;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instarecipe.Database;
import com.example.instarecipe.R;
import com.example.instarecipe.ui.recipe.RecipeAdapter1;
import com.example.instarecipe.RecyclerViewInterface;
import com.example.instarecipe.model.Recipe;
import com.example.instarecipe.ui.profilePage.FollowersFollowingFragment;
import com.example.instarecipe.ui.recipe.RecipeFragment;
import com.example.instarecipe.ui.explorePage.ExploreFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileFragment extends Fragment implements RecyclerViewInterface {
    String uid, searchQuery, currentUserUid, currentUserName, comingFrom, comingFromUid;
    RecyclerViewInterface recyclerViewInterface;
    RecipeAdapter1 recipeAdapter1;
    ArrayList<Recipe> recipeList;
    Boolean isFollowing = false;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        recyclerViewInterface = this;

        // Retrieve the user UID and search query from the arguments
        uid = getArguments().getString("ID");
        searchQuery = getArguments().getString("searchQuery");
        comingFrom = getArguments().getString("comingFrom");
        comingFromUid = "";
        if (getArguments().containsKey("comingFromUid")) {
            comingFromUid = getArguments().getString("comingFromUid");
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserUid = currentUser.getUid();
        currentUserName = currentUser.getDisplayName();

        // Get the user's file
        DocumentReference userFile = db.collection("users").document(uid);
        userFile.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        // Get all UI items
                        ConstraintLayout profileFragment = view.findViewById(R.id.profile_fragment);
                        ImageButton menuButton = view.findViewById(R.id.menu_button);
                        CircleImageView profilePicView = view.findViewById(R.id.profile_pic);
                        TextView nameView = view.findViewById(R.id.name);
                        TextView usernameView =  view.findViewById(R.id.comment);
                        ConstraintLayout followersView = view.findViewById(R.id.followers);
                        TextView followersCountView = view.findViewById(R.id.followers_count);
                        ConstraintLayout followingView = view.findViewById(R.id.following);
                        TextView followingCountView = view.findViewById(R.id.following_count);
                        TextView recipeCountView = view.findViewById(R.id.recipe_count);
                        MaterialButton followBtn = view.findViewById(R.id.follow_btn);
                        RecyclerView recyclerView = view.findViewById(R.id.recipes_recyclerview);

                        // Check if user is followed by current user
                        DocumentReference followers = userFile.collection("followers").document(currentUserUid);
                        followers.get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();

                                            // Check if user is followed by current user
                                            if (document.exists()) {
                                                // The document exists in the collection, i.e. current user follows this user
                                                isFollowing = true;

                                            } else {
                                                // The document does not exist in the collection
                                                isFollowing = false;
                                            }

                                            // Retrieve user info
                                            String profilePic = documentSnapshot.getString("profilePic");
                                            String name = documentSnapshot.getString("name");
                                            String username = documentSnapshot.getString("username");
                                            int followers = documentSnapshot.getLong("followers").intValue();
                                            int following = documentSnapshot.getLong("following").intValue();
                                            int recipeCount = documentSnapshot.getLong("recipeCount").intValue();

                                            // Setup recycler view with user's recipes
                                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                            recyclerView.setHasFixedSize(true);
                                            Database.getUsersRecipes(uid,
                                                    new OnSuccessListener<ArrayList<Recipe>>() {
                                                        @Override
                                                        public void onSuccess(ArrayList<Recipe> recipeArrayList) {
                                                            // Display the retrieved recipe list in recyclerview list
                                                            recipeList = new ArrayList<>();
                                                            recipeList.addAll(recipeArrayList);

                                                            // Set up the accounts recycler view adapter
                                                            recipeAdapter1 = new RecipeAdapter1(getContext(), recipeList, recyclerViewInterface, "ProfilePage", null);
                                                            recyclerView.setAdapter(recipeAdapter1);

                                                            // Call notifyDataSetChanged() on the adapter to refresh the RecyclerView
                                                            recipeAdapter1.notifyDataSetChanged();

                                                            // Display the recipes
                                                            recyclerView.setVisibility(View.VISIBLE);
                                                        }

                                                    },
                                                    new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Handle the failure
                                                            System.out.println(e.getMessage());
                                                        }
                                                    });

                                            // Display the retrieved info
                                            Glide.with(getActivity()).load(profilePic).into(profilePicView);
                                            menuButton.setVisibility(View.GONE);
                                            nameView.setText(name);
                                            usernameView.setText(username);
                                            followersCountView.setText(String.valueOf(followers));
                                            followingCountView.setText(String.valueOf(following));
                                            recipeCountView.setText(String.valueOf(recipeCount));
                                            followBtn.setVisibility(View.VISIBLE);
                                            if (isFollowing) {
                                                followBtn.setText("Unfollow");
                                                followBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C34C4E")));
                                            } else {
                                                followBtn.setText("Follow");
                                                followBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6396BD")));
                                            }
                                            // Display all together after all info has been fetched
                                            profileFragment.setVisibility(View.VISIBLE);

                                            // Follow Button
                                            followBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    // Perform additional actions based on the new pressed state
                                                    isFollowing = !isFollowing;
                                                    if (isFollowing) {
                                                        // User follows this user now
                                                        followBtn.setText("Unfollow");
                                                        followBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C34C4E")));
                                                        followersCountView.setText(String.valueOf(Integer.parseInt(followersCountView.getText().toString())+1));
                                                        Database.addFollower(currentUserUid, currentUserName, uid, name);
                                                    } else {
                                                        // User does not follow this user now
                                                        followBtn.setText("Follow");
                                                        followBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6396BD")));
                                                        followersCountView.setText(String.valueOf(Integer.parseInt(followersCountView.getText().toString())-1));
                                                        Database.removeFollower(currentUserUid, uid);
                                                    }
                                                }
                                            });

                                            // Display followers list button
                                            followersView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    // Create an instance of the destination fragment
                                                    FollowersFollowingFragment followersFollowingFragment = new FollowersFollowingFragment();
                                                    // Pass the user ID and the search query as an argument to the UserProfileFragment
                                                    Bundle args = new Bundle();
                                                    args.putString("uid", uid);
                                                    args.putString("page", "followers");
                                                    followersFollowingFragment.setArguments(args);
                                                    // Get the FragmentManager
                                                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                                    // Start a new FragmentTransaction
                                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                    // Replace the current fragment with the destination fragment
                                                    fragmentTransaction.replace(R.id.frameLayout, followersFollowingFragment);
                                                    // Come back to current fragment on back pressed
                                                    fragmentTransaction.addToBackStack(null);
                                                    // Commit the transaction
                                                    fragmentTransaction.commit();
                                                }
                                            });

                                            // Display following list button
                                            followingView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    // Create an instance of the destination fragment
                                                    FollowersFollowingFragment followersFollowingFragment = new FollowersFollowingFragment();
                                                    // Pass the user ID and the search query as an argument to the UserProfileFragment
                                                    Bundle args = new Bundle();
                                                    args.putString("uid", uid);
                                                    args.putString("page", "following");
                                                    followersFollowingFragment.setArguments(args);
                                                    // Get the FragmentManager
                                                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                                    // Start a new FragmentTransaction
                                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                    // Replace the current fragment with the destination fragment
                                                    fragmentTransaction.replace(R.id.frameLayout, followersFollowingFragment);
                                                    // Come back to current fragment on back pressed
                                                    fragmentTransaction.addToBackStack(null);
                                                    // Commit the transaction
                                                    fragmentTransaction.commit();
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

                    if (comingFrom.equals("ExploreFragment")) {
                        // Go back to Explore Fragment's Accounts tab and display previous search query
                        ExploreFragment exploreFragment = new ExploreFragment();
                        // Send the search query
                        Bundle args = new Bundle();
                        args.putString("searchQuery", searchQuery);
                        args.putString("backFrom", "userProfile");
                        exploreFragment.setArguments(args);
                        // Perform the fragment transaction
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frameLayout, exploreFragment);
                        fragmentTransaction.commit();
                    } else {
                        // Go back to Followers/Following Fragment and display previous search query
                        FollowersFollowingFragment followersFollowingFragment = new FollowersFollowingFragment();
                        // Send the search query
                        Bundle args = new Bundle();
                        args.putString("searchQuery", searchQuery);
                        args.putString("uid", comingFromUid);
                        if (comingFrom.equals("FollowersFragment")) {
                            args.putString("page", "followers");
                        } else if (comingFrom.equals("FollowingFragment")) {
                            args.putString("page", "following");
                        }
                        followersFollowingFragment.setArguments(args);
                        // Perform the fragment transaction
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frameLayout, followersFollowingFragment);
                        fragmentTransaction.commit();
                    }

                } else {
                    // Default back functionality (go back as normal to the previous page)
                    this.setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {

        // On item click for the recipes
        String recipeID = recipeAdapter1.getRecipeList().get(position).getRecipeID();
        // Create an instance of the destination fragment
        RecipeFragment recipeFragment = new RecipeFragment();
        // Pass the recipe ID as an argument to the RecipeFragment
        Bundle args = new Bundle();
        args.putString("ID", recipeID);
        args.putString("searchQuery", "N/A");
        recipeFragment.setArguments(args);
        // Get the FragmentManager
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        // Start a new FragmentTransaction
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Replace the current fragment with the destination fragment
        fragmentTransaction.replace(R.id.frameLayout, recipeFragment);
        // Come back to current fragment on back pressed
        fragmentTransaction.addToBackStack(null);
        // Commit the transaction
        fragmentTransaction.commit();
    }

    @Override
    public void onItemLongClick(int position) {
        // Do nothing
    }

}
