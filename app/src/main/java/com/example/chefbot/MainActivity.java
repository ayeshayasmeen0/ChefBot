package com.example.chefbot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etIngredients;
    private Button btnFindRecipes;
    private TextView ivFavorites, ivSettings; // ✅ Changed from ImageView to TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        etIngredients = findViewById(R.id.etIngredients);
        btnFindRecipes = findViewById(R.id.btnFindRecipes);

        // ✅ TEXTVIEW NOW (not ImageView)
        ivFavorites = findViewById(R.id.ivFavorites);
        ivSettings = findViewById(R.id.ivSettings);

        // Find Recipes Button
        btnFindRecipes.setOnClickListener(v -> findRecipes());

        // Favorites Button
        ivFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        // Settings Button
        ivSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        });

        // Quick suggestion buttons
        setupQuickButtons();
    }

    private void findRecipes() {
        String userInput = etIngredients.getText().toString().trim();

        if (userInput.isEmpty()) {
            etIngredients.setError("Please enter some ingredients");
            etIngredients.requestFocus();
            return;
        }

        // Pass ingredients to RecipeActivity
        Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
        intent.putExtra("USER_INGREDIENTS", userInput);
        startActivity(intent);
    }

    private void setupQuickButtons() {
        // Breakfast button
        findViewById(R.id.btnQuick1).setOnClickListener(v -> {
            etIngredients.setText("eggs, milk, bread, butter, cheese");
            Toast.makeText(this, "Breakfast ingredients added!", Toast.LENGTH_SHORT).show();
        });

        // Lunch button
        findViewById(R.id.btnQuick2).setOnClickListener(v -> {
            etIngredients.setText("chicken, rice, tomatoes, onions, spices");
            Toast.makeText(this, "Lunch ingredients added!", Toast.LENGTH_SHORT).show();
        });

        // Dinner button
        findViewById(R.id.btnQuick3).setOnClickListener(v -> {
            etIngredients.setText("pasta, vegetables, olive oil, garlic, herbs");
            Toast.makeText(this, "Dinner ingredients added!", Toast.LENGTH_SHORT).show();
        });
    }
}