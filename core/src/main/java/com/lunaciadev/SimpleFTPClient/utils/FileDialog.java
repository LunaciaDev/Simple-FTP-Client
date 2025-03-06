package com.lunaciadev.SimpleFTPClient.utils;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import com.badlogic.gdx.Gdx;

public class FileDialog {
    private ExecutorService service;

    public Signal uploadFileSelected = new Signal();

    public FileDialog() {
        service = Executors.newSingleThreadExecutor();
    }

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

    public void dispose() {
        service.shutdown();
    }
}
