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
import org.neo4j.graphalgo.CommonEvaluators;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.EstimateEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphalgo.impl.shortestpath.Dijkstra;
import org.neo4j.graphalgo.impl.util.DoubleAdder;
import org.neo4j.graphalgo.impl.util.DoubleComparator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.Traversal;

/**
 *
 * @author bartbaas
 */
public class OSMRoute {

    private Neo4jSpatialDataStore dataStore;
    private SpatialDatabaseService spatialDatabaseService;
    private Layer pointsLayer;
    private Node startNode = null;
    private Node endNode = null;

    public void dispose() {
    }

    public OSMRoute(Layer pointsLayer, Coordinate startCoord, Coordinate endCoord) {
        this.pointsLayer = pointsLayer;
        this.spatialDatabaseService = pointsLayer.getSpatialDatabase();
        this.startNode = getNode(startCoord);
        this.endNode = getNode(endCoord);
    }

    public OSMRoute(Layer pointsLayer, Node startNode, Node endNode) {
        this.pointsLayer = pointsLayer;
        this.spatialDatabaseService = pointsLayer.getSpatialDatabase();
        this.startNode = startNode;
        this.endNode = endNode;
    }

    private Node getNode(Coordinate coordinate) {
        long start = System.currentTimeMillis();

        Iterable<Node> nodeList = GeoPipeline.startNearestNeighborSearch(pointsLayer, coordinate, 10).sort("Distance").getMin("Distance").toNodeList();
        //Iterable<Node> nodeList = GeoPipeline.startNearestNeighborLatLonSearch(pointsLayer, coordinate, 10).sort("Distance").getMin("Distance").toNodeList();
        
        Node node = nodeList.iterator().next();
        System.out.println("\tGot nearby node: " + node.getId());
        long stop = System.currentTimeMillis();
        System.out.println("\tFound " + " item in " + (stop - start) + "ms");

        return node;
    }

    public void dijkstra(boolean store) {
        //spatialDatabaseService.deleteLayer("pathnodes", new NullListener());
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
            System.out.println("Cost: " + sp.getCost() + ", Length: " + pathNodes.size());
            if (pathNodes != null && store == true) {
                EditableLayer layer = spatialDatabaseService.getOrCreateEditableLayer("pathnodes");
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
            System.out.println("Error finding Path, Unable to find Path");
        } finally {
            tx.finish();
        }
    }

    public void dijkstra2() {
        // START SNIPPET: dijkstraUsage
        PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(
                Traversal.expanderForTypes(SpatialRelationshipTypes.NETWORK), new CostEvaluator<Double>() {

            public Double getCost(Relationship relationship, Direction direction) {
                Node startNd = relationship.getStartNode();
                if (direction.equals(Direction.OUTGOING)) {
                    // TODO use a constant name
                    // TODO if property doesn't exists, decode geometry and calculate it
                    return (Double) startNd.getProperty("_distance");
                } else {
                    return 0.0;
                }
            }
        });
        WeightedPath path = finder.findSinglePath(startNode, endNode);
        if (path != null) {
            GraphDatabaseService databaseService = spatialDatabaseService.getDatabase();
            Transaction tx = databaseService.beginTx();
            try {
                EditableLayer layer = spatialDatabaseService.getOrCreateEditableLayer("pathnodes2");
                layer.setCoordinateReferenceSystem(pointsLayer.getCoordinateReferenceSystem());
                for (Node geomNode : path.nodes()) {
                    //System.out.println(geomNode.getProperty("gtype"));
                    if (geomNode.getProperty("gtype").equals(2)) {
                        //layer.add(geomNode);
                    }
                }
                tx.success();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Error storing Path, Unable to find Path");
            } finally {
                tx.finish();
            }
        }


        // Get the weight for the found path
        System.out.println("Dijkstra weight: " + path.weight() + ", Dijkstra length: " + path.length());
    }

    public void astar() {
        GraphDatabaseService databaseService = spatialDatabaseService.getDatabase();
        Transaction tx = databaseService.beginTx();
        try {
            EstimateEvaluator<Double> estimateEvaluator = new EstimateEvaluator<Double>() {

                public Double getCost(final Node node, final Node goal) {
                    return (Double) node.getProperty("_distance");
                }
            };
            PathFinder<WeightedPath> astar = GraphAlgoFactory.aStar(
                    Traversal.expanderForTypes(SpatialRelationshipTypes.NETWORK, Direction.BOTH),
                    CommonEvaluators.doubleCostEvaluator("_distance"),
                    estimateEvaluator);

            WeightedPath path = astar.findSinglePath(startNode, endNode);
            System.out.println("A-star weight: " + path.weight());
            tx.success();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Error finding Path, Unable to find Path");
        } finally {
            tx.finish();
        }
    }
}
