package com.lunaciadev.SimpleFTPClient.core;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.lunaciadev.SimpleFTPClient.utils.AsciiListener;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

/**
 * Implement the FTP Client-side, providing function call instead of command composition.
 */
public class FTP {
    enum Command {
        USER,
        PASS,
        ACCT
    }

    private Socket controlSocket;
    private AsciiListener controlListener;
    private BufferedWriter controlSender;
    private Command lastCommand;

    private StringBuilder stringBuilder;

    // Signals
    // Trying out new Signal syntax here, since I keep forgetting to initialize the Signals on the other project.

    /**
     * Emitted after a login attempt has finished.
     * 
     * @param result {@link Boolean} {@code True} if the authentication succeeded, {@code False} otherwise.
     * @param response {@link String} The server's message.
     */
    public Signal login_attemptFinished = new Signal();

    /**
     * Emitted after a username attempt has succeeded, but a password is needed.
     */
    public Signal login_passRequest = new Signal();

    /**
     * Emitted after a password attempt has succeeded, but account information is needed. Rarely used, but safe than sorry.
     */
    public Signal login_acctRequest = new Signal();

    public FTP() {
        controlSocket = null;
        controlListener = null;
        stringBuilder = new StringBuilder();
    }

    /**
     * Attempt to establish a TCP connection to port 21 of address.
     *
     * @param address {@link String} A string, being the domain name or IPv4 address of the target.
     * @return {@code True} if the connection succeeded, {@code False} otherwise.
     */
    public boolean connect(String address) {
        try {
            controlSocket = new Socket(address, 21);

            controlListener = new AsciiListener(controlSocket.getInputStream());
            controlListener.controlResponse.connect(this::onControlResponse);

            controlSender = new BufferedWriter(new OutputStreamWriter(controlSocket.getOutputStream()));
        } catch (Exception e) {
            // maybe attempt multiple time here?
            return false;
        }

        return true;
    }

    /**
     * Compose a String based from all the arguments space-separated, ending with CRLF.
     * 
     * @param args {@link String} List of all argument parts
     */
    private String composeCommand(String... args) {
        stringBuilder.setLength(0);

        for (String string : args) {
            stringBuilder.append(string).append(" ");
        }

        stringBuilder.setLength(stringBuilder.length() - 1);
        stringBuilder.append("\r\n");
        
        return stringBuilder.toString();
    }

    /**
     * Slot, triggered by {@link AsciiListener#controlResponse}.
     */
    private void onControlResponse(Object... args) {
        /*
         * Switch galore. Pray to god that the server also follow the FTP protocol. The moment it shoot out a 6xx code the entire app EXPLODE.
         * Why not create a separate handler for each command... then we have function galore as well! Both way ends with one gigantic class, at least the latter is easier to the eye.
         */

        String responseString = ((String) args[0]).split(" ", 2)[1];
        char responseCode = ((String) args[0]).charAt(0);

        switch (lastCommand) {
            case USER:
                login_handleUserCommand(responseCode, responseString);
                break;

            case PASS:
                login_handlePassCommand(responseCode, responseString);
                break;
            
            case ACCT:
                login_handleAcctCommand(responseCode, responseString);
                break;
        }
    }

    /****** LOGIN ******/

    /**
     * I decided to divide the login into 2 separate stage, so that it is much easier to deal with as listening is not blocking.
     */

    // LOGIN REQUESTS

    /**
     * Send the username to the FTP server.
     * 
     * @param username {@link String} Username of the user.
     */
    public void login_sendUsername(String username) {
        try {
            lastCommand = Command.USER;
            controlSender.write(composeCommand("USER", username));
            controlSender.flush();
        } catch (Exception e) {
            login_attemptFinished.emit(false, e.getMessage());
        }
    }

    /**
     * Send the password to the FTP server.
     * 
     * @param password {@link String} Password of the user.
     */
    public void login_sendPassword(String password) {
        try {
            lastCommand = Command.PASS;
            controlSender.write(composeCommand("PASS", password));
            controlSender.flush();
        } catch (Exception e) {
            login_attemptFinished.emit(false, e.getMessage());
        }
    }

    /**
     * Send the account information to the FTP server.
     *
     * @param account {@link String} Account of the user.
     */
    public void login_sendAccount(String account) {
        try {
            lastCommand = Command.ACCT;
            controlSender.write(composeCommand("ACCT", account));
            controlSender.flush();
        } catch (Exception e) {
            login_attemptFinished.emit(false, e.getMessage());
        }
    }

    // LOGIN RESPONSE HANDLER

    /**
     * <p> Handle {@link FTP#login_sendUsername}. </p>
     * 
     * Per the FTP State Diagram for Login Sequence (pg. 56, RFC 959), the result of response code are as follows:
     * 1xx, 4xx and 5xx are failed login. Might have anon in here?!?
     * 3xx require password to fully authenticate.
     * 2xx mean authentication successful, no need for password.
     */
    private void login_handleUserCommand(char responseCode, String responseString) {
        switch (responseCode) {
            case '1':
            case '4':
            case '5':
                login_attemptFinished.emit(false, responseString);
                break;

            case '2':
                login_attemptFinished.emit(true, responseString);
                break;

            case '3':
                login_passRequest.emit();
        }
    }

    /**
     * <p> Handle {@link FTP#login_sendPassword} </p>
     * 
     * Per the FTP State Diagram for Login Sequence (pg. 56, RFC 959), the result of response code are as follows:
     * 1xx, 4xx and 5xx are failed login.
     * 2xx mean authentication successful.
     * 3xx require account information. man.
     */
    private void login_handlePassCommand(char responseCode, String responseString) {
        switch (responseCode) {
            case '1':
            case '4':
            case '5':
                login_attemptFinished.emit(false, responseString);
                break;

            case '2':
                login_attemptFinished.emit(true, responseString);
                break;

            case '3':
                login_acctRequest.emit();
                break;
        }
    }

    /**
     * <p> Handle {@link FTP#login_sendAccount} </p>
     * 
     * Per the FTP State Diagram for Login Sequence (pg. 56, RFC 959), the result of response code are as follows:
     * 1xx, 3xx, 4xx, 5xx are failed login.
     * 2xx mean authentication successful.
     */
    private void login_handleAcctCommand(char responseCode, String responseString) {
        switch (responseCode) {
            case '1':
            case '3':
            case '4':
            case '5':
                login_attemptFinished.emit(false, responseString);
                break;

            case '2':
                login_attemptFinished.emit(true, responseString);
                break;
        }
    }

    /******  ******/
}

/* TEMPLATE for command response handler, we will be writing these for a long while. Cannot template javadocs, good luck future me.

private void abc_handleLoremIpsum(char responseCode, String responseString) {
    switch (responseCode) {
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
    }
}

*/

// TEMPLATE for response handler javadocs

/**
 * <p> Handle {@link FTP#} </p>
 * 
 * Per the FTP State Diagram for ... (pg. xx, RFC 959), the result of response code are as follows:
 * 1xx
 * 2xx
 * 3xx
 * 4xx
 * 5xx
 */