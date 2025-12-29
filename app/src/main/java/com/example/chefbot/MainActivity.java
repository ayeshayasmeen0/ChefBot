package com.example.chefbot;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Use SIMPLE layout

        Button btn = findViewById(R.id.btnTest);
        btn.setOnClickListener(v -> {
            Toast.makeText(this, "App is working!", Toast.LENGTH_SHORT).show();
        });
    }
}