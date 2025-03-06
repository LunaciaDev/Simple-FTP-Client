package com.lunaciadev.SimpleFTPClient.utils;

public class ConnectUtils {
    public ConnectUtils() {}

    private String username;
    private String password;

    /****** SIGNALS ******/

    /**
     * Emitted at the first stage of connecting, to establish a TCP connection to the server
     * 
     * @param address {@link String}
     */
    public Signal requestConnection = new Signal();

    /**
     * Emitted at the second stage of connection
     * 
     * @param username {@link String}
     * @param password {@link String}
     */
    public Signal requestLogin = new Signal();

    /****** END SIGNALS ******/

    public void startConnectProcess(Object... args) {
        username = (String) args[2];
        password = (String) args[3];

        requestConnection.emit(args[0], Integer.parseInt((String) args[1]));
    }

    public void startLoginProcess(Object... args) {
        if (!(boolean) args[0]) return;

        requestLogin.emit(username, password);
    }
}
