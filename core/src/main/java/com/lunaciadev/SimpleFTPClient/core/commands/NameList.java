package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Queue;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

public class NameList extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;
    private ExecutorService dataService;
    private volatile boolean allDataReceived = false;
    private boolean malformedData = false;

    private final Signal dataStruct = new Signal();

    public NameList() {
        dataStruct.connect(this::checkResult);
    }

    public void setData(final BufferedReader socketListener, final BufferedWriter socketWriter,
            final ExecutorService dataService) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.dataService = dataService;
    }

    @Override
    public void run() {
        try {
            String[] parsedResponse;
            String[] addr;
            allDataReceived = false;

            socketWriter.write("PASV\r\n");
            socketWriter.flush();
            forwardControlResponse("PASV\n");

            parsedResponse = listenForResponse(socketListener);

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
                    final InetSocketAddress socketAddress = new InetSocketAddress(
                            String.join(".", addr[0], addr[1], addr[2], addr[3]),
                            Integer.parseInt(addr[4]) * 256 + Integer.parseInt(addr[5]));
                    int retryCount = 0;
                    while (retryCount <= 2) {
                        try {
                            final Socket dataSocket = new Socket();

                            dataSocket.connect(socketAddress, 10 * 1000); // 10s Socket Establish Timeout

                            final BufferedReader dataReader = new BufferedReader(
                                    new InputStreamReader(dataSocket.getInputStream()));
                            final Queue<String> response = new Queue<>();
                            String temp;

                            while (!allDataReceived) {
                                while ((temp = dataReader.readLine()) != null) {
                                    response.addLast(temp);
                                }
                            }

                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    dataStruct.emit(response);
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

            socketWriter.write("NLST\r\n");
            socketWriter.flush();
            forwardControlResponse("NLST");

            while (true) {
                parsedResponse = listenForResponse(socketListener);

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

    @SuppressWarnings("unchecked")
    private void checkResult(final Object... args) {
        if (malformedData)
            finish(false, null);
        else
            finish(true, (Queue<String>) args[0]);
    }

    private void finish(final boolean status, final Queue<String> result) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                completed.emit(status, result);
            }

        });
    }

}
