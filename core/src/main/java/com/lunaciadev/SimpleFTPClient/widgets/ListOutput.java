package com.lunaciadev.SimpleFTPClient.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;

public class ListOutput {
    private Label output;
    private ScrollPane scrollPane;
    private Table group;
    private Label currentDirectory;

    public ListOutput(DataPackage dataPackage) {
        output = new Label("", dataPackage.getSkin(), "mono");
        output.setWrap(true);
        output.setAlignment(Align.topLeft);

        group = new Table();
        currentDirectory = new Label("", dataPackage.getSkin(), "mono");
        currentDirectory.setAlignment(Align.left);

        HorizontalGroup temp = new HorizontalGroup();
        temp.grow();

        temp.addActor(new Label("Current Directory: ", dataPackage.getSkin()));
        temp.addActor(currentDirectory);

        Container<Label> container = new Container<>(output);
        container.pad(5).fill();

        scrollPane = new ScrollPane(container);

        group.add(temp).left();
        group.row();
        group.add(scrollPane).grow();
    }

    public Table getLayout() {
        return group;
    }

    /**
     * Slot, connected to {@link FTPClient#listCompleted}
     */
    public void addOutput(Object... args) {
        if (!(boolean) args[0]) return;

        output.setText((String) args[1]);
    }

    public void workingDirectoryChanged(Object... args) {
        if (!(boolean) args[0]) return;

        String currentDir = ((String) args[1]).substring(1, ((String) args[1]).length() - 1);
        currentDirectory.setText(currentDir);
    }
}
