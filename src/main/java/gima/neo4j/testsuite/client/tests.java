package gima.neo4j.testsuite.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import gima.neo4j.testsuite.shared.Messages;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class tests implements EntryPoint {

    private ScrollPanel scrollPanel = new ScrollPanel();
    private long _lStartTime;
    private HTML log = new HTML();
    private boolean toggle = true;
    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while "
            + "attempting to contact the server. Please check your network "
            + "connection and try again.";
    private AmsterdamServiceAsync amsterdamService = GWT.create(AmsterdamService.class);

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        initRootPanel();
    }

    /**
     * This method build the components of the DashBoard.
     */
    private void initRootPanel() {
        RootPanel rootPanel = RootPanel.get();

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        rootPanel.add(verticalPanel, 10, 10);
        verticalPanel.setSize("600px", "500px");

        AbsolutePanel absolutePanel = new AbsolutePanel();
        verticalPanel.add(absolutePanel);

        Image imgNeo = new Image("img/neo4j_logo.jpg");
        absolutePanel.add(imgNeo, 50, 0);

        Image imgGima = new Image("img/gima_logo.jpg");
        absolutePanel.add(imgGima, 450, 0);

        Label lblMainLabel = new Label("Neo4j DashBoard");
        absolutePanel.add(lblMainLabel);
        lblMainLabel.setStyleName("gwt-MainLabel");
        lblMainLabel.setSize("400px", "100");

        TabPanel tabTest = new TabPanel();
        verticalPanel.add(tabTest);
        tabTest.setSize("100%", "400px");

        TabPanel tabLog = new TabPanel();
        verticalPanel.add(tabLog);
        tabLog.setSize("100%", "200px");

        tabLog.add(scrollPanel, "Log");
        scrollPanel.setSize("100%", "200px");
        scrollPanel.add(log);

        FlexTable flexTableMedemblik = new FlexTable();
        FlexCellFormatter cellFormatter = flexTableMedemblik.getFlexCellFormatter();
        tabTest.add(flexTableMedemblik, "Spatial Amsterdam", false);
        flexTableMedemblik.setSize("100%", "450px");

        Label lblDatabase = new Label("Instance");
        lblDatabase.setStyleName("title");
        flexTableMedemblik.setWidget(0, 0, lblDatabase);
        cellFormatter.setStyleName(0, 0, "FlexTable-title");

        Button btnStart1 = new Button("Start database");
        btnStart1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                SendMessage(Messages.Type.START);
            }
        });
        flexTableMedemblik.setWidget(1, 0, btnStart1);

        Label lblStart1 = new Label("Start a Neo4j instance at the server");
        lblStart1.setStyleName("summary");
        flexTableMedemblik.setWidget(1, 1, lblStart1);

        Button btnStats1 = new Button("Statistics");
        btnStats1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                SendMessage(Messages.Type.STATISTICS);
            }
        });
        flexTableMedemblik.setWidget(2, 0, btnStats1);

        Label lblStats1 = new Label("Statistics about the database");
        lblStats1.setStyleName("summary");
        flexTableMedemblik.setWidget(2, 1, lblStats1);

        Button btnStop1 = new Button("Stop database");
        lblStart1.setStyleName("summary");
        btnStop1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                SendMessage(Messages.Type.STOP);
            }
        });
        flexTableMedemblik.setWidget(3, 0, btnStop1);

        Label lblStop1 = new Label("Stop the Neo4j instance at the server");
        lblStop1.setStyleName("summary");
        flexTableMedemblik.setWidget(3, 1, lblStop1);

        Button btnDelete1 = new Button("Delete database");
        btnDelete1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                SendMessage(Messages.Type.DELETE);
            }
        });
        flexTableMedemblik.setWidget(4, 0, btnDelete1);

        Label lblData = new Label("Data");
        lblData.setStyleName("title");
        flexTableMedemblik.setWidget(5, 0, lblData);
        cellFormatter.setStyleName(5, 0, "FlexTable-title");

        Button btnImport1 = new Button("Import data");
        btnImport1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                SendMessage(Messages.Type.MAKE_OSM);
            }
        });
        flexTableMedemblik.setWidget(6, 0, btnImport1);

        Label lblImport1 = new Label("Import data from an OSM file");
        lblImport1.setStyleName("summary");
        flexTableMedemblik.setWidget(6, 1, lblImport1);

        Button btnDynLabels1 = new Button("Dynamic labels");
        btnDynLabels1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                SendMessage(Messages.Type.MAKE_NETWORK);
            }
        });
        flexTableMedemblik.setWidget(7, 0, btnDynLabels1);

        Label lblDynLabels1 = new Label("Create network topology");
        lblDynLabels1.setStyleName("summary");
        flexTableMedemblik.setWidget(7, 1, lblDynLabels1);

        Label lblTests = new Label("Tests");
        lblTests.setStyleName("title");
        flexTableMedemblik.setWidget(0, 2, lblTests);
        cellFormatter.setStyleName(0, 2, "FlexTable-title");

        Button btnEmpty1 = new Button("Empty RPC call");
        btnEmpty1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                SendMessage(Messages.Type.TEST_EMPTY);
            }
        });
        flexTableMedemblik.setWidget(1, 2, btnEmpty1);

        Label lblEmpty1 = new Label("Measure RPC time");
        lblEmpty1.setStyleName("summary");
        flexTableMedemblik.setWidget(1, 3, lblEmpty1);

        Button btnTest1 = new Button("Test1");
        btnTest1.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                double[] bbox1 = {4.88557, 4.91214, 52.36694, 52.37674}; //Centre of Amsterdam
                double[] bbox2 = {4.84779, 4.86592, 52.36498, 52.37478}; //Old West
                
                if (toggle = true) {
                SendMessage(Messages.Type.TEST_BOUNDINGBOX, bbox1); 
                }
                else {
                SendMessage(Messages.Type.TEST_BOUNDINGBOX, bbox2);
                }
                toggle = !toggle;
            }
        });
        flexTableMedemblik.setWidget(2, 2, btnTest1);

        Label lblTest1 = new Label("Send two bouding box queries to the server");
        lblTest1.setStyleName("summary");
        flexTableMedemblik.setWidget(2, 3, lblTest1);

        Button btnTest2 = new Button("Test2");
        btnTest2.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                SendMessage(Messages.Type.TEST_EXPORTLAYERS);
            }
        });
        flexTableMedemblik.setWidget(3, 2, btnTest2);

        Label lblTest2 = new Label("Export dynamic layers to png");
        lblTest2.setStyleName("summary");
        flexTableMedemblik.setWidget(3, 3, lblTest2);

        Button btnTest3 = new Button("Test3");
        btnTest3.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                SendMessage(Messages.Type.DOTESTS);
            }
        });
        flexTableMedemblik.setWidget(4, 2, btnTest3);

        Label lblTest3 = new Label("Do some random tests");
        lblTest3.setStyleName("summary");
        flexTableMedemblik.setWidget(4, 3, lblTest3);

        Button btnTest4 = new Button("Test4");
        flexTableMedemblik.setWidget(5, 2, btnTest4);

        Label lblTest4 = new Label("New label");
        lblTest4.setStyleName("summary");
        flexTableMedemblik.setWidget(5, 3, lblTest4);

        Button btnTest5 = new Button("Test5");
        flexTableMedemblik.setWidget(6, 2, btnTest5);

        Label lblTest5 = new Label("New label");
        lblTest5.setStyleName("summary");
        flexTableMedemblik.setWidget(6, 3, lblTest5);

        Label lblSeriousError = new Label("Serious Error");
        lblSeriousError.setStyleName("serverResponseLabelError");
        lblSeriousError.setVisible(false);
        flexTableMedemblik.setWidget(8, 0, lblSeriousError);

        FlexTable flexTableNL = new FlexTable();
        tabTest.add(flexTableNL, "Spatial Netherlands", false);
        flexTableNL.setSize("400px", "400px");

        tabTest.selectTab(0);
        tabLog.selectTab(0);
    }

    private void SendMessage(Messages.Type task) {
        SendMessage(task, null);
    }

    private void SendMessage(Messages.Type task, double[] obj) {
        Log("<div class=green>Sending task '" + task.toString() + "'</div>");
        if (amsterdamService == null) {
            amsterdamService = GWT.create(AmsterdamService.class);
        }

        AsyncCallback<String> callback = new AsyncCallback<String>() {

            public void onFailure(Throwable caught) {
                // TODO: Do something with errors.
                Log("<div class=red>" + caught.getMessage() + "</div>");
            }

            public void onSuccess(String result) {
                // TODO: Do something
                Log(result);
                long lEndTime = System.currentTimeMillis();
                double difference = (1.0 * (lEndTime - _lStartTime) / 1000.0); //check different
                Log("<div class=blue>Elapsed time: " + difference + " seconds.</div>");
                Log("<hr>");
            }
        };
        amsterdamService.SendTask(task, obj, callback);
        _lStartTime = System.currentTimeMillis();
    }

    private void Log(String text) {
        log.setHTML(log.getHTML() + text);
        scrollPanel.scrollToBottom();
        //textArea.setText(textArea.getText() + "\n"+ text);
        //textArea.getElement().setScrollTop(textArea.getElement().getScrollHeight());
    }
}