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
package org.openhab.binding.homewizard.internal.devices.energy_socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.devices.HomeWizardEnergyMeterMeasurementPayload;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard Energy Socket.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class HomeWizardEnergySocketMeasurementPayload extends HomeWizardEnergyMeterMeasurementPayload {

    @SerializedName(value = "reactive_power_var", alternate = "active_reactive_power_var")
    private double reactivePower;
    @SerializedName(value = "apparent_power_va", alternate = "active_apparent_power_va")
    private double apparentPower;
    @SerializedName(value = "power_factor", alternate = "active_power_factor")
    private double powerFactor;

    /**
     * Getter for the active reactive power
     *
     * @return active reactive power
     */
    public double getReactivePower() {
        return reactivePower;
    }

    /**
     * Getter for the active apparent power
     *
     * @return active apparent power
     */
    public double getApparentPower() {
        return apparentPower;
    }

    /**
     * Getter for the active power factor
     *
     * @return active power factor
     */
    public double getPowerFactor() {
        return powerFactor;
    }

    @Override
    public String toString() {
        return super.toString() + "  " + String.format("""
                        Energy Socket Data [
                        reactivePower: %f apparentPower: %f powerFactor: %f]
                """, reactivePower, apparentPower, powerFactor);
    }
}
