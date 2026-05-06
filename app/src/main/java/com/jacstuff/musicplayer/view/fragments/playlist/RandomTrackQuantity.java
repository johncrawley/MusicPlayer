package com.jacstuff.musicplayer.view.fragments.playlist;

public class RandomTrackQuantity {

    public final static int MAX_VALUE = 200;
    public final static int INITIAL_VALUE = 1;
    private final int SECOND_VALUE = 5;
    private final int THIRD_VALUE = 10;
    private final int INC_AMOUNT = 10;
    private int currentValue = INITIAL_VALUE;

    public int get(){
        return currentValue;
    }


    public void increment(){
        currentValue = switch (currentValue){
            case INITIAL_VALUE -> SECOND_VALUE;
            case SECOND_VALUE -> THIRD_VALUE;
            case MAX_VALUE -> MAX_VALUE;
            default -> currentValue + INC_AMOUNT;
        };
    }


    public void decrement(){
        currentValue = switch (currentValue){
            case INITIAL_VALUE, SECOND_VALUE -> INITIAL_VALUE;
            case THIRD_VALUE -> SECOND_VALUE;
            default -> currentValue - INC_AMOUNT;
        };
    }
}
