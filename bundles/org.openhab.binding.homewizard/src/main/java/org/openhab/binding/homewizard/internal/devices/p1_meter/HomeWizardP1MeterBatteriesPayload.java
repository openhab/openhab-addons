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
package org.openhab.binding.homewizard.internal.devices.p1_meter;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard devices.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 *
 */
@NonNullByDefault
public class HomeWizardP1MeterBatteriesPayload {

    private String mode = "";

    @SerializedName("power_w")
    private Double power = 0.0;

    @SerializedName("target_power_w")
    private Double targetPower = 0.0;

    @SerializedName("max_consumption_w")
    private Double maxConsumption = 0.0;

    @SerializedName("max_production_w")
    private Double maxProduction = 0.0;

    /**
     * Getter for the mode
     *
     * @return mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * Getter for the power in watt
     *
     * @return power
     */
    public int getPower() {
        return power.intValue();
    }

    /**
     * Getter for the target power in watt
     *
     * @return target power
     */
    public int getTargetPower() {
        return targetPower.intValue();
    }

    /**
     * Getter for the max consumption in watt
     *
     * @return max consumption
     */
    public int getMaxConsumption() {
        return maxConsumption.intValue();
    }

    /**
     * Getter for the max production in watt
     *
     * @return max production
     */
    public int getMaxProduction() {
        return maxProduction.intValue();
    }

    @Override
    public String toString() {
        return String.format("""
                Data [mode: %s power: %f targetPower: %f
                maxConsumption: %f maxProduction: %f

                """, mode, power, targetPower, maxConsumption, maxProduction);
    }
}
