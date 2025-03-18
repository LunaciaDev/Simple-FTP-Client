package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;

public class CurrentDirectory extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;
    private Pattern directoryPattern;

    public CurrentDirectory() {
        directoryPattern = Pattern.compile("([\"'])(.*)\\1");
    }

    public void setData(final BufferedReader socketListener, final BufferedWriter socketWriter) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
    }

    @Override
    public void run() {
        try {
            String[] parsedResponse;
            final String command = String.format("PWD\r\n");
            socketWriter.write(command);
            socketWriter.flush();
            forwardControlResponse(command);

            final String response = socketListener.readLine();
            parsedResponse = parseResponse(response);
            forwardControlResponse(response);

            switch (parsedResponse[0].charAt(0)) {
                case '2':
                    Matcher m = directoryPattern.matcher(parsedResponse[1]);
                    m.find();
                    String result = m.toMatchResult().group(0).replace("\"\"", "\"").replace("''", "'");

                    finish(true, result);
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

    private void finish(final boolean status, String result) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                completed.emit(status, result);
            }

        });
    }
}
