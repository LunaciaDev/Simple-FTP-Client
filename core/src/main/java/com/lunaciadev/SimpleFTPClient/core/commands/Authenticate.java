package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.badlogic.gdx.Gdx;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

public class Authenticate extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;

    private String username, password;

    public Signal completed = new Signal();

    public Authenticate(BufferedReader socketListener, BufferedWriter socketWriter, String username, String password) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        String[] response;

        try {
            socketWriter.write(String.format("USER %s\r\n", username));
            socketWriter.flush();
            response = parseResponse(socketListener.readLine());

            switch (response[0].charAt(0)) {
                case '2':
                    finish(true, false);
                    return;
                
                case '1':
                case '5':
                case '4':
                    finish(false, false);
                    return;

                case '3':
                    break;
            }

            // Need password now.
            socketWriter.write(String.format("PASS %s\r\n", password));
            socketWriter.flush();
            response = parseResponse(socketListener.readLine());

            switch (response[0].charAt(0)) {
                case '2':
                    finish(true, false);
                    return;
                
                case '1':
                case '5':
                case '4':
                    finish(false, false);
                    return;

                case '3':
                    finish(false, true);
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void finish(boolean status, boolean needAcct) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                completed.emit(status, needAcct);
            }
        });
    }
}
