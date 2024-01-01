/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;

import inet.ipaddr.IPAddress;

/**
 * The {@link ConnectionManager} is the Java class used to handle api requests related to connection
 *
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ConnectionManager extends ConfigurableRest<ConnectionManager.Status, ConnectionManager.StatusResponse> {
    private static final String PATH = "connection";

    protected static class StatusResponse extends Response<Status> {
    }

    private enum State {
        GOING_UP,
        UP,
        GOING_DOWN,
        DOWN,
        UNKNOWN
    }

    private enum Type {
        ETHERNET,
        RFC2684,
        PPPOATM,
        UNKNOWN
    }

    private enum Media {
        FTTH,
        ETHERNET,
        XDSL,
        BACKUP_4G,
        UNKNOWN
    }

    public static record Status(State state, Type type, Media media, @Nullable List<Integer> ipv4PortRange,
            @Nullable IPAddress ipv4, // This can be null if state is not up
            @Nullable IPAddress ipv6, // This can be null if state is not up
            long rateUp, // current upload rate in byte/s
            long rateDown, // current download rate in byte/s
            long bandwidthUp, // available upload bandwidth in bit/s
            long bandwidthDown, // available download bandwidth in bit/s
            long bytesUp, // total uploaded bytes since last connection
            long bytesDown // total downloaded bytes since last connection
    ) {
    }

    public ConnectionManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, StatusResponse.class, session.getUriBuilder().path(PATH), null);
    }
}
