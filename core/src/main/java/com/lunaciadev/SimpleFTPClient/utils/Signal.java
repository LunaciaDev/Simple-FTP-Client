package com.lunaciadev.SimpleFTPClient.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Signal class, inspired by Qt's Signal and Slot mechanism.
 * </p>
 * <p>
 * A Signal store the function pointers of from all Slots. When the signal is
 * emitted, it will call all saved pointer with the argument provided to the
 * emit call.
 * </p>
 * <p>
 * To not have to write a separate signal class for every possible arguments
 * set, I used varargs. This mean every signal must document what it will be
 * passing to its Slot for the slot to be able to cast the argument correctly,
 * thus the javadocs attached to the signals.
 * </p>
 * <p>
 * Note that unlike Qt, Signal and Slot here is <strong>NOT THREAD-SAFE</strong>.
 * </p>
 * 
 * @author LunaciaDev
 */
public class Signal {
    private final List<Slot> slots = new ArrayList<Slot>();

    public void connect(final Slot slot) {
        slots.add(slot);
    }

    public void emit(final Object... args) {
        for (final Slot slot : slots) {
            slot.onSignal(args);
        }
    }
}