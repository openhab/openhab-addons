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
package org.openhab.binding.silvercrestwifisocket.internal.entities;

import org.openhab.binding.silvercrestwifisocket.internal.enums.SilvercrestWifiSocketRequestType;
import org.openhab.binding.silvercrestwifisocket.internal.enums.SilvercrestWifiSocketVendor;

/**
 * This POJO represents one Wifi Socket request.
 *
 * @author Jaime Vaz - Initial contribution
 * @author Christian Heimerl - for integration of EasyHome
 *
 */
public class SilvercrestWifiSocketRequest {

    private String macAddress;
    private SilvercrestWifiSocketRequestType type;
    private SilvercrestWifiSocketVendor vendor;

    /**
     * Default constructor.
     *
     * @param macAddress the mac address
     * @param type the {@link SilvercrestWifiSocketRequestType}
     * @param vendor the {@link SilvercrestWifiSocketVendor}
     */
    public SilvercrestWifiSocketRequest(final String macAddress, final SilvercrestWifiSocketRequestType type,
            final SilvercrestWifiSocketVendor vendor) {
        this.macAddress = macAddress;
        this.type = type;
        this.vendor = vendor;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public SilvercrestWifiSocketRequestType getType() {
        return this.type;
    }

    public void setType(final SilvercrestWifiSocketRequestType type) {
        this.type = type;
    }

    public SilvercrestWifiSocketVendor getVendor() {
        return vendor;
    }

    public void setVendor(SilvercrestWifiSocketVendor vendor) {
        this.vendor = vendor;
    }
}
