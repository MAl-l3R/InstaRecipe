package com.example.instarecipe.ui.profilePage;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instarecipe.Database;
import com.example.instarecipe.R;
import com.example.instarecipe.RecyclerViewInterface;
import com.example.instarecipe.model.Recipe;
import com.example.instarecipe.ui.explorePage.ExploreFragment;
import com.example.instarecipe.ui.recipe.RecipeAdapter1;
import com.example.instarecipe.ui.recipe.RecipeFragment;
import com.example.instarecipe.ui.startupPage.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements RecyclerViewInterface {

    GoogleSignInClient mGoogleSignInClient;
    String signInMethod;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    RecyclerViewInterface recyclerViewInterface;
    RecipeAdapter1 recipeAdapter1;
    ArrayList<Recipe> recipeList;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        recyclerViewInterface = this;

        // Get current user
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

        // Get the user's file
        DocumentReference userFile = db.collection("users").document(currentUser.getUid());
        userFile.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        // Configure shared preferences
                        sharedPreferences = getActivity().getSharedPreferences("SavedInfo", MODE_PRIVATE);
                        editor = sharedPreferences.edit();
                        signInMethod = sharedPreferences.getString("SignInMethod", null);

                        // Get all UI items
                        ConstraintLayout profileFragment = view.findViewById(R.id.profile_fragment);
                        ImageButton menuButton = view.findViewById(R.id.menu_button);
                        DrawerLayout drawerLayout = view.findViewById(R.id.drawer_layout);
                        NavigationView navigationView = view.findViewById(R.id.sideNavigationView);
                        CircleImageView profilePicView = view.findViewById(R.id.profile_pic);
                        TextView nameView = view.findViewById(R.id.name);
                        TextView usernameView =  view.findViewById(R.id.comment);
                        ConstraintLayout followersView = view.findViewById(R.id.followers);
                        TextView followersCountView = view.findViewById(R.id.followers_count);
                        ConstraintLayout followingView = view.findViewById(R.id.following);
                        TextView followingCountView = view.findViewById(R.id.following_count);
                        TextView recipeCountView = view.findViewById(R.id.recipe_count);
                        RecyclerView recyclerView = view.findViewById(R.id.recipes_recyclerview);

                        // Retrieve user info
                        String profilePic = documentSnapshot.getString("profilePic");
                        String name = documentSnapshot.getString("name");
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        int followers = documentSnapshot.getLong("followers").intValue();
                        int following = documentSnapshot.getLong("following").intValue();
                        int recipeCount = documentSnapshot.getLong("recipeCount").intValue();

                        // Setup recycler view with user's recipes
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setHasFixedSize(true);
                        Database.getUsersRecipes(currentUser.getUid(),
                                new OnSuccessListener<ArrayList<Recipe>>() {
                                    @Override
                                    public void onSuccess(ArrayList<Recipe> recipeArrayList) {
                                        // Display the retrieved recipe list in recyclerview list
                                        recipeList = new ArrayList<>();
                                        recipeList.addAll(recipeArrayList);

                                        // Set up the accounts recycler view adapter
                                        recipeAdapter1 = new RecipeAdapter1(getContext(), recipeList, recyclerViewInterface, "ProfilePage");
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
                        nameView.setText(name);
                        usernameView.setText(username);
                        followersCountView.setText(String.valueOf(followers));
                        followingCountView.setText(String.valueOf(following));
                        recipeCountView.setText(String.valueOf(recipeCount));
                        // Display everything now
                        profileFragment.setVisibility(View.VISIBLE);

                        // Display followers list button
                        followersView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Create an instance of the destination fragment
                                FollowersFollowingFragment followersFollowingFragment = new FollowersFollowingFragment();
                                // Pass the user ID and the search query as an argument to the UserProfileFragment
                                Bundle args = new Bundle();
                                args.putString("uid", currentUser.getUid());
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
                                args.putString("uid", currentUser.getUid());
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


                        // Configure Google Sign In/Out
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build();
                        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

                        // Open Side Navigation Drawer
                        menuButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                drawerLayout.openDrawer(GravityCompat.END);
                            }
                        });

                        // Side Navigation Drawer Actions
                        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                            @Override
                            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                                int option = item.getItemId();
                                drawerLayout.closeDrawer(GravityCompat.END);
                                switch (option)
                                {
                                    // Edit Profile Button
                                    case R.id.edit_profile:
                                        Intent intent = new Intent(getActivity(), ProfileSetupActivity.class);
                                        intent.putExtra("profilePic", profilePic);
                                        intent.putExtra("name", name);
                                        intent.putExtra("username", username);
                                        intent.putExtra("email", currentUser.getEmail());
                                        startActivity(intent);
                                        break;
                                    // Logout Button
                                    case R.id.logout_button:
                                        // Sign out the way user signed in
                                        if (signInMethod.equals("Google")) {
                                            mGoogleSignInClient.signOut()
                                                    .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            FirebaseAuth.getInstance().signOut();
                                                            toggleLoginState("Sign Out", getActivity(), LoginActivity.class);
                                                            Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            FirebaseAuth.getInstance().signOut();
                                            toggleLoginState("Sign out", getActivity(), LoginActivity.class);
                                            Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    default:
                                        return true;
                                }
                                return true;
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

    private void toggleLoginState(String signInMethod, android.content.Context from, Class to) {
        // Intent intent = new Intent(getActivity(), LoginActivity.class);
        Intent intent = new Intent(from, to);
        if (!signInMethod.equals("Sign Out")) {
            intent.putExtra("SignInMethod", signInMethod);
        }
        // Make sure user can't come back to this screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
        // Show Delete Button
        recipeAdapter1.showDeleteButton(position);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handle the back button
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (recipeAdapter1.getSelectedForDeleteItemPosition() != RecyclerView.NO_POSITION) {
                    // If a delete button is shown, delete it
                    recipeAdapter1.hideDeleteButton();
                } else {
                    // Exit the app
                    requireActivity().finish();
                }
            }
        });
    }

}