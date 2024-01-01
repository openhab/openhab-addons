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
package org.openhab.binding.volumio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VolumioConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Patrick Sernetz - Initial contribution
 * @author Chris Wohlbrecht - Adapt for openHAB 3
 * @author Michael Loercher - Adaption for openHAB 3
 */
@NonNullByDefault
public class VolumioConfiguration {

    private String hostName = "";

    private int port;

    private String protocol = "";

    private int timeout;

    public String getHost() {
        return hostName;
    }

    public void setHost(String host) {
        this.hostName = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
