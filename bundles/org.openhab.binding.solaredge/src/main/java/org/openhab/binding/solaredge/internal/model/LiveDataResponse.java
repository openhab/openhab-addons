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
package org.openhab.binding.solaredge.internal.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * this class is used to map the live data json response
 *
 * @author Alexander Friese - initial contribution
 */
public class LiveDataResponse {
    public static final String GRID = "GRID";
    public static final String LOAD = "LOAD";
    public static final String PV = "PV";
    public static final String STORAGE = "STORAGE";

    public static class Value {
        public String status;
        public Double currentPower;
    }

    public static class BatteryValue {
        public String status;
        public Double currentPower;
        public Double chargeLevel;
        public String critical;
    }

    public static class Connection {
        public String from;
        public String to;
    }

    public static class SiteCurrentPowerFlow {
        public String unit;

        @SerializedName(GRID)
        public Value grid;

        @SerializedName(LOAD)
        public Value load;

        @SerializedName(PV)
        public Value pv;

        @SerializedName(STORAGE)
        public BatteryValue storage;

        public List<Connection> connections;
    }

    private SiteCurrentPowerFlow siteCurrentPowerFlow;

    public final SiteCurrentPowerFlow getSiteCurrentPowerFlow() {
        return siteCurrentPowerFlow;
    }

    public final void setSiteCurrentPowerFlow(SiteCurrentPowerFlow siteCurrentPowerFlow) {
        this.siteCurrentPowerFlow = siteCurrentPowerFlow;
    }

}
