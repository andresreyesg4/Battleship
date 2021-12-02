package com.cs477.project1_areyes24;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Multiplayer extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private ListView player_list;
    private ArrayList<String> roomList;
    private DatabaseReference roomsReference;
    private String playername, roomname;
    private Button matchmaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);
        roomList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        SharedPreferences pref = getSharedPreferences("PREFS", 0);
        playername = pref.getString("playerName","");
        roomname = "Battle with " + playername;
        player_list = findViewById(R.id.user_list);
        matchmaker = findViewById(R.id.matchroom);
        matchmaker.setOnClickListener(this::match);
        player_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                roomname = roomList.get(i);
                databaseReference = database.getReference("rooms/" + roomname + "/player2");
                addEvent();
                databaseReference.setValue(playername);
            }
        });
        addRoom();
        if(roomList.isEmpty()){
            Toast.makeText(Multiplayer.this, "There is no rooms to play, Create one!", Toast.LENGTH_SHORT);
        }
    }

    private void addRoom() {
        roomsReference = database.getReference("rooms"  );
        roomsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear();
                Iterable<DataSnapshot> rooms = snapshot.getChildren();
                for(DataSnapshot snapshot1: rooms){
                    roomList.add(snapshot1.getKey());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(Multiplayer.this,
                            android.R.layout.simple_list_item_1, roomList);
                    player_list.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void match(View view){
        matchmaker.setText("Creating Match");
        matchmaker.setEnabled(false);
        roomname = playername;
        databaseReference = database.getReference("rooms/" + roomname + "/player1");
        addEvent();
        databaseReference.setValue(playername);
    }

    private void addEvent() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                matchmaker.setText("Create Match");
                matchmaker.setEnabled(true);
                Intent intent = new Intent(getApplicationContext(), Battleship.class);
                intent.putExtra("roomName", roomname);
                intent.putExtra("multiplayer", true);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                matchmaker.setText("Create Match");
                matchmaker.setEnabled(true);
                Toast.makeText(Multiplayer.this, "Error!!", Toast.LENGTH_SHORT);
            }
        });
    }
}