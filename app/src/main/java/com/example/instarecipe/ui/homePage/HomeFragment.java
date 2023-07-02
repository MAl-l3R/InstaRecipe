package com.example.instarecipe.ui.homePage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instarecipe.Database;
import com.example.instarecipe.R;
import com.example.instarecipe.RecyclerViewInterface;
import com.example.instarecipe.model.Recipe;
import com.example.instarecipe.ui.recipe.RecipeAdapter1;
import com.example.instarecipe.ui.recipe.RecipeFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements RecyclerViewInterface {

    TextView goFollowMessage;
    RecyclerView recyclerView;
    ArrayList<Recipe> recipeList;
    RecipeAdapter1 recipeAdapter1;
    RecyclerViewInterface recyclerViewInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Display this if user does not follow anybody
        goFollowMessage = view.findViewById(R.id.go_follow_people);

        // Set up the recycler view
        recyclerView = view.findViewById(R.id.recipes_list_home);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerViewInterface = this;

        // Retrieve and display the recipe posts
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Database.getFollowingRecipes(currentUser.getUid(),
                new OnSuccessListener<ArrayList<Recipe>>() {
                    @Override
                    public void onSuccess(ArrayList<Recipe> recipeArrayList) {
                        // Display the retrieved recipe list in recyclerview list
                        recipeList = new ArrayList<>();
                        recipeList.addAll(recipeArrayList);

                        // Show go follow people message if user does not follow anyone
                        if (recipeList.isEmpty()) {
                            goFollowMessage.setVisibility(View.VISIBLE);

                        } else {
                            // Else, set up the recycler view with the posts
                            recipeAdapter1 = new RecipeAdapter1(getContext(), recipeList, recyclerViewInterface, "HomePage");
                            recyclerView.setAdapter(recipeAdapter1);

                            // Call notifyDataSetChanged() on the adapter to refresh the RecyclerView
                            recipeAdapter1.notifyDataSetChanged();

                            // Display the recipes
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }

                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the failure
                        System.out.println(e.getMessage());
                    }
                });

        return view;
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

