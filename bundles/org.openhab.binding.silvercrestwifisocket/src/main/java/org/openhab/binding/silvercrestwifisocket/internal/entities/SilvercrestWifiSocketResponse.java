/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.openhab.binding.silvercrestwifisocket.internal.enums.SilvercrestWifiSocketResponseType;
import org.openhab.binding.silvercrestwifisocket.internal.enums.SilvercrestWifiSocketVendor;

/**
 * This POJO represents one Wifi Socket Response.
 *
 * @author Jaime Vaz - Initial contribution
 * @author Christian Heimerl - for integration of EasyHome
 *
 */
public class SilvercrestWifiSocketResponse {

    private String macAddress;
    private String hostAddress;
    private SilvercrestWifiSocketResponseType type;
    private SilvercrestWifiSocketVendor vendor;

    /**
     * Default constructor.
     *
     * @param macAddress the mac address
     * @param hostAddress the host address
     * @param type the {@link SilvercrestWifiSocketResponseType}
     * @param vendor the vendor of the socket
     */
    public SilvercrestWifiSocketResponse(final String macAddress, final String hostAddress,
            final SilvercrestWifiSocketResponseType type, final SilvercrestWifiSocketVendor vendor) {
        this.macAddress = macAddress;
        this.hostAddress = hostAddress;
        this.type = type;
        this.vendor = vendor;
    }

    /**
     * Constructor.
     *
     * @param macAddress the mac address
     * @param type the {@link SilvercrestWifiSocketResponseType}
     * @param vendor the vendor of the socket
     */
    public SilvercrestWifiSocketResponse(final String macAddress, final SilvercrestWifiSocketResponseType type,
            final SilvercrestWifiSocketVendor vendor) {
        this(macAddress, null, type, vendor);
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public SilvercrestWifiSocketResponseType getType() {
        return this.type;
    }

    public void setType(final SilvercrestWifiSocketResponseType type) {
        this.type = type;
    }

    public String getHostAddress() {
        return this.hostAddress;
    }

    public void setHostAddress(final String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public SilvercrestWifiSocketVendor getVendor() {
        return vendor;
    }

    public void setVendor(SilvercrestWifiSocketVendor vendor) {
        this.vendor = vendor;
    }
}
