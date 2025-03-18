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
package org.openhab.binding.homewizard.internal.devices.plug_in_battery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.devices.HomeWizardEnergyMeterMeasurementPayload;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard Energy Socket.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 *
 */
@NonNullByDefault
public class HomeWizardPlugInBatteryMeasurementPayload extends HomeWizardEnergyMeterMeasurementPayload {

    @SerializedName("state_of_charge_pct")
    private double stateOfCharge;

    private double cycles;

    /**
     * Getter for the state of charge
     *
     * @return state of charge
     */
    public double getStateOfCharge() {
        return stateOfCharge;
    }

    /**
     * Getter for the number of cycles
     *
     * @return number of cycles
     */
    public int getCycles() {
        return (int) cycles;
    }

    @Override
    public String toString() {
        return String.format("""
                Battery Data [stateOfCharge: %f cycles: %f"]
                 """, stateOfCharge, cycles);
    }
}
