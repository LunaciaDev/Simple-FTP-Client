package com.lunaciadev.SimpleFTPClient.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.Gdx;
import com.lunaciadev.SimpleFTPClient.core.commands.Account;
import com.lunaciadev.SimpleFTPClient.core.commands.Login;
import com.lunaciadev.SimpleFTPClient.core.commands.Connect;
import com.lunaciadev.SimpleFTPClient.core.commands.List;
import com.lunaciadev.SimpleFTPClient.core.commands.Quit;
import com.lunaciadev.SimpleFTPClient.core.commands.Retrieve;
import com.lunaciadev.SimpleFTPClient.core.commands.Store;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

/**
 * Implement the FTP Client-side, providing function call instead of command composition.
 * 
 * Each command execution is pushed into a worker thread.
 */

public class FTP {
    private ExecutorService controlService;
    private ExecutorService dataService;
    
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
     * Signal sent when {@link FTP#login(String, String)} finished.
     * 
     * @param result {@link Boolean} {@code True} if the authentication is successful. If {@code False}, check the next field.
     * @param accountNeeded {@link Boolean} if {@code True}, need account to finish authentication. Otherwise, the username/password was rejected.
     */
    public Signal loginCompleted = new Signal();

    /**
     * Signal sent when {@link FTP#sendAccount(String)} finished.
     * 
     * @param result {@link Boolean} {@code True} if the authentication is successful, {@code False} otherwise.
     */
    public Signal sendAccountCompleted = new Signal();

    /**
     * Signal sent when {@link FTP#quit()} finished.
     * 
     * @param result {@link Boolean} {@code True} if the command is successful, {@code False} otherwise.
     */
    public Signal quitCompleted = new Signal();

    /**
     * Signal sent when {@link FTP#list()} finished
     * 
     * @param status {@link Boolean} {@code True} if the command is successful, {@code False} otherwise.
     * @param payload {@link String} the result.
     */
    public Signal listCompleted = new Signal();

    /**
     * Signal sent when {@link FTP#retrieve(String, String)} finished.
     * 
     * @param result {@link Boolean} {@code True} if the command is successful, {@code False} otherwise.
     */
    public Signal retrieveCompleted = new Signal();

    /**
     * Signal sent when {@link FTP#store(String, String)} finished.
     * 
     * @param result {@link Boolean} {@code True} if the command is successful, {@code False} otherwise.
     */
    public Signal storeCompleted = new Signal();

    public FTP() {
        controlService = Executors.newSingleThreadExecutor();
        dataService = Executors.newSingleThreadExecutor();
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

        controlService.submit(task);
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
    public void login(String username, String password) {
        Login task = new Login(socketListener, socketWriter, username, password);
        task.completed.connect(this::authenticateCallback);
        controlService.submit(task);
    }

    private void authenticateCallback(Object... args) {
        loginCompleted.emit(args);
    }

    /**
     * Send account information to the FTP Server. Only needed if {@link FTP#login(String, String)} requires.
     * 
     * @param account
     */
    public void sendAccount(String account) {
        Account task = new Account(socketListener, socketWriter, account);
        task.completed.connect(this::sentAccountCallback);
        controlService.submit(task);
    }

    private void sentAccountCallback(Object... args) {
        sendAccountCompleted.emit(args);
    }

    /**
     * Log out from the FTP Server.
     */
    public void quit() {
        Quit task = new Quit(socketListener, socketWriter);
        task.completed.connect(this::quitCallback);
        controlService.submit(task);
    }

    private void quitCallback(Object... args) {
        if (!(boolean) args[0]) {
            quitCompleted.emit(false);
            return;
        }

        try {
            controlSocket.close();
        } catch (Exception e) {
            // TODO: handle exception
            Gdx.app.error("Exception", e.getMessage());
            e.printStackTrace();
        }

        quitCompleted.emit(true);
    }

    /**
     * Since this is baked list command for the ui, we can just simply call LIST without any argument as we only have to show the current directory.
     */
    public void list() {
        List task = new List(socketListener, socketWriter, dataService);
        task.completed.connect(this::listCallback);
        controlService.submit(task);
    }

    private void listCallback(Object... args) {
        listCompleted.emit(args);
    }
    
    /**
     * Download a file from the FTP Server
     * 
     * @param fileName The name of the file.
     * @param localCWD The current working directory of the client.
     */
    public void retrieve(String fileName, String localCWD) {
        Retrieve task = new Retrieve(socketListener, socketWriter, fileName, localCWD, dataService);
        task.completed.connect(this::retrieveCallback);
        controlService.submit(task);
    }

    private void retrieveCallback(Object... args) {
        retrieveCompleted.emit(args);
    }

    /**
     * Upload the file to the FTP Server
     * 
     * @param fileName The name of the file
     * @param localCWD The current working directory of the client
     */
    public void store(String fileName, String localCWD) {
        Store task = new Store(socketListener, socketWriter, fileName, localCWD, dataService);
        task.completed.connect(this::storeCallback);
        controlService.submit(task);
    }

    public void storeCallback(Object... args) {
        storeCompleted.emit(args);
    }
}