package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.badlogic.gdx.Gdx;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

public class Quit extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;

    /**
     * @param status {@link Boolean} {@code True} if disconnected successfully, {@code False} otherwise.
     */
    public Signal completed = new Signal();

    public Quit() {}

    public void setData(BufferedReader socketListener, BufferedWriter socketWriter) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
    }

    @Override
    public void run() {
        String[] parsedResponse;

        try {
            socketWriter.write("QUIT\r\n");
            socketWriter.flush();

            final String quitResponse = socketListener.readLine();

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    ftpControlReceived.emit(quitResponse);
                }
            });

            parsedResponse = parseResponse(quitResponse);

            switch (parsedResponse[0].charAt(0)) {
                case '2':
                    finish(true);
                    break;

                default:
                    finish(false);
                    break;
            }

        } catch (Exception e) {
            // TODO: handle exception
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
