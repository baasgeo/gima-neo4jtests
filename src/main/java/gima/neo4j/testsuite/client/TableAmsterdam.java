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
public class TableAmsterdam extends Grid {

    private boolean toggle = true;
    private boolean store = false;
    private ScrollPanel scrollPanel = new ScrollPanel();
    private HTML log = new HTML();
    private GwtMessages messages;
    private String name = "Amsterdam";
    private String description = "Spatial tests on Open Street Map data from the Amsterdam area";
    private Messages.Db db = Messages.Db.AMSTERDAM;

    public TableAmsterdam() {
        this.messages = new GwtMessages(scrollPanel, log);
        this.resize(8, 4);
    }

    public String Name() {
        return name;
    }

    public void SetupGui() {
        CellFormatter cellFormatter = this.getCellFormatter();

        Label lblDatabase = new Label(this.name);
        lblDatabase.setStyleName("title");
        this.setWidget(0, 0, lblDatabase);
        cellFormatter.setStyleName(0, 0, "FlexTable-title");

        Button btnStart1 = new Button("Start database");
        btnStart1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.START, db);
            }
        });
        this.setWidget(1, 0, btnStart1);

        Label lblStart1 = new Label("Start a Neo4j instance at the server");
        lblStart1.setStyleName("summary");
        this.setWidget(1, 1, lblStart1);

        Button btnStats1 = new Button("Statistics");
        btnStats1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.STATISTICS, db);
            }
        });
        this.setWidget(2, 0, btnStats1);

        Label lblStats1 = new Label("Statistics about the database");
        lblStats1.setStyleName("summary");
        this.setWidget(2, 1, lblStats1);

        Button btnStop1 = new Button("Stop database");
        lblStart1.setStyleName("summary");
        btnStop1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.STOP, db);
            }
        });
        this.setWidget(3, 0, btnStop1);

        Label lblStop1 = new Label("Stop the Neo4j instance at the server");
        lblStop1.setStyleName("summary");
        this.setWidget(3, 1, lblStop1);

        Button btnDelete1 = new Button("Delete database");
        btnDelete1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.DELETE, db);
            }
        });
        this.setWidget(4, 0, btnDelete1);

        Label lblData = new Label("Data");
        lblData.setStyleName("title");
        this.setWidget(5, 0, lblData);
        cellFormatter.setStyleName(5, 0, "FlexTable-title");

        Button btnImport1 = new Button("Import data");
        btnImport1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.MAKE_OSM, db);
            }
        });
        this.setWidget(6, 0, btnImport1);

        Label lblImport1 = new Label("Import data from an OSM file");
        lblImport1.setStyleName("summary");
        this.setWidget(6, 1, lblImport1);

        Button btnDynLabels1 = new Button("Route network");
        btnDynLabels1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.MAKE_NETWORK, db);
            }
        });
        this.setWidget(7, 0, btnDynLabels1);

        Label lblDynLabels1 = new Label("Create network topology");
        lblDynLabels1.setStyleName("summary");
        this.setWidget(7, 1, lblDynLabels1);

        Label lblTests = new Label("Tests");
        lblTests.setStyleName("title");
        this.setWidget(0, 2, lblTests);
        cellFormatter.setStyleName(0, 2, "FlexTable-title");

        Button btnEmpty1 = new Button("Empty RPC call");
        btnEmpty1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.TEST_EMPTY, db);
            }
        });
        this.setWidget(1, 2, btnEmpty1);

        Label lblEmpty1 = new Label("Measure RPC time");
        lblEmpty1.setStyleName("summary");
        this.setWidget(1, 3, lblEmpty1);

        Button btnTest1 = new Button("Test1");
        btnTest1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                double[] bbox1 = {4.88557, 4.91214, 52.36694, 52.37674}; //Centre of Amsterdam
                double[] bbox2 = {4.84779, 4.86592, 52.36498, 52.37478}; //Old West

                if (toggle = true) {
                    messages.SendMessage(Messages.Type.TEST_BOUNDINGBOX, db, bbox1, store);
                } else {
                    messages.SendMessage(Messages.Type.TEST_BOUNDINGBOX, db, bbox2, store);
                }
                toggle = !toggle;
            }
        });
        this.setWidget(2, 2, btnTest1);

        Label lblTest1 = new Label("Send two bouding box queries to the server");
        lblTest1.setStyleName("summary");
        this.setWidget(2, 3, lblTest1);

        Button btnTest2 = new Button("Test2");
        btnTest2.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                messages.SendMessage(Messages.Type.TEST_EXPORTLAYERS, db);
            }
        });
        this.setWidget(3, 2, btnTest2);

        Label lblTest2 = new Label("Export dynamic layers to png");
        lblTest2.setStyleName("summary");
        this.setWidget(3, 3, lblTest2);

        Button btnTest3 = new Button("Route");
        btnTest3.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                double[] route = {4.8949258, 52.3692863, 4.8594936, 52.3576170};
                messages.SendMessage(Messages.Type.TEST_ROUTE, db, route, store);
            }
        });
        this.setWidget(4, 2, btnTest3);

        Label lblTest3 = new Label("Shortest path test");
        lblTest3.setStyleName("summary");
        this.setWidget(4, 3, lblTest3);

        Button btnTest4 = new Button("Test4");
        this.setWidget(5, 2, btnTest4);

        Label lblTest4 = new Label("New label");
        lblTest4.setStyleName("summary");
        this.setWidget(5, 3, lblTest4);

        Button btnTest5 = new Button("Test5");
        this.setWidget(6, 2, btnTest5);

        Label lblTest5 = new Label("New label");
        lblTest5.setStyleName("summary");
        this.setWidget(6, 3, lblTest5);
        
        CheckBox chkStore = new CheckBox("Store results");
        chkStore.addValueChangeHandler(new ValueChangeHandler() {

            public void onValueChange(ValueChangeEvent event) {
                store = ((CheckBox) event.getSource()).getValue();
            }
        }) ;
        this.setWidget(7, 2, chkStore);
        
        Label lblStore = new Label("Store the results of a particular test");
        lblStore.setStyleName("summary");
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
