package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import com.badlogic.gdx.Gdx;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

public class List extends Command implements Runnable {
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;
    private ExecutorService dataService;
    private volatile boolean allDataReceived = false;
    private boolean malformedData = false;

    /**
     * @param status {@link Boolean} {@code True} if command finished successfully, {@code False} otherwise.
     * @param listing {@link String} the list result.
     */
    public Signal completed = new Signal();

    private Signal dataStruct = new Signal();

    public List(BufferedReader socketListener, BufferedWriter socketWriter, ExecutorService dataService) {
        this.socketListener = socketListener;
        this.socketWriter = socketWriter;
        this.dataService = dataService;
        dataStruct.connect(this::checkResult);
    }

    @Override
    public void run() {
        String[] response;
        String[] addr;

        try {
            socketWriter.write("PASV\r\n");
            socketWriter.flush();
            response = handleResponse(socketListener.readLine());

            switch (response[0].charAt(0)) {
                case '2':
                    addr = parsePasvResponse(response[1]);
                    break;
            
                default:
                    finish(false, null);
                    return;
            }

            dataService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket dataSocket = new Socket(String.join(".", addr[0], addr[1], addr[2], addr[3]), Integer.parseInt(addr[4]) << 8 + Integer.parseInt(addr[5]));
                        BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String temp;

                        while (!allDataReceived) {
                            while ((temp = dataReader.readLine()) != null) {
                                response.append(temp);
                            }
                        }

                        final String result = response.toString();

                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                dataStruct.emit(result);
                            }
                        });

                        dataSocket.close();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            });

            socketWriter.write("LIST\r\n");
            socketWriter.flush();
            while (true) {
                response = handleResponse(socketListener.readLine());

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

    private void checkResult(Object... args) {
        if (malformedData) finish(false, null);
        else finish(true, (String) args[0]);
    } 

    private void finish(boolean status, String result) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                completed.emit(status, result);
            }
            
        });
    }
}
