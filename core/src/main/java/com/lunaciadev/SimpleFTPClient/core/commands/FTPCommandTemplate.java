package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.badlogic.gdx.Gdx;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

public class FTPCommandTemplate extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;

    /**
     * 
     */
    public Signal completed = new Signal();

    public FTPCommandTemplate(BufferedReader socketListener, BufferedWriter socketWriter) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
    }

    @Override
    public void run() {
        try {
            
        } catch (Exception e) {
            Gdx.app.error("Exception", e.getMessage());
            e.printStackTrace();
        }
    }

    private void finish() {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'run'");
            }
            
        });
    }
}
