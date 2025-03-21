package com.lunaciadev.SimpleFTPClient.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.data.RequestType;
import com.lunaciadev.SimpleFTPClient.utils.Signal;

public class GeneralDialog {
    private Dialog dialog;
    private TextField filenameField;
    private Label errorLabel;
    private Label requestLabel;
    private float stringHeight;

    private RequestType currentRequestType;

    private Stage stage;

    /****** SIGNALS ******/

    /**
     * Emitted when the OK button is clicked.
     * 
     * @param fileName {@link String} the submitted filename
     */
    public Signal submitButtonClicked = new Signal();

    /****** END SIGNALS ******/

    public GeneralDialog(DataPackage dataPackage) {
        dialog = new Dialog("", dataPackage.getSkin());
        filenameField = new TextField("", dataPackage.getSkin());
        errorLabel = new Label("", dataPackage.getSkin());
        requestLabel = new Label("", dataPackage.getSkin());

        stringHeight = filenameField.getStyle().font.getLineHeight() + 4f;

        setLayout(dataPackage);
    }

    private void setLayout(DataPackage dataPackage) {
        dialog.pad(20);

        dialog.getContentTable().defaults().height(stringHeight);

        dialog.getContentTable().add(requestLabel);
        dialog.getContentTable().add(filenameField);
        dialog.row();
        dialog.getContentTable().add(errorLabel).colspan(3);

        TextButton submitButton = new TextButton("OK", dataPackage.getSkin());
        TextButton cancelButton = new TextButton("Cancel", dataPackage.getSkin(), "no-highlight");

        submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                submitButtonClicked.emit(currentRequestType, filenameField.getText());
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
        dialog.button(submitButton);
        dialog.button(cancelButton);
        dialog.getButtonTable().clearListeners();
    }

    /**
     * Slot, triggered by TBA
     */
    public void onDownloadIssues(Object... args) {
        errorLabel.setText((String) args[0]);
    }

    public void hideDialog(Object... args) {
        dialog.hide();
    }

    /**
     * Slot, triggerd by TBA
     */
    public void onDialogRequest(Object... args) {
        currentRequestType = (RequestType) args[0];
        requestLabel.setText(currentRequestType.getLabelString());
        dialog.show(stage);
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
