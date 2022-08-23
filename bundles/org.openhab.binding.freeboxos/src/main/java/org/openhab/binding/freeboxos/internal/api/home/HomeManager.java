package org.openhab.binding.freeboxos.internal.api.home;

import java.util.List;

import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.home.HomeNode.HomeNodesResponse;
import org.openhab.binding.freeboxos.internal.api.home.HomeNodeEndpointState.HomeNodeEndpointStateResponse;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.RestManager;

public class HomeManager extends RestManager {
    public static final String HOME_PATH = "home";
    public static final String NODES_SUB_PATH = "nodes";
    public static final String ENDPOINTS_SUB_PATH = "endpoints";

    public HomeManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.HOME, HOME_PATH);
    }

    public List<HomeNode> getHomeNodes() throws FreeboxException {
        return getList(HomeNodesResponse.class, NODES_SUB_PATH);
    }

    public <T> HomeNodeEndpointState getEndpointsState(int nodeId, int stateSignalId) throws FreeboxException {
        return get(HomeNodeEndpointStateResponse.class, ENDPOINTS_SUB_PATH, String.valueOf(nodeId),
                String.valueOf(stateSignalId));
    }

    public <T> void putCommand(int nodeId, int stateSignalId, T value) throws FreeboxException {
        put(GenericResponse.class, new EndpointGenericValue<T>(value), ENDPOINTS_SUB_PATH, String.valueOf(nodeId),
                String.valueOf(stateSignalId));
    }
}
