/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.server;

import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.File;
import java.math.BigDecimal;
import org.geotools.referencing.CRS;
import org.neo4j.collections.rtree.Envelope;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.osm.OSMLayer;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.nioneo.store.PropertyStore;

/**
 *
 * @author bartbaas
 */
public class Tools {
    
    protected static File getNeoPath(EmbeddedGraphDatabase graphDb) {
        return new File(graphDb.getStoreDir());
    }
   
    protected static EmbeddedGraphDatabase getGraphDb(SpatialDatabaseService geoDb) {
        return (EmbeddedGraphDatabase)geoDb.getDatabase();
    }
    
    protected static SpatialDatabaseService getGeoDb(OSMLayer osmLayer) {
        return (SpatialDatabaseService)osmLayer.getSpatialDatabase();
    }
    
    protected static long calculateDiskUsage(File file) {
        if(file.isDirectory()) {
            long count = 0;
            for(File sub:file.listFiles()) {
                count += calculateDiskUsage(sub);
            }
            return count;
        } else {
            return file.length();
        }
    }

    protected static long databaseDiskUsage(EmbeddedGraphDatabase graphDb) {
        return calculateDiskUsage(getNeoPath(graphDb));
    }

    protected static long countNodes(Class<?> cls, EmbeddedGraphDatabase graphDb) {
        return ((EmbeddedGraphDatabase)graphDb).getConfig().getGraphDbModule().getNodeManager().getNumberOfIdsInUse(cls);
    }
    
    protected static int countLayers(SpatialDatabaseService geoDb) {
        return geoDb.getLayerNames().length;

    }
    
    protected static String getLayerBoudingBox(OSMLayer osmLayer){
        GeometryFactory factory = osmLayer.getGeometryFactory();
        Envelope boundingBox = osmLayer.getIndex().getBoundingBox();
        return boundingBox.toString();
    }
    
    protected static String getCoordinateReferenceSystem(OSMLayer osmLayer) {
        return osmLayer.getCoordinateReferenceSystem().toString();
    }

    public static String printDatabaseStats(EmbeddedGraphDatabase graphDb) {
        BigDecimal bd = new BigDecimal((databaseDiskUsage(graphDb)/(1024.0*1024.0)));
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        
        String output =
        "<i>Database stats:</i>"
        + "<br>Total disk usage:     "+bd.longValue()+"MB"
        + "<br>Total # nodes:        "+countNodes(Node.class, graphDb)
        + "<br>Total # rels:         "+countNodes(Relationship.class, graphDb)
        + "<br>Total # props:        "+countNodes(PropertyStore.class, graphDb);
        return output;
    }
    
        public static String printDatabaseStats(SpatialDatabaseService geoDb) {
        EmbeddedGraphDatabase graphDb = getGraphDb(geoDb);
        
        String output =
        printDatabaseStats(graphDb)
        + "<br>Spatial stats:"
        + "<br>Total spatial layers: "+countLayers(geoDb);
        
        return output;
    }
        
    public static String printDatabaseStats(OSMLayer osmLayer) {
        SpatialDatabaseService geoDb = getGeoDb(osmLayer);
        
        String output =
        printDatabaseStats(geoDb)
        + "<br>Reference system:     "+getCoordinateReferenceSystem(osmLayer)
        + "<br>Unit of measure:      "+CRS.getEllipsoid(osmLayer.getCoordinateReferenceSystem()).getAxisUnit().toString()
        + "<br>Layer Bounding Box:   "+getLayerBoudingBox(osmLayer).toString();
        return output;
    }
    
}
