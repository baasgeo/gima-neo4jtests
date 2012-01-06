/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.server;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import java.util.List;
import org.geotools.data.DataStore;
import org.geotools.data.neo4j.Neo4jSpatialDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.osm.OSMDataset;
import org.neo4j.gis.spatial.osm.OSMDataset.Way;
import org.neo4j.gis.spatial.osm.OSMLayer;
import org.neo4j.gis.spatial.pipes.osm.OSMGeoPipeline;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author bartbaas
 */
public class OSMTests {

    public static void checkOSMSearch(OSMLayer layer) throws IOException {
        OSMDataset osm = (OSMDataset) layer.getDataset();
        Way way = null;
        int count = 0;
        for (Way wayNode : osm.getWays()) {
            way = wayNode;
            if (count++ > 100) {
                break;
            }
        }
        System.out.println("Number of ways: " + count);
        Envelope bbox = way.getEnvelope();
        runSearches(layer, bbox, true);
        org.neo4j.collections.rtree.Envelope layerBBox = layer.getIndex().getBoundingBox();
        double[] centre = layerBBox.centre();
        double width = layerBBox.getWidth() / 100.0;
        double height = layerBBox.getHeight() / 100.0;
        bbox = new Envelope(centre[0] - width, centre[0] + width, centre[1] - height, centre[1] + height);
        runSearches(layer, bbox, false);
    }

    private static void runSearches(OSMLayer layer, Envelope bbox, boolean willHaveResult) {
        for (int i = 0; i < 4; i++) {
            Geometry searchArea = layer.getGeometryFactory().toGeometry(bbox);
            runWithinSearch(layer, searchArea, willHaveResult);
            bbox.expandBy(bbox.getWidth(), bbox.getHeight());
        }
    }

    private static void runWithinSearch(OSMLayer layer, Geometry searchArea, boolean willHaveResult) {
        long start = System.currentTimeMillis();
        List<SpatialDatabaseRecord> results = OSMGeoPipeline.startWithinSearch(layer, searchArea).toSpatialDatabaseRecordList();
        long time = System.currentTimeMillis() - start;
        System.out.println("Took " + time + "ms to find " + results.size() + " search results in layer " + layer.getName()
                + " using search within " + searchArea);
        if (willHaveResult) {
            System.out.println("Should be at least one result, but got zero " + results.size());
        }
    }

    public static void debugEnvelope(Envelope bbox, String layer, String name) {
        System.out.println("Layer '" + layer + "' has envelope '" + name + "': " + bbox);
        System.out.println("\tX: [" + bbox.getMinX() + ":" + bbox.getMaxX() + "]");
        System.out.println("\tY: [" + bbox.getMinY() + ":" + bbox.getMaxY() + "]");
    }

    public static void checkIndexAndFeatureCount(Layer layer) throws IOException {
        if (layer.getIndex().count() < 1) {
            System.out.println("Warning: index count zero: " + layer.getName());
        }
        System.out.println("Layer '" + layer.getName() + "' has " + layer.getIndex().count() + " entries in the index");
        GraphDatabaseService service = layer.getSpatialDatabase().getDatabase();
        DataStore store = new Neo4jSpatialDataStore(service);
        SimpleFeatureSource featureSource = store.getFeatureSource((String)layer.getName());        
        SimpleFeatureCollection features = featureSource.getFeatures();
        //TODO fix this...
        //System.out.println("Layer '" + layer.getName() + "' has " + features.size() + " features");
        //assertEquals("FeatureCollection.size for layer '" + layer.getName() + "' not the same as index count", layer.getIndex().count(), features.size());
    }
}
