package com.lunaciadev.SimpleFTPClient.utils;

public class ControlResponseHandler {
    public ControlResponseHandler controlResponseHandler;

    /**
     * Break down a response to 2 part: it's code and message.
     * 
     * @param response The server's response.
     * @return 2 string. The first is the code, the second is the message.
     */
    public String[] handleResponse(String response) {
        return response.strip().split(" ", 1);
    }
}
