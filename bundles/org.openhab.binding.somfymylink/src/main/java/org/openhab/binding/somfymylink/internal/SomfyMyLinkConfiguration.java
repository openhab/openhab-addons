/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.somfymylink.internal;

/**
 * The {@link SomfyMyLinkConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Chris Johnson - Initial contribution
 */
public class SomfyMyLinkConfiguration {
    private String ipAddress;
    private String systemId;

    /*
     * public boolean sameConnectionParameters(IPBridgeConfig config) {
     * return StringUtils.equals(ipAddress, config.ipAddress) && StringUtils.equals(user, config.user)
     * && StringUtils.equals(password, config.password) && (reconnect == config.reconnect)
     * && (heartbeat == config.heartbeat);
     * }
     */

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }
}