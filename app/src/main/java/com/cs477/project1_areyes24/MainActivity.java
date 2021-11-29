package com.cs477.project1_areyes24;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button multiplayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        multiplayer = findViewById(R.id.multiplayer);
        multiplayer.setOnClickListener(this::startMultiplayer);
    }

    // function to handle multiplayer mode
    public void startMultiplayer(View view){
        // create an intent for the multiplayer
        Intent intent = new Intent(this, Authenticate.class);
        startActivity(intent);
    }

    // Function to handle the click of the start button to begin the game.
    public void startGame(View view){
        // create an intent for the activity
        Intent intent = new Intent(this, Battleship.class);
        // start the activity
        startActivity(intent);
    }
}