package com.lunaciadev.SimpleFTPClient.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

public class DownloadDialog {
    private Dialog dialog;
    private TextField filenameField;
    private Label errorLabel;
    private float stringHeight;

    private Stage stage;

    /****** SIGNALS ******/

    /**
     * Emitted when the login button is clicked.
     * 
     * @param fileName {@link String} the submitted filename
     */
    public Signal downloadButtonClicked = new Signal();

    /****** END SIGNALS ******/

    public DownloadDialog(DataPackage dataPackage) {
        dialog = new Dialog("", dataPackage.getSkin());
        filenameField = new TextField("", dataPackage.getSkin());
        errorLabel = new Label("", dataPackage.getSkin());

        stringHeight = filenameField.getStyle().font.getLineHeight() + 4f;

        setLayout(dataPackage);
    }

    private void setLayout(DataPackage dataPackage) {
        dialog.pad(20);

        dialog.getContentTable().defaults().height(stringHeight);

        dialog.getContentTable().add(new Label("File Name: ", dataPackage.getSkin()));
        dialog.getContentTable().add(filenameField);
        dialog.row();
        dialog.getContentTable().add(errorLabel).colspan(3);

        TextButton downloadButton = new TextButton("Select", dataPackage.getSkin());
        TextButton cancelButton = new TextButton("Cancel", dataPackage.getSkin(), "no-highlight");

        downloadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                downloadButtonClicked.emit(filenameField.getText());
            }
        });

        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                dialog.hide();
            }
        });

        dialog.getButtonTable().defaults().height(stringHeight);
        dialog.button(downloadButton);
        dialog.button(cancelButton);
        dialog.getButtonTable().clearListeners();
    }

    /**
     * Slot, triggered by TBA
     */
    public void onDownloadIssues(Object... args) {
        errorLabel.setText((String) args[0]);
    }

    public void downloadStarted(Object... args) {
        dialog.hide();
    }

    /**
     * Slot, triggerd by TBA
     */
    public void onDownloadDialogRequested(Object... args) {
        dialog.show(stage);
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
