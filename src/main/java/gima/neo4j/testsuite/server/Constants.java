/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.server;

import org.neo4j.collections.rtree.RTreeIndex;

/**
 *
 * @author bartbaas
 */
public interface Constants {

    // Node properties
    String PROP_LAYER = "layer";
    String PROP_LAYERNODEEXTRAPROPS = "layerprops";
    String PROP_CRS = "layercrs";
    String PROP_CREATIONTIME = "ctime";
    String PROP_GEOMENCODER = "geomencoder";
    String PROP_GEOMENCODER_CONFIG = "geomencoder_config";
    String PROP_LAYER_CLASS = "layer_class";
    String PROP_TYPE = "gtype";
    String PROP_QUERY = "query";
    String PROP_WKB = "wkb";
    String PROP_WKT = "wkt";
    String[] RESERVED_PROPS = new String[]{
        RTreeIndex.PROP_BBOX,
        PROP_LAYER,
        PROP_LAYERNODEEXTRAPROPS,
        PROP_CRS,
        PROP_CREATIONTIME,
        PROP_TYPE,
        PROP_WKB,
        PROP_WKT
    };
    // OpenGIS geometry type numbers 
    int GTYPE_GEOMETRY = 0;
    int GTYPE_POINT = 1;
    int GTYPE_LINESTRING = 2;
    int GTYPE_POLYGON = 3;
    int GTYPE_MULTIPOINT = 4;
    int GTYPE_MULTILINESTRING = 5;
    int GTYPE_MULTIPOLYGON = 6;
}
