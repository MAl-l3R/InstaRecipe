package com.example.instarecipe.ui.profilePage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instarecipe.Database;
import com.example.instarecipe.R;
import com.example.instarecipe.RecyclerViewInterface;
import com.example.instarecipe.model.User;
import com.example.instarecipe.ui.explorePage.accountsTab.AccountsAdapter;
import com.example.instarecipe.ui.explorePage.accountsTab.UserProfileFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class FollowersFollowingFragment extends Fragment implements RecyclerViewInterface {
    String uid, page;
    SearchView searchBar;
    RecyclerView recyclerView;
    RecyclerViewInterface recyclerViewInterface;
    AccountsAdapter accountsAdapter;
    ArrayList<User> accountsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_followers_following, container, false);

        searchBar = view.findViewById(R.id.search_bar);
        searchBar.clearFocus();
        recyclerView = view.findViewById(R.id.recyclerView);

        // Set up the recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerViewInterface = this;

        accountsList = new ArrayList<>();

        Bundle args = getArguments();
        if (args != null && args.containsKey("uid") && args.containsKey("page")) {
            // Get uid
            uid = args.getString("uid");
            page = args.getString("page");

            // If search query already exists, set that as the query and update list accordingly
            if (args.containsKey("searchQuery")) {
                String searchQuery = args.getString("searchQuery");

                // Set the search query in the SearchView
                searchBar.setQuery(searchQuery, false);
            }

            // Update recycler view
            updateRecyclerView();

        }

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

        // Display all users according to search query
        Database.searchForFollowersFollowing(page, uid, searchQuery,
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

    @Override
    public void onItemClick(int position) {

        // On item click for the accounts tab
        String userUID = accountsAdapter.getAccountsList().get(position).getUid();
        // Create an instance of the destination fragment
        UserProfileFragment userProfileFragment = new UserProfileFragment();
        // Pass the user ID and the search query as an argument to the UserProfileFragment
        Bundle args = new Bundle();
        args.putString("ID", userUID);
        args.putString("searchQuery", searchBar.getQuery().toString());
        args.putString("comingFromUid", uid);
        if (page.equals("followers")) {
            args.putString("comingFrom", "FollowersFragment");
        } else if (page.equals("following")) {
            args.putString("comingFrom", "FollowingFragment");
        }
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

    @Override
    public void onItemLongClick(int position) {
        // Do nothing
    }

}

