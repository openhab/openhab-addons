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
package org.openhab.binding.mideaac.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.handler.capabilities.CapabilitiesResponse;

/**
 * The {@link Response} performs the polling byte data stream decoding
 * The {@link CapabilitiesResponse} performs the capability byte data stream decoding
 * The {@link EnergyResponse} performs the energy byte stream data decoding
 * The {@link HumidityResponse} performs decoding of unsolicited message 0xA0
 * The {@link TemperatureResponse} performs decoding of unsolicited message 0xA1
 * 
 * @author Leo Siepel - Initial contribution
 * @author Bob Eckhoff - added additional Callbacks after Response
 */
@NonNullByDefault
public interface Callback {
    /**
     * Updates channels with a standard response (0xC0).
     *
     * @param response The standard response from the device used to update channels.
     */
    void updateChannels(Response response);

    /**
     * Updates channels with a capabilities response (0xB5).
     *
     * @param capabilitiesResponse The capabilities response from the device used to update properties.
     */
    void updateChannels(CapabilitiesResponse capabilitiesResponse);

    /**
     * Updates channels with a Energy response (0xC1 - 0x44).
     *
     * @param energyResponse The Energy response from the device used to update energy.
     */
    void updateChannels(EnergyResponse energyResponse);

    /**
     * Updates humidity with a Energy response (0xC1 - 0x45).
     *
     * @param energyResponse The Energy response from a humidity Poll used to update humidity.
     */
    void updateHumidityFromEnergy(EnergyResponse energyResponse);

    /**
     * Updates channels with an unsolicted Humidity Response (0xA0).
     *
     * @param humidityResponse The unsolicited (0xA0) response from the device used to update properties.
     */
    void updateChannels(HumidityResponse humidityResponse);

    /**
     * Updates channels with an unsolicited Temperature response (0xA1).
     *
     * @param temperatureResponse The unsolicited (0xA1) response from the device used to update properties.
     */
    void updateChannels(TemperatureResponse temperatureResponse);
}
