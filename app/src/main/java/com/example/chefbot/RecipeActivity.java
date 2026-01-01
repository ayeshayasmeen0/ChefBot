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

public class RecipeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout recipesLayout;
    private LinearLayout noRecipesLayout;
    private ProgressBar progressBar;
    private TextView tvRecipeCount;
    private TextView tvIngredients;

    // Local storage for favorites
    private SharedPreferences sharedPreferences;
    private Set<String> favoritesSet;

    private static class Recipe {
        private String id;
        private String name;
        private String ingredients;
        private String time;

        public Recipe(String id, String name, String ingredients, String time) {
            this.id = id;
            this.name = name;
            this.ingredients = ingredients;
            this.time = time;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getIngredients() { return ingredients; }
        public String getTime() { return time; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // Initialize Firebase Firestore ONLY
        db = FirebaseFirestore.getInstance();

        // Initialize local favorites storage
        sharedPreferences = getSharedPreferences("ChefBotFavorites", MODE_PRIVATE);
        favoritesSet = sharedPreferences.getStringSet("favorites", new HashSet<>());

        // Initialize views
        recipesLayout = findViewById(R.id.recipesLayout);
        noRecipesLayout = findViewById(R.id.noRecipesLayout);
        progressBar = findViewById(R.id.progressBar);
        tvRecipeCount = findViewById(R.id.tvRecipeCount);
        tvIngredients = findViewById(R.id.tvIngredients);

        // Set back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Get ingredients from intent
        String ingredients = getIntent().getStringExtra("USER_INGREDIENTS");

        if (ingredients != null && !ingredients.isEmpty()) {
            tvIngredients.setText("Ingredients: " + ingredients);
            fetchRecipes(ingredients);
            Toast.makeText(this, "Finding recipes...", Toast.LENGTH_SHORT).show();
        } else {
            tvIngredients.setText("No ingredients provided");
            showNoRecipesMessage();
        }
    }

    private void fetchRecipes(String userIngredients) {
        progressBar.setVisibility(View.VISIBLE);
        recipesLayout.setVisibility(View.GONE);
        noRecipesLayout.setVisibility(View.GONE);

        String[] userIngArray = userIngredients.toLowerCase().split("[,\\s]+");

        db.collection("recipe")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        List<Recipe> matchingRecipes = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String recipeName = document.getString("name");
                            String recipeIngredients = document.getString("ingredients");
                            String cookingTime = document.getString("time");
                            String recipeId = document.getId();

                            if (recipeName != null && recipeIngredients != null && cookingTime != null) {
                                if (canMakeRecipe(recipeIngredients.toLowerCase(), userIngArray)) {
                                    Recipe recipe = new Recipe(recipeId, recipeName, recipeIngredients, cookingTime);
                                    matchingRecipes.add(recipe);
                                }
                            }
                        }

                        displayRecipes(matchingRecipes);

                    } else {
                        Toast.makeText(RecipeActivity.this,
                                "Error fetching recipes. Check internet.",
                                Toast.LENGTH_SHORT).show();
                        showNoRecipesMessage();
                    }
                });
    }

    private boolean canMakeRecipe(String recipeIngredients, String[] userIngredients) {
        for (String userIng : userIngredients) {
            String trimmedIng = userIng.trim();
            if (!trimmedIng.isEmpty() && recipeIngredients.contains(trimmedIng)) {
                return true;
            }
        }
        return false;
    }

    private void displayRecipes(List<Recipe> recipes) {
        recipesLayout.removeAllViews();
        tvRecipeCount.setText("(" + recipes.size() + " found)");

        if (recipes.isEmpty()) {
            showNoRecipesMessage();
            return;
        }

        recipesLayout.setVisibility(View.VISIBLE);
        noRecipesLayout.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);

            View recipeCard = inflater.inflate(R.layout.recipe_card_item, recipesLayout, false);

            TextView tvRecipeName = recipeCard.findViewById(R.id.tvRecipeName);
            TextView tvRecipeTime = recipeCard.findViewById(R.id.tvRecipeTime);
            TextView tvRecipeIngredients = recipeCard.findViewById(R.id.tvRecipeIngredients);

            // Set data with emojis
            tvRecipeName.setText("🍳 " + recipe.getName());
            tvRecipeTime.setText("⏱ " + recipe.getTime());
            tvRecipeIngredients.setText("📝 " + recipe.getIngredients());

            // Add favorite button (Text view as button)
            LinearLayout cardLayout = (LinearLayout) recipeCard;
            TextView btnFavorite = new TextView(this);
            btnFavorite.setId(View.generateViewId());

            // Check if recipe is in favorites
            if (favoritesSet.contains(recipe.getId())) {
                btnFavorite.setText("❤️");
            } else {
                btnFavorite.setText("🤍");
            }

            btnFavorite.setTextSize(24);
            btnFavorite.setPadding(16, 0, 16, 0);
            btnFavorite.setClickable(true);

            // Add to card
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = android.view.Gravity.END;
            cardLayout.addView(btnFavorite, 0, params); // Add at top

            // Favorite button click
            final String recipeId = recipe.getId();
            btnFavorite.setOnClickListener(v -> {
                toggleFavorite(recipeId, btnFavorite);
            });

            // Recipe card click
            recipeCard.setOnClickListener(v -> {
                Toast.makeText(RecipeActivity.this,
                        recipe.getName() + " selected",
                        Toast.LENGTH_SHORT).show();
            });

            // Margin between cards
            if (i < recipes.size() - 1) {
                LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) recipeCard.getLayoutParams();
                cardParams.bottomMargin = 16;
                recipeCard.setLayoutParams(cardParams);
            }

            recipesLayout.addView(recipeCard);
        }
    }

    private void toggleFavorite(String recipeId, TextView favoriteButton) {
        if (favoritesSet.contains(recipeId)) {
            // Remove from favorites
            favoritesSet.remove(recipeId);
            favoriteButton.setText("🤍");
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            // Add to favorites
            favoritesSet.add(recipeId);
            favoriteButton.setText("❤️");
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        }

        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("favorites", new HashSet<>(favoritesSet));
        editor.apply();
    }

    private void showNoRecipesMessage() {
        recipesLayout.setVisibility(View.GONE);
        noRecipesLayout.setVisibility(View.VISIBLE);
        tvRecipeCount.setText("(0 found)");
    }
}