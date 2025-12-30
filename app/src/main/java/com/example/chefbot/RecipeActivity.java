package com.example.chefbot;

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
import java.util.List;

public class RecipeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout recipesLayout;
    private LinearLayout noRecipesLayout;
    private LinearLayout comingSoonLayout;
    private ProgressBar progressBar;
    private TextView tvRecipeCount;
    private TextView tvIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // Initialize views
        recipesLayout = findViewById(R.id.recipesLayout);
        noRecipesLayout = findViewById(R.id.noRecipesLayout);
        comingSoonLayout = findViewById(R.id.comingSoonLayout);
        progressBar = findViewById(R.id.progressBar);
        tvRecipeCount = findViewById(R.id.tvRecipeCount);
        tvIngredients = findViewById(R.id.tvIngredients);

        // Set back button click listener
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Hide coming soon message (we're implementing real functionality)
        comingSoonLayout.setVisibility(View.GONE);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get ingredients from intent
        String ingredients = getIntent().getStringExtra("USER_INGREDIENTS");

        if (ingredients != null && !ingredients.isEmpty()) {
            tvIngredients.setText(ingredients);
            // Fetch recipes from Firestore
            fetchRecipes(ingredients);

            Toast.makeText(this, "Finding recipes for: " + ingredients,
                    Toast.LENGTH_SHORT).show();
        } else {
            tvIngredients.setText("No ingredients provided");
            showNoRecipesMessage();
            Toast.makeText(this, "No ingredients provided", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchRecipes(String userIngredients) {
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        recipesLayout.setVisibility(View.GONE);
        noRecipesLayout.setVisibility(View.GONE);

        // Convert user ingredients to lowercase for case-insensitive matching
        String[] userIngArray = userIngredients.toLowerCase().split("[,\\s]+");

        db.collection("recipe")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        List<Recipe> matchingRecipes = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Get recipe data from Firestore
                            String recipeName = document.getString("name");
                            String recipeIngredients = document.getString("ingredients");
                            String cookingTime = document.getString("time");
                            String recipeId = document.getId();

                            if (recipeName != null && recipeIngredients != null && cookingTime != null) {
                                // Check if recipe can be made with user's ingredients
                                if (canMakeRecipe(recipeIngredients.toLowerCase(), userIngArray)) {
                                    Recipe recipe = new Recipe(recipeId, recipeName, recipeIngredients, cookingTime);
                                    matchingRecipes.add(recipe);
                                }
                            }
                        }

                        // Display matching recipes
                        displayRecipes(matchingRecipes);

                    } else {
                        Toast.makeText(RecipeActivity.this,
                                "Error fetching recipes: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        showNoRecipesMessage();
                    }
                });
    }

    private boolean canMakeRecipe(String recipeIngredients, String[] userIngredients) {
        // Basic matching - at least one ingredient matches
        for (String userIng : userIngredients) {
            String trimmedIng = userIng.trim();
            if (!trimmedIng.isEmpty() && recipeIngredients.contains(trimmedIng)) {
                return true;
            }
        }
        return false;
    }

    private void displayRecipes(List<Recipe> recipes) {
        // Clear previous recipes
        recipesLayout.removeAllViews();

        // Update recipe count
        tvRecipeCount.setText("(" + recipes.size() + " found)");

        if (recipes.isEmpty()) {
            showNoRecipesMessage();
            return;
        }

        recipesLayout.setVisibility(View.VISIBLE);
        noRecipesLayout.setVisibility(View.GONE);

        // Inflate and add recipe cards
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);

            View recipeCard = inflater.inflate(R.layout.recipe_card_item, recipesLayout, false);

            // Get views from card
            TextView tvRecipeName = recipeCard.findViewById(R.id.tvRecipeName);
            TextView tvRecipeTime = recipeCard.findViewById(R.id.tvRecipeTime);
            TextView tvRecipeIngredients = recipeCard.findViewById(R.id.tvRecipeIngredients);

            // Set data
            tvRecipeName.setText(recipe.getName());
            tvRecipeTime.setText("⏱ " + recipe.getTime());
            tvRecipeIngredients.setText("📝 " + recipe.getIngredients());

            // Add click listener
            int finalI = i;
            recipeCard.setOnClickListener(v -> {
                Toast.makeText(RecipeActivity.this,
                        "Selected: " + recipe.getName(),
                        Toast.LENGTH_SHORT).show();
                // You can navigate to detailed recipe view here
            });

            // Add margin between cards
            if (i < recipes.size() - 1) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) recipeCard.getLayoutParams();
                params.bottomMargin = 16;
                recipeCard.setLayoutParams(params);
            }

            recipesLayout.addView(recipeCard);
        }
    }

    private void showNoRecipesMessage() {
        recipesLayout.setVisibility(View.GONE);
        noRecipesLayout.setVisibility(View.VISIBLE);
        tvRecipeCount.setText("(0 found)");
    }

    // Simple Recipe model class
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
}