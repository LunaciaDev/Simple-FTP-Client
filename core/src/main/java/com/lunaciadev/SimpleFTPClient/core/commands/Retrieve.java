package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;

import com.badlogic.gdx.Gdx;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

public class Retrieve extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;
    private ExecutorService dataService;
    private volatile boolean allDataReceived = false;
    private boolean malformedData = false;

    private String fileName;
    private String localCWD;

    public Signal completed = new Signal();

    public Retrieve(BufferedReader socketListener, BufferedWriter socketWriter, String fileName, String localCWD) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.fileName = fileName;
        this.localCWD = localCWD;
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

        Path downloadTarget = Path.of(localCWD + fileName + ".tmp");
        String[] response;
        String[] addr;

        try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(downloadTarget,
                StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));) {

            socketWriter.write("PASV\r\n");
            socketWriter.flush();
            response = parseResponse(socketListener.readLine());

            switch (response[0].charAt(0)) {
                case '2':
                    addr = parsePasvResponse(response[1]);
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

                        BufferedInputStream in = new BufferedInputStream(dataSocket.getInputStream());

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
                    }
                }
            });

            socketWriter.write(String.format("RETR %s\r\n", fileName));
            socketWriter.flush();
            while (true) {
                response = parseResponse(socketListener.readLine());

                switch (response[0].charAt(0)) {
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
