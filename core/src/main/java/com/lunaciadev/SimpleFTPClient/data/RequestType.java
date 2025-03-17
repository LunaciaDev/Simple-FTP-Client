package com.lunaciadev.SimpleFTPClient.data;

public enum RequestType {
    CD("Directory to change to:"),
    RM("File to remove:"),
    DOWNLOAD("File to Download:");

    private String data;

    private RequestType(String data) {
        this.data = data;
    }

    public String getLabelString() {
        return data;
    }
}
