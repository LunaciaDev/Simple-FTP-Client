package com.lunaciadev.SimpleFTPClient.utils;

public class LoginUtils {
    public LoginUtils() {}

    private String username;
    private String password;

    /****** SIGNALS ******/

    /**
     * Emitted at the first stage of connecting, to establish a TCP connection to the server
     * 
     * @param address {@link String}
     */
    Signal requestConnection = new Signal();

    /**
     * Emitted at the second stage of connection
     * 
     * @param username {@link String}
     * @param password {@link String}
     */
    Signal requestLogin = new Signal();

    /****** END SIGNALS ******/

    public void startConnectProcess(Object... args) {
        username = (String) args[1];
        password = (String) args[2];

        requestConnection.emit(args[0]);
    }

    public void startLoginProcess(Object... args) {
        if (!(boolean) args[0]) return;

        requestLogin.emit(username, password);
    }
}
