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
package org.openhab.binding.solaredge.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * this class is used to map the live data json response
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class LiveDataResponse {
    public static final String GRID = "GRID";
    public static final String LOAD = "LOAD";
    public static final String PV = "PV";
    public static final String STORAGE = "STORAGE";

    public static class Value {
        public @Nullable String status;
        public @Nullable Double currentPower;
    }

    public static class BatteryValue {
        public @Nullable String status;
        public @Nullable Double currentPower;
        public @Nullable Double chargeLevel;
        public @Nullable String critical;
    }

    public static class Connection {
        public @Nullable String from;
        public @Nullable String to;
    }

    public static class SiteCurrentPowerFlow {
        public @Nullable String unit;

        @SerializedName(GRID)
        public @Nullable Value grid;

        @SerializedName(LOAD)
        public @Nullable Value load;

        @SerializedName(PV)
        public @Nullable Value pv;

        @SerializedName(STORAGE)
        public @Nullable BatteryValue storage;

        public @Nullable List<Connection> connections;
    }

    private @Nullable SiteCurrentPowerFlow siteCurrentPowerFlow;

    public final @Nullable SiteCurrentPowerFlow getSiteCurrentPowerFlow() {
        return siteCurrentPowerFlow;
    }

    public final void setSiteCurrentPowerFlow(SiteCurrentPowerFlow siteCurrentPowerFlow) {
        this.siteCurrentPowerFlow = siteCurrentPowerFlow;
    }
}
