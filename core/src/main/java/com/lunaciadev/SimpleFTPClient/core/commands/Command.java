package com.lunaciadev.SimpleFTPClient.core.commands;

public class Command {
    /**
     * Break down a response to 2 part: it's code and message.
     * 
     * @param response The server's response.
     * @return 2 string. The first is the code, the second is the message.
     */
    public String[] parseResponse(String response) {
        return response.strip().split(" ", 2);
    }

    public String[] parsePasvResponse(String response) {
        String[] temp = response.strip().split(" ");
        temp = temp[temp.length - 1].replaceFirst("\\(", "").replaceFirst("\\)", "").replaceFirst("\\.", "").split(",");

        return temp;
    }
}
