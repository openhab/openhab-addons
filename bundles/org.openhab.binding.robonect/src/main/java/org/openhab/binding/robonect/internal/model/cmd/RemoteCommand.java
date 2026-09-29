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

    public RemoteCommand() {
        this(0);
    }

    public RemoteCommand(int remoteStart) {
        if (remoteStart < 0 || remoteStart > 5) {
            throw new IllegalArgumentException("Remote start must be between 0 and 5");
        }
        this.remoteStart = remoteStart;
    }

    public RemoteCommand withName(String name) {
        this.name = name;
        return this;
    }

    public RemoteCommand withDistance(int distance) {
        this.distance = distance;
        return this;
    }

    public RemoteCommand withProportion(int proportion) {
        this.proportion = proportion;
        return this;
    }

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
