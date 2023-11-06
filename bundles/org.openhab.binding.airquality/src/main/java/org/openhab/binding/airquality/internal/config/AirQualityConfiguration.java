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
package org.openhab.binding.airquality.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.airquality.internal.AirQualityException;

/**
 * The {@link AirQualityConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Kuba Wolanin - Initial contribution
 */
@NonNullByDefault
public class AirQualityConfiguration {
    public static final String LOCATION = "location";
    public static final String STATION_ID = "stationId";

    public String location = "";
    public int stationId = 0;
    public int refresh = 60;

    public void checkValid() throws AirQualityException {
        if (location.trim().isEmpty() && stationId == 0) {
            throw new AirQualityException("Either 'location' or 'stationId' is mandatory and must be configured");
        }
        if (refresh < 30) {
            throw new AirQualityException("Parameter 'refresh' must be at least 30 minutes");
        }
    }
}
