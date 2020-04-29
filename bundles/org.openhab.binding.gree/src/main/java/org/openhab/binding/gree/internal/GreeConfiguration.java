/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.gree.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GreeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
public class GreeConfiguration {
    private String ipAddress = "";
    private String broadcastAddress = "";
    private int refresh = 0;

    public String getIpAddress() {
        return ipAddress;
    }

    public String getBroadcastAddress() {
        return broadcastAddress;
    }

    public void setRefresh(Integer refresh) {
        this.refresh = refresh;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setBroadcastAddress(String ipAddress) {
        this.broadcastAddress = ipAddress;
    }

    public Integer getRefresh() {
        return refresh;
    }

    public boolean isValid() {
        try {
            if (ipAddress.isEmpty()) {
                return false;
            }
            if (broadcastAddress.isEmpty()) {
                return false;
            }
            if (refresh <= 0) {
                throw new IllegalArgumentException("Refresh time must be positive number!");
            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Config: ipAddress=" + ipAddress + ", broadcastAddress=" + broadcastAddress + ", refresh="
                + String.valueOf(refresh);
    }
}
