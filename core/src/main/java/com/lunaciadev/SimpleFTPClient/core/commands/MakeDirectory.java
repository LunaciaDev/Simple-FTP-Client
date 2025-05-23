package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.badlogic.gdx.Gdx;

public class MakeDirectory extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;

    private String dirName;

    public MakeDirectory() {
    }

    public void setData(final BufferedReader socketListener, final BufferedWriter socketWriter, final String dirName) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.dirName = dirName;
    }

    @Override
    public void run() {
        try {
            String[] parsedResponse;
            final String command = String.format("MKD %s\r\n", dirName);
            socketWriter.write(command);
            socketWriter.flush();
            forwardControlResponse(command);

            parsedResponse = listenForResponse(socketListener);

            switch (parsedResponse[0].charAt(0)) {
                case '2':
                    finish(true, parsedResponse[1]);
                    return;

                case '1':
                case '5':
                case '4':
                case '3':
                    finish(false, parsedResponse[1]);
                    return;
            }
        } catch (final Exception e) {
            // TODO handle exception
            Gdx.app.error("Exception", e.getMessage());
            e.printStackTrace();
        }
    }

    private void finish(final boolean status, final String result) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                completed.emit(status, result);
            }

        });
    }
}