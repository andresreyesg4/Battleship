package com.cs477.project1_areyes24;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Battleship extends AppCompatActivity {
    private final int WIDTH = 8;
    private final int HEIGHT = 8;
    private final int HORIZONTAL = 0;
    private TextView [][]ship_layout;
    private Button[][]buttons;
    private Ship[] player_ships;
    private Ship[] pc_ships;
    private Board player_board;
    private Board pc_board;
    private int player_life = 16, pc_life = 16;
    private FirebaseDatabase database;
    private DatabaseReference turnTraker;
    private DatabaseReference lifeTracker;
    private DatabaseReference gameover;
    private DatabaseReference coordinate_moves;
    private DatabaseReference guest_coordinate_moves;
//    private DatabaseReference other_player_moves;
//    private DatabaseReference my_moves;
//    private DatabaseReference other_player_hitmiss;
    private DatabaseReference my_hitmiss;
    private String playerName, roomName, role, next_move;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battleship);
        // initiate the database only if necessary
        Bundle extras = getIntent().getExtras();
        if(extras.getBoolean("multiplayer")){
            // when multiplayer we are only creating one board.
            role = ""; next_move = "";
            database = FirebaseDatabase.getInstance();
            SharedPreferences preferences = getSharedPreferences("PREFS", 0);
            playerName = preferences.getString("playerName", "");
            roomName = extras.getString("roomName");
            if(roomName.equals(playerName)){
                role = "host";
            }else {
                role = "guest";
            }
            player_ships = new Ship[5];
            ship_init(player_ships);
            player_board = new Board();
            setBoard(player_board, player_ships);
            build_ship_layout();
            build_attack_layout();
            next_move = role + ":None";
            coordinate_moves = database.getReference("rooms/" + roomName + "/coordinate_moves");
            coordinate_moves.setValue(next_move);
            addCoordinateListener();
        }else {
            // initialize the ships
            player_ships = new Ship[5];
            ship_init(player_ships);
            pc_ships = new Ship[5];
            ship_init(pc_ships);

            // initiate both boards
            player_board = new Board();
            pc_board = new Board();

            // place the ships in each board
            setBoard(player_board, player_ships); // the only board that will be visible.
            setBoard(pc_board, pc_ships);

            // call the layout functions to build the layout.
            build_ship_layout();
            build_attack_layout();
        }
    }

    private void addCoordinateListener() {
        coordinate_moves.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(role.equals("host")){
                    if(snapshot.getValue(String.class).contains("guest")){
                        // coming from the guest
                        String move = snapshot.getValue(String.class);
                        String[] coord = move.substring(7).split(",");
                        int row = Integer.parseInt(coord[0]);
                        int column = Integer.parseInt(coord[1]);
                        if (player_board.getValue(row, column) != 0 && buttons[row][column].isEnabled()) {
                            ship_layout[row][column].setBackgroundColor(Color.RED);
                            buttons[row][column].setEnabled(false);
                            player_life--;
                            if (player_life == 0) {
                                // player died
                            }
                        }else{
                            ship_layout[row][column].setBackgroundColor(Color.YELLOW);
                            player_board.setValue(row,column,-1);
                        }
                        Toast.makeText(Battleship.this, "" + snapshot.
                                        getValue(String.class).replace("guest:", ""),
                                Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(snapshot.getValue(String.class).contains("host")){
                        // coming from the host
                        String move = snapshot.getValue(String.class);
                        String[] coord = move.substring(6).split(",");
                        int row = Integer.parseInt(coord[0]);
                        int column = Integer.parseInt(coord[1]);
                        if (player_board.getValue(row, column) != 0 && buttons[row][column].isEnabled()) {
                            ship_layout[row][column].setBackgroundColor(Color.RED);
//                            shi[row][column].setEnabled(false);
                            player_life--;
                            if (player_life == 0) {
                                // player died
                            }
                        }else{
                            ship_layout[row][column].setBackgroundColor(Color.YELLOW);
                            player_board.setValue(row, column, -1);
                        }
                        Toast.makeText(Battleship.this, "" + snapshot.
                                        getValue(String.class).replace("host:", ""),
                                Toast.LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                coordinate_moves.setValue(next_move);
            }
        });
    }

    // Function that programmatically builds the gridlayout of the players ships
    public void build_ship_layout(){
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int h = (size.y/2) / (HEIGHT + 2);
        GridLayout grid_ship_layout = findViewById(R.id.top_grid);
        grid_ship_layout.setColumnCount(WIDTH);
        // get the width of the phone and then divide.
        grid_ship_layout.setRowCount(HEIGHT);
        ship_layout = new TextView[WIDTH][HEIGHT];
        for(int i = 0; i < WIDTH; i++){
            for(int j = 0; j < HEIGHT; j++) {
                int temp = player_board.getValue(i, j);
                ship_layout[i][j] = new TextView(this);
                ship_layout[i][j].setBackgroundColor(Color.rgb(60, 185, 225));
                ship_layout[i][j].setWidth(100);
                ship_layout[i][j].setHeight(h);
                if(temp != 0){
                    ship_layout[i][j].setText(Integer.toString(temp));
                    ship_layout[i][j].setBackgroundColor(Color.GRAY);
                    ship_layout[i][j].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }
                grid_ship_layout.addView(ship_layout[i][j]);
            }
        }
    }

    // Function that programmatically builds the gridlayout of the possible attacks.
    public void build_attack_layout(){
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        GridLayout grid_attack_layout = findViewById(R.id.attack_grid);
        int h = (size.y/2) / (HEIGHT + 2);
        grid_attack_layout.setRowCount(HEIGHT);
        grid_attack_layout.setColumnCount(WIDTH);
        buttons = new Button[WIDTH][HEIGHT];
        ButtonHandler buttonHandler = new ButtonHandler();
        for(int i = 0; i < WIDTH; i++){
            for(int j = 0; j < HEIGHT; j++){
                buttons[i][j] = new Button(this);
                buttons[i][j].setOnClickListener(buttonHandler);
                buttons[i][j].setBackgroundColor(Color.rgb(60,185,225));
                buttons[i][j].setText("x");
                buttons[i][j].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                grid_attack_layout.addView(buttons[i][j], 100, h);
            }
        }
    }

    // function to randomly place the ship in a given board.
    public void setBoard(Board board, Ship[] ships){
        Random random = new Random();
        int row, column = 0;
        // generate the random placement of the ships on both boards.
        if(board != null && ships != null){
            for(int i = 0; i < ships.length; i++){
                Ship temp = ships[i];
                boolean placed = false;
                while(!placed){
                    if(temp.getDirection()== HORIZONTAL){
                        row = random.nextInt(HEIGHT);     // HEIGHT, WIDTH = 8
                        column = random.nextInt(WIDTH - temp.getSize());
                    }else{
                        row = random.nextInt(HEIGHT - temp.getSize());
                        column = random.nextInt(WIDTH);
                    }
                    placed = true;

                    // check to make sure al locations for the ship are free.
                    if(temp.getDirection() == HORIZONTAL){
                        for(int index = 0; index < temp.getSize(); index++){
                            if(board.getValue(row,column + index) != 0){ placed = false; }
                        }
                    }else{
                        for(int index = 0; index < temp.getSize(); index++){
                            if(board.getValue(row + index, column) != 0){ placed = false;}
                        }
                    }

                    // if all is okay, place the sip here.
                    if(placed){
                        if(temp.getDirection() == HORIZONTAL){
                            for(int index = 0; index < temp.getSize(); index++){
                                board.setValue(row, column + index, temp.getNumber()); }
                        }else{
                            for(int index = 0; index < temp.getSize(); index++){
                                board.setValue(row + index, column, temp.getNumber()); }
                        }
                        temp.setRowCol(row, column);
                    }
                }
            }
        }
    }

    // function to initialize the ships array.
    public void ship_init(Ship[] ships){
        Random random = new Random();
        int direction = 0, size = 0;
        for (int i = 0; i < ships.length; i++){
            direction = random.nextInt(2);
            size = 5 - i;
            if(size == 2){size++;}
            ships[i] = new Ship(size, direction, i + 1);
        }
    }

    /*
    * add when a ship has sunken on the pc board
    * give pc some mind.*/
    // create a button handler class to handle each button on the grid.
    private class ButtonHandler implements View.OnClickListener {
        public void onClick(View v) {
            Bundle bundle = getIntent().getExtras();
            boolean is_multiplayer = bundle.getBoolean("multiplayer");
            if(is_multiplayer){
                multiplayer(v);
            }else {
                single_player(v);
            }
        }

        private void multiplayer(View v) {
            // implement the game playing with multiplayer.
            TextView message = findViewById(R.id.message);
            boolean game_won = false;
//            initialize
            if(role.equals("host")){
//                guest_coordinate_moves = database.getReference("rooms/" + roomName + "/guest_coordinate_moves");
//                next_move = "None";
//                guest_coordinate_moves.setValue(next_move);
//                addGuestCoordinateListener();
                // send playerOnes moves
                for(int row = 0; row < 8; row++){
                    for(int column = 0; column < 8; column++){
                        if(v == buttons[row][column]){
                            next_move = "host: " + Integer.toString(row) + "," + Integer.toString(column);
                            coordinate_moves.setValue(next_move);
//                            guest_coordinate_moves.setValue(next_move);
//                            addGuestCoordinateListener();
                        }
                    }
                }
            }else{
//                coordinate_moves = database.getReference("rooms/" + roomName + "/host_coordinate_moves");
//                next_move = "None";
//                coordinate_moves.setValue(next_move);
//                addHostCoordinateListener();
                // send playerTwo moves
                for(int row = 0; row < 8; row++){
                    for(int column = 0; column < 8; column++){
                        if(v == buttons[row][column]){
                            next_move = "guest: " + Integer.toString(row) + "," + Integer.toString(column);
                            coordinate_moves.setValue(next_move);
//                            addHostCoordinateListener();
                        }
                    }
                }
            }

        }

//        private void addGuestCoordinateListener() {
//            guest_coordinate_moves.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    String move = (String) snapshot.getValue();
//                    if(snapshot != null && role.equals("guest")){
//                        if (move.contains("host:")) {
//                            String[] coor = move.substring(7).split(",");
//                            int row = Integer.parseInt(coor[0]);
//                            int column = Integer.parseInt(coor[1]);
//                            System.out.println(row + ", " + column);
//                            if (player_board.getValue(row, column) != 0 && buttons[row][column].isEnabled()) {
//                                buttons[row][column].setBackgroundColor(Color.RED);
//                                buttons[row][column].setEnabled(false);
//                                player_life--;
//                                if (player_life == 0) {
//                                    // player died
//                                }
//                            }
//                        }
//                    }
//
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }

//        // listener for next move
//        private void addHostCoordinateListener() {
//            coordinate_moves.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    // next move received
////                    DatabaseReference temp = database.getReference("rooms/guest_coordinate_moves");
////                    String move;
////                    move = (String) snapshot.getValue();
////                    Task<DataSnapshot> task = temp.get();
////                    task.
//                    if(snapshot != null){
//                        // player one
//                        if(move.contains("guest:")){
//                            String[] coor = move.substring(7).split(",");
//                            int row = Integer.parseInt(coor[0]);
//                            int column = Integer.parseInt(coor[1]);
//                            System.out.println(row + ", " + column);
//                            if(player_board.getValue(row, column) != 0 && buttons[row][column].isEnabled()){
//                                buttons[row][column].setBackgroundColor(Color.RED);
//                                buttons[row][column].setEnabled(false);
//                                player_life--;
//                                if(player_life == 0){
//                                    // player died
//                                }
//                            }
//                        }
//
//                    }else{
//                        // player two
//                        if(snapshot != null && move.contains("guest:")){
//                            if (move.contains("guest:")) {
//                                String[] coor = move.substring(7).split(",");
//                                int row = Integer.parseInt(coor[0]);
//                                int column = Integer.parseInt(coor[1]);
//                                System.out.println(row + ", " + column);
//                                if (player_board.getValue(row, column) != 0 && buttons[row][column].isEnabled()) {
//                                    buttons[row][column].setBackgroundColor(Color.RED);
//                                    buttons[row][column].setEnabled(false);
//                                    player_life--;
//                                    if (player_life == 0) {
//                                        // player died
//                                    }
//                                }
//                            }
//                        }
//
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }

        public void single_player(View v){
            Random random = new Random();
            TextView message = findViewById(R.id.message);
            int r, c;
            boolean game_won = false;
            for (int row = 0; row < 8; row++) {
                for (int column = 0; column < 8; column++) {
                    if (v == buttons[row][column]) {
                        if (pc_board.getValue(row, column) != 0 && buttons[row][column].isEnabled()) {
                            buttons[row][column].setBackgroundColor(Color.RED);
                            buttons[row][column].setEnabled(false);
                            pc_life--;
                            if (pc_life == 0) {
                                message.setText("You Win!!");
                                game_won = true;
                            }
                        }else if(buttons[row][column].isEnabled()){
                            buttons[row][column].setEnabled(false);
                            buttons[row][column].setText("");
                        }
                        r = random.nextInt(8);
                        c = random.nextInt(8);
                        while (player_board.getValue(r, c) == -1) {
                            r = random.nextInt(8);
                            c = random.nextInt(8);
                        }
                        if (player_board.getValue(r, c) != 0 && game_won == false) {
                            ship_layout[r][c].setBackgroundColor(Color.RED);
                            player_board.setValue(r, c, -1);
                            player_life--;
                            if (player_life == 0) {
                                message.setText("You Loose!");
                                game_won = true;
                            }

                        } else {
                            ship_layout[r][c].setBackgroundColor(Color.YELLOW);
                            player_board.setValue(r, c, -1);
                        }
                    }
                    if(game_won){
                        disable_all(buttons);
                        break;
                    }
                }
            }
        }

        // function to disable all the buttons when someone wins.
        private void disable_all(Button[][] buttons){
            for (int i = 0; i < buttons.length; i++){
                for(int j = 0; j < buttons.length; j++){
                    if(buttons[i][j].isEnabled()){
                        buttons[i][j].setEnabled(false);
                    }
                }
            }
        }
    }
}