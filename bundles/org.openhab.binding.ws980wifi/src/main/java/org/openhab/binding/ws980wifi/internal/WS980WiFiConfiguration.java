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
package org.openhab.binding.ws980wifi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WS980WiFiConfiguration} class contains fields mapping thing configuration parameters.
 * Configuration parameters have an individual value for each thing. In contrast property values are
 * the same for all Things of one Thing-Type.
 * The configuration parameters can be modified via the administration interfac. properties not.
 * 
 * @author Joerg Dokupil - Initial contribution
 */
@NonNullByDefault
public class WS980WiFiConfiguration {

    private int refreshInterval;
    private String host;
    private String port;

    public WS980WiFiConfiguration() {
        this.host = "0.0.0.0";
        this.port = "45000";
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getRefreshInterval() {
        return this.refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
