package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.badlogic.gdx.Gdx;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

public class Account extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;
    
    private String account;

    public Signal completed = new Signal();

    public Account(BufferedReader socketListener, BufferedWriter socketWriter, String account) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.account = account;
    }

    @Override
    public void run() {
        String[] parsedResponse;

        try {
            socketWriter.write(String.format("USER %s\r\n", account));
            socketWriter.flush();
            
            final String response = socketListener.readLine();

            parsedResponse = parseResponse(socketListener.readLine());

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    ftpControlReceived.emit(response);
                }
            });

            switch (parsedResponse[0].charAt(0)) {
                case '2':
                    finish(true);
                    return;
                
                case '1':
                case '5':
                case '4':
                case '3':
                    finish(false);
                    return;
            }
        }
        catch (Exception e) {
            // TODO handle exception
            Gdx.app.error("Exception", e.getMessage());
            e.printStackTrace();
        }
    }

    private void finish(boolean status) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                completed.emit(status);
            }
            
        });
    }
}
