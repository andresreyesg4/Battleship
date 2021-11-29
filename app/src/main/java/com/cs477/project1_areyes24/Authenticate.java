package com.cs477.project1_areyes24;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Authenticate extends AppCompatActivity {
    private Button sign_in;
    private EditText username;
    private String playerName = "";
    private FirebaseDatabase database;
    private DatabaseReference playerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);
        sign_in = findViewById(R.id.sing_in);
        sign_in.setOnClickListener(this::signIn);
        username = findViewById(R.id.user_name);
        database = FirebaseDatabase.getInstance();
        SharedPreferences pref = getSharedPreferences("PREFS", 0);
        playerName = pref.getString("playerName", "");
        if(!playerName.equals("")){
            playerRef = database.getReference("players/" + playerName);
            addListener();
            playerRef.setValue("");
        }
    }

    public void signIn(View view){
        String usr = username.getText().toString();
        if(!usr.isEmpty()){
            playerName = usr;
            username.setText("");
            sign_in.setText("Signing in");
            sign_in.setEnabled(false);
            playerRef = database.getReference("players/" + playerName);
            addListener();
            playerRef.setValue("");
        }else{
            Toast.makeText(this, "Please enter a user name", Toast.LENGTH_SHORT);
        }

    }

    private void addListener() {
        // read from the database
        playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!playerName.equals("")){
                    SharedPreferences preferences = getSharedPreferences("PREFS", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("playerName", playerName);
                    editor.apply();
                    startActivity(new Intent(getApplicationContext(), Multiplayer.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                sign_in.setText("Sign IN");
                sign_in.setEnabled(true);
                Toast.makeText(Authenticate.this, "Error!", Toast.LENGTH_SHORT);
            }
        });
    }
}