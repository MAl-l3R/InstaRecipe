package com.example.instarecipe.model;

import java.util.ArrayList;

public class User {

    private String uid, name, username, email, profilePicUrl;
    private int followers, following, recipeCount;
    private ArrayList<Recipe> recipes;

    // Constructor
    public User(String uid, String name, String username, String email, String profilePicUrl, int followers, int following, int recipeCount, ArrayList<Recipe> recipes) {
        this.uid = uid;
        this.name = name;
        this.username = username;
        this.email = email;
        this.profilePicUrl = profilePicUrl;
        this.followers = followers;
        this.following = following;
        this.recipeCount = recipeCount;
        this.recipes = recipes;
    }

    // UID
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    // Name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // Username
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    // Email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // Profile Picture
    public String getProfilePicUrl() {
        return profilePicUrl;
    }
    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    // Followers
    public int getFollowers() {
        return followers;
    }
    public void incrementFollowers() {
        this.followers += 1;
    }
    public void decrementFollowers() {
        this.followers -= 1;
    }

    // Following
    public int getFollowing() {
        return following;
    }
    public void incrementFollowing() {
        this.following += 1;
    }
    public void decrementFollowing() {
        this.following -= 1;
    }

    // Recipe Count
    public int getRecipeCount() {
        return recipeCount;
    }
    public void incrementRecipeCount() {
        this.recipeCount += 1;
    }
    public void decrementRecipeCount() {
        this.recipeCount -= 1;
    }

    // Recipes
    public ArrayList<Recipe> getRecipes() {
        return recipes;
    }
    public void setRecipes(ArrayList<Recipe> recipes) {
        this.recipes = recipes;
    }

}
