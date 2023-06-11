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
package org.openhab.binding.weatherunderground.internal.json;

/**
 * The {@link WeatherUndergroundJsonResponse} is the Java class used
 * to map the entry "response" from the JSON response to a Weather Underground
 * request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonResponse {

    // Commented members indicate properties returned by the API not used by the binding

    // private String version;
    // private String termsofService;
    // private Object features;
    private WeatherUndergroundJsonError error;

    public WeatherUndergroundJsonResponse() {
    }

    /**
     * Get the error type returned by the Weather Underground service
     *
     * @return the error type or null if no error
     */
    public String getErrorType() {
        return (error == null) ? null : error.getType();
    }

    /**
     * Get the error description returned by the Weather Underground service
     *
     * @return the error description or null if no error
     */
    public String getErrorDescription() {
        return (error == null) ? null : error.getDescription();
    }
}
