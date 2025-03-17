package com.lunaciadev.SimpleFTPClient.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Align;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;

public class ListOutput {
    private Label output;
    private ScrollPane scrollPane;

    public ListOutput(DataPackage dataPackage) {
        output = new Label("", dataPackage.getSkin(), "mono");
        output.setWrap(true);
        output.setAlignment(Align.topLeft);

        Container<Label> container = new Container<>(output);
        container.pad(5).fill();

        scrollPane = new ScrollPane(container);
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
