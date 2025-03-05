package com.lunaciadev.SimpleFTPClient.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
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

    private FTPClient ftpClient;

    public MainScreen(DataPackage dataPackage) {
        this.stage = new Stage(new ScreenViewport());

        this.controlPane = new ControlPane(dataPackage);
        this.socketOutput = new ControlSocketOutput(dataPackage);
        this.listOutput = new ListOutput(dataPackage);
        this.progressInfo = new ProgressInfo(dataPackage);

        this.ftpClient = new FTPClient();

        setLayout();
    }

    private void setLayout() {
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
