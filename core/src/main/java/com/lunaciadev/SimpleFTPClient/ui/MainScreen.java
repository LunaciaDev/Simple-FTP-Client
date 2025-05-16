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
import com.lunaciadev.SimpleFTPClient.data.RequestType;
import com.lunaciadev.SimpleFTPClient.utils.ConnectUtils;
import com.lunaciadev.SimpleFTPClient.utils.DownloadUtils;
import com.lunaciadev.SimpleFTPClient.utils.FileDialog;
import com.lunaciadev.SimpleFTPClient.utils.Signal;
import com.lunaciadev.SimpleFTPClient.widgets.ControlPane;
import com.lunaciadev.SimpleFTPClient.widgets.ControlSocketOutput;
import com.lunaciadev.SimpleFTPClient.widgets.ListOutput;

/**
 * The main screen of the application. Also initialize relevant components.
 * 
 * @author LunaciaDev
 */
public class MainScreen implements Screen {
    private Table rootTable;
    private Stage stage;

    private ControlPane controlPane;
    private ControlSocketOutput socketOutput;
    private ListOutput listOutput;
    private ConnectDialog connectDialog;
    private ConnectUtils loginUtils;
    private GeneralDialog textDialog;
    private DownloadUtils downloadUtils;

    private FTPClient ftpClient;
    private FileDialog fileDialog;

    private Signal downloadFile = new Signal();
    private Signal changeDir = new Signal();

    public MainScreen(DataPackage dataPackage) {
        this.stage = new Stage(new ScreenViewport());

        this.controlPane = new ControlPane(dataPackage);
        this.socketOutput = new ControlSocketOutput(dataPackage);
        this.listOutput = new ListOutput(dataPackage);
        this.connectDialog = new ConnectDialog(dataPackage);
        this.loginUtils = new ConnectUtils();
        this.textDialog = new GeneralDialog(dataPackage);
        this.downloadUtils = new DownloadUtils();

        this.ftpClient = new FTPClient();
        this.fileDialog = new FileDialog();

        Gdx.input.setInputProcessor(stage);

        /****** CONNECTING SIGNALS ******/

        // Returning FTP Control Responses
        ftpClient.ftpControlResponse.connect(socketOutput::addOutput);

        // Text Dialog Handler
        textDialog.submitButtonClicked.connect(this::dialogHandler);

        // Connect to FTP Server
        // Show connection dialog
        controlPane.connectButtonClicked.connect(connectDialog::onConnectDialogRequested);
        // Start connect process
        connectDialog.loginButtonClicked.connect(loginUtils::startConnectProcess);
        // Open a TCP connection to the Server
        loginUtils.requestConnection.connect(ftpClient::connect);
        ftpClient.connectCompleted.connect(connectDialog::onConnectCommandFinished);
        // Start FTP login process
        ftpClient.connectCompleted.connect(loginUtils::startLoginProcess);
        loginUtils.requestLogin.connect(ftpClient::login);
        // Post-login tasks
        ftpClient.loginCompleted.connect(connectDialog::onLoginCommandFinished);
        ftpClient.loginCompleted.connect(controlPane::onConnectStatusUpdate);
        ftpClient.loginCompleted.connect(loginUtils::onLoginFinished);
        loginUtils.requestRefresh.connect(ftpClient::list);
        loginUtils.requestRefresh.connect(ftpClient::currentDirectory);
        ftpClient.currentDirectoryCompleted.connect(listOutput::workingDirectoryChanged);

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
        // Show download dialog
        controlPane.downloadButtonClicked.connect(textDialog::onDialogRequest);
        // Check if selected file exists
        this.downloadFile.connect(downloadUtils::getFileLists);
        downloadUtils.checkFileExist.connect(ftpClient::nameList);
        ftpClient.nameListCompleted.connect(downloadUtils::onHaveFileList);
        // Show native folder picker
        downloadUtils.selectDownloadFolder.connect(fileDialog::downloadFileDialog);
        fileDialog.downloadFolderSelected.connect(downloadUtils::folderSelected);
        // Start downloading
        downloadUtils.downloadFile.connect(ftpClient::retrieve);
        downloadUtils.downloadFile.connect(textDialog::hideDialog);

        // Change Directory
        controlPane.changeDirButtonClicked.connect(textDialog::onDialogRequest);
        this.changeDir.connect(ftpClient::changeDirectory);
        ftpClient.changeDirectoryCompleted.connect(textDialog::hideDialog);
        ftpClient.changeDirectoryCompleted.connect(ftpClient::currentDirectory);
        ftpClient.changeDirectoryCompleted.connect(ftpClient::list);

        // CDUP
        controlPane.cdupButtonClicked.connect(ftpClient::changeToParentDirectory);

        setLayout();
    }

    private void dialogHandler(Object... args) {
        switch ((RequestType) args[0]) {
            case DOWNLOAD:
                downloadFile.emit(args[1]);
                break;

            case CD:
                changeDir.emit(args[1]);
                break;
        }
    }

    private void setLayout() {
        connectDialog.setStage(stage);
        textDialog.setStage(stage);

        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        rootTable.defaults()
                .expandX().fill();

        rootTable.add(controlPane.getLayout());
        rootTable.row();
        rootTable.add(listOutput.getLayout()).space(10, 0, 10, 0).minHeight(Value.percentHeight(0.55f, rootTable))
                .expandY();
        rootTable.row();
        rootTable.add(socketOutput.getLayout()).space(10, 0, 10, 0).minHeight(Value.percentHeight(0.25f, rootTable))
                .expandY();

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
