package com.lunaciadev.SimpleFTPClient.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lunaciadev.SimpleFTPClient.core.commands.FTPConnect;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

/**
 * Implement the FTP Client-side, providing function call instead of command composition.
 * 
 * Each command execution is pushed into a worker thread.
 */

public class FTP {
    private ExecutorService service;
    
    private Socket controlSocket;
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;

    /**
     * Signal sent when {@link FTP#connect(String, Integer)} finished.
     * 
     * @param result {@link Boolean} {@code True} if the connection is successful, {@code False} otherwise
     * @param message {@link String} The error message, if any.
     */
    private Signal connectCompleted = new Signal();

    public FTP() {
        service = Executors.newSingleThreadExecutor();
    }

    /**
     * Connect to the specified ftpServer's control port.
     * 
     * @param ftpServer Can be an IPv4 or it's domain name.
     * @param port Specify the control port. Pass null to use the default port 21.
     */
    public void connect(String ftpServer, Integer port) {
        if (port == null) {
            port = 21;
        }

        FTPConnect task = new FTPConnect(ftpServer, port);
        task.completed.connect(this::connectCallback);

        service.submit(task);
    }

    private void connectCallback(Object... args) {
        if (!(boolean) args[0]) {
            connectCompleted.emit(false, args[4]);
            return;
        }

        controlSocket = (Socket) args[1];
        socketListener = (BufferedReader) args[2];
        socketWriter = (BufferedWriter) args[3];

        connectCompleted.emit(true, args[4]);
    }
}