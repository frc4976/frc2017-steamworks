package ca._4976.library.controllers;

import ca._4976.library.listeners.BooleanListener;

import java.util.ArrayList;

public class Button {

    int id;

    private ArrayList<BooleanListener> listeners = new ArrayList<>();
    private boolean[] values = new boolean[2];
    private int onTime = 0;

    Button(int id) { this.id = id; }

    public boolean get() { throw new NullPointerException(); }

    void eval() {

        values[0] = values[1];
        values[1] = get();

        if (values[1] && onTime > -1) onTime++; else onTime = 0;

        if (onTime == -1 && !values[1]) onTime = 0;

        if (values[0] != values[1]) for (BooleanListener listener : listeners) listener.changed();

        if (!values[0] && values[1]) for (BooleanListener listener : listeners) listener.rising();

        if (values[0] && !values[1]) for (BooleanListener listener : listeners) listener.falling();

        if (onTime > 24) for (BooleanListener listener : listeners) {

            listener.held();
            onTime = -1;
        }
    }

    public void addListener(BooleanListener listener) { listeners.add(listener); }
}