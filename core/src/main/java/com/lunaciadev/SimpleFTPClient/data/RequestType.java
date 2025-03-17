package com.lunaciadev.SimpleFTPClient.data;

public enum RequestType {
    CD("Directory to change to:"),
    RM("File to remove:"),
    RMDIR("Directory to remove:"),
    MKDIR("Directory name:"),
    DOWNLOAD("File to Download:");

    private String data;

    private RequestType(String data) {
        this.data = data;
    }

    public String getLabelString() {
        return data;
    }
}
