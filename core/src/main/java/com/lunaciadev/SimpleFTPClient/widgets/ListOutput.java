package com.lunaciadev.SimpleFTPClient.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;

public class ListOutput {
    private Label output;
    private ScrollPane scrollPane;

    public ListOutput(DataPackage dataPackage) {
        output = new Label("", dataPackage.getSkin());
        output.setWrap(true);
        scrollPane = new ScrollPane(output);
    }

    public ScrollPane getLayout() {
        return scrollPane;
    }

    public void addOutput(String data) {
        output.setText(data);
    }
}
