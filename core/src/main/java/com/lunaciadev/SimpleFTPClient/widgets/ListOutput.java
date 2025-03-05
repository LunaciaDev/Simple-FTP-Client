package com.lunaciadev.SimpleFTPClient.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;

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

    /**
     * Slot, connected to {@link FTPClient#listCompleted}
     */
    public void addOutput(Object... args) {
        if (!(boolean) args[0]) return;

        output.setText((String) args[1]);
    }
}
