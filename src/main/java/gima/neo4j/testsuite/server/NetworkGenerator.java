/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.server;

import java.util.Iterator;

import org.neo4j.gis.spatial.filter.SearchIntersect;
import org.neo4j.graphdb.Node;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import java.util.List;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialRelationshipTypes;
import org.neo4j.gis.spatial.osm.OSMGeometryEncoder;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.gis.spatial.pipes.osm.OSMGeoPipeline;

/**
 *
 * @author bartbaas
 */
public class NetworkGenerator {
    // Constructor

    public NetworkGenerator(EditableLayer pointsLayer, EditableLayer edgesLayer) {
        this(pointsLayer, edgesLayer, null);
    }

    public NetworkGenerator(EditableLayer pointsLayer, EditableLayer edgesLayer, Double buffer) {
        this.pointsLayer = pointsLayer;
        this.edgesLayer = edgesLayer;
        this.buffer = buffer;
    }

    public int edgePointCounter() {
        return edgePointCounter;
    }

    // Public methods
    public void add(SpatialDatabaseRecord record) {
        Geometry geometry = record.getGeometry();
        if (geometry instanceof MultiLineString) {
            add((MultiLineString) geometry, record);
        } else if (geometry instanceof LineString) {
            add((LineString) geometry, null);
        } else {
            // TODO better handling?
            throw new IllegalArgumentException("geometry type not supported: " + geometry.getGeometryType());
        }
    }

    public void add(MultiLineString lines) {
        add(lines, null);
    }

    public void add(LineString line) {
        add(line, null);
    }

    // Private methods
    protected void add(MultiLineString line, SpatialDatabaseRecord record) {
        for (int i = 0; i < line.getNumGeometries(); i++) {
            add((LineString) line.getGeometryN(i), record);
        }
    }

    protected void add(LineString line, SpatialDatabaseRecord edge) {
        if (edge == null) {
            edge = edgesLayer.add(line);
        }

        // TODO reserved property?  
        //edge.setProperty("_network_length", edge.getGeometry().getLength());
        //edge.setProperty("_distance", calcDistance(line));
        edge.setProperty("_distance", distance(line));
        
        addEdgePoint(edge.getGeomNode(), line.getStartPoint());
        addEdgePoint(edge.getGeomNode(), line.getEndPoint());
    }

    protected void addEdgePoint(Node node, Geometry edgePoint) {
        //if (buffer != null) {
        //    edgePoint = edgePoint.buffer(buffer.doubleValue());
        //}

        Iterator<SpatialDatabaseRecord> results =
                GeoPipeline.startNearestNeighborLatLonSearch(pointsLayer, edgePoint.getCoordinate(), buffer.doubleValue())
                //.sort("OrthodromicDistance")
                //.getMin("OrthodromicDistance")
                .toSpatialDatabaseRecordList()
                .iterator();

        //Iterator<SpatialDatabaseRecord> results = pointsLayer.getIndex().search(
        //        new SearchIntersect(pointsLayer, edgePoint));
        
        if (!results.hasNext()) {
            SpatialDatabaseRecord point = pointsLayer.add(edgePoint);
            node.createRelationshipTo(point.getGeomNode(), SpatialRelationshipTypes.NETWORK);
            edgePointCounter++;
        } else {
            while (results.hasNext()) {
                node.createRelationshipTo(results.next().getGeomNode(), SpatialRelationshipTypes.NETWORK);
            }
        }
    }

    private double distance(LineString line) {
        double length = 0.0;
        for (int i = 0; i < line.getNumPoints() - 1; i++) {
            length = length + distance(line.getPointN(i), line.getPointN(i + 1));
        }
        return length;
    }

    private double distance(final Point point1, final Point point2) {
        DefaultEllipsoid WGS84 = DefaultEllipsoid.WGS84;
        return WGS84.orthodromicDistance(point1.getCoordinate().x, point1.getCoordinate().y, point2.getCoordinate().x, point2.getCoordinate().y); //lonA, latA, lonB, latB
    }

    protected Double calcDistance(LineString line) {
        Double length = 0.0;
        for (int i = 0; i < line.getNumPoints() - 1; i++) {
            length = length + calcDistance(line.getPointN(i), line.getPointN(i + 1));
        }
        return length;
    }

    protected Double calcDistance(final Point point1, final Point point2) {
        double dist = calcDistance(
                point1.getCoordinate().y, // Latitude
                point1.getCoordinate().x, // Longtitude
                point2.getCoordinate().y,
                point2.getCoordinate().x);
        return dist;
    }

    protected double calcDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double RADIUS_EARTH = 6371 * 1000; // Meters

        latitude1 = Math.toRadians(latitude1);
        longitude1 = Math.toRadians(longitude1);
        latitude2 = Math.toRadians(latitude2);
        longitude2 = Math.toRadians(longitude2);
        double cLa1 = Math.cos(latitude1);
        double x_A = RADIUS_EARTH * cLa1 * Math.cos(longitude1);
        double y_A = RADIUS_EARTH * cLa1 * Math.sin(longitude1);
        double z_A = RADIUS_EARTH * Math.sin(latitude1);
        double cLa2 = Math.cos(latitude2);
        double x_B = RADIUS_EARTH * cLa2 * Math.cos(longitude2);
        double y_B = RADIUS_EARTH * cLa2 * Math.sin(longitude2);
        double z_B = RADIUS_EARTH * Math.sin(latitude2);
        return Math.sqrt((x_A - x_B) * (x_A - x_B) + (y_A - y_B)
                * (y_A - y_B) + (z_A - z_B) * (z_A - z_B));
    }
    // Attributes
    private EditableLayer pointsLayer;
    private EditableLayer edgesLayer;
    private Double buffer;
    private int edgePointCounter;
}
