package com.cs477.project1_areyes24;

public class Board {
    // create the boards attribute
    private int[][]board;

    public Board(){
        board = new int[8][8];
        board_init(board);
    }

    private void board_init(int[][] b){
        for(int i = 0; i < b.length; i++){
            for(int j = 0; j < b.length; j++){
                b[i][j] = 0;
            }
        }
    }

    public void setValue(int row, int column, int value){
        if(row >= 0 && column >= 0)
        board[row][column] = value;
    }

    public int getValue(int row, int column){
        if(row >= 0 && column >= 0) {
            return board[row][column];
        }
        return -1;
    }

}
