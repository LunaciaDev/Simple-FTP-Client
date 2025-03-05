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

public class Store extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;
    private ExecutorService dataService;
    private volatile boolean allDataReceived = false;
    private boolean malformedData = false;

    private String fileName;
    private String localCWD;

    public Signal completed = new Signal();

    public Store(BufferedReader socketListener, BufferedWriter socketWriter, String fileName, String localCWD, ExecutorService service) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.fileName = fileName;
        this.localCWD = localCWD;
        this.dataService = service;
    }

    @Override
    public void run() {
        Path uploadTarget = Path.of(localCWD + fileName);
        String[] parsedResponse;
        String[] addr;

        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(uploadTarget));) {
            socketWriter.write("PASV\r\n");
            socketWriter.flush();

            final String pasvResponse = socketListener.readLine();

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    ftpControlReceived.emit(pasvResponse);
                }
            });

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

            final String typeResponse = socketListener.readLine();

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    ftpControlReceived.emit(typeResponse);
                }
            });

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

                        BufferedOutputStream out = new BufferedOutputStream(dataSocket.getOutputStream());

                        // 8kB buffer. Has to do this to keep track of read bytes, transferTo would just
                        // block indefinitely?
                        byte[] buffer = new byte[8192];

                        while (!allDataReceived) {
                            while ((in.read(buffer)) != -1) {
                                out.write(buffer);
                            }
                        }

                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                checkResult(true);
                            }
                        });

                        dataSocket.close();
                    } catch (Exception e) {
                        // TODO: handle exception
                        Gdx.app.error("Exception", e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            socketWriter.write(String.format("STOR %s\r\n", fileName));
            socketWriter.flush();
            while (true) {
                final String storResponse = socketListener.readLine();

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        ftpControlReceived.emit(storResponse);
                    }
                });

                parsedResponse = parseResponse(storResponse);

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
        } catch (Exception e) {
            // TODO: handle exception
            Gdx.app.error("Exception", e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkResult(boolean status) {
        if (malformedData)
            finish(false);
        else
            finish(status);
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
