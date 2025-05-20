package com.lunaciadev.SimpleFTPClient.core.commands;

import java.io.BufferedReader;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

/**
 * <p>
 * Shared fields across commands.
 * </p>
 * 
 * <p>
 * I personally dislike this style as it "hides" possible field/methods away,
 * and is a pain to find, but it is good when dealing with duplicated code.
 * </p>
 * 
 * <p>
 * Why not bring {@code finish} method here? Each command has its own spec about
 * what to send back on finish, so.
 * </p>
 */
public class Command {
    public Signal ftpControlReceived = new Signal();
    public Signal completed = new Signal();

    /**
     * Break down a response to 2 part: it's code and message.
     * 
     * @param response The server's response.
     * @return 2 string. The first is the code, the second is the message.
     */
    protected String[] parseResponse(final String response) {
        return response.split("[ -]", 2);
    }

    protected String[] listenForResponse(final BufferedReader socketListener) throws IOException {
        String raw = socketListener.readLine();
        forwardControlResponse(raw);

        String[] response = parseResponse(raw);

        /**
         * Multi-line response always start with ***-response and end with *** response, the star are the same code from start to end
         * If there is a line starting with 3 number, that must be infixed with space.
         */
        if (raw.charAt(3) == '-') {
            while (true) {
                raw = socketListener.readLine();
                forwardControlResponse(raw);

                String[] parsed = parseResponse(raw);

                if (parsed[0].contentEquals(response[0])) {
                    break;
                }
            }
        }

        return response;
    }

    protected String[] parsePasvResponse(final String response) {
        String[] temp = response.strip().split(" ");
        temp = temp[temp.length - 1].replaceFirst("\\(", "").replaceFirst("\\)", "").replaceFirst("\\.", "").split(",");

        return temp;
    }

    protected void forwardControlResponse(final String response) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                ftpControlReceived.emit(response.strip());
            }
        });
    }
}
