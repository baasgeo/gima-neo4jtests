package gima.neo4j.testsuite.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import gima.neo4j.testsuite.shared.Messages;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class tests implements EntryPoint {

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
        verticalPanel.setSize("600px", "600px");

        AbsolutePanel absolutePanel = new AbsolutePanel();
        absolutePanel.setSize("600px", "80px");
        verticalPanel.add(absolutePanel);

        Image imgNeo = new Image("img/neo4j_logo.jpg");
        absolutePanel.add(imgNeo, 50, 0);

        Image imgGima = new Image("img/gima_logo.jpg");
        absolutePanel.add(imgGima, 450, 0);

        Label lblMainLabel = new Label("Neo4j DashBoard");
        absolutePanel.add(lblMainLabel);
        lblMainLabel.setStyleName("gwt-MainLabel");
        lblMainLabel.setSize("400px", "100");

        TabPanel tabDashboard = new TabPanel();
        verticalPanel.add(tabDashboard);

        VerticalPanel tabPanelMedemblik = new VerticalPanel();
        VerticalPanel tabPanelAmsterdam = new VerticalPanel();
        VerticalPanel tabPanelNl = new VerticalPanel();

        GridTable tableMedemblik = new GridTable();
        tableMedemblik.name = "Medemblik";
        tableMedemblik.description = "Spatial tests on Open Street Map data from the Medemblik area";
        tableMedemblik.db = Messages.Db.MEDEMBLIK;
        tableMedemblik.routes = new double[][]{
            {5.09060, 52.76522, 5.10949, 52.76080},
            {5.11160, 52.77358, 5.10554, 52.76486}};
        tableMedemblik.bboxes = new double[][]{
            {5.09623, 52.77227, 5.10315, 52.76724}, //Meerlaan
            {5.10528, 52.76338, 5.10885, 52.76078}};
        tableMedemblik.points = new double[][]{
            {5.09060, 52.76522},
            {5.10949, 52.76080},
            {5.11160, 52.77358},
            {5.10554, 52.76486}};
        tableMedemblik.setSize("100%", "330px");
        tabDashboard.add(tabPanelMedemblik, tableMedemblik.Name(), false);
        tableMedemblik.SetupGui();
        tabPanelMedemblik.add(tableMedemblik);
        tabPanelMedemblik.add(tableMedemblik.LogPanel());

        GridTable tableAmsterdam = new GridTable();
        tableAmsterdam.name = "Amsterdam";
        tableAmsterdam.description = "Spatial tests on Open Street Map data from the Amsterdam area";
        tableAmsterdam.db = Messages.Db.AMSTERDAM;
        tableAmsterdam.routes = new double[][]{
            {4.8949, 52.3692, 4.8594, 52.3576},
            {4.8799, 52.3931, 4.8954, 52.3996},
            {4.8339, 52.3539, 4.9465, 52.3973},
            {4.9217, 52.3602, 4.9412, 52.3302}};
        tableAmsterdam.bboxes = new double[][]{
            {4.88557, 52.37674, 4.91214, 52.36694}, //Centre of Amsterdam
            {4.84779, 52.37478, 4.86592, 52.36498}, //Old West
            {4.94322, 52.34834, 4.96767, 52.33037}};
        tableAmsterdam.points = new double[][]{ 
            {4.8594, 52.3576},
            {4.8799, 52.3931},  
            {4.9465, 52.3973},
            {4.9412, 52.3302}};
        tableAmsterdam.setSize("100%", "330px");
        tabDashboard.add(tabPanelAmsterdam, tableAmsterdam.Name(), false);
        tableAmsterdam.SetupGui();
        tabPanelAmsterdam.add(tableAmsterdam);
        tabPanelAmsterdam.add(tableAmsterdam.LogPanel());

        GridTable tableNl = new GridTable();
        tableNl.name = "North-Holland";
        tableNl.description = "Spatial tests on Open Street Map data from North-Holland";
        tableNl.db = Messages.Db.NH;
        tableNl.routes = new double[][]{
            {4.8949, 52.3692, 4.8594, 52.3576},
            {4.8799, 52.3931, 4.8954, 52.3996},
            {4.8339, 52.3539, 4.9465, 52.3973},
            {4.9217, 52.3602, 4.9412, 52.3302}};
        tableNl.bboxes = new double[][]{
            {4.88557, 52.37674, 4.91214, 52.36694}, //Centre of Amsterdam
            {4.84779, 52.37478, 4.86592, 52.36498}, //Old West
            {4.94322, 52.34834, 4.96767, 52.33037}};
        tableNl.points = new double[][]{
            {4.8949, 52.3692}, {4.8594, 52.3576},
            {4.8799, 52.3931}, {4.8954, 52.3996},
            {4.8339, 52.3539}, {4.9465, 52.3973},
            {4.9217, 52.3602}, {4.9412, 52.3302}};
        tableNl.setSize("100%", "330px");
        tabDashboard.add(tabPanelNl, tableNl.Name(), false);
        tableNl.SetupGui();
        tabPanelNl.add(tableNl);
        tabPanelNl.add(tableNl.LogPanel());

        tabDashboard.selectTab(0);
    }
}