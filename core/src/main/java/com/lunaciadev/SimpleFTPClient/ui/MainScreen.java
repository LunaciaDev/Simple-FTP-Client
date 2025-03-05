package com.lunaciadev.SimpleFTPClient.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;

public class MainScreen implements Screen {
    private DataPackage dataPackage;
    private Table rootTable;
    private Stage stage;
    private Skin skin;

    public MainScreen(DataPackage dataPackage) {
        this.dataPackage = dataPackage;
        this.skin = dataPackage.getSkin();
        this.stage = new Stage(new ScreenViewport());
        setLayout();
    }

    private void setLayout() {
        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        HorizontalGroup mainWidgetGroup = new HorizontalGroup();
        VerticalGroup serverFSView = new VerticalGroup();
        VerticalGroup controlGroup = new VerticalGroup();
        VerticalGroup localFSView = new VerticalGroup();

        mainWidgetGroup.addActor(serverFSView);
        mainWidgetGroup.addActor(controlGroup);
        mainWidgetGroup.addActor(localFSView);

        rootTable.add(mainWidgetGroup)
                .expandX().fill();

        HorizontalGroup statusGroup = new HorizontalGroup();

        rootTable.row();
        rootTable.add(statusGroup)
                .expandX().fill();
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
