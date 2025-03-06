package com.lunaciadev.SimpleFTPClient.widgets;

import java.nio.file.Files;
import java.nio.file.Path;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;

public class ProgressInfo {
    private ProgressBar progressBar;
    private Label progressValue;
    private HorizontalGroup group;

    public ProgressInfo(DataPackage dataPackage) {
        progressValue = new Label("", dataPackage.getSkin());
        progressBar = new ProgressBar(0, 1, 1, false, dataPackage.getSkin());
        group = new HorizontalGroup();
        setLayout();
    }

    private void setLayout() {
        group.pad(0, 0, 0, 5);

        group.addActor(progressBar);
        group.addActor(progressValue);

        progressBar.setAnimateDuration(0.2f);
    }

    public HorizontalGroup getLayout() {
        return group;
    }

    public void taskStarted(Object... args) {
        try {
            progressBar.setRange(0, Files.size((Path) args[0]));
        }
        catch (Exception e) {
            Gdx.app.error("Exception", e.getMessage());
        }
    }

    public void updateBar(Object... args) {
        progressBar.setValue(progressBar.getValue() + (int) args[0]);
        progressValue.setText(String.format("%.0f%%", progressBar.getValue() / progressBar.getMaxValue() * 100));
    }

    public void taskFinished(Object... args) {
        if (!(boolean) args[0]) {
            progressValue.setText("Failed");
        }
        else {
            progressValue.setText("Completed");
        }

        progressBar.setValue(0);
    }
}
