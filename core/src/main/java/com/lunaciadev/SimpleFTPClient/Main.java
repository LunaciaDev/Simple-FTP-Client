package com.lunaciadev.SimpleFTPClient;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.ui.MainScreen;

public class Main extends Game {
    private Screen currentScreen;
    private DataPackage dataPackage;

    @Override
    public void create() {
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        this.dataPackage = new DataPackage(skin);
        this.currentScreen = new MainScreen(dataPackage);
        this.screen = currentScreen;
    }

    @Override
    public void dispose() {
        
    }
}