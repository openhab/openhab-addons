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
package org.openhab.binding.pilight.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PilightBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Stefan Röllin - Initial contribution
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
@NonNullByDefault
public class PilightBridgeConfiguration {

    private String ipAddress = "";
    private int port = 0;
    private int delay = 500;
    private boolean backgroundDiscovery = true;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public boolean getBackgroundDiscovery() {
        return backgroundDiscovery;
    }

    public void setBackgroundDiscovery(boolean flag) {
        backgroundDiscovery = flag;
    }
}
