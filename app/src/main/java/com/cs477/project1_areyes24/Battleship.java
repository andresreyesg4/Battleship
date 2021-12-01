package com.cs477.project1_areyes24;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.WildcardType;
import java.util.Random;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battleship);
        // initialize the ships
        player_ships = new Ship[5];
        ship_init(player_ships);
        pc_ships = new Ship[5];
        ship_init(pc_ships);

        // initiate both boards
        player_board = new Board();
        pc_board = new Board();

        // place the ships in each board
        setBoard(player_board,player_ships); // the only board that will be visible.
        setBoard(pc_board, pc_ships);

        // call the layout functions to build the layout.
        build_ship_layout();
        build_attack_layout();
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
        }

        public void single_player(View v){
            Random random = new Random();
            TextView message = findViewById(R.id.message);
            int r = 0, c = 0;
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