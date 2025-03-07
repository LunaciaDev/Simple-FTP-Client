package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import com.badlogic.gdx.Gdx;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

/**
 * <p>
 * Abstraction of FTP's {@code LIST} command.
 * </p>
 * 
 * <p>
 * This command return format is not restricted. FileZilla implemented a parsing
 * method to have a nice file display based on this command, and it is a giant
 * block of code and regex. I guess I'll take the easy way out and just display
 * raw return data.
 * </p>
 */
public class List extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;
    private ExecutorService dataService;
    private volatile boolean allDataReceived = false;
    private boolean malformedData = false;

    private final Signal dataStruct = new Signal();

    public List() {
    }

    public void setData(final BufferedReader socketListener, final BufferedWriter socketWriter,
            final ExecutorService dataService) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.dataService = dataService;

        dataStruct.connect(this::checkResult);
    }

    @Override
    public void run() {
        try {
            String[] parsedResponse;
            String[] addr;

            socketWriter.write("PASV\r\n");
            socketWriter.flush();
            forwardControlResponse("PASV\n");

            final String pasvResponse = socketListener.readLine();

            forwardControlResponse(pasvResponse);

            parsedResponse = parseResponse(pasvResponse);

            switch (parsedResponse[0].charAt(0)) {
                case '2':
                    addr = parsePasvResponse(parsedResponse[1]);
                    break;

                default:
                    finish(false, null);
                    return;
            }

            // prepare the socket to be ready to read first.
            // TODO interrupt the thread, just in case if it get stuck in read.
            dataService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Socket dataSocket = new Socket(String.join(".", addr[0], addr[1], addr[2], addr[3]),
                                Integer.parseInt(addr[4]) * 256 + Integer.parseInt(addr[5]));
                        final BufferedReader dataReader = new BufferedReader(
                                new InputStreamReader(dataSocket.getInputStream()));
                        final StringBuilder response = new StringBuilder();
                        String temp;

                        while (!allDataReceived) {
                            while ((temp = dataReader.readLine()) != null) {
                                response.append(temp).append("\n");
                            }
                        }

                        response.setLength(response.length() - 1);

                        final String result = response.toString();

                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                dataStruct.emit(result);
                            }
                        });

                        dataSocket.close();
                    } catch (final Exception e) {
                        // TODO: handle exception
                        Gdx.app.error("Exception", e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            socketWriter.write("LIST\r\n");
            socketWriter.flush();
            forwardControlResponse("LIST\n");
            while (true) {
                final String listResponse = socketListener.readLine();
                forwardControlResponse(listResponse);

                parsedResponse = parseResponse(listResponse);

                switch (parsedResponse[0].charAt(0)) {
                    case '2':
                        allDataReceived = true;
                        return;

                    case '1':
                        break;

                    default:
                        allDataReceived = true;
                        malformedData = true;
                        return;
                }
            }

        } catch (final Exception e) {
            // TODO: handle exception
            Gdx.app.error("Exception", e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkResult(final Object... args) {
        if (malformedData)
            finish(false, null);
        else
            finish(true, (String) args[0]);
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
