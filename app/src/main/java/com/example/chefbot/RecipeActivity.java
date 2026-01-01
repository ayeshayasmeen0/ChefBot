package com.example.chefbot;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecipeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout recipesLayout;
    private LinearLayout noRecipesLayout;
    private ProgressBar progressBar;
    private TextView tvRecipeCount;
    private TextView tvIngredients;

    private SharedPreferences sharedPreferences;
    private Set<String> favoritesSet;

    // 🔴 FIXED USER ID (later FirebaseAuth se dynamic kar sakti ho)
    private String userId = "user_123";

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

        db = FirebaseFirestore.getInstance();

        sharedPreferences = getSharedPreferences("ChefBotFavorites", MODE_PRIVATE);
        favoritesSet = new HashSet<>(sharedPreferences.getStringSet("favorites", new HashSet<>()));

        recipesLayout = findViewById(R.id.recipesLayout);
        noRecipesLayout = findViewById(R.id.noRecipesLayout);
        progressBar = findViewById(R.id.progressBar);
        tvRecipeCount = findViewById(R.id.tvRecipeCount);
        tvIngredients = findViewById(R.id.tvIngredients);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

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
                                    matchingRecipes.add(
                                            new Recipe(recipeId, recipeName, recipeIngredients, cookingTime)
                                    );
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
        for (String ing : userIngredients) {
            String trimmedIng = ing.trim();
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

            View card = inflater.inflate(R.layout.recipe_card_item, recipesLayout, false);

            ((TextView) card.findViewById(R.id.tvRecipeName)).setText("🍳 " + recipe.getName());
            ((TextView) card.findViewById(R.id.tvRecipeTime)).setText("⏱ " + recipe.getTime());
            ((TextView) card.findViewById(R.id.tvRecipeIngredients))
                    .setText("📝 " + recipe.getIngredients());

            TextView btnFavorite = card.findViewById(R.id.btnFavorite);

            btnFavorite.setText(
                    favoritesSet.contains(recipe.getId()) ? "❤️" : "🤍"
            );

            btnFavorite.setOnClickListener(v ->
                    toggleFavorite(recipe, btnFavorite)
            );

            // Recipe card click
            card.setOnClickListener(v -> {
                Toast.makeText(RecipeActivity.this,
                        recipe.getName() + " selected",
                        Toast.LENGTH_SHORT).show();
            });

            // Margin between cards
            if (i < recipes.size() - 1) {
                LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) card.getLayoutParams();
                cardParams.bottomMargin = 16;
                card.setLayoutParams(cardParams);
            }

            recipesLayout.addView(card);
        }
    }

    private void toggleFavorite(Recipe recipe, TextView btn) {
        if (favoritesSet.contains(recipe.getId())) {
            // Remove from favorites
            favoritesSet.remove(recipe.getId());
            btn.setText("🤍");

            // Toast message with recipe name
            Toast.makeText(this, "❌ " + recipe.getName() + " removed from favorites", Toast.LENGTH_SHORT).show();

            // Remove from Firebase
            removeFromFirebaseFavorites(recipe.getId());

        } else {
            // Add to favorites
            favoritesSet.add(recipe.getId());
            btn.setText("❤️");

            // Toast message with recipe name
            Toast.makeText(this, "✅ " + recipe.getName() + " added to favorites", Toast.LENGTH_SHORT).show();

            // Add to Firebase
            addToFirebaseFavorites(recipe);
        }

        // Save to SharedPreferences
        sharedPreferences.edit()
                .putStringSet("favorites", new HashSet<>(favoritesSet))
                .apply();
    }

    // 🔥 FIXED FIREBASE ADD - Use recipeId as document ID
    private void addToFirebaseFavorites(Recipe recipe) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", recipe.getName());
        data.put("ingredients", recipe.getIngredients());
        data.put("time", recipe.getTime());
        data.put("timestamp", System.currentTimeMillis());

        // Use recipeId as the document ID for easy deletion
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(recipe.getId())  // ✅ Use recipeId as document ID
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "✅ " + recipe.getName() + " added to Firebase (ID: " + recipe.getId() + ")");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "❌ Error adding favorite: " + e.getMessage());
                });
    }

    // 🔥 FIXED FIREBASE REMOVE - Simple delete with recipeId as document ID
    private void removeFromFirebaseFavorites(String recipeId) {
        Log.d("Firebase", "🗑️ Attempting to delete recipe: " + recipeId);

        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(recipeId)  // ✅ Use recipeId as document ID
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "✅ SUCCESS: Deleted from Firebase: " + recipeId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "❌ FAILED to delete: " + e.getMessage());

                    // Try alternative method if direct delete fails
                    tryAlternativeDelete(recipeId);
                });
    }

    // Alternative method: Find by recipeId field and delete
    private void tryAlternativeDelete(String recipeId) {
        Log.d("Firebase", "🔄 Trying alternative delete for: " + recipeId);

        db.collection("users")
                .document(userId)
                .collection("favorites")
                .whereEqualTo("recipeId", recipeId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String docId = document.getId();
                            Log.d("Firebase", "Found document with ID: " + docId);

                            // Delete using actual document ID
                            db.collection("users")
                                    .document(userId)
                                    .collection("favorites")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firebase", "✅ Alternative delete successful");
                                    });
                        }
                    } else {
                        Log.d("Firebase", "⚠️ No document found with recipeId: " + recipeId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "❌ Alternative delete also failed: " + e.getMessage());
                });
    }

    private void showNoRecipesMessage() {
        recipesLayout.setVisibility(View.GONE);
        noRecipesLayout.setVisibility(View.VISIBLE);
        tvRecipeCount.setText("(0 found)");
    }
}