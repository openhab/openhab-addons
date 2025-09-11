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
package org.openhab.binding.homewizard.internal.devices.water_meter;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard Energy Socket.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 *
 */
@NonNullByDefault
public class HomeWizardWaterMeterMeasurementPayload {

    @SerializedName("active_liter_lpm")
    private double activeLiter;

    @SerializedName("total_liter_m3")
    private double totalLiter;

    /**
     * Getter for the active liter per minute
     *
     * @return active liter per minute
     */
    public double getActiveLiter() {
        return activeLiter;
    }

    /**
     * Getter for the total liter
     *
     * @return total liter
     */
    public double getTotalLiter() {
        return totalLiter;
    }

    @Override
    public String toString() {
        return String.format("""
                Water Meter Data [activeLiter: %f totalLiter: %f"]
                 """, activeLiter, totalLiter);
    }
}
