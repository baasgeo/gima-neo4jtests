/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.client;

/**
 *
 * @author bartbaas
 */
public interface Constants {

    static String DELETE = "Delete database";
    static String EMPTY = "Empty RPC call";
    static String IMPORT = "Import data";
    static String NETWORK = "Route network";
    static String START = "Start database";
    static String STATISTICS = "Statistics";
    static String STOP = "Stop database";
    static String STORE = "Store results";
    static String TEST1 = "Bounding box";
    static String TEST2 = "Closest point";
    static String TEST3 = "Route";
    static String TEST4 = "GML request";
    static String TEST5 = "Test5";

    public class Labels {

        static String DATA = "Data";
        static String DELETE = "Delete database";
        static String STATS = "Statistics about the database";
        static String STOP = "Stop the Neo4j instance at the server";
        static String START = "Start a Neo4j instance at the server";
        static String NETWORK = "Create network topology";
        static String IMPORT = "Import data from an OSM file";
        static String EMPTY = "Measure RPC time";
        static String STORE = "Store the results of a runned test";
        static String TESTS = "Tests";
        static String TEST1 = "Send bouding box queries to the server";
        static String TEST2 = "Find the closest node in the network from lat/lon";
        static String TEST3 = "Shortest path test";
        static String TEST4 = "Get GML data from bouding box";
        static String TEST5 = "New label";
    }

    public class Style {

        static String FLEXTABLETITLE = "FlexTable-title";
        static String SUMMARY = "summary";
        static String TITLE = "title";
    }
}
