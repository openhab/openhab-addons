/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.weathercompany.internal.config;

import org.openhab.binding.weathercompany.internal.WeatherCompanyBindingConstants;

/**
 * The {@link WeatherCompanyConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class WeatherCompanyConfig {
    /**
     * API key
     */
    public String apiKey;

    /**
     * Type of location (postal, geocode, etc.)
     */
    public String locationType;

    /**
     * Postal code
     */
    public String postalCode;

    /**
     * Geocode
     */
    public String geocode;

    /**
     * IATA code
     */
    public String iataCode;

    /**
     * Language
     */
    public String language;

    /**
     * Personal Weather Station Id
     */
    public String pwsStationId;

    /**
     * Interval with which forecast will be updated.
     */
    public int refreshInterval;

    @Override
    public String toString() {
        return "WeatherCompanyConfig: { apiKey=" + WeatherCompanyBindingConstants.REPLACE_API_KEY + ", locationType="
                + locationType + ", postalCode=" + postalCode + ", geocode=" + geocode + ", iataCode=" + iataCode
                + ", language=" + language + ", pwsStationId=" + pwsStationId + ", refreshInterval=" + refreshInterval
                + " }";
    }
}
