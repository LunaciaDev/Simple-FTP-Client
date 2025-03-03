package com.lunaciadev.SimpleFTPClient.utils;

@FunctionalInterface
public interface Slot {
    void onSignal(Object... args);
}