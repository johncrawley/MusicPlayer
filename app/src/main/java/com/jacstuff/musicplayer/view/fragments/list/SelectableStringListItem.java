package com.jacstuff.musicplayer.view.fragments.list;

public class SelectableStringListItem {

    private final String value;
    private boolean isSelected;

    public SelectableStringListItem(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public boolean isSelected(){
        return isSelected;
    }

    public void toggleSelected(){
        isSelected = !isSelected;
    }

}
