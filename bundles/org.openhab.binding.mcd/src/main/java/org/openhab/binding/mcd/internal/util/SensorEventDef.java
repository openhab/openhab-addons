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
package org.openhab.binding.mcd.internal.util;

/**
 * enum that lists SensorEventDefinitions
 * 
 * @author Simon Dengler - Initial contribution
 */
public enum SensorEventDef {
    DO_NOT_USE,
    UNDEFINED,
    BED_EXIT,
    BED_ENTRY,
    FALL,
    CHANGE_POSITION,
    BATTERY_STATE,
    INACTIVITY,
    ALARM,
    OPEN,
    CLOSE,
    ON,
    OFF,
    ACTIVITY,
    URINE,
    GAS,
    VITAL_VALUE,
    ROOM_EXIT,
    ROOM_ENTRY,
    REMOVE_SENSOR,
    SIT_DOWN,
    STAND_UP,
    INACTIVITY_ROOM,
    SMOKE_ALARM,
    HEAT,
    COLD,
    QUALITY_AIR,
    ALARM_AIR,
    ROOM_TEMPERATURE,
    HUMIDITY,
    AIR_PRESSURE,
    CO2,
    INDEX_UV;
}
