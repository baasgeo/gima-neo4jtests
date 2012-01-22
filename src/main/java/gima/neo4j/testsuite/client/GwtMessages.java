/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import gima.neo4j.testsuite.shared.Messages;

/**
 *
 * @author bartbaas
 */
public class GwtMessages {

    private long startTime;
    private ScrollPanel scrollPanel;
    private HTML log;
    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while "
            + "attempting to contact the server. Please check your network "
            + "connection and try again.";
    private GwtServiceAsync gwtService = GWT.create(GwtService.class);

    public GwtMessages(ScrollPanel scrollPanel, HTML log) {
        this.scrollPanel = scrollPanel;
        this.log = log;
    }

    public void SendMessage(Messages.Type task, Messages.Db db) {
        SendMessage(task, db, null, false);
    }

    public void SendMessage(Messages.Type task, Messages.Db db, boolean store) {
        SendMessage(task, db, null, store);
    }

    public void SendMessage(Messages.Type task, Messages.Db db, double[][] obj) {
        SendMessage(task, db, obj, false);
    }

    public void SendMessage(Messages.Type task, Messages.Db db, double[][] obj, boolean store) {
        Log("<div class=green>Sending task '" + task.toString() + "'</div>");
        if (gwtService == null) {
            gwtService = GWT.create(GwtService.class);
        }

        AsyncCallback<String> callback = new AsyncCallback<String>() {

            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                Log("<div class=red>" + caught.getMessage() + "</div>");
            }

            public void onSuccess(String result) {
                // TODO: Do something
                Log(result);
                long endTime = System.currentTimeMillis();
                double difference = (1.0 * (endTime - startTime) / 1000.0); //check different
                Log("<div class=blue>Elapsed time: " + difference + " seconds.</div>");
                Log("<hr>");
            }
        };
        gwtService.SendTask(task, db, obj, store, callback);
        startTime = System.currentTimeMillis();
    }

    private void Log(String text) {
        log.setHTML(log.getHTML() + text);
        scrollPanel.scrollToBottom();
    }
}
