package com.lunaciadev.SimpleFTPClient.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.utils.LoginUtils;
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
    private LoginUtils loginUtils;

    private FTPClient ftpClient;

    public MainScreen(DataPackage dataPackage) {
        this.stage = new Stage(new ScreenViewport());

        this.controlPane = new ControlPane(dataPackage);
        this.socketOutput = new ControlSocketOutput(dataPackage);
        this.listOutput = new ListOutput(dataPackage);
        this.progressInfo = new ProgressInfo(dataPackage);
        this.connectDialog = new ConnectDialog(dataPackage);
        this.loginUtils = new LoginUtils();

        this.ftpClient = new FTPClient();

        controlPane.disconnectButtonClicked.connect(ftpClient::quit);
        controlPane.refreshButtonClicked.connect(ftpClient::list);
        controlPane.connectButtonClicked.connect(connectDialog::onConnectDialogRequested);

        connectDialog.loginButtonClicked.connect(loginUtils::startConnectProcess);

        ftpClient.listCompleted.connect(listOutput::addOutput);
        ftpClient.connectCompleted.connect(connectDialog::onConnectCommandFinished);
        ftpClient.connectCompleted.connect(loginUtils::startLoginProcess);
        ftpClient.loginCompleted.connect(connectDialog::onLoginCommandFinished);
        ftpClient.loginCompleted.connect(controlPane::onConnectStatusUpdate);
        ftpClient.ftpControlResponse.connect(socketOutput::addOutput);

        setLayout();
    }

    private void setLayout() {
        stage.addActor(connectDialog.getDialog());
        connectDialog.setStage(stage);

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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resize'");
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pause'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dispose'");
    }
    
}
