package com.lunaciadev.SimpleFTPClient.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.badlogic.gdx.Gdx;

public class AsciiListener {
    private BufferedReader controlReader;
    private Thread thread;
    private volatile boolean stopThread = false;

    public Signal controlResponse;

    public AsciiListener(InputStream controlInputStream) {
        controlReader = new BufferedReader(new InputStreamReader(controlInputStream));
        
        // start listening on a separate thread
        // TODO allow binary data?

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stopThread) {
                    try {
                        final String response = controlReader.readLine();

                        // run this runnable in the main thread
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                controlResponse.emit(response);
                            }
                        });
                    }
                    catch (Exception e) {
                        // TODO unhandled exception
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Slot, to be added later to stop the thread
     */
    public void onStopThreadSignal(Object... args) {
        stopThread = true;
    }

    /**
     * Slot, to restart thread
     */
    public void onStartThreadSignal(Object... args) {
        InputStream controlInputStream = (InputStream) args[0];

        controlReader = new BufferedReader(new InputStreamReader(controlInputStream));

        stopThread = false;
        thread.start();
    }
}
