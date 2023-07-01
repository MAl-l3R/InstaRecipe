package com.example.instarecipe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.instarecipe.databinding.ActivityMainBinding;
import com.example.instarecipe.ui.addRecipePage.AddRecipeFragment;
import com.example.instarecipe.ui.explorePage.ExploreFragment;
import com.example.instarecipe.ui.favoritesPage.FavoritesFragment;
import com.example.instarecipe.ui.homePage.HomeFragment;
import com.example.instarecipe.ui.profilePage.ProfileFragment;
import com.example.instarecipe.ui.profilePage.ProfileSetupActivity;
import com.example.instarecipe.ui.startupPage.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseAuth mFirebaseAuth;
    String signInMethod;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Configure shared preferences
        sharedPreferences = this.getSharedPreferences("SavedInfo", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Check if a user is logged in
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Add user to database
            // If already added, then display Welcome Back message instead
            //Database.addUser(this, currentUser);

            editor.putString("UserUid", currentUser.getUid());
            editor.commit();

            // If new user, launch profile setup page
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().exists()) {
                                Intent intent = new Intent(MainActivity.this, ProfileSetupActivity.class);
                                intent.putExtra("email", currentUser.getEmail());
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
            });

        } else {
            // Go back to Login Page if user not logged in
            toggleLoginState("Sign Out", MainActivity.this, LoginActivity.class);
        }

        // Get sign in method
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)  {
            if (bundle.getString("SignInMethod") != null) {
                signInMethod = bundle.getString("SignInMethod");
                // Save sign in method in shared preferences
                editor.putString("SignInMethod", signInMethod);
                editor.commit();
            }
        }


        // Start home page
        replaceFragment(new HomeFragment());

        // Bottom Navigation Bar Actions
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.explore:
                    replaceFragment(new ExploreFragment());
                    break;
                case R.id.addrecipe:
                    replaceFragment(new AddRecipeFragment());
                    break;
                case R.id.favorites:
                    replaceFragment(new FavoritesFragment());
                    break;
                case R.id.profile:
                    replaceFragment(new ProfileFragment());
                    break;
            }
            return true;
        });

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void toggleLoginState(String signInMethod, android.content.Context from, Class to) {
        // Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        Intent intent = new Intent(from, to);
        if (!signInMethod.equals("Sign Out")) {
            intent.putExtra("SignInMethod", signInMethod);
        }
        // Make sure user can't come back to this screen
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}