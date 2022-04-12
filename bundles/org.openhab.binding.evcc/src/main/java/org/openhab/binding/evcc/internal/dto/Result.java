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
package org.openhab.binding.evcc.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the result object of the status response (/api/state).
 *
 * @author Florian Hotze - Initial contribution
 */
public class Result {

    // TO DO LATER
    // @SerializedName("auth")
    // private Auth auth;

    @SerializedName("batteryConfigured")
    private Boolean batteryConfigured;

    @SerializedName("batteryPower")
    private float batteryPower;

    @SerializedName("batterySoC")
    private int batterySoC;

    @SerializedName("currency")
    private String currency;

    @SerializedName("gridConfigured")
    private Boolean gridConfigured;

    @SerializedName("gridPower")
    private float gridPower;

    @SerializedName("homePower")
    private float homePower;

    @SerializedName("loadpoints")
    private Loadpoint[] loadpoints;

    @SerializedName("prioritySoC")
    private int batteryPrioritySoC;

    @SerializedName("pvConfigured")
    private Boolean pvConfigured;

    @SerializedName("pvPower")
    private float pvPower;

    @SerializedName("savingsAmount")
    private float savingsAmount;

    @SerializedName("savingsEffectivePrice")
    private float savingsEffectivePrice;

    @SerializedName("savingsGridCharged")
    private float savingsGridCharged;

    @SerializedName("savingsSelfConsumptionCharged")
    private float savingsSelfConsumptionCharged;

    @SerializedName("savingsSelfConsumptionPercent")
    private float savingsSelfConsumptionPercent;

    @SerializedName("savingsSince")
    private int savingsSince;

    @SerializedName("savingsTotalCharged")
    private float savingsTotalCharged;

    @SerializedName("siteTitle")
    private String siteTitle;

    @SerializedName("tariffFeedIn")
    private float tariffFeedIn;

    @SerializedName("tariffGrid")
    private float tariffGrid;

    /**
     * @return whether battery is configured
     */
    public Boolean getBatteryConfigured() {
        return batteryConfigured;
    }

    /**
     * @return the battery's power
     */
    public float getBatteryPower() {
        return batteryPower;
    }

    /**
     * @return the battery's priority state of charge
     */
    public int getBatteryPrioritySoC() {
        return batteryPrioritySoC;
    }

    /**
     * @return the battery's state of charge
     */
    public int getBatterySoC() {
        return batterySoC;
    }

    /**
     * @return the currency used for savings calculation
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @return whether grid is configured
     */
    public Boolean getGridConfigured() {
        return gridConfigured;
    }

    /**
     * @return the grid's power
     */
    public float getGridPower() {
        return gridPower;
    }

    /**
     * @return the home's power
     */
    public float getHomePower() {
        return homePower;
    }

    /**
     * @return the loadpoints
     */
    public Loadpoint[] getLoadpoints() {
        return loadpoints;
    }

    /**
     * @return whether pv is configured
     */
    public Boolean getPvConfigured() {
        return pvConfigured;
    }

    /**
     * @return the pv's power
     */
    public float getPvPower() {
        return pvPower;
    }

    /**
     * @return the savings amount
     */
    public float getSavingsAmount() {
        return savingsAmount;
    }

    /**
     * @return the savings effective price
     */
    public float getSavingsEffectivePrice() {
        return savingsEffectivePrice;
    }

    /**
     * @return the savings grid charged
     */
    public float getSavingsGridCharged() {
        return savingsGridCharged;
    }

    /**
     * @return the savings self consumption charged
     */
    public float getSavingsSelfConsumptionCharged() {
        return savingsSelfConsumptionCharged;
    }

    /**
     * @return the savings self consumption percent
     */
    public float getSavingsSelfConsumptionPercent() {
        return savingsSelfConsumptionPercent;
    }

    /**
     * @return the UNIX timestamp since the savings
     */
    public int getSavingsSince() {
        return savingsSince;
    }

    /**
     * @return the savings total charged
     */
    public float getSavingsTotalCharged() {
        return savingsTotalCharged;
    }

    /**
     * @return the site's title
     */
    public String getSiteTitle() {
        return siteTitle;
    }

    /**
     * @return the feed-in tariff
     */
    public float getTariffFeedIn() {
        return tariffFeedIn;
    }

    /**
     * @return the grid's tariff
     */
    public float getTariffGrid() {
        return tariffGrid;
    }
}
