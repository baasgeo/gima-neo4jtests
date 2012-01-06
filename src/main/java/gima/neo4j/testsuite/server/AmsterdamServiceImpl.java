/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import gima.neo4j.testsuite.client.AmsterdamService;
import gima.neo4j.testsuite.shared.Messages;

/**
 *
 * @author bartbaas
 */
public class AmsterdamServiceImpl extends RemoteServiceServlet implements AmsterdamService {

    public String SendTask(Messages.Type type, double[] obj) {
        SingletonAmsterdam instance = SingletonAmsterdam.getInstance();
        //Logger.getLogger(MedemblikServiceImpl.class.getName()).log(Level.INFO, null, "Executing task :" + type);
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
                    return instance.testSearchPoints(obj, true);
                default:
                    return "<div class=red>Not implemented yet.</red>";
            }
        } catch (Exception ex) {
            return (ex.toString());
        }
    }

    public String SendTask(Messages.Type type) {
        return SendTask(type, null);
    }
}
