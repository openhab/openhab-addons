/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solarmax.internal.connector;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SolarMaxCommandKey} enum defines the commands that are understood by the SolarMax device
 *
 * @author Jamie Townsend - Initial contribution
 */
@NonNullByDefault
public enum SolarMaxCommandKey {
    // for further commands, that are not implemented here, see this binding's README.md file

    // Valid commands which returned a non-null value during testing
    buildNumber("BDN"), //
    startups("CAC"), //
    acPhase1Current("IL1"), //
    acPhase2Current("IL2"), //
    acPhase3Current("IL3"), //
    energyGeneratedToday("KDY"), //
    operatingHours("KHR"), //
    energyGeneratedYesterday("KLD"), //
    energyGeneratedLastMonth("KLM"), //
    energyGeneratedLastYear("KLY"), //
    energyGeneratedThisMonth("KMT"), //
    energyGeneratedTotal("KT0"), //
    energyGeneratedThisYear("KYR"), //
    currentPowerGenerated("PAC"), //
    softwareVersion("SWV"), //
    heatSinkTemperature("TKK"), //
    acFrequency("TNF"), //
    acPhase1Voltage("UL1"), //
    acPhase2Voltage("UL2"), //
    acPhase3Voltage("UL3"), //
    UNKNOWN("UNKNOWN") // really unknown - shouldn't ever be sent to the device
    ;

    private String commandKey;

    private SolarMaxCommandKey(String commandKey) {
        this.commandKey = commandKey;
    }

    public String getCommandKey() {
        return this.commandKey;
    }

    public static SolarMaxCommandKey getKeyFromString(String commandKey) {
        for (SolarMaxCommandKey key : SolarMaxCommandKey.values()) {
            if (key.commandKey.equals(commandKey)) {
                return key;
            }
        }
        return UNKNOWN;
    }
}
