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

public class ConnectDialog {
    private Dialog dialog;
    private TextField serverAddress;
    private TextField usernameField;
    private TextField passwordField;
    private Label errorLabel;

    /****** SIGNALS ******/

    /**
     * Emitted when the login button is clicked.
     * 
     * @param address {@link String} The FTP server address
     * @param username {@link String}
     * @param password {@link String}
     */
    public Signal loginButtonClicked = new Signal();

    /****** END SIGNALS ******/

    public ConnectDialog(DataPackage dataPackage) {
        dialog = new Dialog("Connect to a FTP Server", dataPackage.getSkin());
        serverAddress = new TextField("", dataPackage.getSkin());
        usernameField = new TextField("", dataPackage.getSkin());
        passwordField = new TextField("", dataPackage.getSkin());
        setLayout(dataPackage);
    }

    private void setLayout(DataPackage dataPackage) {
        dialog.add(new Label("FTP Server Address: ", dataPackage.getSkin()));
        dialog.add(serverAddress);
        dialog.row();
        dialog.add(new Label("Username: ", dataPackage.getSkin()));
        dialog.add(usernameField);
        dialog.row();
        dialog.add(new Label("Password: ", dataPackage.getSkin()));
        dialog.add(passwordField);

        TextButton showPassword = new TextButton("Show", dataPackage.getSkin());
        showPassword.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                passwordField.setPasswordMode(!passwordField.isPasswordMode());
            }
        });

        dialog.add(showPassword);
        passwordField.setPasswordMode(true);
        dialog.row();
        dialog.add(errorLabel);

        TextButton loginButton = new TextButton("Login", dataPackage.getSkin());
        TextButton cancelButton = new TextButton("Cancel", dataPackage.getSkin());

        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                loginButtonClicked.emit(serverAddress.getText(), usernameField.getText(), passwordField.getText());
            }
        });

        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                dialog.hide();
            }
        });

        dialog.button(loginButton);
        dialog.button(cancelButton);
    }

    /**
     * Slot, triggered by TBA
     */
    public void onConnectCommandFinished(boolean status) {
        if (!status) {
            errorLabel.setText("Cannot connect to the server. Please check the address and your Internet connection.");
        }
    }

    /**
     * Slot, triggered by TBA
     */
    public void onLoginCommandFinished(boolean status) {
        if (!status) {
            errorLabel.setText("Username or Password is incorrect.");
        }
        else {
            dialog.hide();
        }
    }

    /**
     * Slot, triggerd by TBA
     */
    public void onConnectDialogRequested(Stage stage) {
        dialog.show(stage);
    }

    public Dialog getDialog() {
        return dialog;
    }
}
