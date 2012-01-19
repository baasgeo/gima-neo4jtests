/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import gima.neo4j.testsuite.client.GwtService;
import gima.neo4j.testsuite.shared.Messages;
import java.io.File;

/**
 *
 * @author bartbaas
 */
public class GwtServiceImpl extends RemoteServiceServlet implements GwtService {

    private static File basePath = new File(System.getProperty("user.home") + "/data/neodb");
    private Neo amsterdamDb = new Neo();
    private Neo medemblikDb = new Neo();

    public String SendTask(Messages.Type type, Messages.Db db, double[] obj, boolean store) {

        switch (db) {
            case MEDEMBLIK:
                medemblikDb.layerName = "Medemblik";
                medemblikDb.dbPath = new File(basePath, "medemblik.gdb");
                medemblikDb.osmfile = (System.getProperty("user.home") + "/data/osm/medemblik.osm");
                return SendOperation(medemblikDb, type, obj, store);
            case AMSTERDAM:
                amsterdamDb.layerName = "Amsterdam";
                amsterdamDb.dbPath = new File(basePath, "amsterdam.gdb");
                amsterdamDb.osmfile = (System.getProperty("user.home") + "/data/osm/amsterdam.osm");
                return SendOperation(amsterdamDb, type, obj, store);
            case NL:
                return "<div class=red>Not implemented yet.</red>";
            default:
                return "<div class=red>Not implemented yet.</red>";
        }
        //Logger.getLogger(MedemblikServiceImpl.class.getName()).log(Level.INFO, null, "Executing task :" + type);
    }

    private String SendOperation(Neo instance, Messages.Type type, double[] obj, boolean store) {
        try {
            switch (type) {
                case TEST_EMPTY:
                    return "Empty callback.";
                case START:
                    return instance.Start();
                case STOP:
                    return instance.Stop();
                case STATISTICS:
                    return instance.Stats();
                case STATUS:
                    return null;
                case DOTESTS:
                    return instance.DoTests();
                case MAKE_OSM:
                    //return instance.ImportOSM();
                    return instance.ImportOSM_Batch();
                case MAKE_NETWORK:
                    return instance.MakeTopology();
                case MAKE_DYNAMICLAYERS:
                    return instance.AddDynamicLayers();
                case DELETE:
                    return instance.Delete();
                case TEST_EXPORTLAYERS:
                    return instance.ExportImages();
                case TEST_BOUNDINGBOX:
                    return instance.testSearchPoints(obj, store);
                case TEST_ROUTE:
                    return instance.ShortestPath(obj, store);
                default:
                    return "<div class=red>Not implemented yet.</red>";
            }
        } catch (Exception ex) {
            return (ex.toString());
        }
    }
}
