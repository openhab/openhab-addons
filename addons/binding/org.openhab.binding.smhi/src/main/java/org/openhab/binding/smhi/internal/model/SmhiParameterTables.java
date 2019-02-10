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
package org.openhab.binding.smhi.internal.model;

import java.util.HashMap;

/**
 * The {@link smhiParameterTables} is the Java class used to map the JSON response to an SMHI
 * request.
 *
 * @author Michael Parment - Initial contribution
 */
public class SmhiParameterTables {

    private HashMap<Integer, String> condition = new HashMap<Integer, String>();

    {
        condition.put(1, "Clear sky");
        condition.put(2, "Nearly clear sky");
        condition.put(3, "Variable cloudiness");
        condition.put(4, "Halfclear sky");
        condition.put(5, "Cloudy sky");
        condition.put(6, "Overcast");
        condition.put(7, "Fog");
        condition.put(8, "Light rain showers");
        condition.put(9, "Moderate rain showers");
        condition.put(10, "Heavy rain showers");
        condition.put(11, "Thunderstorm");
        condition.put(12, "Light sleet showers");
        condition.put(13, "Moderate sleet showers");
        condition.put(14, "Heavy sleet showers");
        condition.put(15, "Light snow showers");
        condition.put(16, "Moderate snow showers");
        condition.put(17, "Heavy snow showers");
        condition.put(18, "Light rain");
        condition.put(19, "Moderate rain");
        condition.put(20, "Heavy rain");
        condition.put(21, "Thunder");
        condition.put(22, "Light sleet");
        condition.put(23, "Moderate sleet");
        condition.put(24, "Heavy sleet");
        condition.put(25, "Light snowfall");
        condition.put(26, "Moderate snowfall");
        condition.put(27, "Heavy snowfall");
    }

    public HashMap<Integer, String> getCondition() {
        return condition;
    }

    private HashMap<Integer, String> pcat = new HashMap<Integer, String>();

    {
        pcat.put(0, "No precipitation");
        pcat.put(1, "Snow");
        pcat.put(2, "Snow and rain");
        pcat.put(3, "Rain");
        pcat.put(4, "Drizzle");
        pcat.put(5, "Freezing rain");
        pcat.put(6, "Freezing drizzle");
    }

    public HashMap<Integer, String> getPcat() {
        return pcat;
    }
}
