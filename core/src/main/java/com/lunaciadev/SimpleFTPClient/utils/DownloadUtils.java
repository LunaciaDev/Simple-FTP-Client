package com.lunaciadev.SimpleFTPClient.utils;

import java.nio.file.Path;

import com.badlogic.gdx.utils.Queue;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;

/**
 * This class provide a procedural approach for downloading file from the FTP
 * Server.
 * 
 * @author LunaciaDev
 */
public class DownloadUtils {
    private String fileName;

    /****** SIGNALS ******/

    /**
     * Emitted when a NLST command is needed to check if the requested file exists.
     */
    public Signal checkFileExist = new Signal();

    /**
     * Emitted when the selected file is confirmed to exist on the Server, now
     * requesting a dialog to choose where to save the file.
     */
    public Signal selectDownloadFolder = new Signal();

    /**
     * Emitted when the selected file does not exist in the current directory on the
     * Server.
     * 
     * @param errorMessage {@link String}
     */
    public Signal selectRejected = new Signal();

    /**
     * Emitted when the user has selected the location to save the downloaded file
     * to, initiating a download.
     * 
     * @param fileName         {@link String}
     * @param downloadLocation {@link Path} The local path where the downloaded file
     *                         will be saved to
     */
    public Signal downloadFile = new Signal();

    /****** END SIGNALS ******/

    public DownloadUtils() {
    }

    public void getFileLists(final Object... args) {
        this.fileName = (String) args[0];
        checkFileExist.emit();
    }

    /**
     * This is connected to {@link FTPClient#nameList}, which guarantee that if the
     * status is OK, there will be a Queue<String>.
     */
    public void onHaveFileList(final Object... args) {
        if (!(boolean) args[0]) {
            selectRejected.emit("Cannot get file listing. Check your internet connection?");
        }

        @SuppressWarnings("unchecked")
        final
        Queue<String> nameList = (Queue<String>) args[1];

        for (int i = 0; i < nameList.size; i++) {
            if (fileName.contentEquals(nameList.get(i))) {
                selectDownloadFolder.emit();
                return;
            }
        }

        selectRejected.emit("File does not exists in current directory.");
    }

    /**
     * Slot, connected to {@link FileDialog#downloadFolderSelected}
     */
    public void folderSelected(final Object... args) {
        downloadFile.emit(fileName, args[0]);
    }
}
