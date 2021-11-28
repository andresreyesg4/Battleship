package com.cs477.project1_areyes24;

public class Ship {
    // create the ship's attributes.
    private int size, direction, number, row, column;

    // Ship's constructor.
    public Ship(int size, int direction, int number){
        this.size = size;
        this.direction = direction;
        this.number = number;
        row = 0;
        column = 0;
    }

    public void setRowCol(int row, int column){
        this.row = row;
        this.column = column;
    }

    public int getSize(){
        return this.size;
    }

    public int getDirection(){
        return this.direction;
    }

    public int getNumber(){
        return this.number;
    }
}
