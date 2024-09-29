/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mcd.internal.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * class that contains MCD SensorEventDefinitions
 * 
 * @author Simon Dengler - Initial contribution
 */
@NonNullByDefault
public class SensorEventDef {

    // Sensor Events in order of their ids as specified by C&S syncapi
    private static final String[] EVENT_DEFINITION_ARRAY = { "", "UNDEFINED", "BEDEXIT", "BEDENTRY", "FALL",
            "CHANGEPOSITION", "BATTERYSTATE", "INACTIVITY", "ALARM", "OPEN", "CLOSE", "ON", "OFF", "ACTIVITY",
            "CAPACITY", "GAS", "VITALVALUE", "ROOMEXIT", "ROOMENTRY", "REMOVESENSOR", "SITDOWN", "STANDUP",
            "INACTIVITYROOM", "SMOKEALARM", "HEAT", "COLD", "QUALITYAIR", "ALARMAIR", "ROOMTEMPERATURE", "HUMIDITY",
            "AIRPRESSURE", "CO2", "INDEXUV", "WEARTIME", "FIRSTURINE", "NEWDIAPER", "DIAPERREMOVED", "NOCONNECTION",
            "LOWBATTERY", "CONTROLLSENSOR", "LYING", "SPILLED", "DAMAGED", "GEOEXIT", "GEOENTRY", "WALKING", "RESTING",
            "TURNAROUND", "HOMEEMERGENCY", "TOILETFLUSH", "DORSALPOSITION", "ABDOMINALPOSITION", "LYINGLEFT",
            "LYINGRIGHT", "LYINGHALFLEFT", "LYINGHALFRIGHT", "MOVEMENT", "PRESENCE", "NUMBERPERSONS",
            "BRIGHTNESSZONE" };
    private static ArrayList<String> sensorEventDefinition = new ArrayList<>(Arrays.asList(EVENT_DEFINITION_ARRAY));

    public static ArrayList<String> getSensorEventDefinition() {
        return sensorEventDefinition;
    }

    public static int getSensorEventId(String eventName) {
        return sensorEventDefinition.indexOf(eventName);
    }
}
