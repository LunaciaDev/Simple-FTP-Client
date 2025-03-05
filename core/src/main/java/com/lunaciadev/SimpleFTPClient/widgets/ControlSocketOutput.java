package com.lunaciadev.SimpleFTPClient.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Queue;
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
     * Add a string to the control output.
     * One line at a time!
     * 
     * @param data
     */
    public void addOutput(String data) {
        data = data.strip();

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
