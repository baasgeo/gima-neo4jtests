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

        TableMedemblik tableMedemblik = new TableMedemblik();   
        tableMedemblik.setSize("100%", "330px");
        tabDashboard.add(tabPanelMedemblik, tableMedemblik.Name(), false);
        tableMedemblik.SetupGui();
        tabPanelMedemblik.add(tableMedemblik);
        tabPanelMedemblik.add(tableMedemblik.LogPanel());
        
        TableAmsterdam tableAmsterdam = new TableAmsterdam(); 
        tableAmsterdam.setSize("100%", "330px");
        tabDashboard.add(tabPanelAmsterdam, tableAmsterdam.Name(), false);
        tableAmsterdam.SetupGui();
        tabPanelAmsterdam.add(tableAmsterdam);
        tabPanelAmsterdam.add(tableAmsterdam.LogPanel());
        
        TableNetherlands tableNl = new TableNetherlands(); 
        tableNl.setSize("100%", "330px");
        tabDashboard.add(tabPanelNl, tableNl.Name(), false);
        tableNl.SetupGui();
        tabPanelNl.add(tableNl);
        tabPanelNl.add(tableNl.LogPanel());

        tabDashboard.selectTab(0);
    }
}