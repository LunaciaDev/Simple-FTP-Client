package com.lunaciadev.SimpleFTPClient.utils;

import com.lunaciadev.SimpleFTPClient.core.FTPClient;
import com.lunaciadev.SimpleFTPClient.ui.ConnectDialog;

/**
 * This utils class provide a procedural approach for connecting to a FTP
 * server.
 * 
 * @author LunaciaDev
 */
public class ConnectUtils {
    // TODO: Refactor to show an explicit state machine.
    public ConnectUtils() {
    }

    private String username;
    private String password;

    /****** SIGNALS ******/

    /**
     * Emitted at the first stage of connecting, to establish a TCP connection to
     * the server
     * 
     * @param address {@link String}
     */
    public Signal requestConnection = new Signal();

    /**
     * Emitted at the second stage of connection, after the server requested
     * authentication
     * 
     * @param username {@link String}
     * @param password {@link String}
     */
    public Signal requestLogin = new Signal();

    /**
     * Emitted after successfully connected to the server, automatically requesting
     * an LIST command to populate user's view.
     */
    public Signal requestRefresh = new Signal();

    /****** END SIGNALS ******/

    /**
     * Slot, connected to {@link ConnectDialog#loginButtonClicked}
     */
    public void startConnectProcess(final Object... args) {
        username = (String) args[2];
        password = (String) args[3];

        requestConnection.emit(args[0], Integer.parseInt((String) args[1]));
    }

    /**
     * Slot, connected to {@link FTPClient#connectCompleted}
     */
    public void startLoginProcess(final Object... args) {
        if (!(boolean) args[0])
            return;

        requestLogin.emit(username, password);
    }

    /**
     * Slot, connnected to {@link FTPClient#loginCompleted}
     */
    public void onLoginFinished(final Object... args) {
        if (!(boolean) args[0])
            return;

        requestRefresh.emit();
    }
}
