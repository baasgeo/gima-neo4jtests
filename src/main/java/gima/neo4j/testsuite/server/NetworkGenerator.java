/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.server;

import java.util.Iterator;

import org.neo4j.graphdb.Node;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialRelationshipTypes;
import org.neo4j.gis.spatial.pipes.GeoPipeline;

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
            add((MultiLineString) geometry, null);
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
 
            //edge.setProperty("_network_length", edge.getGeometry().getLength());
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
                .toSpatialDatabaseRecordList().iterator();

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
        return WGS84.orthodromicDistance(
                point1.getCoordinate().x,
                point1.getCoordinate().y,
                point2.getCoordinate().x,
                point2.getCoordinate().y); //lonA, latA, lonB, latB
    }
    // Attributes
    private EditableLayer pointsLayer;
    private EditableLayer edgesLayer;
    private Double buffer;
    private int edgePointCounter;
}
