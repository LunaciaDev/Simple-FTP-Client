package com.lunaciadev.SimpleFTPClient.widgets;

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
        progressBar = new ProgressBar(0, 1, 0.01f, false, dataPackage.getSkin());
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

    public void resetBar() {
        progressBar.setValue(0);
        progressValue.setText("Ready");
    }

    public void updateBar(float progress) {
        progressBar.setValue(progress);

        if (progress >= 1) progressValue.setText("Complete");
        else progressValue.setText(String.format("%.0f%", progress * 100));
    }
}
