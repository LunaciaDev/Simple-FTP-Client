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
import com.lunaciadev.SimpleFTPClient.utils.Signal;
import com.lunaciadev.SimpleFTPClient.widgets.ControlPane;

/**
 * A form for the user to fill in, to connect to a FTP server with
 * authentications.
 * 
 * @author LunaciaDev
 */
public class ConnectDialog {
    private final Dialog dialog;
    private final TextField serverAddress;
    private final TextField usernameField;
    private final TextField passwordField;
    private final TextField portField;
    private final Label errorLabel;

    private final float stringHeight;

    private Stage stage;

    /****** SIGNALS ******/

    /**
     * Emitted when the login button is clicked.
     * 
     * @param address  {@link String} The FTP server's address
     * @param port     {@link String}
     * @param username {@link String}
     * @param password {@link String}
     */
    public Signal loginButtonClicked = new Signal();

    /****** END SIGNALS ******/

    public ConnectDialog(final DataPackage dataPackage) {
        dialog = new Dialog("", dataPackage.getSkin());
        serverAddress = new TextField("", dataPackage.getSkin());
        usernameField = new TextField("", dataPackage.getSkin());
        passwordField = new TextField("", dataPackage.getSkin());
        portField = new TextField("21", dataPackage.getSkin());
        errorLabel = new Label("", dataPackage.getSkin());

        stringHeight = errorLabel.getStyle().font.getLineHeight() + 4f;

        passwordField.setPasswordMode(true);
        portField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());

        setLayout(dataPackage);
    }

    private void setLayout(final DataPackage dataPackage) {
        dialog.pad(20);

        dialog.getContentTable().defaults().height(stringHeight);

        dialog.getContentTable().add(new Label("Address: ", dataPackage.getSkin()));
        dialog.getContentTable().add(serverAddress);
        dialog.getContentTable().add(new Label("Port: ", dataPackage.getSkin()));
        dialog.getContentTable().add(portField);
        dialog.getContentTable().row();
        dialog.getContentTable().add(new Label("Username: ", dataPackage.getSkin()));
        dialog.getContentTable().add(usernameField);
        dialog.getContentTable().row();
        dialog.getContentTable().add(new Label("Password: ", dataPackage.getSkin()));
        dialog.getContentTable().add(passwordField);

        passwordField.setPasswordCharacter('*');

        final TextButton showPassword = new TextButton("Show", dataPackage.getSkin(), "no-highlight");
        showPassword.addListener(new ClickListener() {
            @Override
            public void clicked(final InputEvent event, final float x, final float y) {
                super.clicked(event, x, y);
                passwordField.setPasswordMode(!passwordField.isPasswordMode());
            }
        });

        dialog.getContentTable().add(showPassword);
        dialog.getContentTable().row();
        dialog.getContentTable().add(errorLabel).colspan(4);

        final TextButton loginButton = new TextButton("Login", dataPackage.getSkin());
        final TextButton cancelButton = new TextButton("Cancel", dataPackage.getSkin(), "no-highlight");

        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(final InputEvent event, final float x, final float y) {
                super.clicked(event, x, y);
                loginButtonClicked.emit(serverAddress.getText(), portField.getText(), usernameField.getText(),
                        passwordField.getText());
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
        dialog.button(loginButton);
        dialog.button(cancelButton);

        // remove default listener from the buttonTable, they detect click and
        // immediately hide the dialog for some reason
        dialog.getButtonTable().clearListeners();
    }

    /**
     * Slot, triggered by {@link FTPClient#connectCompleted}
     */
    public void onConnectCommandFinished(final Object... args) {
        if (!(boolean) args[0]) {
            errorLabel.setText("Cannot connect to the server. The server may be busy, or a problem with Internet.");
        }
    }

    /**
     * Slot, triggered by {@link FTPClient#loginCompleted}
     */
    public void onLoginCommandFinished(final Object... args) {
        if (!(boolean) args[0]) {
            errorLabel.setText("Username or Password is incorrect.");

            // TODO: Add ACCT authentication
        } else {
            dialog.hide();
        }
    }

    /**
     * Slot, triggerd by {@link ControlPane#connectButtonClicked}
     */
    public void onConnectDialogRequested(final Object... args) {
        dialog.show(stage);
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setStage(final Stage stage) {
        this.stage = stage;
    }
}
