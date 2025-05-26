package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedChannelException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;

import com.badlogic.gdx.Gdx;

/**
 * TODO fix this thing
 */
public class Retrieve extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;
    private ExecutorService dataService;
    private boolean malformedData = false;
    private Path downloadFolder;

    private String fileName;

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
            String[] parsedResponse;
            String[] addr;
            Path filePath = downloadFolder.resolve(fileName);
            malformedData = false;

            socketWriter.write("PASV\r\n");
            socketWriter.flush();

            forwardControlResponse("PASV");

            parsedResponse = listenForResponse(socketListener);

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
                    final InetSocketAddress socketAddress = new InetSocketAddress(
                            String.join(".", addr[0], addr[1], addr[2], addr[3]),
                            Integer.parseInt(addr[4]) * 256 + Integer.parseInt(addr[5]));
                    int retryCount = 0;
                    while (retryCount <= 2) {
                        try {
                            final Socket dataSocket = new Socket();

                            dataSocket.connect(socketAddress, 10 * 1000); // 10s Socket Establish Timeout

                            final BufferedInputStream in = new BufferedInputStream(dataSocket.getInputStream());

                            // create a new file, truncate if exists.
                            Files.write(filePath, new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                            // 8kB buffer. Has to do this to keep track of read bytes, transferTo would just
                            // block indefinitely?
                            final byte[] buffer = new byte[8192];
                            int byteRead = 0;

                            try {
                                while ((byteRead = in.read(buffer)) != -1) {
                                    Files.write(filePath, buffer, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
                                }
                            } catch (ClosedChannelException e) {
                                Gdx.app.error("Exception", Integer.toString(byteRead));
                                e.printStackTrace();
                            }

                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    checkResult(true);
                                }
                            });

                            dataSocket.close();
                            return;
                        } catch (final SocketTimeoutException e) {
                            if (retryCount < 2) {
                                Gdx.app.postRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        forwardControlResponse("[APP] PASV connection timed out. Retrying...");
                                    }
                                });
                                retryCount++;
                                continue;
                            }

                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    forwardControlResponse(
                                            "[APP] Failed to establish PASV connection. App will stay frozen until a timeout is received from server.");
                                }
                            });
                            return;
                        } catch (final Exception e) {
                            // TODO: handle exception
                            Gdx.app.error("Exception", e.getMessage());
                            e.printStackTrace();
                            retryCount++;
                            continue;
                        }
                    }
                }
            });

            final String retrieveCmd = String.format("RETR %s\r\n", fileName);
            socketWriter.write(retrieveCmd);
            socketWriter.flush();

            forwardControlResponse(retrieveCmd);

            while (true) {
                parsedResponse = listenForResponse(socketListener);

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
