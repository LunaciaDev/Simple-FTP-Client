package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
    private boolean malformedData = false;

    private Path uploadTarget;

    public Store() {
    }

    public void setData(final BufferedReader socketListener, final BufferedWriter socketWriter, final Path uploadTarget,
            final ExecutorService service) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.uploadTarget = uploadTarget;
        this.dataService = service;
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

            parsedResponse = listenForResponse(socketListener);

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

                            final BufferedOutputStream out = new BufferedOutputStream(dataSocket.getOutputStream());

                            // 8kB buffer. Has to do this to keep track of read bytes, transferTo would just
                            // block indefinitely?
                            final byte[] buffer = new byte[8192];
                            int byteRead;

                            while ((byteRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, byteRead);
                                out.flush();
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

            final String storCommand = String.format("STOR %s\r\n", uploadTarget.getFileName());
            socketWriter.write(storCommand);
            socketWriter.flush();

            forwardControlResponse(storCommand);

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
