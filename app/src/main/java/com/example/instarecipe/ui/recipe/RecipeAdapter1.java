package com.example.instarecipe.ui.recipe;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instarecipe.Database;
import com.example.instarecipe.R;
import com.example.instarecipe.RecyclerViewInterface;
import com.example.instarecipe.model.Recipe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecipeAdapter1 extends RecyclerView.Adapter<RecipeAdapter1.RecipeViewHolder1> {

    private static RecyclerView recyclerView;
    private final RecyclerViewInterface recyclerViewInterface;
    private static RecipeViewHolder1 selectedViewHolder;
    private static int selectedItemPosition = RecyclerView.NO_POSITION;
    RecipeAdapter1 adapter;
    ArrayList<Recipe> recipeList;
    Context context;
    String viewPage;

    public RecipeAdapter1(Context context, ArrayList<Recipe> recipeList, RecyclerViewInterface recyclerViewInterface, String viewPage) {
        this.context = context;
        this.recipeList = recipeList;
        this.recyclerViewInterface = recyclerViewInterface;
        this.viewPage = viewPage;
        this.adapter = this;
    }

    public ArrayList<Recipe> getRecipeList() {
        return recipeList;
    }
    public int getSelectedForDeleteItemPosition() {
        return selectedItemPosition;
    }

    @NonNull
    @Override
    public RecipeViewHolder1 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_1, parent, false);
        recyclerView = (RecyclerView) parent;
        return new RecipeViewHolder1(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder1 holder, int position) {

        // If displaying in a profile page, then no need to show profile pic and username
        if (viewPage.equals("ProfilePage")) {
            holder.profilePicOutline.setVisibility(View.GONE);
            holder.profilePicture.setVisibility(View.GONE);
            holder.username.setVisibility(View.GONE);
            // Get the existing layout parameters of the view
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.recipePicture.getLayoutParams();

            // Set the desired marginTop value
            int marginTop = 45; // Replace with your desired marginTop value
            layoutParams.topMargin = marginTop;

            // Apply the updated layout parameters to the view
            holder.recipePicture.setLayoutParams(layoutParams);
        }

        // Get the recipe object
        Recipe recipe = recipeList.get(position);

        // First fetch the username and profile pic then set everything and display
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(recipe.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Glide.with(context).load(recipe.getImageUrl()).into(holder.recipePicture);
                        Glide.with(context).load(documentSnapshot.getString("profilePic")).into(holder.profilePicture);
                        holder.username.setText(documentSnapshot.getString("username"));
                        holder.recipeName.setText(recipe.getName());
                        holder.recipeCard.setVisibility(View.VISIBLE);

                        // Delete recipe button
                        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                // Launch dialog box
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Delete Recipe");
                                builder.setMessage("Are you sure you want to delete this recipe?");
                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // User clicked "Yes", delete the recipe
                                        Database.deleteRecipe(recipe.getUid(), recipe.getRecipeID(), context, recipeList, holder.getAdapterPosition(), adapter);
                                    }
                                });
                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // User clicked "No", dismiss the dialog
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(e.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder1 extends RecyclerView.ViewHolder {

        CardView recipeCard;
        CircleImageView profilePicture, profilePicOutline;
        TextView username, recipeName;
        ImageView recipePicture;
        Button deleteButton;
        boolean isDeleteButtonVisible;
        public RecipeViewHolder1(View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            recipeCard = itemView.findViewById(R.id.recipe_card);
            profilePicture = itemView.findViewById(R.id.profile_pic);
            profilePicOutline = itemView.findViewById(R.id.profile_pic_white_outline);
            username = itemView.findViewById(R.id.comment);
            recipePicture = itemView.findViewById(R.id.recipe_picture);
            recipeName = itemView.findViewById(R.id.recipe_name);
            deleteButton = itemView.findViewById(R.id.delete_button);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && recyclerViewInterface != null) {
                        recyclerViewInterface.onItemClick(pos);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && recyclerViewInterface != null) {
                        recyclerViewInterface.onItemLongClick(pos);
                        return true; // Return true to consume the long-click event
                    }
                    return false;
                }
            });
        }

        private void showDeleteButton() {
            isDeleteButtonVisible = true;
            deleteButton.setVisibility(View.VISIBLE);
            selectedViewHolder = this;
        }

        private void hideDeleteButton() {
            isDeleteButtonVisible = false;
            deleteButton.setVisibility(View.GONE);
            selectedViewHolder = null;
        }
    }

    public void showDeleteButton(int position) {
        if (selectedItemPosition != RecyclerView.NO_POSITION) {
            // Hide the delete button for the previously selected item
            RecipeViewHolder1 prevSelectedViewHolder = (RecipeViewHolder1) recyclerView.findViewHolderForAdapterPosition(selectedItemPosition);
            if (prevSelectedViewHolder != null) {
                prevSelectedViewHolder.hideDeleteButton();
            }
        }

        // Show the delete button for the newly selected item
        RecipeViewHolder1 selectedViewHolder = (RecipeViewHolder1) recyclerView.findViewHolderForAdapterPosition(position);
        if (selectedViewHolder != null) {
            selectedViewHolder.showDeleteButton();
        }

        selectedItemPosition = position;
    }

    public void hideDeleteButton() {
        if (selectedViewHolder != null) {
            selectedViewHolder.hideDeleteButton();
            selectedItemPosition = RecyclerView.NO_POSITION;
        }
    }
}
