package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;

import com.badlogic.gdx.Gdx;

/**
 * TODO fix this thing
 */
public class Retrieve extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;
    private ExecutorService dataService;
    private volatile boolean allDataReceived = false;
    private boolean malformedData = false;
    private Path downloadTarget;

    private String fileName;
    private String localCWD;

    public Retrieve() {}

    public void setData(final BufferedReader socketListener, final BufferedWriter socketWriter, final String fileName, final String localCWD, final ExecutorService service) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.fileName = fileName;
        this.localCWD = localCWD;
        this.dataService = service;
    }

    @Override
    public void run() {
        /*
         * TODO Allow resume of download
         * 
         * RETR can be preceeded by REST <byte downloaded>\r\n, which will allow
         * appending and resuming of download.
         * 
         * Current implementation download the file from scratch.
         */

        downloadTarget = Path.of(localCWD + fileName + ".tmp");
        String[] parsedResponse;
        String[] addr;

        try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(downloadTarget));) {
            socketWriter.write("PASV\r\n");
            socketWriter.flush();

            forwardControlResponse("PASV\r\n");

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

            forwardControlResponse("TYPE I\r\n");

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
                    try (Socket dataSocket = new Socket(String.join(".", addr[0], addr[1], addr[2], addr[3]),
                            Integer.parseInt(addr[4]) * 256 + Integer.parseInt(addr[5]));) {

                        final BufferedInputStream in = new BufferedInputStream(dataSocket.getInputStream());

                        // 8kB buffer. Has to do this to keep track of read bytes, transferTo would just
                        // block indefinitely?
                        final byte[] buffer = new byte[8192];

                        while (!allDataReceived) {
                            while ((in.read(buffer)) != -1) {
                                out.write(buffer);
                            }
                        }

                        final Path finalFile = Path.of(localCWD, fileName);
                        Files.move(downloadTarget, finalFile, StandardCopyOption.REPLACE_EXISTING);

                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                checkResult(true);
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

            final String retrieveCmd = String.format("RETR %s\r\n", fileName);
            socketWriter.write(retrieveCmd);
            socketWriter.flush();

            forwardControlResponse(retrieveCmd);

            while (true) {
                final String retrResponse = socketListener.readLine();
                forwardControlResponse(retrResponse);

                parsedResponse = parseResponse(retrResponse);

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

    private void checkResult(final boolean status) {
        if (malformedData) {
            try {
                Files.delete(downloadTarget);
            }
            catch (final Exception e) {
                // TODO: handle exception
                Gdx.app.error("Exception", e.getMessage());
                e.printStackTrace();
            }

            finish(false);
        }
        else {
            finish(status);
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
