package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.badlogic.gdx.Gdx;

/**
 * Connect to the FTP Server by attempting to open a TCP Connection to the
 * specified server at the specified port. Return the socket and both
 * reader/writer.
 */
public class Connect extends Command implements Runnable {
    private String server;
    private Integer port;

    public Connect() {
    }

    public void setData(final String server, final Integer port) {
        this.server = server;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            final Socket controlSocket = new Socket(server, port);
            final BufferedReader socketReader = new BufferedReader(
                    new InputStreamReader(controlSocket.getInputStream()));
            final BufferedWriter socketWriter = new BufferedWriter(
                    new OutputStreamWriter(controlSocket.getOutputStream()));

            // remove the welcome message.
            final String response = socketReader.readLine();

            forwardControlResponse(response);

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    completed.emit(true, controlSocket, socketReader, socketWriter, "");
                }
            });
        } catch (final Exception e) {
            // TODO handle exception

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    completed.emit(false, null, null, null, e.getMessage());
                }
            });
        }
    }
}
