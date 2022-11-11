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
package org.openhab.binding.evcc.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the result object of the status response (/api/state).
 * This DTO was written for evcc version 0.91.
 *
 * @author Florian Hotze - Initial contribution
 */
public class Result {
    // Data types from https://github.com/evcc-io/evcc/blob/master/api/api.go
    // and from https://docs.evcc.io/docs/reference/configuration/messaging/#msg

    // TO DO LATER
    // @SerializedName("auth")
    // private Auth auth;

    @SerializedName("batteryConfigured")
    private boolean batteryConfigured;

    @SerializedName("batteryPower")
    private double batteryPower;

    @SerializedName("batterySoC")
    private int batterySoC;

    @SerializedName("gridConfigured")
    private boolean gridConfigured;

    @SerializedName("gridPower")
    private double gridPower;

    @SerializedName("homePower")
    private double homePower;

    @SerializedName("loadpoints")
    private Loadpoint[] loadpoints;

    @SerializedName("prioritySoC")
    private double batteryPrioritySoC;

    @SerializedName("pvConfigured")
    private boolean pvConfigured;

    @SerializedName("pvPower")
    private double pvPower;

    @SerializedName("siteTitle")
    private String siteTitle;

    /**
     * @return whether battery is configured
     */
    public boolean getBatteryConfigured() {
        return batteryConfigured;
    }

    /**
     * @return battery's power
     */
    public double getBatteryPower() {
        return batteryPower;
    }

    /**
     * @return battery's priority state of charge
     */
    public double getBatteryPrioritySoC() {
        return batteryPrioritySoC;
    }

    /**
     * @return battery's state of charge
     */
    public int getBatterySoC() {
        return batterySoC;
    }

    /**
     * @return whether grid is configured
     */
    public boolean getGridConfigured() {
        return gridConfigured;
    }

    /**
     * @return grid's power
     */
    public double getGridPower() {
        return gridPower;
    }

    /**
     * @return home's power
     */
    public double getHomePower() {
        return homePower;
    }

    /**
     * @return all configured loadpoints
     */
    public Loadpoint[] getLoadpoints() {
        return loadpoints;
    }

    /**
     * @return whether pv is configured
     */
    public boolean getPvConfigured() {
        return pvConfigured;
    }

    /**
     * @return pv's power
     */
    public double getPvPower() {
        return pvPower;
    }

    /**
     * @return site's title/name
     */
    public String getSiteTitle() {
        return siteTitle;
    }
}
