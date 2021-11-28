package com.cs477.project1_areyes24;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Function to handle the click of the start button to begin the game.
    public void startGame(View view){
        // create an intent for the activity
        Intent intent = new Intent(this, Battleship.class);
        // start the activity
        startActivity(intent);
    }
}