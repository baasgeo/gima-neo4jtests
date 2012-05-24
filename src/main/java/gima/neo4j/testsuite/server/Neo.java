/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.server;

import com.vividsolutions.jts.geom.Coordinate;
import gima.neo4j.testsuite.osmcheck.Parser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.geotools.data.neo4j.DefaultResourceInfo;

import org.neo4j.collections.rtree.Envelope;
import org.neo4j.collections.rtree.NullListener;
import org.neo4j.gis.spatial.ConsoleListener;
import org.neo4j.gis.spatial.DynamicLayerConfig;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.Utilities;
import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.gis.spatial.osm.OSMLayer;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.gis.spatial.pipes.osm.OSMGeoPipeline;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.batchinsert.BatchInserterImpl;
import org.neo4j.kernel.impl.transaction.TxModule;
import org.neo4j.kernel.impl.transaction.XaDataSourceManager;
import org.neo4j.kernel.impl.transaction.xaframework.XaDataSource;
import org.neo4j.kernel.impl.util.FileUtils;

/**
 *
 * @author bartbaas
 */
public class Neo {

    //public String layerName = "amsterdam";
    //public File basePath = new File(System.getProperty("user.home") + "/data/neodb");//("target/var");
    //public String osmfile = (System.getProperty("user.home") + "/data/osm/amsterdam.osm");//("target/var");
    //public File dbPath = new File(basePath, "amsterdam.gdb");
    public String layerName;
    public String osmfile;
    public File dbPath;
    public static final Map<String, String> NORMAL_CONFIG = new HashMap<String, String>();

    static {
        NORMAL_CONFIG.put("neostore.nodestore.db.mapped_memory", "50M");
        NORMAL_CONFIG.put("neostore.relationshipstore.db.mapped_memory", "120M");
        NORMAL_CONFIG.put("neostore.propertystore.db.mapped_memory", "150M");
        NORMAL_CONFIG.put("neostore.propertystore.db.strings.mapped_memory", "200M");
        NORMAL_CONFIG.put("neostore.propertystore.db.arrays.mapped_memory", "0M");
        NORMAL_CONFIG.put("dump_configuration", "false");
    }
    public static final Map<String, String> LARGE_CONFIG = new HashMap<String, String>();

    static {
        LARGE_CONFIG.put("neostore.nodestore.db.mapped_memory", "100M");
        LARGE_CONFIG.put("neostore.relationshipstore.db.mapped_memory", "300M");
        LARGE_CONFIG.put("neostore.propertystore.db.mapped_memory", "400M");
        LARGE_CONFIG.put("neostore.propertystore.db.strings.mapped_memory", "800M");
        LARGE_CONFIG.put("neostore.propertystore.db.arrays.mapped_memory", "10M");
        LARGE_CONFIG.put("dump_configuration", "true");
    }
    private static final Logger LOG = Logger.getLogger(DefaultResourceInfo.class.getName());
    public Map<String, String> mapConfig;
    private Boolean isRunning = false;
    private GraphDatabaseService graphService;
    private SpatialDatabaseService spatialService;
    private OSMLayer osmLayer;

    public Boolean isRunning() {
        return isRunning;
    }

    public String LayerName() {
        return layerName;
    }

    public OSMLayer osmLayer() {
        if (osmLayer != null) {
            return osmLayer;
        }
        if (spatialService.containsLayer(layerName)) {
            osmLayer = (OSMLayer) spatialService.getLayer(layerName);
            return osmLayer;
        }
        return null;
    }

    public Neo() {
    }

    private EmbeddedGraphDatabase embedDb() {
        return (EmbeddedGraphDatabase) graphService;
    }

    public String Start() {
        if (isRunning == true) {
            return "Database is already running.";
        }

        graphService = new EmbeddedGraphDatabase(dbPath.toString(), mapConfig);
        spatialService = new SpatialDatabaseService(graphService);
        registerShutdownHook(embedDb());
        isRunning = true;

        // get the XaDataSource for the native store
        TxModule txModule = embedDb().getConfig().getTxModule();
        XaDataSourceManager xaDsMgr = txModule.getXaDataSourceManager();
        XaDataSource xaDs = xaDsMgr.getXaDataSource("nioneodb");

        // turn off log rotation
        //xaDs.setAutoRotate(false);
        // increase log target size to 100MB (default 10MB)
        xaDs.setLogicalLogTargetSize(100 * 1024 * 1024L);


        return "Database started ...";
    }

    public String Stats() {
        if (isRunning == false) {
            return "You have to start the database first.";
        }
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        String results = "<i>Java statistics:</i>";
        results = results + "<br>Available memory: " + (maxMemory / 1048576) + " Mb";
        results = results + "<br>Allocated memory: " + (allocatedMemory / 1048576) + " Mb";
        results = results + "<br>Free memory: " + (freeMemory / 1048576) + " Mb";
        results = results + "<br>Memory in use: " + ((allocatedMemory - freeMemory) / 1048576) + " Mb";

        if (osmLayer() != null) {
            results = results + "<br>" + OSMStats.printDatabaseStats(osmLayer());
        } else {
            results = results + "<br>" + OSMStats.printDatabaseStats(spatialService);
        }
        return results;
    }

    public String ImportOSM(boolean extralayers) throws IOException, XMLStreamException, InterruptedException {
        Parser parser = new Parser(osmfile);
        parser.runExample();
        
        
        if (isRunning == false) {
            return "You have to start the database first.";
        } else {
            String osmPath = checkOSMFile(osmfile);
            if (osmPath == null) {
                return "Invalid osm file.";
            }

            String results = "<i>Importing osm data:</i>";
            results = results + "<br>Loading layer " + layerName + " from " + osmfile;
            System.out.println("\n=== Loading layer " + layerName + " from " + osmPath + " ===");
            long start = System.currentTimeMillis();

            gima.neo4j.testsuite.osmcheck.OSMImporter importer = new gima.neo4j.testsuite.osmcheck.OSMImporter(layerName, new ConsoleListener());
            //importer.setCharset(Charset.forName("UTF-8"));
            importer.importFile(graphService, osmPath, true, 5000);

            // Weird hack to force GC on large loads
            if (System.currentTimeMillis() - start > 300000) {
                for (int i = 0; i < 3; i++) {
                    System.gc();
                    Thread.sleep(1000);
                }
            }
            importer.reIndex(graphService, 5000, true, false);
            Stop();
            Start();
            
            if (extralayers) {
                OSMExports.addDynamicLayers(graphService, osmLayer());
            }
            
            results = results + "<br>" + OSMStats.printDatabaseStats(osmLayer());
            return results;
        }
    }

    public String ImportOSM_Batch(boolean extralayers) throws XMLStreamException, IOException, InterruptedException {
        if (isRunning == true) {
            return "You have to stop the database first.";
        } else {
            String osmPath = checkOSMFile(osmfile);
            if (osmPath == null) {
                return "Could not find the osm-file.";
            }

            Delete();            
            String results = "<i>Importing osm data (batch):</i>";
            results = results + "<br>Loading layer " + layerName + " from " + osmfile;
            System.out.println("\n=== B A T C H :: Loading layer " + layerName + " from " + osmPath + " ===");

            // The sequence here is: start batch inserter, import file, shutdown batch inserter, start database, reindex, shutdown database 
            gima.neo4j.testsuite.osmcheck.OSMImporter importer = new gima.neo4j.testsuite.osmcheck.OSMImporter(layerName, new ConsoleListener());
            //OSMImporter importer = new OSMImporter(layerName, new ConsoleListener());
            //importer.setCharset(Charset.forName("UTF-8"));

            long start = System.currentTimeMillis();
            BatchInserterImpl inserter = new BatchInserterImpl(dbPath.toString());
            importer.importFile(inserter, osmfile, true);
            long batchEnd = System.currentTimeMillis();
            results = results + "<br>Batchinserter took: " + (batchEnd - start) / 1000.0 + " sec";

            inserter.getGraphDbService().shutdown();
            graphService = new EmbeddedGraphDatabase(dbPath.toString(), mapConfig);
            importer.reIndex(graphService, 10000, true, false);
            long indexEnd = System.currentTimeMillis();
            results = results + "<br>Indexing took: " + (indexEnd - batchEnd) / 1000.0 + " sec";
            Stop();
            Start();

            if (extralayers) {
                OSMExports.addDynamicLayers(graphService, osmLayer());
            }
            
            results = results + "<br>" + OSMStats.printDatabaseStats((EmbeddedGraphDatabase)graphService);
            return results;
        }
    }

    private static String checkOSMFile(String osm) {
        File osmFile = new File(osm);
        if (!osmFile.exists()) {
            osmFile = new File(new File("osm"), osm);
            if (!osmFile.exists()) {
                return null;
            }
        }
        return osmFile.getPath();
    }

    public String findClosestNode(double[][] coords, boolean store) {
        if (isRunning == false) {
            return "You have to start the database first.";
        }
        DynamicLayerConfig dynLayer = osmLayer().addSimpleDynamicLayer(Constants.GTYPE_POINT);
        
        String results = "<i>Closest point results for " + layerName + ":</i>";
        for (int i = 0; i < coords.length; i++) {
            long start = System.currentTimeMillis();

            Coordinate coord = new Coordinate(coords[i][0], coords[i][1]); // Lon, Lat
            GeoPipeFlow pipeFlow = OSMTests.findClosestNode(coord, dynLayer);

            results = results + "<br>Input coordinate: " + coord.toString();
            results = results + "<br>Found closest node at distance: " + ((Double)pipeFlow.getProperty("OrthodromicDistance") * 1000.0);
            results = results + "<br>Node id is: " + pipeFlow.getId();
            results = results + "<br>Nearest neighbor coordinates: " + pipeFlow.getGeometry().getCoordinate().toString();

            long stop = System.currentTimeMillis();
            results = results + "<br>Operation took: " + (stop - start) + "ms";
            results = results + "<br>-----";
        }
        return results;
    }

    public String searchBbox(double[][] coords, Boolean exportimg) {
        if (isRunning == false) {
            return "You have to start the database first.";
        }

        String results = "<i>Bouding box results for " + layerName + ":</i>";
        for (int i = 0; i < coords.length; i++) {
            long start = System.currentTimeMillis();
            long stop;
            Envelope bbox = new Envelope(coords[i][0], coords[i][2], coords[i][3], coords[i][1]); // double minx, double maxx, double miny, double maxy
            if (exportimg == true) {
                OSMExports.exportImageSnippet(graphService, OSMTests.findGeometriesInLayer(osmLayer(), bbox), osmLayer().getName() + "_" + bbox.toString());
                stop = System.currentTimeMillis();
                results = results + "<br>Exported image to file: " + osmLayer().getName() + "_" + bbox.toString();
            } else {
                int numLine = 0;
                int numPoint = 0;
                int numPoly = 0;
                int numTotal = 0;

                for (GeoPipeFlow flow : OSMTests.findGeometriesInLayer(osmLayer(), bbox)) {
                    if (flow.getProperties().get("GeometryType") == "LineString") {
                        numLine++;
                    } else if (flow.getProperties().get("GeometryType") == "Point") {
                        numPoint++;
                    } else if (flow.getProperties().get("GeometryType") == "Polygon") {
                        numPoly++;
                    } else {
                        System.out.println(flow.getProperties().get("GeometryType"));
                    }
                    numTotal++;
                }
                results = results
                        + "<br>" + bbox.toString()
                        + "<br>Found geometries in bounding box: "
                        + "<br>Points: " + numPoint
                        + "<br>Lines: " + numLine
                        + "<br>Polygons: " + numPoly
                        + "<br>Total: " + numTotal;

                //results = results + "<br>Found geometries: " + OSMTests.findGeometriesInLayer(osmLayer(), bbox).count();
                stop = System.currentTimeMillis();
            }
            results = results + "<br>Operation took: " + (stop - start) + "ms";
            results = results + "<br>-----";
        }
        return results;
    }

        public String getGML(double[][] coords, Boolean exportimg) {
        if (isRunning == false) {
            return "You have to start the database first.";
        }
        String results = "<i>Bouding box results for " + layerName + ":</i><br>";
        
        try {
            FileWriter outFile = new FileWriter(System.getProperty("user.home") + "/data/" + layerName + ".gml");
            PrintWriter out = new PrintWriter(outFile);
            out.println("<?xml version='1.0' encoding='utf-8'?>");
            out.println("<gml xmlns='http://www.opengis.net/gml/3.2.1'>");
        
        String gml = "";
        for (int i = 0; i < coords.length; i++) {
            long start = System.currentTimeMillis();
            long stop;
            Envelope bbox = new Envelope(coords[i][0], coords[i][2], coords[i][3], coords[i][1]); // double minx, double maxx, double miny, double maxy

            for (GeoPipeFlow flow : OSMTests.findGeometriesInLayerToGML(osmLayer(), bbox)) {
                //gml = gml + flow.getProperties().get( "GML" );   
                out.println(flow.getProperties().get( "GML" ));
            }
            out.println("</gml>");
            gml = gml.replace("<", "&lt;");
            gml = gml.replace(">", "&gt;");

            //results = results + "<br>Found geometries: " + OSMTests.findGeometriesInLayer(osmLayer(), bbox).count();
            stop = System.currentTimeMillis();

            results = "<br>Operation took: " + (stop - start) + "ms";
            results = results + "<br>-----";
        }
                } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Neo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    public String DoTests() {
        //OSMLayer osmLayer = (OSMLayer) geoDb.getLayer(_layerName);
        com.vividsolutions.jts.geom.Envelope bbox = Utilities.fromNeo4jToJts(osmLayer.getIndex().getBoundingBox());

        System.out.println("Layer has bounding box: " + bbox);
        //geoStore = new Neo4jSpatialDataStore(graphDb);
        try {
            OSMTests.debugEnvelope(bbox, layerName, "bbox");
            OSMTests.checkIndexAndFeatureCount(osmLayer());
            OSMTests.checkOSMSearch(osmLayer());
        } catch (IOException ex) {
            //Logger.getLogger(SingletonMedemblik.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Done with the tests.";
    }

    public String AddDynamicLayers() {
        OSMExports.addDynamicLayers(graphService, osmLayer());
        return "There are now " + spatialService.getLayerNames().length + " layers in the dataset.";
    }

    public String ShortestPath(double[][] routes, boolean storeRoute) {
        String results = "<i>Routing results for " + layerName + ":</i>";

        Layer points = spatialService.getLayer(osmLayer().getName() + " - network points");

        for (int i = 0; i < routes.length; i++) {
            long start = System.currentTimeMillis();
            Coordinate startCoord = new Coordinate(routes[i][0], routes[i][1]); // Lon, Lat
            Coordinate endCoord = new Coordinate(routes[i][2], routes[i][3]);

            OSMRoute osmRoute = new OSMRoute(points, startCoord, endCoord, storeRoute);
            osmRoute.RouteLayerName(osmLayer().getName() + " - Route" + i);
            results = results + "<br>Found nearby startnode: " + osmRoute.StartNodeId() + " for " + startCoord.toString();
            results = results + "<br>Found nearby endnode: " + osmRoute.EndNodeId() + " for " + endCoord.toString();
            results = results + "<br>Node search time: " + osmRoute.NodeSearchTime() + "ms";
            results = results + "<br>" + osmRoute.dijkstra();
            if (storeRoute) {
                results = results + "<br>Route is stored in layer: " + osmLayer().getName() + " - Route" + i;
            }
            long stop = System.currentTimeMillis();
            results = results + "<br>Operation took: " + (stop - start) + "ms";
            results = results + "<br>-----";
        }
        return results;
    }

    public String SpatialJoin(double[][] polygon, Boolean exportimg) {
        //DISJOINT(the_geom, POLYGON((-90 40, -90 45, -60 45, -60 40, -90 40)))
        String results = "<i>Spatial join for " + layerName + ":</i>";
        
        long start = System.currentTimeMillis();
        long stop;
        try {
            GeoPipeline pipe = GeoPipeline.start(osmLayer()) 
                    .cqlFilter("name = Beckmanstraat")
                    //.cqlFilter("DISJOINT(buffer(the_geom, 20), POINT(4.88557 52.37674))")
                    .copyDatabaseRecordProperties();
            //DISJOINT(buffer(the_geom, 10) , POINT(1 2))

            System.out.println(pipe.toString());

            if (exportimg == true) {
                OSMExports.exportImageSnippet(graphService, pipe, osmLayer().getName() + "_disjoint");
                stop = System.currentTimeMillis();
                results = results + "<br>Exported image to file: " + osmLayer().getName() + "_disjoint";
            } else {
                List<GeoPipeFlow> list = pipe.toList();
                //System.out.println("Found close node at distance: " + closests.get(0).getProperty("OrthodromicDistance"));
                //System.out.println("Coordinates are: " + closests.get(0).getGeometry().getCoordinate().toString());
                for (GeoPipeFlow pipeFlow : list) {
                    for (String prop : pipeFlow.getPropertyNames()) {
                        if (pipeFlow.getProperty(prop) != null) {
                            System.out.println("\t" + prop + ":" + pipeFlow.getProperty(prop));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            return ex.toString();
        }
        return "";
    }

    public String MakeTopology() {
        String results = "<i>Creating routing network for " + layerName + ":</i>";
        try {
            System.out.println("Deleting layers...");
            osmLayer().removeDynamicLayer("routelines");
            spatialService.deleteLayer(osmLayer().getName() + " - network points", new NullListener());
            spatialService.deleteLayer(osmLayer().getName() + " - network edges", new NullListener());
        } catch (Exception ex) {
        }
        System.out.println("\tLayers: " + spatialService.getLayerNames().length);

        //DynamicLayerConfig routelayer = osmLayer().addLayerConfig("routelines", 2, "highway is not null and geometryType(the_geom) = 'LineString'");
        //findGeometriesInLayer(routelayer, bbox, true);

        NetworkGenerator networkGenerator = null;

        Transaction tx = graphService.beginTx();
        try {
            // new SearchAll()
            List<SpatialDatabaseRecord> list = OSMGeoPipeline
                    .startOsm(osmLayer())
                    .cqlFilter("highway is not null and highway not in ('cycleway','footway','pedestrain','service') and the_geom IS NOT NULL and geometryType(the_geom) = 'LineString'") //.cqlFilter("highway is not null and geometryType(the_geom) = 'LineString'")
                    .copyDatabaseRecordProperties()
                    .toSpatialDatabaseRecordList();

            int listCount = list.size();
            System.out.println("Found highway linestrings: " + listCount);
            results = results + "<br>Found highway linestrings: " + listCount;

            // create Network Points Layer
            EditableLayer netPointsLayer = spatialService.getOrCreateEditableLayer(osmLayer().getName() + " - network points");
            netPointsLayer.setCoordinateReferenceSystem(osmLayer().getCoordinateReferenceSystem());

            // create Network Edges Layer
            EditableLayer netEdgesLayer = spatialService.getOrCreateEditableLayer(osmLayer().getName() + " - network edges");
            netEdgesLayer.setCoordinateReferenceSystem(osmLayer().getCoordinateReferenceSystem());

            //Integer geomType = osmLayer().getGeometryType();
            networkGenerator = new NetworkGenerator(netPointsLayer, netEdgesLayer, 0.002); // Find nodes within 2 meter distance

            tx.success();
            tx.finish();

            Iterator<SpatialDatabaseRecord> it = list.iterator();
            while (it.hasNext()) {
                tx = graphService.beginTx();
                try {
                    int worked = 0;
                    for (int i = 0; i < 5000 && it.hasNext(); i++) {
                        networkGenerator.add(it.next());
                        worked++;
                    }
                    System.out.println("Processed " + worked + " of " + listCount);

                    tx.success();
                } finally {
                    tx.finish();
                }
            }
        } catch (Exception ex) {
            return (ex.toString());
        } finally {
            System.out.println("Finished created network linestrings, created " + networkGenerator.edgePointCounter() + " edgepoints");
            results = results + "<br>Finished created network linestrings, created " + networkGenerator.edgePointCounter() + " edgepoints";
        }
        return results;
    }

    public String ExportImages() {
        ArrayList<Layer> layers = (ArrayList<Layer>) osmLayer().getLayers();
        layers.remove(0); //remove the baselayer from the list
        OSMExports.exportImages(graphService, osmLayer(), layers);
        return "All layers are exported";

    }

    public void ClearData() {
        Transaction tx = graphService.beginTx();
        try {
            for (Node node : graphService.getAllNodes()) {
                for (Relationship rel : node.getRelationships()) {
                    rel.delete();
                }
                node.delete();
            }
            tx.success();
        } finally {
            tx.finish();
        }
        System.out.println("Database cleared ...");
    }

    public String Stop() {
        
        spatialService = null;
        graphService.shutdown();
        graphService = null;
        isRunning = false;
        osmLayer = null;
        return "Database shut down ...";
    }

    public String Delete() {
        if (isRunning == true) {
            Stop();
        }

        try {
            FileUtils.deleteRecursively(dbPath);
            return "Database deleted ...";
        } catch (IOException ex) {
            return ex.toString();
        }
    }

    private static void registerShutdownHook(final EmbeddedGraphDatabase graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }
}
