package com.lunaciadev.SimpleFTPClient.data;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class DataPackage {
    private final Skin skin;

    public DataPackage(final Skin skin) {
        this.skin = skin;
    }

    public Skin getSkin() {
        return skin;
    }
}
