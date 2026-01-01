package com.example.chefbot;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout favoritesContainer;
    private LinearLayout emptyState;
    private ProgressBar progressBar;

    // Local storage for favorites
    private SharedPreferences sharedPreferences;
    private Set<String> favoritesSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize Firebase Firestore ONLY (no Auth)
        db = FirebaseFirestore.getInstance();

        // Initialize local favorites storage
        sharedPreferences = getSharedPreferences("ChefBotFavorites", MODE_PRIVATE);
        favoritesSet = sharedPreferences.getStringSet("favorites", new HashSet<>());

        // Initialize views
        favoritesContainer = findViewById(R.id.favoritesContainer);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);

        TextView tvClose = findViewById(R.id.tvClose);
        tvClose.setOnClickListener(v -> finish());

        // Load favorites
        loadFavorites();
    }

    private void loadFavorites() {
        progressBar.setVisibility(View.VISIBLE);
        favoritesContainer.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        if (favoritesSet.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            showEmptyState();
            return;
        }

        // Convert Set to List for Firestore query
        List<String> favoriteIds = new ArrayList<>(favoritesSet);

        db.collection("recipe")
                .whereIn("__name__", favoriteIds)  // Query recipes by their IDs
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        List<FavoriteRecipe> favoriteRecipes = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String recipeName = document.getString("name");
                            String recipeIngredients = document.getString("ingredients");
                            String cookingTime = document.getString("time");
                            String recipeId = document.getId();

                            if (recipeName != null && recipeIngredients != null && cookingTime != null) {
                                FavoriteRecipe recipe = new FavoriteRecipe(
                                        recipeId,
                                        recipeName,
                                        recipeIngredients,
                                        cookingTime
                                );
                                favoriteRecipes.add(recipe);
                            }
                        }

                        if (favoriteRecipes.isEmpty()) {
                            showEmptyState();
                        } else {
                            showFavorites(favoriteRecipes);
                        }

                    } else {
                        Toast.makeText(FavoritesActivity.this,
                                "Failed to load favorites. Check internet.",
                                Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                });
    }

    private void showFavorites(List<FavoriteRecipe> recipes) {
        favoritesContainer.removeAllViews();
        favoritesContainer.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (FavoriteRecipe recipe : recipes) {
            View favoriteCard = inflater.inflate(R.layout.favorite_card_item, favoritesContainer, false);

            TextView tvRecipeName = favoriteCard.findViewById(R.id.tvRecipeName);
            TextView tvRecipeTime = favoriteCard.findViewById(R.id.tvRecipeTime);
            TextView tvRecipeIngredients = favoriteCard.findViewById(R.id.tvRecipeIngredients);
            TextView btnRemove = favoriteCard.findViewById(R.id.btnRemove);

            tvRecipeName.setText("🍳 " + recipe.getName());
            tvRecipeTime.setText("⏱ " + recipe.getTime());
            tvRecipeIngredients.setText("📝 " + recipe.getIngredients());
            btnRemove.setText("🗑️");

            // Remove button click listener
            btnRemove.setOnClickListener(v -> {
                removeFromFavorites(recipe.getRecipeId(), favoriteCard);
            });

            // Recipe click listener
            favoriteCard.setOnClickListener(v -> {
                Toast.makeText(FavoritesActivity.this,
                        recipe.getName() + " selected",
                        Toast.LENGTH_SHORT).show();
            });

            favoritesContainer.addView(favoriteCard);
        }
    }

    private void removeFromFavorites(String recipeId, View cardView) {
        // Remove from local storage
        favoritesSet.remove(recipeId);

        // Update SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("favorites", new HashSet<>(favoritesSet));
        editor.apply();

        // Remove card from view
        favoritesContainer.removeView(cardView);

        // Check if no more favorites
        if (favoritesContainer.getChildCount() == 0) {
            showEmptyState();
        }

        Toast.makeText(FavoritesActivity.this,
                "Removed from favorites",
                Toast.LENGTH_SHORT).show();
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        favoritesContainer.setVisibility(View.GONE);
    }

    // Inner class for favorite recipes
    private static class FavoriteRecipe {
        private String recipeId;
        private String name;
        private String ingredients;
        private String time;

        public FavoriteRecipe(String recipeId, String name, String ingredients, String time) {
            this.recipeId = recipeId;
            this.name = name;
            this.ingredients = ingredients;
            this.time = time;
        }

        public String getRecipeId() { return recipeId; }
        public String getName() { return name; }
        public String getIngredients() { return ingredients; }
        public String getTime() { return time; }
    }
}