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
package org.openhab.binding.remoteopenhab.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RemoteopenhabServerConfiguration} is responsible for holding
 * configuration informations associated to a remote openHAB server
 * thing type
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RemoteopenhabServerConfiguration {
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String REST_PATH = "restPath";

    public String host = "";
    public boolean useHttps = false;
    public int port = 8080;
    public boolean trustedCertificate = false;
    public String restPath = "/rest/";
    public String token = "";
    public String username = "";
    public String password = "";
    public boolean authenticateAnyway = false;
    public int accessibilityInterval = 3;
    public int aliveInterval = 5;
    public boolean restartIfNoActivity = false;
}
