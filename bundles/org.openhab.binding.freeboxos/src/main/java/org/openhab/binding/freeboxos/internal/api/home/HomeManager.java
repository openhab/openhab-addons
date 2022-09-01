/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.freeboxos.internal.api.home;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.home.HomeNode.HomeNodesResponse;
import org.openhab.binding.freeboxos.internal.api.home.HomeNodeEndpointState.HomeNodeEndpointStateResponse;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.RestManager;

/**
 * The {@link HomeManager} is the Java class used to handle api requests
 * related to home
 *
 * @author ben12 - Initial contribution
 */
@NonNullByDefault
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

    public <T> @Nullable HomeNodeEndpointState getEndpointsState(int nodeId, int stateSignalId)
            throws FreeboxException {
        return get(HomeNodeEndpointStateResponse.class, ENDPOINTS_SUB_PATH, String.valueOf(nodeId),
                String.valueOf(stateSignalId));
    }

    public <T> void putCommand(int nodeId, int stateSignalId, T value) throws FreeboxException {
        put(GenericResponse.class, new EndpointGenericValue<T>(value), ENDPOINTS_SUB_PATH, String.valueOf(nodeId),
                String.valueOf(stateSignalId));
    }
}
