package com.lunaciadev.SimpleFTPClient.utils;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import com.badlogic.gdx.Gdx;

import com.lunaciadev.SimpleFTPClient.widgets.ControlPane;

/**
 * Abstraction over TinyFileDialogs, providing access to native file picker
 * across different host systems.
 * 
 * @author LunaciaDev
 */
public class FileDialog {
    private ExecutorService service;

    /****** SIGNALS ******/

    /**
     * Emitted after the user has selected which file to upload from the file
     * picker.
     * 
     * @param selectedFilePath {@link Path} The path to the file that is selected.
     */
    public Signal uploadFileSelected = new Signal();

    /**
     * Emitted after the user has selected which folder to save the downloaded file
     * from the server.
     * 
     * @param downloadFolderPath {@link Path} The path to the folder that is
     *                           selected.
     */
    public Signal downloadFolderSelected = new Signal();

    /****** END SIGNALS ******/

    // we remember the last selected folder to make the UX better for downloading
    // multiple files.
    private String lastDownloadFolder = null;

    public FileDialog() {
        // Opening a file dialog is a blocking call, so it will have its own thread.
        service = Executors.newSingleThreadExecutor();
    }

    /**
     * Slot, connected to {@link ControlPane#uploadButtonClicked}
     */
    public void uploadFileDialog(Object... args) {
        service.submit(new Runnable() {
            @Override
            public void run() {
                String selectedFilePath = TinyFileDialogs.tinyfd_openFileDialog("Upload File", null, null, null, false);

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (selectedFilePath != null) {
                            uploadFileSelected.emit(Path.of(selectedFilePath));
                        }
                    }
                });
            }
        });
    }

    /**
     * Slot, connected to {@link DownloadUtils#selectDownloadFolder}
     */
    public void downloadFileDialog(Object... args) {
        service.submit(new Runnable() {
            @Override
            public void run() {
                String defaultPath = lastDownloadFolder == null ? System.getProperty("user.home") : lastDownloadFolder;
                String folderPath = TinyFileDialogs.tinyfd_selectFolderDialog("Select Download Folder", defaultPath);
                lastDownloadFolder = folderPath == null ? lastDownloadFolder : folderPath;

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (folderPath != null) {
                            downloadFolderSelected.emit(Path.of(folderPath));
                        }
                    }
                });
            }
        });
    }

    public void dispose() {
        service.shutdown();
    }
}
