/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.server;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.List;
import org.geotools.data.neo4j.Neo4jSpatialDataStore;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.SpatialRelationshipTypes;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.impl.shortestpath.Dijkstra;
import org.neo4j.graphalgo.impl.util.DoubleAdder;
import org.neo4j.graphalgo.impl.util.DoubleComparator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author bartbaas
 */
public class OSMRoute {

    private Neo4jSpatialDataStore dataStore;
    private SpatialDatabaseService spatialDatabaseService;
    private String routeLayerName = "path";
    private Layer pointsLayer;
    private boolean store = false;
    private Node startNode = null;
    private Node endNode = null;
    private long nodeSearchTime = 0;

    public void dispose() {
    }

    public OSMRoute(Layer pointsLayer, Coordinate startCoord, Coordinate endCoord, Boolean store) {
        this.pointsLayer = pointsLayer;
        this.spatialDatabaseService = pointsLayer.getSpatialDatabase();
        this.startNode = getNode(startCoord);
        this.endNode = getNode(endCoord);
        this.store = store;
    }

    public OSMRoute(Layer pointsLayer, Node startNode, Node endNode, Boolean store) {
        this.pointsLayer = pointsLayer;
        this.spatialDatabaseService = pointsLayer.getSpatialDatabase();
        this.startNode = startNode;
        this.endNode = endNode;
        this.store = store;
    }

    public void RouteLayerName(String name) {
        this.routeLayerName = name;
    }

    public long StartNodeId() {
        return startNode.getId();
    }

    public long EndNodeId() {
        return endNode.getId();
    }
    
    public long NodeSearchTime() {
        return nodeSearchTime;
    }

    private Node getNode(Coordinate coordinate) {
        long start = System.currentTimeMillis();

        //Iterable<Node> nodeList = GeoPipeline.startNearestNeighborSearch(pointsLayer, coordinate, 1000).sort("Distance").getMin("Distance").toNodeList();
        // Check whether we can find a node from which is located within a distance of 500 meters
        Iterable<Node> nodeList = GeoPipeline.startNearestNeighborLatLonSearch(pointsLayer, coordinate, 0.5)
                .sort("OrthodromicDistance")
                .getMin("OrthodromicDistance")
                .toNodeList();

        Node node = nodeList.iterator().next();
        long stop = System.currentTimeMillis();
        nodeSearchTime = nodeSearchTime + (stop - start);

        if (store) {
            GraphDatabaseService databaseService = spatialDatabaseService.getDatabase();
            Transaction tx = databaseService.beginTx();
            try {
                EditableLayer layer = spatialDatabaseService.getOrCreateEditableLayer(pointsLayer.getName() + " - foundpoints");
                layer.setCoordinateReferenceSystem(pointsLayer.getCoordinateReferenceSystem());
                layer.add(node);
                tx.success();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                tx.finish();
            }
        }
        System.out.println("\tGot nearby node: " + node.getId());        
        System.out.println("\tFound " + " item in " + (stop - start) + "ms");        
        
        return node;
    }

    public String dijkstra() {
        String result;
        System.out.println(routeLayerName);
        
        GraphDatabaseService databaseService = spatialDatabaseService.getDatabase();
        Transaction tx = databaseService.beginTx();
        try {
            // point <- edge -> point <- edge -> point
            Dijkstra<Double> sp = new Dijkstra<Double>(
                    0.0,
                    startNode,
                    endNode,
                    new CostEvaluator<Double>() {

                        public Double getCost(Relationship relationship, Direction direction) {
                            Node startNd = relationship.getStartNode();
                            if (direction.equals(Direction.INCOMING)) {
                                // TODO use a constant name
                                // TODO if property doesn't exists, decode geometry and calculate it
                                //System.out.println("\tNode: "+ startNd.getId());                                
                                return (Double) startNd.getProperty("_distance");                                
                            } else {
                                return 0.0;
                            }
                        }
                    },
                    new DoubleAdder(),
                    new DoubleComparator(),
                    Direction.BOTH,
                    SpatialRelationshipTypes.NETWORK);

            List<Node> pathNodes = sp.getPathAsNodes();

            System.out.println("Length: " + sp.getCost());
            System.out.println("Segments: " + pathNodes.size());
            result = "Length: " + sp.getCost().longValue() + ", Segments: " + pathNodes.size();
            if (pathNodes != null && store == true) {
                EditableLayer layer = spatialDatabaseService.getOrCreateEditableLayer(routeLayerName);
                layer.setCoordinateReferenceSystem(pointsLayer.getCoordinateReferenceSystem());
                for (Node geomNode : pathNodes) {
                    //System.out.println(geomNode.getProperty("gtype"));
                    if (geomNode.getProperty("gtype").equals(2)) {
                        layer.add(geomNode);
                    }
                }
            }

            tx.success();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            result = "Error finding Path, Unable to find Path";
        } finally {
            tx.finish();
        }
        return result;
    }
}
