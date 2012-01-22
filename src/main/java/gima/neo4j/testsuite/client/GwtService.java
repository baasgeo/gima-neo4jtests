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
@RemoteServiceRelativePath("messages")
public interface GwtService extends RemoteService {
    
    String SendTask(Messages.Type type, Messages.Db db, double[][] obj, boolean store) throws IllegalArgumentException;  
    
}