package com.lunaciadev.SimpleFTPClient.utils;

import java.util.ArrayList;
import java.util.List;

public class Signal {
    private List<Slot> slots = new ArrayList<Slot>();

    public void connect(Slot slot) {
        slots.add(slot);
    }

    public void emit(Object... args) {
        for (Slot slot : slots) {
            slot.onSignal(args);
        }
    }
}