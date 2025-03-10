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
import com.lunaciadev.SimpleFTPClient.utils.Signal;

/**
 * TODO fix this thing
 */
public class Retrieve extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;
    private ExecutorService dataService;
    private volatile boolean allDataReceived = false;
    private boolean malformedData = false;
    private boolean hasSize = false;
    private Path downloadFolder;

    private String fileName;

    public Signal setProgressBar = new Signal();
    public Signal partialTransferred = new Signal();

    public Retrieve() {
    }

    public void setData(final BufferedReader socketListener, final BufferedWriter socketWriter, final String fileName,
            final Path downloadFolder, final ExecutorService service) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.fileName = fileName;
        this.downloadFolder = downloadFolder;
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

        try {
            Files.createFile(downloadFolder.resolve(fileName));
        } catch (Exception e) {

        }

        try (BufferedOutputStream out = new BufferedOutputStream(
                Files.newOutputStream(downloadFolder.resolve(fileName)));) {
            String[] parsedResponse;
            String[] addr;
            allDataReceived = false;
            malformedData = false;
            hasSize = false;

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

            // Check if we can get the fileSize.
            String sizeCommand = String.format("SIZE %s\r\n", fileName);

            socketWriter.write(sizeCommand);
            socketWriter.flush();

            forwardControlResponse(sizeCommand);

            final String sizeResponse = socketListener.readLine();

            forwardControlResponse(typeResponse);

            parsedResponse = parseResponse(typeResponse);

            switch (parsedResponse[0].charAt(0)) {
                case '2':
                    setProgressBar.emit(Integer.parseInt(sizeResponse));
                    hasSize = true;
                    break;

                default:
                    break;
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
                        int temp;

                        while (!allDataReceived) {
                            while ((temp = in.read(buffer)) != -1) {
                                // a bit of roundabout since cross thread communication must be finals to be
                                // thread-safe in a loop.
                                final int bytesReceived = temp;
                                out.write(buffer);

                                if (hasSize) {
                                    Gdx.app.postRunnable(new Runnable() {
                                        @Override
                                        public void run() {
                                            partialTransferred.emit(bytesReceived);
                                        }
                                    });
                                }
                            }
                        }

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
                Files.delete(downloadFolder);
            } catch (final Exception e) {
                // TODO: handle exception
                Gdx.app.error("Exception", e.getMessage());
                e.printStackTrace();
            }

            finish(false);
        } else {
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
