package com.lunaciadev.SimpleFTPClient;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lunaciadev.SimpleFTPClient.core.FTP;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private boolean inProgress;
    private FTP ftp;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        inProgress = false;
    }

    @Override
    public void render() {
        if (inProgress) return;

        inProgress = true;

        ftp = new FTP();
        ftp.connectCompleted.connect(this::connectCallback);
        ftp.authenticateCompleted.connect(this::loginCallback);
        ftp.listCompleted.connect(this::listCallback);
        ftp.quitCompleted.connect(this::quitCallback);

        ftp.connect("ftp.dlptest.com", null);
    }

    private void quitCallback(Object... args) {
        Gdx.app.exit();
    }

    private void listCallback(Object... args) {
        if (!(boolean) args[0]) {
            Gdx.app.log("INFO", "List unsuccessful");
            Gdx.app.exit();
        }

        Gdx.app.log("INFO", (String) args[1]);
        ftp.quit();
    }

    private void connectCallback(Object... args) {
        if (!(boolean) args[0]) {
            Gdx.app.log("INFO", "Connect Unsuccessful");
            Gdx.app.exit();
        }

        Gdx.app.log("INFO", "Connect successful");
        ftp.authenticate("dlpuser", "rNrKYTX9g7z3RgJRmxWuGHbeu");
    }

    private void loginCallback(Object... args) {
        if (!(boolean) args[0]) {
            Gdx.app.log("INFO", "Login Unsuccessful");
            Gdx.app.exit();
        }

        Gdx.app.log("INFO", "Login successful");
        ftp.list();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
