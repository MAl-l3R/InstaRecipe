package com.example.instarecipe.ui.explorePage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instarecipe.Database;
import com.example.instarecipe.R;
import com.example.instarecipe.RecyclerViewInterface;
import com.example.instarecipe.model.Recipe;
import com.example.instarecipe.model.User;
import com.example.instarecipe.ui.recipe.RecipeFragment;
import com.example.instarecipe.ui.explorePage.accountsTab.AccountsAdapter;
import com.example.instarecipe.ui.explorePage.accountsTab.UserProfileFragment;
import com.example.instarecipe.ui.explorePage.recipesTab.RecipeAdapter2;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class ExploreFragment extends Fragment implements RecyclerViewInterface {
    SearchView searchBar;
    TabLayout tabLayout;
    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;
    RecyclerViewInterface recyclerViewInterface;
    AccountsAdapter accountsAdapter;
    RecipeAdapter2 recipeAdapter2;
    ArrayList<User> accountsList;
    int selectedTabPosition = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        searchBar = view.findViewById(R.id.search_bar);
        searchBar.clearFocus();
        tabLayout = view.findViewById(R.id.tabLayout);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerViewInterface = this;

        accountsList = new ArrayList<>();

        // Retrieve the previous search query if returning from a user profile or a recipe post
        Bundle args = getArguments();
        if (args != null && args.containsKey("searchQuery") && args.containsKey("backFrom")) {
            String searchQuery = args.getString("searchQuery");

            // Set the search query in the SearchView
            searchBar.setQuery(searchQuery, false);

            // Check where the user is coming from
            if (args.getString("backFrom").equals("userProfile")) {
                // Switch to the accounts tab
                selectedTabPosition = 1;
            } else {
                // Switch to the recipes tab
                selectedTabPosition = 0;
            }
            tabLayout.selectTab(tabLayout.getTabAt(selectedTabPosition));
            updateRecyclerView();

        } else {
            // Else, set the initial tab and content (i.e. recipes tab)
            updateRecyclerView();
        }

        // Update selected tab position and tab contents when tab is switched
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Update recycler view if tab changed
                selectedTabPosition = tab.getPosition();
                updateRecyclerView();

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No action needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No action needed
            }
        });

        // Search Bar
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateRecyclerView();
                return true;
            }
        });

        return view;
    }

    private void updateRecyclerView() {
        // Load Recipes or Accounts list into RecyclerView
        recyclerView.setVisibility(View.INVISIBLE);

        // Get the search query
        String searchQuery = "";
        if (searchBar != null && searchBar.getQuery() != null) {
            searchQuery = searchBar.getQuery().toString().toLowerCase();
        }

        // Check which tab user is on
        if (selectedTabPosition == 0) {
            // Tasks for the recipes tab
            gridLayoutManager = new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setHasFixedSize(true);
            // Display all users according to search query
            String finalSearchQuery = searchQuery;
            Database.getRecipes(
                    new OnSuccessListener<ArrayList<Recipe>>() {
                        @Override
                        public void onSuccess(ArrayList<Recipe> recipeList) {

                            // Filter the recipes according to search query //
                            // Create a new filtered list to hold the filtered results
                            ArrayList<Recipe> filteredList = new ArrayList<>();

                            // Check if search query exists
                            if (!finalSearchQuery.isEmpty()) {
                                // Apply the filter to the recipeList and populate the filteredList
                                for (Recipe recipe : recipeList) {
                                    // Check if the recipe name or ingredients contain the search query
                                    if (recipe.getName().toLowerCase().contains(finalSearchQuery) ||
                                            recipe.getIngredients().toLowerCase().contains(finalSearchQuery)) {
                                        filteredList.add(recipe);
                                    }
                                }
                            } else {
                                // If searchQuery is empty, display the full list of recipes
                                filteredList.addAll(recipeList);
                            }

                            // Set up the recipes recycler view adapter with the filtered list
                            recipeAdapter2 = new RecipeAdapter2(getContext(), filteredList, recyclerViewInterface);
                            recyclerView.setAdapter(recipeAdapter2);

                            // Call notifyDataSetChanged() on the adapter to refresh the RecyclerView
                            recipeAdapter2.notifyDataSetChanged();
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

        } else if (selectedTabPosition == 1) {
            // Tasks for the accounts tab
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setHasFixedSize(true);
            // Get current user so that current user is not displayed
            String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // Display all users according to search query
            Database.searchForUsers(searchQuery, currentUserUid,
                    new OnSuccessListener<ArrayList<User>>() {
                        @Override
                        public void onSuccess(ArrayList<User> userList) {
                            // Display the retrieved user list (userList) in recyclerview list
                            accountsList.clear();
                            accountsList.addAll(userList);

                            // Set up the accounts recycler view adapter
                            accountsAdapter = new AccountsAdapter(getContext(), accountsList, recyclerViewInterface);
                            recyclerView.setAdapter(accountsAdapter);

                            // Call notifyDataSetChanged() on the adapter to refresh the RecyclerView
                            accountsAdapter.notifyDataSetChanged();
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
        }

    }

    @Override
    public void onItemClick(int position) {

        // Check which tab user is on
        if (selectedTabPosition == 0) {
            // On item click for the recipes tab
            String recipeID = recipeAdapter2.getRecipeList().get(position).getRecipeID();
            // Create an instance of the destination fragment
            RecipeFragment recipeFragment = new RecipeFragment();
            // Pass the recipe ID and the search query as an argument to the RecipeFragment
            Bundle args = new Bundle();
            args.putString("ID", recipeID);
            args.putString("searchQuery", searchBar.getQuery().toString());
            recipeFragment.setArguments(args);
            // Get the FragmentManager
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            // Start a new FragmentTransaction
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Replace the current fragment with the destination fragment
            fragmentTransaction.replace(R.id.frameLayout, recipeFragment);
            // Commit the transaction
            fragmentTransaction.commit();

        } else if (selectedTabPosition == 1) {
            // On item click for the accounts tab
            String userUID = accountsAdapter.getAccountsList().get(position).getUid();
            // Create an instance of the destination fragment
            UserProfileFragment userProfileFragment = new UserProfileFragment();
            // Pass the user ID and the search query as an argument to the UserProfileFragment
            Bundle args = new Bundle();
            args.putString("ID", userUID);
            args.putString("searchQuery", searchBar.getQuery().toString());
            args.putString("comingFrom", "ExploreFragment");
            userProfileFragment.setArguments(args);
            // Get the FragmentManager
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            // Start a new FragmentTransaction
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Replace the current fragment with the destination fragment
            fragmentTransaction.replace(R.id.frameLayout, userProfileFragment);
            // Commit the transaction
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onItemLongClick(int position) {
        // Do nothing
    }
}

