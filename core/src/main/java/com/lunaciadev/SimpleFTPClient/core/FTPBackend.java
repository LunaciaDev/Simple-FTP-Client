package com.lunaciadev.SimpleFTPClient.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lunaciadev.SimpleFTPClient.utils.Signal;

/**
 * FTP Networking Backend, handle the connection between the client and server
 * while also carrying a composition of all implemented FTP commands. Also
 * aggregate certain event to be transferred to the frontend.
 */
public class FTPBackend {
    // Network Thread
    private ExecutorService controlExecutorService;
    private ExecutorService dataExecutorService;

    // Network Socket
    private Socket controlSocket;
    private BufferedReader socketListener;
    private BufferedWriter socketWriter;

    /**
     * As a wrapper, this only need to keep track whether if a connection to an FTP
     * Server is active or not. Data cleanup change accordingly based on the state.
     * Is exposed as a Signal. See {@link FTPBackend_StateMachine}
     */
    private FTPBackend_StateMachine stateMachine;

    /**
     * Emitted when the FTP connection state change. A True value indicate active
     * connection, whilst False indicate inactive connection.
     */
    public Signal connectionStateChanged = new Signal();

    public FTPBackend() {
        this.stateMachine = new FTPBackend_StateMachine();
        this.controlExecutorService = Executors.newSingleThreadExecutor();
        this.dataExecutorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 2 state system: Connected and not connected.
     */
    private class FTPBackend_StateMachine {
        boolean isConnected;

        // initial state
        public FTPBackend_StateMachine() {
            isConnected = false;
        }

        public void onConnected(Object... args) {
            isConnected = true;
            connectionStateChanged.emit(isConnected);
        }

        public void onDisconnected(Object... args) {
            isConnected = false;
            connectionStateChanged.emit(isConnected);
        }
    }
}