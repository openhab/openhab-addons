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
package org.openhab.binding.robonect.internal.model.cmd;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Queries remote-start point information from the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
public class RemoteCommand implements Command {
    private final int remoteStart;
    private String name;
    private Integer distance;
    private Integer proportion;

    /**
     * Creates a command that reads all remote-start points.
     */
    public RemoteCommand() {
        this(0);
    }

    /**
     * Creates a remote-start command for the selected point.
     *
     * @param remoteStart point number from 1 to 5, or 0 to read all points
     * @throws IllegalArgumentException if the point number is outside 0 to 5
     */
    public RemoteCommand(int remoteStart) {
        if (remoteStart < 0 || remoteStart > 5) {
            throw new IllegalArgumentException("Remote start must be between 0 and 5");
        }
        this.remoteStart = remoteStart;
    }

    /**
     * Sets the name of the selected remote-start point.
     *
     * @param name the remote-start point name
     * @return this command
     */
    public RemoteCommand withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the distance of the selected remote-start point.
     *
     * @param distance the distance in metres
     * @return this command
     */
    public RemoteCommand withDistance(int distance) {
        this.distance = distance;
        return this;
    }

    /**
     * Sets the selection proportion for the remote-start point.
     *
     * @param proportion the selection proportion in percent
     * @return this command
     */
    public RemoteCommand withProportion(int proportion) {
        this.proportion = proportion;
        return this;
    }

    /**
     * Builds the remote-start settings URL for this {@link Command}.
     *
     * @param baseURL base URL to which the command parameters are appended
     * @return the complete request URL
     */
    @Override
    public String toCommandURL(String baseURL) {
        StringBuilder url = new StringBuilder(baseURL).append("?cmd=remote");
        if (remoteStart > 0) {
            if (name != null) {
                url.append("&name").append(remoteStart).append("=")
                        .append(URLEncoder.encode(name, StandardCharsets.UTF_8));
            }
            if (distance != null) {
                url.append("&distance").append(remoteStart).append("=").append(distance);
            }
            if (proportion != null) {
                url.append("&proportion").append(remoteStart).append("=").append(proportion);
            }
        }
        return url.toString();
    }
}
