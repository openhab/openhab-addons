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

package org.openhab.binding.octoprint.internal.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OctopiServer} class defines the abstraction of the octoprint server.
 *
 * @author Jan Niklas Freisinger - Initial contribution
 */
public class OctopiServer {
    private final Logger logger = LoggerFactory.getLogger(OctopiServer.class);
    public final String ip;
    public final String apiKey;
    public final String userName;
    String password;

    public OctopiServer(String ip, String apiKey, String userName) {
        this.ip = ip;
        this.apiKey = apiKey;
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "OctopiServer{" + "ip='" + ip + '\'' + ", apiKey='" + apiKey + '\'' + ", userName='" + userName + '\''
                + ", password='" + password + '\'' + '}';
    }
}
