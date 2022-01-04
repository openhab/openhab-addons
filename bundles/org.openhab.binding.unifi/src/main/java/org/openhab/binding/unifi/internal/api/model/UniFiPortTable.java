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
package org.openhab.binding.unifi.internal.api.model;

/**
 * The {@link UniFiPortTable} represents the data model of UniFi port table, which is an extend of port override.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class UniFiPortTable extends UnfiPortOverride {

    private transient UniFiDevice device;

    private String name;

    private boolean enable;

    private boolean up;

    /**
     * If true supports POE.
     */
    private boolean portPoe;

    private boolean poeEnable;

    private String poePower;

    private String poeVoltage;

    private String poeCurrent;

    public UniFiDevice getDevice() {
        return device;
    }

    public void setDevice(final UniFiDevice device) {
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public boolean isUp() {
        return up;
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isPortPoe() {
        return portPoe;
    }

    public boolean isPoeEnable() {
        return poeEnable;
    }

    public String getPoePower() {
        return poePower;
    }

    public String getPoeVoltage() {
        return poeVoltage;
    }

    public String getPoeCurrent() {
        return poeCurrent;
    }
}
