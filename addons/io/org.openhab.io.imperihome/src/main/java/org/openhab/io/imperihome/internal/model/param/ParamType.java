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
package org.openhab.io.imperihome.internal.model.param;

/**
 * Parameter type enumeration. Contains the ISS API parameter key string.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public enum ParamType {

    DEFAULT_ICON("defaultIcon"),
    GENERIC_VALUE("Value"),
    TEMPERATURE_VALUE("Value"),
    TEMPERATURE_DUAL("temp"),
    LUMINOSITY_VALUE("Value"),
    HYGROMETRY_DUAL("hygro"),
    HYGROMETRY_VALUE("Value"),
    CO2_VALUE("Value"),
    PRESSURE_VALUE("Value"),
    NOISE_VALUE("Value"),
    RAIN_VALUE("Value"),
    UV_VALUE("Value"),
    DIMMABLE("dimmable"),
    ENERGY("Energy"),
    STATUS("Status"),
    MULTISWITCH_VALUE("Value"),
    CHOICES("Choices"),
    COLOR("color"),
    LEVEL("Level"),
    WHITE_CHANNEL("whitechannel"),
    WATTS("Watts"),
    KWH("ConsoTotal"),
    ARMABLE("armable"),
    ARMED("Armed"),
    ACKABLE("ackable"),
    TRIPPED("Tripped"),
    LAST_TRIP("lasttrip"),
    ACCUMULATION("Accumulation"),
    SPEED("Speed"),
    DIRECTION("Direction"),
    STOPPABLE("stopable"),
    PULSEABLE("pulseable"),

    //Thermostat parameters
    CUR_MODE("curmode"),
    CUR_SETPOINT("cursetpoint"),
    CUR_TEMP("curtemp"),
    MIN_VAL("minVal"),
    MAX_VAL("maxVal"),
    AVAIL_MODE("availablemodes"),
    STEP("step");

    private final String apiString;

    ParamType(String apiString) {
        this.apiString = apiString;
    }

    public String getApiString() {
        return apiString;
    }

}
