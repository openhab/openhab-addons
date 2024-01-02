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
package org.openhab.binding.asuswrt.internal.structures;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AsuswrtConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtConfiguration {

    // Thing configuration properties
    public static final String CONFIG_USER = "username";
    public static final String CONFIG_PASS = "password";
    public static final String CONFIG_HOSTNAME = "hostname";
    public static final String CONFIG_UPDATE_INTERVAL = "refreshInterval";
    public static final String CONFIG_SSL_AUTH = "useSSL";
    public static final String CONFIG_PORT_HTTP = "httpPort";
    public static final String CONFIG_PORT_HTTPS = "httpsPort";

    // Thing configuration parameters
    public String hostname = "";
    public String username = "";
    public String password = "";
    public int pollingInterval = 20;
    public int reconnectInterval = 60;
    public int discoveryInterval = 3600;
    public int httpPort = 80;
    public int httpsPort = 443;
    public boolean autoDiscoveryEnabled = false;
    public boolean useSSL = false;
}
