package com.lunaciadev.SimpleFTPClient.utils;

import com.badlogic.gdx.utils.Queue;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;


public class DownloadUtils {
    private String fileName;

    public Signal checkFileExist = new Signal();
    public Signal selectDownloadFolder = new Signal();
    public Signal selectRejected = new Signal();
    public Signal downloadFile = new Signal();

    public DownloadUtils() {}

    public void getFileLists(Object... args) {
        this.fileName = (String) args[0];
        checkFileExist.emit(fileName);
    }

    /**
     * This is connected to {@link FTPClient#nameList}, which guarantee that if the status is OK, there will be a Queue<String>.
     */
    public void onHaveFileList(Object... args) {
        if (!(boolean) args[0]) {
            selectRejected.emit("Cannot get file listing. Check your internet connection?");
        }

        @SuppressWarnings("unchecked")
        Queue<String> nameList = (Queue<String>) args[1];
        
        for (int i = 0; i < nameList.size; i++) {
            if (fileName.contentEquals(nameList.get(i))) {
                selectDownloadFolder.emit();
                return;
            }
        }

        selectRejected.emit("File does not exists in current directory.");
    }

    public void folderSelected(Object... args) {
        downloadFile.emit(fileName, args[0]);
    }
}
