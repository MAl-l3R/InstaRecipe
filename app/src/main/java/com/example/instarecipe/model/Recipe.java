package com.example.instarecipe.model;

import com.google.firebase.Timestamp;

public class Recipe {
    private String recipeID;
    private String name;
    private String uid;
    private String cookTime;
    private String ingredients;
    private String recipe;
    private String imageUrl;
    private int likes;
    private Timestamp timestamp;

    public Recipe(String recipeID, String name, String uid, int likes, String cookTime, String ingredients, String recipe, String imageUrl, Timestamp timestamp) {
        this.recipeID = recipeID;
        this.name = name;
        this.uid = uid;
        this.likes = likes;
        this.cookTime = cookTime;
        this.ingredients = ingredients;
        this.recipe = recipe;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // Recipe ID
    public String getRecipeID() {
        return recipeID;
    }
    public void setRecipeID(String recipeID) {
        this.recipeID = recipeID;
    }

    // Name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // UID
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    // Likes
    public int getLikes() {
        return likes;
    }
    public void incrementFollowers() {
        this.likes += 1;
    }
    public void decrementFollowers() {
        this.likes -= 1;
    }

    // Cook Time
    public String getCookTime() {
        return cookTime;
    }
    public void setCookTime(String cookTime) {
        this.cookTime = cookTime;
    }

    // Ingredients
    public String getIngredients() {
        return ingredients;
    }
    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    // Recipe
    public String getRecipe() {
        return recipe;
    }
    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    // Image URL
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Time Stamp
    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
