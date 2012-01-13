/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.neo4j.DefaultResourceInfo;

import org.neo4j.collections.rtree.Envelope;
import org.neo4j.collections.rtree.filter.SearchAll;
import org.neo4j.collections.rtree.filter.SearchFilter;
import org.neo4j.gis.spatial.DynamicLayerConfig;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.LineStringNetworkGenerator;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.Utilities;
import org.neo4j.gis.spatial.osm.OSMDataset;
import org.neo4j.gis.spatial.osm.OSMGeometryEncoder;
import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.gis.spatial.osm.OSMLayer;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.gis.spatial.pipes.filtering.FilterCQL;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.batchinsert.BatchInserterImpl;
import org.neo4j.kernel.impl.util.FileUtils;

/**
 *
 * @author bartbaas
 */
public class SingletonAmsterdam {

    private static final Logger LOG = Logger.getLogger(DefaultResourceInfo.class.getName());
    private static final Map<String, String> NORMAL_CONFIG = new HashMap<String, String>();

    static {
        NORMAL_CONFIG.put("neostore.nodestore.db.mapped_memory", "50M");
        NORMAL_CONFIG.put("neostore.relationshipstore.db.mapped_memory", "120M");
        NORMAL_CONFIG.put("neostore.propertystore.db.mapped_memory", "150M");
        NORMAL_CONFIG.put("neostore.propertystore.db.strings.mapped_memory", "200M");
        NORMAL_CONFIG.put("neostore.propertystore.db.arrays.mapped_memory", "0M");
        NORMAL_CONFIG.put("dump_configuration", "false");
    }
    private static final Map<String, String> LARGE_CONFIG = new HashMap<String, String>();

    static {
        LARGE_CONFIG.put("neostore.nodestore.db.mapped_memory", "100M");
        LARGE_CONFIG.put("neostore.relationshipstore.db.mapped_memory", "300M");
        LARGE_CONFIG.put("neostore.propertystore.db.mapped_memory", "400M");
        LARGE_CONFIG.put("neostore.propertystore.db.strings.mapped_memory", "800M");
        LARGE_CONFIG.put("neostore.propertystore.db.arrays.mapped_memory", "10M");
        LARGE_CONFIG.put("dump_configuration", "true");
    }
    private static SingletonAmsterdam _singletonObj = new SingletonAmsterdam();
    private static Boolean _isRunning = false;
    private final File basePath = new File(System.getProperty("user.home") + "/data/neodb");//("target/var");
    private final String osmfile = (System.getProperty("user.home") + "/data/osm/amsterdam.osm");//("target/var");
    private final File dbPath = new File(basePath, "amsterdam.gdb");
    private GraphDatabaseService graphDb;
    private SpatialDatabaseService geoDb;
    private DataStore geoStore;
    private static OSMLayer osmLayer;
    private String _layerName = "Amsterdam";
    private Index<Node> nodeIndex;

    public static SingletonAmsterdam getInstance() {
        return _singletonObj;
    }

    public static Boolean isRunning() {
        return _isRunning;
    }

    public String LayerName() {
        return _layerName;
    }

    public OSMLayer osmLayer() {
        if (osmLayer != null) {
            return osmLayer;
        }
        if (geoDb.containsLayer(_layerName)) {
            osmLayer = (OSMLayer) geoDb.getLayer(_layerName);
            return osmLayer;
        }
        return null;
    }

    private SingletonAmsterdam() {
    }

    private EmbeddedGraphDatabase embedDb() {
        return (EmbeddedGraphDatabase) graphDb;
    }

    public String Start() {
        if (_isRunning == true) {
            return "Database is already running.";
        }
        graphDb = new EmbeddedGraphDatabase(dbPath.toString(), NORMAL_CONFIG);
        geoDb = new SpatialDatabaseService(graphDb);
        registerShutdownHook(embedDb());
        _isRunning = true;

        // Warming up the caches
        //if (osmLayer() != null) {
        //    Envelope bbox = osmLayer().getIndex().getBoundingBox();
        //    findGeometriesInLayer((EmbeddedGraphDatabase) graphDb, bbox, false).toString();
        //}
        if (osmLayer() != null) {
            OSMDataset osmDataset = (OSMDataset) osmLayer().getDataset();
            OSMGeometryEncoder osmGeometry = (OSMGeometryEncoder) osmDataset.getGeometryEncoder();
        }

        return "Database started ...";
    }

    public String Stats() {
        if (_isRunning == false) {
            return "You have to start the database first.";
        }

        if (osmLayer() != null) {
            return Tools.printDatabaseStats(osmLayer());
        } else {
            return Tools.printDatabaseStats(geoDb);
        }
    }

    public String ImportOSM() throws IOException, XMLStreamException, InterruptedException { //(String osmfile)
        if (_isRunning == false) {
            return "You have to start the database first.";
        } else {
            String osmPath = checkOSMFile(osmfile);
            if (osmPath == null) {
                return "Ïnvalid osm file.";
            }
            System.out.println("\n=== Loading layer " + _layerName + " from " + osmPath + " ===");
            long start = System.currentTimeMillis();

            OSMImporter importer = new OSMImporter(_layerName);
            //importer.setCharset(Charset.forName("UTF-8"));
            importer.importFile(graphDb, osmPath, false, 5000);

            // Weird hack to force GC on large loads
            if (System.currentTimeMillis() - start > 300000) {
                for (int i = 0; i < 3; i++) {
                    System.gc();
                    Thread.sleep(1000);
                }
            }
            importer.reIndex(graphDb, 1000, true, false);
            //checkOSMLayer(osm);
            return Tools.printDatabaseStats(osmLayer());
        }
    }

    public String ImportOSM_Batch() throws XMLStreamException, IOException, InterruptedException { //(String osmfile)
        if (_isRunning == true) {
            return "You have to stop the database first.";
        } else {
            Delete();
            // The sequence here is: start batch inserter, import file, shutdown batch inserter, start database, reindex, shutdown database 
            String osmPath = checkOSMFile(osmfile);
            if (osmPath == null) {
                return "Could not find the osm-file.";
            }

            OSMImporter importer = new OSMImporter(_layerName);
            BatchInserterImpl inserter = new BatchInserterImpl(dbPath.toString());
            importer.importFile(inserter, osmfile, false);
            inserter.getGraphDbService().shutdown();
            graphDb = new EmbeddedGraphDatabase(dbPath.toString());
            importer.reIndex(graphDb, 1000, true, false);
            Stop();
            Start();
            //OSMTests.checkOSMSearch(osmLayer());
            return Tools.printDatabaseStats(osmLayer());
        }
    }

    protected static String checkOSMFile(String osm) {
        File osmFile = new File(osm);
        if (!osmFile.exists()) {
            osmFile = new File(new File("osm"), osm);
            if (!osmFile.exists()) {
                return null;
            }
        }
        return osmFile.getPath();
    }

    public String testSearchPoints(double[] coord, Boolean exportimg) {
        if (_isRunning == false) {
            return "You have to start the database first.";
        }
        Envelope bbox = new Envelope(coord[0], coord[1], coord[2], coord[3]); //double minx, double maxx, double miny, double maxy
        return findGeometriesInLayer(osmLayer(), bbox, exportimg).toString();
    }

    public String DoTests() {
        //OSMLayer osmLayer = (OSMLayer) geoDb.getLayer(_layerName);
        com.vividsolutions.jts.geom.Envelope bbox = Utilities.fromNeo4jToJts(osmLayer.getIndex().getBoundingBox());

        System.out.println("Layer has bounding box: " + bbox);
        //geoStore = new Neo4jSpatialDataStore(graphDb);
        try {
            OSMTests.debugEnvelope(bbox, _layerName, "bbox");
            OSMTests.checkIndexAndFeatureCount(osmLayer());
            OSMTests.checkOSMSearch(osmLayer());
        } catch (IOException ex) {
            //Logger.getLogger(SingletonMedemblik.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Done with the tests.";
    }

    public String AddDynamicLayers() {
        OSMExports.addDynamicLayers(graphDb, osmLayer());
        return "There are now " + geoDb.getLayerNames().length + " layers in the dataset.";
    }

    public String MakeTopology() {
        double[] coord = {4.84779, 4.86592, 52.36498, 52.37478};
        Envelope bbox = new Envelope(coord[0], coord[1], coord[2], coord[3]);

        boolean removeDynamicLayer = osmLayer().removeDynamicLayer("routelines");
        DynamicLayerConfig routelayer = osmLayer().addLayerConfig("routelines", 2, "highway is not null and geometryType(the_geom) = 'LineString'");
        
        //findGeometriesInLayer(routelayer, bbox, true);

        LineStringNetworkGenerator networkGenerator;

        Transaction tx = graphDb.beginTx();
        try {
            // TODO put these layer nodes in relationship?

            // create Network Points Layer
            EditableLayer netPointsLayer = geoDb.getOrCreateEditableLayer(osmLayer().getName() + " - network points");
            netPointsLayer.setCoordinateReferenceSystem(osmLayer().getCoordinateReferenceSystem());

            // create Network Edges Layer
            EditableLayer netEdgesLayer = geoDb.getOrCreateEditableLayer(osmLayer().getName() + " - network edges");
            netEdgesLayer.setCoordinateReferenceSystem(osmLayer().getCoordinateReferenceSystem());

            Integer geomType = osmLayer().getGeometryType();

            networkGenerator = new LineStringNetworkGenerator(netPointsLayer, netEdgesLayer);

            tx.success();
        } finally {
            tx.finish();
        }

        tx = graphDb.beginTx();
        try {
            osmLayer().getIndex().count();
            tx.success();
        } finally {
            tx.finish();
        }

        try {
            List<SpatialDatabaseRecord> results = GeoPipeline
                                        .start(osmLayer(), new SearchAll())
                                        .cqlFilter("highway is not null and geometryType(the_geom) = 'LineString'" )
                                        .toSpatialDatabaseRecordList();
            System.out.println("Iterator has: " + results.size());

            Iterator<SpatialDatabaseRecord> it = results.iterator();
            while (it.hasNext()) {
                tx = graphDb.beginTx();
                try {
                    int worked = 0;
                    for (int i = 0; i < 1000 && it.hasNext(); i++) {
                        networkGenerator.add(it.next());
                        worked++;
                    }

                    tx.success();
                } finally {
                    tx.finish();
                }
            }
        } catch (Exception ex) {
            return (ex.toString());
        } finally {
        }
        return "Not implemented yet";
    }

    public String ExportImages() {
        //OSMLayer osmLayer = (OSMLayer) geoDb.getLayer(_layerName);

        //StyledImageExporter imageExporter = new StyledImageExporter(graphDb);
        //imageExporter.setExportDir(System.getProperty("user.home") + "/data/export/" + osmLayer.getName());
        //imageExporter.setZoom(1.0);
        //imageExporter.setOffset(-0.05, -0.05);
        //imageExporter.setSize(1024, 768);
        //try {
        //    imageExporter.saveLayerImage(_layerName);
        //} catch (IOException ex) {
        //    // Nothing
        //}

        ArrayList<Layer> layers = (ArrayList<Layer>) osmLayer().getLayers();
        layers.remove(0); //remove the baselayer from the list
        OSMExports.exportImages(graphDb, osmLayer(), layers);
        return "All layers are exported";

    }

    private List<Node> findGeometriesInLayer(Layer layer, Envelope envelope, Boolean exportimg) {
        //Layer layer = geoDb.getDynamicLayer(layerName);
        //if (layer == null) {
        //    layer = geoDb.getLayer(layerName);
        //}

        com.vividsolutions.jts.geom.Envelope bbox = Utilities.fromNeo4jToJts(envelope);
        // TODO why a SearchWithin and not a SearchIntersectWindow?
        //return GeoPipeline.startWithinSearch(layer, layer.getGeometryFactory().toGeometry(envelope)).toNodeList();
        GeoPipeline pipeline = GeoPipeline.startWithinSearch(layer, layer.getGeometryFactory().toGeometry(bbox));
        if (exportimg == true) {
            OSMExports.exportImageSnippet(graphDb, pipeline, layer.getName() + "_" + bbox.toString());
        }
        return pipeline.toNodeList();
    }

    public void ClearData() {
        Transaction tx = graphDb.beginTx();
        try {
            for (Node node : graphDb.getAllNodes()) {
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
        graphDb.shutdown();
        _isRunning = false;
        osmLayer = null;
        return "Database shut down ...";
    }

    public String Delete() {
        Stop();
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
                _isRunning = false;
            }
        });
    }
}
