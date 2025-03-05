package com.lunaciadev.SimpleFTPClient.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Queue;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;

public class ControlSocketOutput {
    private Label output;
    private ScrollPane scrollPane;
    private Queue<String> dataHistory;
    private int historySize;

    private final int MAX_HIST_SIZE = 100;

    public ControlSocketOutput(DataPackage dataPackage) {
        output = new Label("", dataPackage.getSkin());
        output.setWrap(true);
        scrollPane = new ScrollPane(output);

        dataHistory = new Queue<>(MAX_HIST_SIZE);
        historySize = 0;
    }

    public ScrollPane getLayout() {
        return scrollPane;
    }

    /**
     * Slot, connected to {@link FTPClient#ftpControlResponse}
     */
    public void addOutput(Object... args) {
        String data = ((String) args[0]);

        if (historySize < MAX_HIST_SIZE) {
            dataHistory.addLast(data);
        }
        else {
            dataHistory.removeFirst();
            dataHistory.addLast(data);
        }

        output.setText(dataHistory.toString("\n"));
    }
}
