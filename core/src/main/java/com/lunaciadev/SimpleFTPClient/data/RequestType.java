package com.lunaciadev.SimpleFTPClient.data;

public enum RequestType {
    CD("Directory to change to:"),
    MKD("Name of new directory:"),
    DOWNLOAD("File to download:");

    private final String data;

    private RequestType(final String data) {
        this.data = data;
    }

    public String getLabelString() {
        return data;
    }
}
