package com.lunaciadev.SimpleFTPClient.core.commands;

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
    public String[] parseResponse(final String response) {
        return response.strip().split(" ", 2);
    }

    public String[] parsePasvResponse(final String response) {
        String[] temp = response.strip().split(" ");
        temp = temp[temp.length - 1].replaceFirst("\\(", "").replaceFirst("\\)", "").replaceFirst("\\.", "").split(",");

        return temp;
    }

    public void forwardControlResponse(final String response) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                ftpControlReceived.emit(response.strip());
            }
        });
    }
}
