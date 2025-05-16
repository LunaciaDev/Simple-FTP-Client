package com.lunaciadev.SimpleFTPClient.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;

/**
 * This widget provide the textual view to the LIST command output.
 * 
 * @author LunaciaDev
 */
public class ListOutput {
    private final Table group;
    private final Label currentDirectory;
    private final ScrollPane scrollPane;
    private final Label output;

    public ListOutput(final DataPackage dataPackage) {
        output = new Label("", dataPackage.getSkin(), "mono");
        output.setWrap(true);
        output.setAlignment(Align.topLeft);

        group = new Table();
        currentDirectory = new Label("", dataPackage.getSkin(), "mono");
        currentDirectory.setAlignment(Align.left);

        final HorizontalGroup temp = new HorizontalGroup();
        temp.grow();

        temp.addActor(new Label("Current Directory: ", dataPackage.getSkin()));
        temp.addActor(currentDirectory);

        final Container<Label> container = new Container<>(output);
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
    public void addOutput(final Object... args) {
        if (!(boolean) args[0]) return;

        output.setText((String) args[1]);
    }

    /**
     * Slot, connected to {@link FTPClient#currentDirectoryCompleted}
     */
    public void workingDirectoryChanged(final Object... args) {
        if (!(boolean) args[0]) return;

        final String currentDir = ((String) args[1]).substring(1, ((String) args[1]).length() - 1);
        currentDirectory.setText(currentDir);
    }
}
