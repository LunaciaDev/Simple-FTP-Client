package com.lunaciadev.SimpleFTPClient.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lunaciadev.SimpleFTPClient.core.commands.Account;
import com.lunaciadev.SimpleFTPClient.core.commands.Authenticate;
import com.lunaciadev.SimpleFTPClient.core.commands.Connect;
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
    public Signal connectCompleted = new Signal();

    /**
     * Signal sent when {@link FTP#authenticate(String, String)} finished.
     * 
     * @param result {@link Boolean} {@code True} if the authentication is successful. If {@code False}, check the next field.
     * @param accountNeeded {@link Boolean} if {@code True}, need account to finish authentication. Otherwise, the username/password was rejected.
     */
    public Signal authenticateCompleted = new Signal();

    /**
     * Signal sent when {@link FTP#sendAccount(String)} finished.
     * 
     * @param result {@link Boolean} {@code True} if the authentication is successful, {@code False} otherwise.
     */
    public Signal sendAccountCompleted = new Signal();

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

        Connect task = new Connect(ftpServer, port);
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

    /**
     * Authenticate to the FTP Server. The UI should not call this if not connected.
     * 
     * @param username
     * @param password
     */
    public void authenticate(String username, String password) {
        Authenticate task = new Authenticate(socketListener, socketWriter, username, password);
        task.completed.connect(this::authenticateCallback);
        service.submit(task);
    }

    private void authenticateCallback(Object... args) {
        authenticateCompleted.emit(args);
    }

    /**
     * Send account information to the FTP Server. Only needed if {@link FTP#authenticate(String, String)} requires.
     * 
     * @param account
     */
    public void sendAccount(String account) {
        Account task = new Account(socketListener, socketWriter, account);
        task.completed.connect(this::sentAccountCallback);
        service.submit(task);
    }

    private void sentAccountCallback(Object... args) {

    }
}