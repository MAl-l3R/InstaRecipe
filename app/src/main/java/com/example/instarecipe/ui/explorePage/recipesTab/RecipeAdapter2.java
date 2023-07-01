package com.example.instarecipe.ui.explorePage.recipesTab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.instarecipe.R;
import com.example.instarecipe.RecyclerViewInterface;
import com.example.instarecipe.model.Recipe;
import java.util.ArrayList;


public class RecipeAdapter2 extends RecyclerView.Adapter<RecipeAdapter2.RecipeViewHolder2> {

    private final RecyclerViewInterface recyclerViewInterface;
    ArrayList<Recipe> recipeList;
    Context context;

    public RecipeAdapter2(Context context, ArrayList<Recipe> recipesList, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.recipeList = recipesList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public ArrayList<Recipe> getRecipeList() {
        return recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder2 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_2, parent, false);
        return new RecipeViewHolder2(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder2 holder, int position) {
        Recipe recipe = recipeList.get(position);
        Glide.with(context).load(recipe.getImageUrl()).into(holder.recipePicture);
        holder.recipeCard.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder2 extends RecyclerView.ViewHolder {

        CardView recipeCard;
        ImageView recipePicture;
        public RecipeViewHolder2(View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            recipeCard = itemView.findViewById(R.id.recipe_card);
            recipePicture = itemView.findViewById(R.id.recipe_picture);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && recyclerViewInterface != null) {
                        recyclerViewInterface.onItemClick(pos);
                    }
                }
            });
        }
    }
}