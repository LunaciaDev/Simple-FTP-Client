package com.lunaciadev.SimpleFTPClient.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Queue;
import com.lunaciadev.SimpleFTPClient.core.FTPClient;
import com.lunaciadev.SimpleFTPClient.data.DataPackage;

/**
 * This widget provide a console-like view to the FTP commands and response that
 * is currently happening.
 * 
 * @author LunaciaDev
 */
public class ControlSocketOutput {
    private final Label output;
    private final ScrollPane scrollPane;
    private final Queue<String> dataHistory;
    private final int historySize;

    // How many line the console will "remember" before discarding old lines.
    private final int MAX_HIST_SIZE = 100;

    public ControlSocketOutput(final DataPackage dataPackage) {
        output = new Label("", dataPackage.getSkin(), "mono");
        output.setWrap(true);
        output.setAlignment(Align.topLeft);

        final Container<Label> container = new Container<>(output);
        container.pad(5).fill();

        scrollPane = new ScrollPane(container);

        dataHistory = new Queue<>(MAX_HIST_SIZE);
        historySize = 0;
    }

    public ScrollPane getLayout() {
        return scrollPane;
    }

    /**
     * Slot, connected to {@link FTPClient#ftpControlResponse}
     */
    public void addOutput(final Object... args) {
        final String data = ((String) args[0]);

        if (historySize < MAX_HIST_SIZE) {
            dataHistory.addLast(data);
        } else {
            dataHistory.removeFirst();
            dataHistory.addLast(data);
        }

        output.setText(dataHistory.toString("\n"));

        // Since this method is running on a different thread from GUI, we need to post
        // the ui-modifying part to the GUI thread.
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                scrollPane.setScrollY(scrollPane.getMaxY());
                scrollPane.updateVisualScroll();
            }
        });
    }
}
