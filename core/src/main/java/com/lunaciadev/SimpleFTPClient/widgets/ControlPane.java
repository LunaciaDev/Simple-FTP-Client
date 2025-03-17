package com.lunaciadev.SimpleFTPClient.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

public class ControlPane {
    private Table group;
    private TextButton connectButton;
    private TextButton refreshButton;
    private TextButton downloadButton;
    private TextButton uploadButton;

    private boolean isConnected;
    private float stringHeight;

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
     */
    public Signal downloadButtonClicked = new Signal();

    /**
     * Emitted when the upload button is clicked.
     */
    public Signal uploadButtonClicked = new Signal();

    /****** END SIGNALS SEGMENT ******/

    public ControlPane(DataPackage dataPackage) {
        isConnected = false;

        connectButton = new TextButton("Connect", dataPackage.getSkin(), "no-highlight");
        refreshButton = new TextButton("Refresh", dataPackage.getSkin(), "no-highlight");
        downloadButton = new TextButton("Download", dataPackage.getSkin(), "no-highlight");
        uploadButton = new TextButton("Upload", dataPackage.getSkin(), "no-highlight");

        stringHeight = connectButton.getStyle().font.getLineHeight() + 4f;

        refreshButton.setDisabled(true);
        downloadButton.setDisabled(true);
        uploadButton.setDisabled(true);

        connectButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // TODO Auto-generated method stub
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
            public void changed(ChangeEvent event, Actor actor) {
                refreshButtonClicked.emit();
            }
        });

        downloadButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                downloadButtonClicked.emit();
            }
        });

        uploadButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                uploadButtonClicked.emit();
            }
        });

        group = new Table();
        setLayout();
    }

    /**
     * Slot, connected to {@link FTPClient#loginCompleted}
     */
    public void onConnectStatusUpdate(Object... args) {
        if ((boolean) args[0]) {
            isConnected = true;
            connectButton.setText("Disconnect");
            refreshButton.setDisabled(false);
            downloadButton.setDisabled(false);
            uploadButton.setDisabled(false);
        }
    }

    public void onDisconnect(Object... args) {
        if ((boolean) args[0]) {
            isConnected = false;
            connectButton.setText("Connect");
            refreshButton.setDisabled(true);
            downloadButton.setDisabled(true);
            uploadButton.setDisabled(true);
        }
    }

    private void setLayout() {
        group.defaults().space(0, 10, 0, 10).height(stringHeight);

        group.add(connectButton);
        group.add(refreshButton);
        group.add(downloadButton);
        group.add(uploadButton);
        group.add().expandX();
    }

    public Table getLayout() {
        return group;
    }
}
