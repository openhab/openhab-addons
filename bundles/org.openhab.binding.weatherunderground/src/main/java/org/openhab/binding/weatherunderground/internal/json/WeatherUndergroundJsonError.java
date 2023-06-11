/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * The {@link WeatherUndergroundJsonError} is the Java class used
 * to map the entry "response.error" from the JSON response to a Weather Underground
 * request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonError {

    private String type;
    private String description;

    public WeatherUndergroundJsonError() {
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
