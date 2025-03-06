package com.lunaciadev.SimpleFTPClient.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.utils.ConnectUtils;
import com.lunaciadev.SimpleFTPClient.utils.FileDialog;
import com.lunaciadev.SimpleFTPClient.widgets.ControlPane;
import com.lunaciadev.SimpleFTPClient.widgets.ControlSocketOutput;
import com.lunaciadev.SimpleFTPClient.widgets.ListOutput;
import com.lunaciadev.SimpleFTPClient.widgets.ProgressInfo;

public class MainScreen implements Screen {
    private Table rootTable;
    private Stage stage;

    private ControlPane controlPane;
    private ControlSocketOutput socketOutput;
    private ListOutput listOutput;
    private ProgressInfo progressInfo;
    private ConnectDialog connectDialog;
    private ConnectUtils loginUtils;

    private FTPClient ftpClient;
    private FileDialog fileDialog;

    public MainScreen(DataPackage dataPackage) {
        this.stage = new Stage(new ScreenViewport());

        this.controlPane = new ControlPane(dataPackage);
        this.socketOutput = new ControlSocketOutput(dataPackage);
        this.listOutput = new ListOutput(dataPackage);
        this.progressInfo = new ProgressInfo(dataPackage);
        this.connectDialog = new ConnectDialog(dataPackage);
        this.loginUtils = new ConnectUtils();

        this.ftpClient = new FTPClient();
        this.fileDialog = new FileDialog();

        Gdx.input.setInputProcessor(stage);

        controlPane.disconnectButtonClicked.connect(ftpClient::quit);
        controlPane.refreshButtonClicked.connect(ftpClient::list);
        controlPane.connectButtonClicked.connect(connectDialog::onConnectDialogRequested);

        controlPane.uploadButtonClicked.connect(fileDialog::uploadFileDialog);

        connectDialog.loginButtonClicked.connect(loginUtils::startConnectProcess);

        loginUtils.requestConnection.connect(ftpClient::connect);
        loginUtils.requestLogin.connect(ftpClient::login);

        ftpClient.listCompleted.connect(listOutput::addOutput);
        ftpClient.connectCompleted.connect(connectDialog::onConnectCommandFinished);
        ftpClient.connectCompleted.connect(loginUtils::startLoginProcess);
        ftpClient.loginCompleted.connect(connectDialog::onLoginCommandFinished);
        ftpClient.loginCompleted.connect(controlPane::onConnectStatusUpdate);
        ftpClient.ftpControlResponse.connect(socketOutput::addOutput);
        ftpClient.quitCompleted.connect(controlPane::onDisconnect);

        setLayout();
    }

    private void setLayout() {
        stage.addActor(connectDialog.getDialog());
        connectDialog.setStage(stage);
        connectDialog.getDialog().hide();

        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        rootTable.defaults()
                .expandX().fill();

        rootTable.add(controlPane.getLayout());
        rootTable.row();
        rootTable.add(listOutput.getLayout());
        rootTable.row();
        rootTable.add(socketOutput.getLayout());
        rootTable.row();
        rootTable.add(progressInfo.getLayout());
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'show'");
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.LIGHT_GRAY);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        return;
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resume'");
    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hide'");
    }

    @Override
    public void dispose() {
        ftpClient.dispose();
        fileDialog.dispose();
    }
    
}
