package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import com.badlogic.gdx.Gdx;

/**
 * <p>
 * Abstraction of FTP's {@code STOR} command.
 * </p>
 * 
 * <p>
 * Upload a file to the server. Also pass upload progress to the UI too.
 * </p>
 */
public class Store extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;
    private ExecutorService dataService;
    private String serverAddr;
    private boolean malformedData = false;

    private Path uploadTarget;

    public Store() {}

    public void setData(final BufferedReader socketListener, final BufferedWriter socketWriter, final Path uploadTarget, final ExecutorService service, final String serverAddr) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.uploadTarget = uploadTarget;
        this.dataService = service;
        this.serverAddr = serverAddr;
    }

    @Override
    public void run() {
        String[] parsedResponse;
        String[] addr;

        malformedData = false;

        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(uploadTarget));) {
            socketWriter.write("PASV\r\n");
            socketWriter.flush();

            forwardControlResponse("PASV");

            final String pasvResponse = socketListener.readLine();

            forwardControlResponse(pasvResponse);

            parsedResponse = parseResponse(pasvResponse);

            switch (parsedResponse[0].charAt(0)) {
                case '2':
                    addr = parsePasvResponse(parsedResponse[1]);
                    break;

                default:
                    checkResult(false);
                    return;
            }

            // set transfer mode to IMAGE (keep the file as-is during transfer)
            socketWriter.write("TYPE I\r\n");
            socketWriter.flush();

            forwardControlResponse("TYPE I");

            final String typeResponse = socketListener.readLine();

            forwardControlResponse(typeResponse);

            parsedResponse = parseResponse(typeResponse);

            switch (parsedResponse[0].charAt(0)) {
                case '2':
                    break;

                default:
                    checkResult(false);
                    return;
            }

            // prepare the listener before sending the command.
            dataService.submit(new Runnable() {
                @Override
                public void run() {
                    try (Socket dataSocket = new Socket(serverAddr,
                            Integer.parseInt(addr[4]) * 256 + Integer.parseInt(addr[5]));) {

                        final BufferedOutputStream out = new BufferedOutputStream(dataSocket.getOutputStream());

                        // 8kB buffer. Has to do this to keep track of read bytes, transferTo would just
                        // block indefinitely?
                        final byte[] buffer = new byte[8192];

                        while (true)  {
                            if (in.read(buffer) == -1) break;

                            out.write(buffer);
                        }

                        dataSocket.close();

                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                checkResult(true);
                            }
                        });
                    } catch (final Exception e) {
                        // TODO: handle exception
                        Gdx.app.error("Exception", e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            final String storCommand = String.format("STOR %s\r\n", uploadTarget.getFileName());
            socketWriter.write(storCommand);
            socketWriter.flush();

            forwardControlResponse(storCommand);

            while (true) {
                final String storResponse = socketListener.readLine();

                forwardControlResponse(storResponse);

                parsedResponse = parseResponse(storResponse);

                switch (parsedResponse[0].charAt(0)) {
                    case '2':
                        return;

                    case '1':
                        break;

                    default:
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

    private void checkResult(final boolean status) {
        if (malformedData)
            finish(false);
        else
            finish(status);
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
