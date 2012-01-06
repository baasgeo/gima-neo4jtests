/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import gima.neo4j.testsuite.shared.Messages;

/**
 *
 * @author bartbaas
 */
@RemoteServiceRelativePath("amsterdam")
public interface AmsterdamService extends RemoteService {

    String SendTask(Messages.Type type) throws IllegalArgumentException;
    
    String SendTask(Messages.Type type, double[] obj) throws IllegalArgumentException;
}