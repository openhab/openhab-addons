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
package org.openhab.binding.mideaac.internal.callbacks;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.devices.a1.A1Response;
import org.openhab.binding.mideaac.internal.devices.ac.EnergyResponse;
import org.openhab.binding.mideaac.internal.devices.ac.HumidityResponse;
import org.openhab.binding.mideaac.internal.devices.ac.Response;
import org.openhab.binding.mideaac.internal.devices.ac.TemperatureResponse;
import org.openhab.binding.mideaac.internal.devices.capabilities.CapabilitiesResponse;

/**
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public interface HumidifierCallback extends Callback {
    /**
     * Updates dehumidifier channels with (0xC8) response.
     *
     * @param a1Response The humidifier (0xC8) response from the device used to update properties.
     */
    void updateChannels(A1Response a1Response);

    default void updateChannels(Response response) {
        // No implementation needed for humidifier
    }

    /**
     * Updates dehumidifier channels with a capabilities response (0xB5).
     * 
     */
    void updateChannels(CapabilitiesResponse capabilitiesResponse);

    default void updateChannels(EnergyResponse energyResponse) {
        // No implementation needed for humidifier
    }

    default void updateHumidityFromEnergy(EnergyResponse energyResponse) {
        // No implementation needed for humidifier
    }

    default void updateChannels(HumidityResponse humidityResponse) {
        // No implementation needed for humidifier
    }

    default void updateChannels(TemperatureResponse temperatureResponse) {
        // No implementation needed for humidifier
    }
}
