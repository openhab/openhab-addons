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
package org.openhab.binding.robonect.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.model.ModelParser;
import org.openhab.binding.robonect.internal.model.RemoteInfo;
import org.openhab.binding.robonect.internal.model.cmd.RemoteCommand;
import org.openhab.binding.robonect.internal.model.cmd.RemoteWebCommand;

/**
 * API operations for remote-start endpoints.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public final class RobonectRemoteStartApi {
    private final RobonectTransport transport;

    /**
     * Creates an endpoint API using the shared transport.
     *
     * @param transport the transport used for requests and parsing
     */
    RobonectRemoteStartApi(RobonectTransport transport) {
        this.transport = transport;
    }

    /**
     * Returns the configured remote-start points.
     *
     * @return the remote-start information including its success status
     */
    public RemoteInfo getRemoteInfo() {
        return ModelParser.parse(transport.sendCommand(new RemoteCommand()), RemoteInfo.class);
    }

    /**
     * Saves a remote-start point through the unofficial web interface.
     *
     * @param remoteStart the remote-start point number
     * @param name the point name
     * @param proportion the selection proportion in percent
     * @param distance the distance in metres
     * @param path the path code
     * @param visible whether the point is visible
     * @param corridorCut whether CorridorCut is enabled
     */
    public void updateRemoteStartViaWebInterface(int remoteStart, String name, int proportion, int distance, int path,
            boolean visible, boolean corridorCut) {
        transport.sendCommand(new RemoteWebCommand(transport.webBaseUrl, remoteStart, name, proportion, distance, path,
                visible, corridorCut), transport.webBaseUrl);
    }

    /**
     * Updates one or more values of a remote-start point.
     *
     * @param remoteStart the remote-start point number
     * @param name the name, or {@code null} to leave it unchanged
     * @param distance the distance in metres, or {@code null} to leave it unchanged
     * @param proportion the selection proportion in percent, or {@code null} to leave it unchanged
     * @return the remote-start information and its success status
     */
    public RemoteInfo updateRemoteStart(int remoteStart, @Nullable String name, @Nullable Integer distance,
            @Nullable Integer proportion) {
        RemoteCommand command = new RemoteCommand(remoteStart);
        if (name != null) {
            command.withName(name);
        }
        if (distance != null) {
            command.withDistance(distance);
        }
        if (proportion != null) {
            command.withProportion(proportion);
        }
        return ModelParser.parse(transport.sendCommand(command), RemoteInfo.class);
    }
}
