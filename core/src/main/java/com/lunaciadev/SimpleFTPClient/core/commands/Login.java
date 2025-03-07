package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.badlogic.gdx.Gdx;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

public class Login extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;

    private String username, password;

    public Signal completed = new Signal();

    public Login() {}

    public void setData(BufferedReader socketListener, BufferedWriter socketWriter, String username, String password) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        String[] parsedResponse;

        try {
            socketWriter.write(String.format("USER %s\r\n", username));
            socketWriter.flush();

            final String userResponse = socketListener.readLine();

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    ftpControlReceived.emit(userResponse);
                }
            });

            parsedResponse = parseResponse(userResponse);

            switch (parsedResponse[0].charAt(0)) {
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

            final String passResponse = socketListener.readLine();

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    ftpControlReceived.emit(passResponse);
                }
            });

            parsedResponse = parseResponse(passResponse);

            switch (parsedResponse[0].charAt(0)) {
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
            Gdx.app.error("Exception", e.getMessage());
            e.printStackTrace();
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
