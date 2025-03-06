package com.lunaciadev.SimpleFTPClient.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.nio.file.Path;
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

public class FTPClient {
    private ExecutorService controlService;
    private ExecutorService dataService;
    
    private Socket controlSocket;
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;

    /****** SIGNALS *****/

    /**
     * Signal sent when {@link FTPClient#connect(String, Integer)} finished.
     * 
     * @param result {@link Boolean} {@code True} if the connection is successful, {@code False} otherwise
     * @param message {@link String} The error message, if any.
     */
    public Signal connectCompleted = new Signal();

    /**
     * Signal sent when {@link FTPClient#login(String, String)} finished.
     * 
     * @param result {@link Boolean} {@code True} if the authentication is successful. If {@code False}, check the next field.
     * @param accountNeeded {@link Boolean} if {@code True}, need account to finish authentication. Otherwise, the username/password was rejected.
     */
    public Signal loginCompleted = new Signal();

    /**
     * Signal sent when {@link FTPClient#sendAccount(String)} finished.
     * 
     * @param result {@link Boolean} {@code True} if the authentication is successful, {@code False} otherwise.
     */
    public Signal sendAccountCompleted = new Signal();

    /**
     * Signal sent when {@link FTPClient#quit()} finished.
     * 
     * @param result {@link Boolean} {@code True} if the command is successful, {@code False} otherwise.
     */
    public Signal quitCompleted = new Signal();

    /**
     * Signal sent when {@link FTPClient#list()} finished
     * 
     * @param status {@link Boolean} {@code True} if the command is successful, {@code False} otherwise.
     * @param payload {@link String} the result.
     */
    public Signal listCompleted = new Signal();

    /**
     * Signal sent when {@link FTPClient#retrieve(String, String)} finished.
     * 
     * @param result {@link Boolean} {@code True} if the command is successful, {@code False} otherwise.
     */
    public Signal retrieveCompleted = new Signal();

    /**
     * Signal sent when {@link FTPClient#store(String, String)} finished.
     * 
     * @param result {@link Boolean} {@code True} if the command is successful, {@code False} otherwise.
     */
    public Signal storeCompleted = new Signal();

    /**
     * Emitted when any command receive an FTP Control Response.
     * 
     * @param response {@link String} The response.
     */
    public Signal ftpControlResponse = new Signal();

    public Signal ftpPartialTransfer = new Signal();

    /****** END SIGNAL REGION ******/

    public FTPClient() {
        controlService = Executors.newSingleThreadExecutor();
        dataService = Executors.newSingleThreadExecutor();
    }

    /**
     * Connect to the specified ftpServer's control port.
     */
    public void connect(Object... args) {
        String ftpServer = (String) args[0];
        int port = (int) args[1];

        Connect task = new Connect(ftpServer, port);
        task.completed.connect(this::onConnectCompleted);
        task.ftpControlReceived.connect(this::onFTPControlReceived);
        controlService.submit(task);
    }

    private void onConnectCompleted(Object... args) {
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
    public void login(Object... args) {
        String username = (String) args[0];
        String password = (String) args[1];

        Login task = new Login(socketListener, socketWriter, username, password);
        task.completed.connect(this::onLoginCompleted);
        task.ftpControlReceived.connect(this::onFTPControlReceived);
        controlService.submit(task);
    }

    private void onLoginCompleted(Object... args) {
        loginCompleted.emit(args);
    }

    /**
     * Send account information to the FTP Server. Only needed if {@link FTPClient#login(String, String)} requires.
     * 
     * @param account
     */
    public void sendAccount(String account) {
        Account task = new Account(socketListener, socketWriter, account);
        task.completed.connect(this::onSendAccountCompleted);
        task.ftpControlReceived.connect(this::onFTPControlReceived);
        controlService.submit(task);
    }

    private void onSendAccountCompleted(Object... args) {
        sendAccountCompleted.emit(args);
    }

    /**
     * Log out from the FTP Server.
     */
    public void quit(Object... args) {
        Quit task = new Quit(socketListener, socketWriter);
        task.completed.connect(this::onQuitCompleted);
        task.ftpControlReceived.connect(this::onFTPControlReceived);
        controlService.submit(task);
    }

    private void onQuitCompleted(Object... args) {
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
    public void list(Object... args) {
        List task = new List(socketListener, socketWriter, dataService);
        task.completed.connect(this::onListCompleted);
        task.ftpControlReceived.connect(this::onFTPControlReceived);
        controlService.submit(task);
    }

    private void onListCompleted(Object... args) {
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
        task.completed.connect(this::onRetrieveCompleted);
        task.ftpControlReceived.connect(this::onFTPControlReceived);
        controlService.submit(task);
    }

    private void onRetrieveCompleted(Object... args) {
        retrieveCompleted.emit(args);
    }

    /**
     * Slot, connected to ...
     * 
     * One argument, a string representing the absolute path to the file.
     */
    public void store(Object... args) {
        Store task = new Store(socketListener, socketWriter, (Path) args[0], dataService);
        task.completed.connect(this::onStoreCompleted);
        task.ftpControlReceived.connect(this::onFTPControlReceived);
        task.partialTransferred.connect(this::partialTransferred);
        controlService.submit(task);
    }

    private void onStoreCompleted(Object... args) {
        storeCompleted.emit(args);
    }

    private void onFTPControlReceived(Object... args) {
        ftpControlResponse.emit(args);
    }

    private void partialTransferred(Object... args) {
        ftpPartialTransfer.emit(args);
    }

    public void dispose() {
        controlService.shutdown();
        dataService.shutdown();
    }
}