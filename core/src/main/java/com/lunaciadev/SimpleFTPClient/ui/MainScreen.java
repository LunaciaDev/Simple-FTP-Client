package com.lunaciadev.SimpleFTPClient.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.utils.ConnectUtils;
import com.lunaciadev.SimpleFTPClient.utils.DownloadUtils;
import com.lunaciadev.SimpleFTPClient.utils.FileDialog;
import com.lunaciadev.SimpleFTPClient.widgets.ControlPane;
import com.lunaciadev.SimpleFTPClient.widgets.ControlSocketOutput;
import com.lunaciadev.SimpleFTPClient.widgets.ListOutput;

public class MainScreen implements Screen {
    private Table rootTable;
    private Stage stage;

    private ControlPane controlPane;
    private ControlSocketOutput socketOutput;
    private ListOutput listOutput;
    private ConnectDialog connectDialog;
    private ConnectUtils loginUtils;
    private DownloadDialog downloadDialog;
    private DownloadUtils downloadUtils;

    private FTPClient ftpClient;
    private FileDialog fileDialog;

    public MainScreen(DataPackage dataPackage) {
        this.stage = new Stage(new ScreenViewport());

        this.controlPane = new ControlPane(dataPackage);
        this.socketOutput = new ControlSocketOutput(dataPackage);
        this.listOutput = new ListOutput(dataPackage);
        this.connectDialog = new ConnectDialog(dataPackage);
        this.loginUtils = new ConnectUtils();
        this.downloadDialog = new DownloadDialog(dataPackage);
        this.downloadUtils = new DownloadUtils();

        this.ftpClient = new FTPClient();
        this.fileDialog = new FileDialog();

        Gdx.input.setInputProcessor(stage);

        /****** CONNECTING SIGNALS ******/
        
        // Returning FTP Control Responses
        ftpClient.ftpControlResponse.connect(socketOutput::addOutput);

        // Connect to FTP Server
        controlPane.connectButtonClicked.connect(connectDialog::onConnectDialogRequested);
        connectDialog.loginButtonClicked.connect(loginUtils::startConnectProcess);
        loginUtils.requestConnection.connect(ftpClient::connect);
        ftpClient.connectCompleted.connect(connectDialog::onConnectCommandFinished);
        ftpClient.connectCompleted.connect(loginUtils::startLoginProcess);
        loginUtils.requestLogin.connect(ftpClient::login);
        ftpClient.loginCompleted.connect(connectDialog::onLoginCommandFinished);
        ftpClient.loginCompleted.connect(controlPane::onConnectStatusUpdate);
        ftpClient.loginCompleted.connect(loginUtils::onLoginFinished);
        loginUtils.requestRefresh.connect(ftpClient::list);

        // Disconnect from FTP Server
        controlPane.disconnectButtonClicked.connect(ftpClient::quit);
        ftpClient.quitCompleted.connect(controlPane::onDisconnect);

        // Refresh file listing
        controlPane.refreshButtonClicked.connect(ftpClient::list);
        ftpClient.listCompleted.connect(listOutput::addOutput);

        // Upload File
        controlPane.uploadButtonClicked.connect(fileDialog::uploadFileDialog);
        fileDialog.uploadFileSelected.connect(ftpClient::store);

        // Download File
        controlPane.downloadButtonClicked.connect(downloadDialog::onDownloadDialogRequested);
        downloadDialog.downloadButtonClicked.connect(downloadUtils::getFileLists);
        downloadUtils.checkFileExist.connect(ftpClient::nameList);
        ftpClient.nameListCompleted.connect(downloadUtils::onHaveFileList);
        downloadUtils.selectDownloadFolder.connect(fileDialog::downloadFileDialog);
        fileDialog.downloadFolderSelected.connect(downloadUtils::folderSelected);
        downloadUtils.downloadFile.connect(ftpClient::retrieve);
        downloadUtils.downloadFile.connect(downloadDialog::downloadStarted);

        setLayout();
    }

    private void setLayout() {
        connectDialog.setStage(stage);
        downloadDialog.setStage(stage);

        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        rootTable.defaults()
                .expandX().fill();

        rootTable.add(controlPane.getLayout());
        rootTable.row();
        rootTable.add(listOutput.getLayout()).pad(10, 0, 10, 0).minHeight(Value.percentHeight(0.5f, rootTable)).expandY();
        rootTable.row();
        rootTable.add(socketOutput.getLayout()).pad(10, 0, 10, 0).minHeight(Value.percentHeight(0.2f, rootTable)).expandY();

        rootTable.pad(10);
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'show'");
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(new Color(0x212529ff));
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
