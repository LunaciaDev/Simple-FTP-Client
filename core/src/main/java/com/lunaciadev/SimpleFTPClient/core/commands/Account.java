package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.badlogic.gdx.Gdx;

/**
 * <p>
 * Abstraction of FTP's {@code ACCT} command.
 * </p>
 * 
 * Require a {@link String} being the user's Account Name. I have no idea what
 * that mean, but RFC 959 allow server to request this during login.
 */
public class Account extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;

    private String account;

    public Account() {
    }

    public void setData(final BufferedReader socketListener, final BufferedWriter socketWriter, final String account) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.account = account;
    }

    @Override
    public void run() {
        try {
            String[] parsedResponse;
            final String command = String.format("ACCT %s\r\n", account);
            socketWriter.write(command);
            socketWriter.flush();
            forwardControlResponse(command);

            parsedResponse = listenForResponse(socketListener);

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
        } catch (final Exception e) {
            // TODO handle exception
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
