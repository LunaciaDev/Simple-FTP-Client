package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.badlogic.gdx.Gdx;

/**
 * <p>
 * Abstraction of the FTP Login Process.
 * </p>
 * 
 * <p>
 * Require both username and password. Yes, password is not cleared after we are
 * done. FTP require password to be sent in plain text anyway. They
 * would have an easier time WireSharking the password out than attempting
 * memory reading.
 * </p>
 */
public class Login extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;

    private String username, password;

    public Login() {
    }

    public void setData(final BufferedReader socketListener, final BufferedWriter socketWriter, final String username,
            final String password) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        try {
            String[] parsedResponse;
            final String userCmd = String.format("USER %s\r\n", username);

            socketWriter.write(userCmd);
            socketWriter.flush();

            forwardControlResponse(userCmd);

            parsedResponse = listenForResponse(socketListener);

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

            forwardControlResponse("PASS *****");

            parsedResponse = listenForResponse(socketListener);

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

        } catch (final Exception e) {
            // TODO: handle exception
            Gdx.app.error("Exception", e.getMessage());
            e.printStackTrace();
        }
    }

    private void finish(final boolean status, final boolean needAcct) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                completed.emit(status, needAcct);
            }
        });
    }
}
