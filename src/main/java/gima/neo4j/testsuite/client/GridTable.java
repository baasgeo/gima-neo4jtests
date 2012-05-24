/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import gima.neo4j.testsuite.shared.Messages;

/**
 *
 * @author bartbaas
 */
public class GridTable extends Grid {

    private boolean store = false;
    private ScrollPanel scrollPanel = new ScrollPanel();
    private HTML log = new HTML();
    private GwtMessages messages;
    
    public String name;
    public String description;
    public Messages.Db db;
    
    public double[][] routes;    
    public double[][] bboxes;    
    public double[][] points;
    public double[][] joinpolygon;

    public GridTable() {
        this.messages = new GwtMessages(scrollPanel, log);
        this.resize(8, 4);
    }

    public String Name() {
        return name;
    }

    public void SetupGui() {
        CellFormatter cellFormatter = this.getCellFormatter();

        Label lblDatabase = new Label(this.name);
        lblDatabase.setStyleName(Constants.Style.TITLE);
        this.setWidget(0, 0, lblDatabase);
        cellFormatter.setStyleName(0, 0, Constants.Style.FLEXTABLETITLE);

        Button btnStart1 = new Button(Constants.START);
        btnStart1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.START, db);
            }
        });
        this.setWidget(1, 0, btnStart1);

        Label lblStart1 = new Label(Constants.Labels.START);
        lblStart1.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(1, 1, lblStart1);

        Button btnStats1 = new Button(Constants.STATISTICS);
        btnStats1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.STATISTICS, db);
            }
        });
        this.setWidget(2, 0, btnStats1);

        Label lblStats1 = new Label(Constants.Labels.STATS);
        lblStats1.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(2, 1, lblStats1);

        Button btnStop1 = new Button(Constants.STOP);
        lblStart1.setStyleName(Constants.Style.SUMMARY);
        btnStop1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.STOP, db);
            }
        });
        this.setWidget(3, 0, btnStop1);

        Label lblStop1 = new Label(Constants.Labels.STOP);
        lblStop1.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(3, 1, lblStop1);

        Button btnDelete1 = new Button(Constants.DELETE);
        btnDelete1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.DELETE, db);
            }
        });
        this.setWidget(4, 0, btnDelete1);

        Label lblDelete1 = new Label(Constants.Labels.DELETE);
        lblDelete1.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(4, 1, lblDelete1);

        Label lblData = new Label(Constants.Labels.DATA);
        lblData.setStyleName(Constants.Style.TITLE);
        this.setWidget(5, 0, lblData);
        cellFormatter.setStyleName(5, 0, Constants.Style.FLEXTABLETITLE);

        Button btnImport1 = new Button(Constants.IMPORT);
        btnImport1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.MAKE_OSM, db, store);
            }
        });
        this.setWidget(6, 0, btnImport1);

        Label lblImport1 = new Label(Constants.Labels.IMPORT);
        lblImport1.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(6, 1, lblImport1);

        Button btnDynLabels1 = new Button(Constants.NETWORK);
        btnDynLabels1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.MAKE_NETWORK, db);
            }
        });
        this.setWidget(7, 0, btnDynLabels1);

        Label lblNetwork1 = new Label(Constants.Labels.NETWORK);
        lblNetwork1.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(7, 1, lblNetwork1);

        Label lblTests = new Label(Constants.Labels.TESTS);
        lblTests.setStyleName(Constants.Style.TITLE);
        this.setWidget(0, 2, lblTests);
        cellFormatter.setStyleName(0, 2, Constants.Style.FLEXTABLETITLE);

        Button btnEmpty1 = new Button(Constants.EMPTY);
        btnEmpty1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.TEST_EMPTY, db);
            }
        });
        this.setWidget(1, 2, btnEmpty1);

        Label lblEmpty1 = new Label(Constants.Labels.EMPTY);
        lblEmpty1.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(1, 3, lblEmpty1);

        Button btnTest1 = new Button(Constants.TEST1);
        btnTest1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.TEST_BOUNDINGBOX, db, bboxes, store);
            }
        });
        this.setWidget(2, 2, btnTest1);

        Label lblTest1 = new Label(Constants.Labels.TEST1);
        lblTest1.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(2, 3, lblTest1);

        Button btnTest2 = new Button(Constants.TEST2);
        btnTest2.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.TEST_CLOSEPOINT, db, points);
            }
        });
        this.setWidget(3, 2, btnTest2);

        Label lblTest2 = new Label(Constants.Labels.TEST2);
        lblTest2.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(3, 3, lblTest2);

        Button btnTest3 = new Button(Constants.TEST3);
        btnTest3.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.TEST_ROUTE, db, routes, store);
            }
        });
        this.setWidget(4, 2, btnTest3);

        Label lblTest3 = new Label(Constants.Labels.TEST3);
        lblTest3.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(4, 3, lblTest3);

        Button btnTest4 = new Button(Constants.TEST4);
        btnTest4.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.TEST_GML, db, bboxes, store);
            }
        });
        this.setWidget(5, 2, btnTest4);

        Label lblTest4 = new Label(Constants.Labels.TEST4);
        lblTest4.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(5, 3, lblTest4);

        Button btnTest5 = new Button(Constants.TEST5);
        this.setWidget(6, 2, btnTest5);

        Label lblTest5 = new Label(Constants.Labels.TEST5);
        lblTest5.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(6, 3, lblTest5);

        CheckBox chkStore = new CheckBox(Constants.STORE);
        chkStore.addValueChangeHandler(new ValueChangeHandler() {

            public void onValueChange(ValueChangeEvent event) {
                store = ((CheckBox) event.getSource()).getValue();
            }
        });
        this.setWidget(7, 2, chkStore);

        Label lblStore = new Label(Constants.Labels.STORE);
        lblStore.setStyleName(Constants.Style.SUMMARY);
        this.setWidget(7, 3, lblStore);
    }

    public TabPanel LogPanel() {
        TabPanel tabLog = new TabPanel();
        tabLog.setSize("100%", "300px");

        tabLog.add(scrollPanel, "Log");
        scrollPanel.setSize("100%", "300px");
        scrollPanel.add(log);

        tabLog.selectTab(0);
        return tabLog;
    }
}
