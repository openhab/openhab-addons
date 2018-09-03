/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.te923;

/**
 * Describe all the channels in an enum with the position in the Te923Con command.
 *
 * @author Gabriel Klein - Initial contribution
 */
public enum Te923Channel {

    // Response from Te923Con
    // T0:H0:T1:H1:T2:H2:T3:H3:T4:H4:T5:H5:PRESS:UV:FC:STORM:WD:WS:WG:WC:RC
    //
    // T0 - temperature from internal sensor in °C
    // H0 - humidity from internal sensor in % rel
    // T1..5 - temperature from external sensor 1..4 in °C
    // H1..5 - humidity from external sensor 1...4 in % rel
    // PRESS - air pressure in mBar
    // UV - UV index from UV sensor
    // FC - station forecast, see below for more details
    // STORM - stormwarning; 0 - no warning, 1 - fix your dog
    // WD - wind direction in n x 22.5°; 0 -> north
    // WS - wind speed in m/s
    // WG - wind gust speed in m/s
    // WC - windchill temperature in °C
    // RC - rain counter (maybe since station starts measurement) as value

    // List all channels
    // TIME("time", 0),
    TEMPERATURE_0("temperature_c0", 1),
    HUMIDITY_0("humidity_c0", 2),
    TEMPERATURE_1("temperature_c1", 3),
    HUMIDITY_1("humidity_c1", 4),
    TEMPERATURE_2("temperature_c2", 5),
    HUMIDITY_2("humidity_c2", 6),
    TEMPERATURE_3("temperature_c3", 7),
    HUMIDITY_3("humidity_c3", 8),
    TEMPERATURE_4("temperature_c4", 9),
    HUMIDITY_4("humidity_c4", 10),
    TEMPERATURE_5("temperature_c5", 11),
    HUMIDITY_5("humidity_c5", 12),
    PRESSURE("pressure", 13),
    UV("uv", 14),
    // FC("fc", 15),
    // STORM("storm", 16),
    WIND_DIRECTION("wind-direction", 17),
    WIND_SPEED("wind-speed", 18),
    // WIND_GUST("wind-gust", 19),
    // WIND_CHILL("wind-chill", 20),
    RAIN_RAW("rain-raw", 21);

    private final String mappingName;

    private final int positionInResponse;

    /**
     * Construct the enum
     *
     * @param mappingName is the mapping id of this parameter
     * @param positionInResponse is the position of this parameter in the response of the te923con command line
     */
    private Te923Channel(String mappingName, int positionInResponse) {
        this.mappingName = mappingName;
        this.positionInResponse = positionInResponse;
    }

    /**
     * Get name used for mapping
     *
     * @return the mapping id of this parameter
     */
    public String getMappingName() {
        return mappingName;
    }

    /**
     * Get position in cmd response
     *
     * @return the position of this parameter in the response of the te923con command line
     */
    public int getPositionInResponse() {
        return positionInResponse;
    }

}
