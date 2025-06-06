package com.lunaciadev.SimpleFTPClient.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.data.RequestType;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

/**
 * This widget provide GUI access to FTP commands.
 * 
 * @author LunaciaDev
 */
public class ControlPane {
    private final Table group;
    private final TextButton connectButton;
    private final TextButton refreshButton;
    private final TextButton downloadButton;
    private final TextButton uploadButton;
    private final TextButton changeDirButton;
    private final TextButton cdupButton;
    private final TextButton mkdirButton;

    private boolean isConnected;
    private final float stringHeight;

    /****** SIGNALS ******/

    /**
     * Emitted when the connect button is clicked.
     */
    public Signal connectButtonClicked = new Signal();

    /**
     * Emitted when the disconnect button is clicked.
     */
    public Signal disconnectButtonClicked = new Signal();

    /**
     * Emitted when the refresh button is clicked.
     */
    public Signal refreshButtonClicked = new Signal();

    /**
     * Emitted when the download button is clicked.
     * 
     * @param requestType {@link RequestType#DOWNLOAD}
     */
    public Signal downloadButtonClicked = new Signal();

    /**
     * Emitted when the upload button is clicked.
     */
    public Signal uploadButtonClicked = new Signal();

    /**
     * Emitted when the change directory button is clicked.
     * 
     * @param requestType {@link RequestType#CD}
     */
    public Signal changeDirButtonClicked = new Signal();

    /**
     * Emitted when the move up directory button is clicked.
     */
    public Signal cdupButtonClicked = new Signal();

    /**
     * Emitted when the make directory button is clicked.
     */
    public Signal mkdirButtonClicked = new Signal();

    /****** END SIGNALS SEGMENT ******/

    public ControlPane(final DataPackage dataPackage) {
        isConnected = false;

        connectButton = new TextButton("Connect", dataPackage.getSkin(), "no-highlight");
        refreshButton = new TextButton("Refresh", dataPackage.getSkin(), "no-highlight");
        downloadButton = new TextButton("Download", dataPackage.getSkin(), "no-highlight");
        uploadButton = new TextButton("Upload", dataPackage.getSkin(), "no-highlight");
        mkdirButton = new TextButton("Make Directory", dataPackage.getSkin(), "no-highlight");
        changeDirButton = new TextButton("Change Directory", dataPackage.getSkin(), "no-highlight");
        cdupButton = new TextButton("To Parent Directory", dataPackage.getSkin(), "no-highlight");

        stringHeight = connectButton.getStyle().font.getLineHeight() + 4f;

        refreshButton.setDisabled(true);
        downloadButton.setDisabled(true);
        uploadButton.setDisabled(true);
        mkdirButton.setDisabled(true);
        changeDirButton.setDisabled(true);
        cdupButton.setDisabled(true);

        connectButton.addListener(new ChangeListener() {
            @Override
            public void changed(final ChangeEvent event, final Actor actor) {
                if (!isConnected) {
                    connectButtonClicked.emit();
                }
                else {
                    disconnectButtonClicked.emit();
                }
            }
        });

        refreshButton.addListener(new ChangeListener() {
            @Override
            public void changed(final ChangeEvent event, final Actor actor) {
                refreshButtonClicked.emit();
            }
        });

        downloadButton.addListener(new ChangeListener() {
            @Override
            public void changed(final ChangeEvent event, final Actor actor) {
                downloadButtonClicked.emit(RequestType.DOWNLOAD);
            }
        });

        uploadButton.addListener(new ChangeListener() {
            @Override
            public void changed(final ChangeEvent event, final Actor actor) {
                uploadButtonClicked.emit();
            }
        });

        mkdirButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                mkdirButtonClicked.emit(RequestType.MKD);
            }
        });

        changeDirButton.addListener(new ChangeListener() {
            @Override
            public void changed(final ChangeEvent event, final Actor actor) {
                changeDirButtonClicked.emit(RequestType.CD);
            }
        });

        cdupButton.addListener(new ChangeListener() {
            @Override
            public void changed(final ChangeEvent event, final Actor actor) {
                cdupButtonClicked.emit();
            }
        });

        group = new Table();
        setLayout();
    }

    /**
     * Slot, connected to {@link FTPClient#loginCompleted}
     */
    public void onConnectStatusUpdate(final Object... args) {
        if ((boolean) args[0]) {
            isConnected = true;
            connectButton.setText("Disconnect");
            refreshButton.setDisabled(false);
            downloadButton.setDisabled(false);
            uploadButton.setDisabled(false);
            mkdirButton.setDisabled(false);
            changeDirButton.setDisabled(false);
            cdupButton.setDisabled(false);
        }
    }

    /**
     * Slot, connected to {@link FTPClient#quitCompleted}
     */
    public void onDisconnect(final Object... args) {
        if ((boolean) args[0]) {
            isConnected = false;
            connectButton.setText("Connect");
            refreshButton.setDisabled(true);
            downloadButton.setDisabled(true);
            uploadButton.setDisabled(true);
            mkdirButton.setDisabled(true);
            changeDirButton.setDisabled(true);
            cdupButton.setDisabled(true);
        }
    }

    private void setLayout() {
        group.defaults().space(0, 10, 0, 10).height(stringHeight);

        group.add(connectButton);
        group.add(refreshButton);
        group.add(downloadButton);
        group.add(uploadButton);
        group.add(mkdirButton);
        group.add(changeDirButton);
        group.add(cdupButton);
        group.add().expandX();
    }

    public Table getLayout() {
        return group;
    }
}
