/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.bloomsky.internal.config;

import static org.openhab.binding.bloomsky.internal.BloomSkyBindingConstants.*;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bloomsky.internal.handler.BloomSkyBridgeHandler;

/**
 * The {@link BloomSkyBridgeConfiguration} is the class used to match the {@link BloomSkyBridgeHandler} configuration.
 *
 * @author Dave J Schoepel - Initial contribution
 *
 */
@NonNullByDefault
public class BloomSkyBridgeConfiguration {

    // Use country code to determine default units for the BloomSky observations
    public final String countryCode = Locale.getDefault().getCountry();

    public @Nullable String apikey; // API Key from BloomSky Device Owner Account
    public int refreshInterval = 5; // Minimum interval is five minutes

    // Initialize with default units based on country the code, start with Imperial units
    public @Nullable String units = IMPERIAL_UNITS;

    /**
     * @param countryCode to test for default display units
     * @return display unit constant based on country code, so that can be used to set the bridge configuration
     */
    public String setDisplayUnits(String countryCode) {
        // Assuming country code is valid using Local to retrieve it
        switch (countryCode) {
            case "US": // United States
            case "LR": // Liberia
            case "MM": // Myanmar
                return IMPERIAL_UNITS;
            default:
                return METRIC_UNITS;
        }
    }

    /**
     * @return Bridge configuration API key
     */
    public @Nullable String getApikey() {
        return apikey;
    }

    /**
     * @return BlooomSky observations refresh interval (minutes) from Bridge configuration
     */
    public int getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * @return Display units from Bridge configuration to be used for API requests
     */
    public @Nullable String getUnits() {
        return units;
    }
}
