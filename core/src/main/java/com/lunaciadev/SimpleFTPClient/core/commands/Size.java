package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.badlogic.gdx.Gdx;

public class Size extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;

    private String fileName;

    public Size() {
    }

    public void setData(final BufferedReader socketListener, final BufferedWriter socketWriter, final String fileName) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        try {
            String[] parsedResponse;
            final String command = String.format("SIZE %s\r\n", fileName);
            socketWriter.write(command);
            socketWriter.flush();
            forwardControlResponse(command);

            final String response = socketListener.readLine();

            parsedResponse = parseResponse(socketListener.readLine());

            forwardControlResponse(response);

            switch (parsedResponse[0].charAt(0)) {
                case '2':
                    finish(true, Long.parseLong(parsedResponse[1]));
                    return;

                case '1':
                case '5':
                case '4':
                case '3':
                    finish(false, null);
                    return;
            }
        } catch (final Exception e) {
            // TODO handle exception
            Gdx.app.error("Exception", e.getMessage());
            e.printStackTrace();
        }
    }

    private void finish(final boolean status, final Long size) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                completed.emit(status, size);
            }

        });
    }
}
