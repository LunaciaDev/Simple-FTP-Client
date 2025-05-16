package com.lunaciadev.SimpleFTPClient;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.ui.MainScreen;

/**
 * Application entry point.
 * 
 * @author LunaciaDev
 */
public class Main extends Game {
    private Screen currentScreen;
    private DataPackage dataPackage;

    @Override
    public void create() {
        final Skin skin = new Skin(Gdx.files.internal("ui/skin.json"));

        this.dataPackage = new DataPackage(skin);
        this.currentScreen = new MainScreen(dataPackage);
        this.screen = currentScreen;
    }

    @Override
    public void dispose() {
        screen.dispose();
    }
}