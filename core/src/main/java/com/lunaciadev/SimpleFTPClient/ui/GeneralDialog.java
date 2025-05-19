package com.lunaciadev.SimpleFTPClient.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.data.RequestType;
import com.lunaciadev.SimpleFTPClient.utils.DownloadUtils;
import com.lunaciadev.SimpleFTPClient.utils.Signal;
import com.lunaciadev.SimpleFTPClient.widgets.ControlPane;

/**
 * A generic dialog, used for CD and Download commands.
 * 
 * @author LunaciaDev
 */
public class GeneralDialog {
    private final Dialog dialog;
    private final TextField filenameField;
    private final Label errorLabel;
    private final Label requestLabel;
    private final float stringHeight;

    private RequestType currentRequestType;

    private Stage stage;

    /****** SIGNALS ******/

    /**
     * Emitted when the OK button is clicked.
     * 
     * @param requestType {@link RequestType}
     * @param fileName {@link String} the submitted string
     */
    public Signal submitButtonClicked = new Signal();

    /****** END SIGNALS ******/

    public GeneralDialog(final DataPackage dataPackage) {
        dialog = new Dialog("", dataPackage.getSkin());
        filenameField = new TextField("", dataPackage.getSkin());
        errorLabel = new Label("", dataPackage.getSkin());
        requestLabel = new Label("", dataPackage.getSkin());

        stringHeight = filenameField.getStyle().font.getLineHeight() + 4f;

        setLayout(dataPackage);
    }

    private void setLayout(final DataPackage dataPackage) {
        dialog.pad(20);

        dialog.getContentTable().defaults().height(stringHeight);

        dialog.getContentTable().add(requestLabel);
        dialog.getContentTable().add(filenameField);
        dialog.row();
        dialog.getContentTable().add(errorLabel).colspan(3);

        final TextButton submitButton = new TextButton("OK", dataPackage.getSkin());
        final TextButton cancelButton = new TextButton("Cancel", dataPackage.getSkin(), "no-highlight");

        submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(final InputEvent event, final float x, final float y) {
                super.clicked(event, x, y);
                submitButtonClicked.emit(currentRequestType, filenameField.getText());
            }
        });

        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(final InputEvent event, final float x, final float y) {
                super.clicked(event, x, y);
                dialog.hide();
            }
        });

        dialog.getButtonTable().defaults().height(stringHeight);
        dialog.button(submitButton);
        dialog.button(cancelButton);

        // remove default listener from the buttonTable, they detect click and
        // immediately hide the dialog for some reason
        dialog.getButtonTable().clearListeners();
    }

    /**
     * Slot, currently unused
     */
    public void onDownloadIssues(final Object... args) {
        errorLabel.setText((String) args[0]);
    }

    /**
     * Slot, triggered by {@link DownloadUtils#downloadFile} and
     * {@link FTPClient#changeDirectoryCompleted}
     */
    public void hideDialog(final Object... args) {
        dialog.hide();
    }

    /**
     * Slot, triggerd by {@link ControlPane#downloadButtonClicked} and
     * {@link ControlPane#changeDirButtonClicked}
     */
    public void onDialogRequest(final Object... args) {
        currentRequestType = (RequestType) args[0];
        requestLabel.setText(currentRequestType.getLabelString());
        dialog.show(stage);
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setStage(final Stage stage) {
        this.stage = stage;
    }
}
