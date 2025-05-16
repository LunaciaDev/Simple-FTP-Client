package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.badlogic.gdx.Gdx;

/**
 * Abstraction of FTP's {@code QUIT} command. Also signal the FTP Client to
 * close the socket once completed.
 */
public class Quit extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;

    public Quit() {
    }

    public void setData(final BufferedReader socketListener, final BufferedWriter socketWriter) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
    }

    @Override
    public void run() {
        String[] parsedResponse;

        try {
            socketWriter.write("QUIT\r\n");
            socketWriter.flush();

            forwardControlResponse("QUIT");

            final String quitResponse = socketListener.readLine();

            forwardControlResponse(quitResponse);

            parsedResponse = parseResponse(quitResponse);

            switch (parsedResponse[0].charAt(0)) {
                case '2':
                    finish(true);
                    break;

                default:
                    finish(false);
                    break;
            }

        } catch (final Exception e) {
            // TODO: handle exception
            Gdx.app.error("Exception", e.getMessage());
            e.printStackTrace();
        }
    }

    private void finish(final boolean status) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                completed.emit(status);
            }

        });
    }
}
