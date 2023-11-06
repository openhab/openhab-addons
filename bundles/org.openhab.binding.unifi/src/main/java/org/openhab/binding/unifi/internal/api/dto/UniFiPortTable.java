/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.api.dto;

import com.google.gson.annotations.Expose;

/**
 * The {@link UniFiPortTable} represents the data model of UniFi port table, which is an extend of port override.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class UniFiPortTable {

    @Expose
    private int portIdx;

    @Expose
    private String portconfId;

    @Expose
    private String poeMode;

    private String name;

    private boolean enable;

    private boolean up;

    /**
     * If true supports PoE.
     */
    private boolean portPoe;

    private boolean poeEnable;

    private String poePower;

    private String poeVoltage;

    private String poeCurrent;

    public int getPortIdx() {
        return portIdx;
    }

    public String getPortconfId() {
        return portconfId;
    }

    public String getPoeMode() {
        return poeMode;
    }

    public String getName() {
        return name;
    }

    public boolean isUp() {
        return up;
    }

    public boolean isEnabled() {
        return enable;
    }

    public boolean isPortPoe() {
        return portPoe;
    }

    public boolean isPoeEnabled() {
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

    @Override
    public String toString() {
        return String.format(
                "UniFiPortTable{portIx: '%d', portconfId: '%s', poeMode: '%s', name: '%s', enable: '%b', up: '%b', portPoe: '%b', poeEnable: '%b, poePower: '%s', poeVoltage: '%s', poeCurrent: '%s'}",
                portIdx, portconfId, poeMode, name, enable, up, portPoe, poeEnable, poePower, poeVoltage, poeCurrent);
    }
}
