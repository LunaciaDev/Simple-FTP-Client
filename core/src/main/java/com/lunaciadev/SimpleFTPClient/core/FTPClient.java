package com.lunaciadev.SimpleFTPClient.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Queue;

import com.lunaciadev.SimpleFTPClient.core.commands.Account;
import com.lunaciadev.SimpleFTPClient.core.commands.ChangeDirectory;
import com.lunaciadev.SimpleFTPClient.core.commands.ChangeParentDir;
import com.lunaciadev.SimpleFTPClient.core.commands.Login;
import com.lunaciadev.SimpleFTPClient.core.commands.NameList;
import com.lunaciadev.SimpleFTPClient.core.commands.Connect;
import com.lunaciadev.SimpleFTPClient.core.commands.CurrentDirectory;
import com.lunaciadev.SimpleFTPClient.core.commands.List;
import com.lunaciadev.SimpleFTPClient.core.commands.Quit;
import com.lunaciadev.SimpleFTPClient.core.commands.Retrieve;
import com.lunaciadev.SimpleFTPClient.core.commands.Store;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

/**
 * Implement the FTP Client-side, providing function call instead of command
 * composition.
 * 
 * Each command execution is pushed into a worker thread.
 * 
 * @author LunaciaDev
 */
public class FTPClient {
    private final ExecutorService controlService;
    private final ExecutorService dataService;

    private Socket controlSocket;
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;
    private String serverAddr;

    /****** SIGNALS *****/

    /**
     * Signal sent when {@link FTPClient#connect(String, Integer)} finished.
     * 
     * @param status        {@link Boolean} {@code True} if the connection is
     *                      successful,
     *                      {@code False} otherwise
     * @param controlSocket {@link Socket} Will be NULL if status is {@code False}.
     * @param socketReader  {@link BufferedReader} Will be NULL if status is
     *                      {@code False}.
     * @param socketWriter  {@link BufferedWriter} Will be NULL if status is
     *                      {@code False}.
     * @param message       {@link String} The error message, if any.
     */
    public Signal connectCompleted;

    /**
     * Signal sent when {@link FTPClient#login} finished.
     * 
     * @param status        {@link Boolean} {@code True} if the authentication is
     *                      successful. If {@code False}, check the next field.
     * @param accountNeeded {@link Boolean} if {@code True}, need account to finish
     *                      authentication. Otherwise, the username/password was
     *                      rejected.
     */
    public Signal loginCompleted;

    /**
     * Signal sent when {@link FTPClient#sendAccount} finished.
     * 
     * @param status {@link Boolean} {@code True} if the authentication is
     *               successful, {@code False} otherwise.
     */
    public Signal accountCompleted;

    /**
     * Signal sent when {@link FTPClient#quit} finished.
     * 
     * @param status {@link Boolean} {@code True} if the command is successful,
     *               {@code False} otherwise.
     */
    public Signal quitCompleted;

    /**
     * Signal sent when {@link FTPClient#list} finished
     * 
     * @param status  {@link Boolean} {@code True} if the command is successful,
     *                {@code False} otherwise.
     * @param payload {@link String} the result. Will be NULL if status is
     *                {@code False}
     */
    public Signal listCompleted;

    /**
     * Signal sent when {@link FTPClient#retrieve} finished.
     * 
     * @param status {@link Boolean} {@code True} if the command is successful,
     *               {@code False} otherwise.
     */
    public Signal retrieveCompleted;

    /**
     * Signal sent when {@link FTPClient#store} finished.
     * 
     * @param status {@link Boolean} {@code True} if the command is successful,
     *               {@code False} otherwise.
     */
    public Signal storeCompleted;

    /**
     * Signal sent when {@link FTPClient#nameList} finished.
     * 
     * @param status   {@link Boolean} {@code True} if the command is successful,
     *                 {@code False} otherwise.
     * @param nameList {@link Queue} A queue of all file/folder name in current
     *                 directory, will be NULL if status is {@code False}
     */
    public Signal nameListCompleted;

    /**
     * Signal sent when {@link FTPClient#currentDirectory} finished.
     * 
     * @param status    {@link Boolean} {@code True} if the command is successful,
     *                  {@code False} otherwise.
     * @param dirString {@link String} The string representing the absolute path to
     *                  the current directory.
     */
    public Signal currentDirectoryCompleted;

    /**
     * Signal sent when {@link FTPClient#changeDirectory} finished.
     * 
     * @param status   {@link Boolean} {@code True} if the command is successful,
     *                 {@code False} otherwise.
     * @param response {@link String} The response from the server.
     */
    public Signal changeDirectoryCompleted;

    /**
     * Emitted when any command receive an FTP Control Response.
     * 
     * @param response {@link String} The response.
     */
    public Signal ftpControlResponse = new Signal();

    /****** END SIGNAL REGION ******/

    /****** COMMANDS ******/

    private final Account accountCommand;
    private final Connect connectCommand;
    private final List listCommand;
    private final Login loginCommand;
    private final Quit quitCommand;
    private final Retrieve retrieveCommand;
    private final Store storeCommand;
    private final NameList nameListCommand;
    private CurrentDirectory currentDirectoryCommand;
    private ChangeDirectory changeDirectoryCommand;
    private ChangeParentDir changeParentDirCommand;

    /****** END COMMANDS REGION ******/

    public FTPClient() {
        controlService = Executors.newSingleThreadExecutor();
        dataService = Executors.newSingleThreadExecutor();

        // Initialize all commands
        accountCommand = new Account();
        connectCommand = new Connect();
        listCommand = new List();
        loginCommand = new Login();
        quitCommand = new Quit();
        retrieveCommand = new Retrieve();
        storeCommand = new Store();
        nameListCommand = new NameList();
        currentDirectoryCommand = new CurrentDirectory();
        changeDirectoryCommand = new ChangeDirectory();
        changeParentDirCommand = new ChangeParentDir();

        // Exposing completed signals
        accountCompleted = accountCommand.completed;
        connectCompleted = connectCommand.completed;
        listCompleted = listCommand.completed;
        loginCompleted = loginCommand.completed;
        quitCompleted = quitCommand.completed;
        retrieveCompleted = retrieveCommand.completed;
        storeCompleted = storeCommand.completed;
        nameListCompleted = nameListCommand.completed;
        currentDirectoryCompleted = currentDirectoryCommand.completed;
        changeDirectoryCompleted = changeDirectoryCommand.completed;
        // these 2 are similar, so they should share the same signal. Less wiring!
        changeParentDirCommand.completed = changeDirectoryCompleted;

        // Connect aggregate signals
        accountCommand.ftpControlReceived.connect(this::onFTPControlReceived);
        connectCommand.ftpControlReceived.connect(this::onFTPControlReceived);
        listCommand.ftpControlReceived.connect(this::onFTPControlReceived);
        loginCommand.ftpControlReceived.connect(this::onFTPControlReceived);
        quitCommand.ftpControlReceived.connect(this::onFTPControlReceived);
        retrieveCommand.ftpControlReceived.connect(this::onFTPControlReceived);
        storeCommand.ftpControlReceived.connect(this::onFTPControlReceived);
        nameListCommand.ftpControlReceived.connect(this::onFTPControlReceived);
        currentDirectoryCommand.ftpControlReceived.connect(this::onFTPControlReceived);
        changeDirectoryCommand.ftpControlReceived.connect(this::onFTPControlReceived);
        changeParentDirCommand.ftpControlReceived.connect(this::onFTPControlReceived);

        // Internal connections
        connectCompleted.connect(this::onConnectCompleted);
        quitCompleted.connect(this::onQuitCompleted);
    }

    /**
     * Connect to the specified ftpServer's control port.
     */
    public void connect(final Object... args) {
        final String ftpServer = (String) args[0];
        final int port = (int) args[1];
        this.serverAddr = ftpServer;

        connectCommand.setData(ftpServer, port);
        controlService.submit(connectCommand);
    }

    /**
     * Authenticate to the FTP Server. The UI should not call this if not connected.
     * 
     * @param username
     * @param password
     */
    public void login(final Object... args) {
        final String username = (String) args[0];
        final String password = (String) args[1];

        loginCommand.setData(socketListener, socketWriter, username, password);
        controlService.submit(loginCommand);
    }

    /**
     * Send account information to the FTP Server. Only needed if
     * {@link FTPClient#login(String, String)} requires.
     * 
     * @param account
     */
    public void sendAccount(final String account) {
        accountCommand.setData(socketListener, socketWriter, account);
        controlService.submit(accountCommand);
    }

    /**
     * Log out from the FTP Server.
     */
    public void quit(final Object... args) {
        quitCommand.setData(socketListener, socketWriter);
        controlService.submit(quitCommand);
    }

    /**
     * Since this is baked list command for the ui, we can just simply call LIST
     * without any argument as we only have to show the current directory.
     */
    public void list(final Object... args) {
        listCommand.setData(socketListener, socketWriter, dataService, serverAddr);
        controlService.submit(listCommand);
    }

    /**
     * Download a file from the FTP Server
     */
    public void retrieve(final Object... args) {
        String fileName = (String) args[0];
        Path downloadFolderPath = (Path) args[1];

        retrieveCommand.setData(socketListener, socketWriter, fileName, downloadFolderPath, dataService, serverAddr);
        controlService.submit(retrieveCommand);
    }

    /**
     * One argument, a string representing the absolute path to the file.
     */
    public void store(final Object... args) {
        storeCommand.setData(socketListener, socketWriter, (Path) args[0], dataService, serverAddr);
        controlService.submit(storeCommand);
    }

    public void nameList(final Object... args) {
        nameListCommand.setData(socketListener, socketWriter, dataService, serverAddr);
        controlService.submit(nameListCommand);
    }

    public void currentDirectory(Object... args) {
        currentDirectoryCommand.setData(socketListener, socketWriter);
        controlService.submit(currentDirectoryCommand);
    }

    public void changeDirectory(Object... args) {
        String dirName = (String) args[0];
        changeDirectoryCommand.setData(socketListener, socketWriter, dirName);
        controlService.submit(changeDirectoryCommand);
    }

    public void changeToParentDirectory(Object... args) {
        changeParentDirCommand.setData(socketListener, socketWriter);
        controlService.submit(changeParentDirCommand);
    }

    public void dispose() {
        controlService.shutdown();
        dataService.shutdown();

        if (controlSocket == null)
            return;

        if (!controlSocket.isClosed()) {
            try {
                controlSocket.close();
            } catch (Exception e) {
                Gdx.app.error("Exception", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void onConnectCompleted(final Object... args) {
        if (!(boolean) args[0])
            return;

        controlSocket = (Socket) args[1];
        socketListener = (BufferedReader) args[2];
        socketWriter = (BufferedWriter) args[3];
    }

    private void onQuitCompleted(final Object... args) {
        if (!(boolean) args[0])
            return;

        try {
            controlSocket.close();
        } catch (final Exception e) {
            // TODO: handle exception
            Gdx.app.error("Exception", e.getMessage());
            e.printStackTrace();
        }
    }

    private void onFTPControlReceived(final Object... args) {
        ftpControlResponse.emit(args);
    }
}