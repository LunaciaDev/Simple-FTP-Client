package com.lunaciadev.SimpleFTPClient.utils;

/**
 * <p>
 * Slot class, inspired by Qt's Signal and Slot.
 * </p>
 * <p>
 * FunctionalInterface allow us to pass a function pointer to the Signal and
 * have that avaialble as a simple call, to put it simply.
 * </p>
 * 
 * @author LunaciaDev
 */
@FunctionalInterface
public interface Slot {
    void onSignal(Object... args);
}