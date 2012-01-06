/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.server;

import java.util.ArrayList;

import org.geotools.data.DataSourceException;
import org.neo4j.collections.rtree.Envelope;
import org.neo4j.gis.spatial.osm.OSMLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import org.geotools.data.neo4j.Neo4jFeatureBuilder;
import org.geotools.data.neo4j.StyledImageExporter;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.styling.Style;
import org.neo4j.collections.rtree.filter.SearchAll;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.gis.spatial.pipes.osm.OSMGeoPipeline;
import org.neo4j.graphdb.GraphDatabaseService;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 *
 * @author bartbaas
 */
public class OSMExports extends OSMTests {
    // OpenGIS geometry type numbers 

    static int GTYPE_GEOMETRY = 0;
    static int GTYPE_POINT = 1;
    static int GTYPE_LINESTRING = 2;
    static int GTYPE_POLYGON = 3;
    static int GTYPE_MULTIPOINT = 4;
    static int GTYPE_MULTILINESTRING = 5;
    static int GTYPE_MULTIPOLYGON = 6;

    public static void addDynamicLayers(GraphDatabaseService graphDb, OSMLayer osmLayer) {
        Envelope bbox = osmLayer.getIndex().getBoundingBox();
        System.out.println("Layer has bounding box: " + bbox);

        // Define dynamic layers
        ArrayList<Layer> layers = new ArrayList<Layer>();
        //SpatialDatabaseService spatialService = new SpatialDatabaseService(graphDb);
        //OSMLayer osmLayer = (OSMLayer) spatialService.getLayer(osmFile);
        LinearRing ring = osmLayer.getGeometryFactory().createLinearRing(
                new Coordinate[]{new Coordinate(bbox.getMinX(), bbox.getMinY()), new Coordinate(bbox.getMinX(), bbox.getMaxY()),
                    new Coordinate(bbox.getMaxX(), bbox.getMaxY()), new Coordinate(bbox.getMaxX(), bbox.getMinY()),
                    new Coordinate(bbox.getMinX(), bbox.getMinY())});
        //Polygon polygon = osmLayer.getGeometryFactory().createPolygon(ring, null);
        layers.add(osmLayer.addLayerConfig("CQL1-highway", GTYPE_LINESTRING, "highway is not null and geometryType(the_geom) = 'LineString'"));
        layers.add(osmLayer.addLayerConfig("CQL2-residential", GTYPE_LINESTRING, "highway = 'residential' and geometryType(the_geom) = 'LineString'"));
        layers.add(osmLayer.addLayerConfig("CQL3-natural", GTYPE_POLYGON, "natural is not null and geometryType(the_geom) = 'Polygon'"));
        /**layers.add(osmLayer.addLayerConfig("CQL4-water", GTYPE_POLYGON, "natural = 'water' and geometryType(the_geom) = 'Polygon'"));
        layers.add(osmLayer.addLayerConfig("CQL5-bbox", GTYPE_GEOMETRY, "BBOX(the_geom, " + toCoordinateText(bbox) + ")"));
        layers.add(osmLayer.addLayerConfig("CQL6-bbox-polygon", GTYPE_GEOMETRY, "within(the_geom, POLYGON(("
                + toCoordinateText(polygon.getCoordinates()) + ")))"));
        layers.add(osmLayer.addSimpleDynamicLayer("highway", "primary"));
        layers.add(osmLayer.addSimpleDynamicLayer("highway", "secondary"));
        layers.add(osmLayer.addSimpleDynamicLayer("highway", "tertiary"));
        layers.add(osmLayer.addSimpleDynamicLayer(GTYPE_LINESTRING, "highway=*"));
        layers.add(osmLayer.addSimpleDynamicLayer(GTYPE_LINESTRING, "highway=footway, bicycle=yes"));
        layers.add(osmLayer.addSimpleDynamicLayer("highway=*, bicycle=yes"));
        layers.add(osmLayer.addSimpleDynamicLayer("highway", "residential"));
        layers.add(osmLayer.addCQLDynamicLayerOnAttribute("highway", "residential", GTYPE_LINESTRING));
        layers.add(osmLayer.addSimpleDynamicLayer("highway", "footway"));
        layers.add(osmLayer.addSimpleDynamicLayer("highway", "cycleway"));
        layers.add(osmLayer.addSimpleDynamicLayer("highway", "track"));
        layers.add(osmLayer.addSimpleDynamicLayer("highway", "path"));
        layers.add(osmLayer.addSimpleDynamicLayer("highway", "unclassified"));
        layers.add(osmLayer.addSimpleDynamicLayer("amenity", "parking", GTYPE_POLYGON));
        layers.add(osmLayer.addSimpleDynamicLayer("railway", null));
        layers.add(osmLayer.addSimpleDynamicLayer("highway", null));
        layers.add(osmLayer.addSimpleDynamicLayer("waterway", null));
        layers.add(osmLayer.addSimpleDynamicLayer("building", null, GTYPE_POLYGON));
        layers.add(osmLayer.addCQLDynamicLayerOnAttribute("building", null, GTYPE_POLYGON));
        layers.add(osmLayer.addSimpleDynamicLayer("natural", null, GTYPE_GEOMETRY));
        layers.add(osmLayer.addSimpleDynamicLayer("natural", "water", GTYPE_POLYGON));
        layers.add(osmLayer.addSimpleDynamicLayer("natural", "wood", GTYPE_POLYGON));
        layers.add(osmLayer.addSimpleDynamicLayer("natural", "coastline"));
        layers.add(osmLayer.addSimpleDynamicLayer("natural", "beach"));
        layers.add(osmLayer.addSimpleDynamicLayer(GTYPE_POLYGON));
        layers.add(osmLayer.addSimpleDynamicLayer(GTYPE_POINT));
        layers.add(osmLayer.addCQLDynamicLayerOnGeometryType(GTYPE_POLYGON));
        layers.add(osmLayer.addCQLDynamicLayerOnGeometryType(GTYPE_POINT));*/
        exportImages(graphDb, osmLayer, layers);
    }

    public static void exportImages(GraphDatabaseService graphDb, OSMLayer osmLayer, ArrayList<Layer> layers) {
        com.vividsolutions.jts.geom.Envelope bbox =
                new com.vividsolutions.jts.geom.Envelope(4.88557, 4.91214, 52.36694, 52.37674); //(52.3239294585924, 52.4278631370635, 4.8246131377312, 4.98776905827439); 
        //double minx, double maxx, double miny, double maxy

        StyledImageExporter imageExporter = new StyledImageExporter(graphDb);
        imageExporter.setExportDir(System.getProperty("user.home") + "/data/export/" + osmLayer.getName());
        imageExporter.setZoom(5.0);
        //imageExporter.setOffset(-0.05, -0.05);
        imageExporter.setSize(1024, 768);

        // Now loop through all dynamic layers and export them to shapefiles,
        // where possible. Layers will multiple geometries cannot be exported
        // and we take note of how many times that happens
        int countMultiGeometryLayers = 0;
        int countMultiGeometryExceptions = 0;
        for (Layer layer : layers) {
            // for (Layer layer : new Layer[] {}) {
            if (layer.getGeometryType() == GTYPE_GEOMETRY) {
                countMultiGeometryLayers++;
            }

            try {
                imageExporter.saveLayerImage(layer.getName(), null, new File(layer.getName() + ".png"), ReferencedEnvelope.reference(bbox));
                //imageExporter.saveLayerImage(layer.getName());
                //imageExporter.saveLayerImage(layer.getName(), null);

            } catch (Exception e) {
                if (e instanceof DataSourceException && e.getMessage().contains("geom.Geometry")) {
                    System.out.println("Got geometry exception on layer with geometry["
                            + SpatialDatabaseService.convertGeometryTypeToName(layer.getGeometryType()) + "]: " + e.getMessage());
                    countMultiGeometryExceptions++;
                } else {
                    System.out.println(e.toString());
                }
            }
        }
    }

    public static void exportImageSnippet(GraphDatabaseService graphDb, GeoPipeline pipeline, String imgName) {
        try {
            //FeatureCollection layerCollection = GeoPipeline.start(layer, new SearchAll()).toFeatureCollection();
            FeatureCollection pipelineCollection;
            pipelineCollection = pipeline.toFeatureCollection();

            ReferencedEnvelope bounds = pipelineCollection.getBounds();
            bounds.expandToInclude(pipelineCollection.getBounds());
            //bounds.expandBy(boundsDelta, boundsDelta);

            StyledImageExporter exporter = new StyledImageExporter(graphDb);

            //Style[] style = new Style[]{
            //            StyledImageExporter.createDefaultStyle(Color.BLUE, Color.CYAN),
            //            StyledImageExporter.createDefaultStyle(Color.RED, Color.ORANGE)
            //        };
            exporter.setExportDir(System.getProperty("user.home") + "/data/export/");
            exporter.saveImage(pipelineCollection, System.getProperty("user.home") + "/data/osm/simple.sld", new File(imgName + ".png"));

            //exporter.saveImage(
            //        new FeatureCollection[]{
            //            layerCollection,
            //            pipelineCollection,},
            //        null,
            //        new File(imgName + ".png"),
            //        bounds);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private static String toCoordinateText(Envelope bbox) {
        return "" + bbox.getMinX() + ", " + bbox.getMinY() + ", " + bbox.getMaxX() + ", " + bbox.getMaxY();
    }

    private static String toCoordinateText(Coordinate[] coordinates) {
        StringBuffer sb = new StringBuffer();
        for (Coordinate c : coordinates) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(c.x).append(" ").append(c.y);
        }
        return sb.toString();
    }
}
