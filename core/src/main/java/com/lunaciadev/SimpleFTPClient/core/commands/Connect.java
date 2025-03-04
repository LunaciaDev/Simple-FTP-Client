package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.badlogic.gdx.Gdx;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

public class Connect implements Runnable {
    private String server;
    private Integer port;

    /**
     * Signal emitted after completion.
     * 
     * @param status {@link Boolean} {@code True} if the connection succeeded, {@code False} otherwise, and all field except message will be null if {@code False} is given.
     * @param socket {@link Socket} the control socket.
     * @param socketReader {@link BufferedReader} the reader for the socket.
     * @param socketWriter {@link BufferedWriter} the writer for the socket.
     * @param message {@link String} the error message.
     */
    public Signal completed = new Signal();

    public Connect(String server, Integer port) {
        this.server = server;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            final Socket controlSocket = new Socket(server, port);
            final BufferedReader socketReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            final BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(controlSocket.getOutputStream()));

            // remove the welcome message.
            socketReader.readLine();

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    completed.emit(true, controlSocket, socketReader, socketWriter, "");
                }
            });
        }
        catch (Exception e) {
            // TODO handle exception
            // Feel free to refactor code to handle each one properly, right now we are assuming best case scenario where nothing go wrong

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    completed.emit(false, null, null, null, e.getMessage());
                }
            });
        }
    }
}


