/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.rest;

import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link VpnServerManager} is the Java class used to handle api requests related to
 * VPN Server monitoring
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VpnServerManager extends ListableRest<VpnServerManager.VpnServer, VpnServerManager.VpnServerResponse> {
    private static final String PATH = "vpn";
    private static final String CONNECTION_SUBPATH = "connection";

    public enum ServerType {
        IPSEC,
        PPTP,
        OPENVPN,
        WIREGUARD
    }

    public enum ServerState {
        STOPPED,
        STARTING,
        STARTED,
        STOPPING,
        ERROR
    }

    public static record VpnServer(ServerState state, ServerType type, String name, int connectionCount,
            int authConnectionCount) {
    }

    public static record VpnConnection(//
            long rxBytes, // received bytes
            long txBytes, // transmitted bytes
            boolean authenticated, // is the connection authenticated
            String user, // user login
            String id, // connection id
            String vpn, // related VPN server id
            String srcIp, // connection source IP address
            String localIp, // attributed IP address from VPN address pool
            ZonedDateTime authTime // timestamp of the authentication
    ) {
    }

    protected class VpnServerResponse extends Response<VpnServer> {
    }

    protected class VpnConnectionsResponse extends Response<VpnConnection> {

    }

    public VpnServerManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, VpnServerResponse.class, session.getUriBuilder().path(PATH));
    }

    public List<VpnConnection> getVpnConnections() throws FreeboxException {
        return get(VpnConnectionsResponse.class, CONNECTION_SUBPATH);
    }
}
