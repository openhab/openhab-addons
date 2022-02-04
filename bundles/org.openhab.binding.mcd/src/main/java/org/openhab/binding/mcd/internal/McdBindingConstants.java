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
package org.openhab.binding.mcd.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link McdBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Simon Dengler - Initial contribution
 */
@NonNullByDefault
public class McdBindingConstants {

    private static final String BINDING_ID = "mcd";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MCD_BRIDGE = new ThingTypeUID(BINDING_ID, "mcdBridge");
    public static final ThingTypeUID THING_TYPE_SENSOR = new ThingTypeUID(BINDING_ID, "mcdSensor");

    // List of all Channel ids
    public static final String LOGIN_STATUS = "login";
    public static final String LAST_VALUE = "lastValue";
    public static final String BED_STATUS = "bedStatus";
    public static final String FALL = "fall";
    public static final String CHANGE_POSITION = "changePosition";
    public static final String BATTERY_STATE = "batteryState";
    public static final String INACTIVITY = "inactivity";
    public static final String ALARM = "alarm";
    public static final String OPEN_SHUT = "openShut";
    public static final String LIGHT = "light";
    public static final String ACTIVITY = "activity";
    public static final String URINE = "urine";
    public static final String GAS = "gas";
    public static final String PRESENCE = "presence";
    public static final String REMOVED_SENSOR = "removedSensor";
    public static final String SIT_STATUS = "sitStatus";
    public static final String INACTIVITY_ROOM = "inactivityRoom";
    public static final String SMOKE_ALARM = "smokeAlarm";
    public static final String HEAT = "heat";
    public static final String COLD = "cold";
    public static final String ALARM_AIR = "alarmAir";
}
