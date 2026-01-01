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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        etIngredients = findViewById(R.id.etIngredients);
        btnFindRecipes = findViewById(R.id.btnFindRecipes);

        // FIXED: Changed from ImageView to TextView
        TextView ivFavorites = findViewById(R.id.ivFavorites);  // Now TextView
        TextView ivSettings = findViewById(R.id.ivSettings);    // Now TextView

        // Find Recipes Button
        btnFindRecipes.setOnClickListener(v -> findRecipes());

        // Favorites Button (now TextView)
        ivFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        // Settings Button (now TextView)
        ivSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        });

        // Quick buttons
        findViewById(R.id.btnQuick1).setOnClickListener(v -> {
            etIngredients.setText("eggs, milk, bread, butter");
            Toast.makeText(this, "Breakfast ingredients added", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnQuick2).setOnClickListener(v -> {
            etIngredients.setText("chicken, rice, tomatoes, spices");
            Toast.makeText(this, "Lunch ingredients added", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnQuick3).setOnClickListener(v -> {
            etIngredients.setText("pasta, cheese, tomato, garlic");
            Toast.makeText(this, "Dinner ingredients added", Toast.LENGTH_SHORT).show();
        });
    }

    private void findRecipes() {
        String userInput = etIngredients.getText().toString().trim();

        if (userInput.isEmpty()) {
            etIngredients.setError("Enter ingredients");
            etIngredients.requestFocus();
            return;
        }

        Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
        intent.putExtra("USER_INGREDIENTS", userInput);
        startActivity(intent);
    }
}