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
package org.openhab.binding.sbus.handler.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration class for the temperature channel.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class TemperatureChannelConfig {

    /**
     * The physical channel number on the Sbus device
     */
    public int channelNumber = 1;

    /**
     * The unit to use for temperature readings (CELSIUS or FAHRENHEIT)
     */
    public String unit = "CELSIUS";

    /**
     * Validates the configuration parameters.
     * 
     * @return true if the configuration is valid
     */
    public boolean isValid() {
        return channelNumber > 0 && ("CELSIUS".equals(unit) || "FAHRENHEIT".equals(unit));
    }

    /**
     * Checks if the configured unit is Fahrenheit
     *
     * @return true if Fahrenheit is configured, false otherwise
     */
    public boolean isFahrenheit() {
        return "FAHRENHEIT".equals(unit);
    }
}
