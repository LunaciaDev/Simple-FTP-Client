package com.lunaciadev.SimpleFTPClient.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;

public class ProgressInfo {
    private Label progressValue;
    private HorizontalGroup group;

    public ProgressInfo(DataPackage dataPackage) {
        progressValue = new Label("", dataPackage.getSkin());
        group = new HorizontalGroup();
        setLayout();
    }

    private void setLayout() {
        group.pad(0, 0, 0, 5);

        group.addActor(progressValue);
    }

    public HorizontalGroup getLayout() {
        return group;
    }

    public void taskFinished(Object... args) {
        if (!(boolean) args[0]) {
            progressValue.setText("Failed");
        }
        else {
            progressValue.setText("Completed");
        }
    }
}
