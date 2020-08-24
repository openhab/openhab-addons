/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.haywardomnilogic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HaywardBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matt Myers - Initial Contribution
 */
@NonNullByDefault
public class HaywardBindingConstants {

    private static final String BINDING_ID = "haywardomnilogic";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BACKYARD = new ThingTypeUID(BINDING_ID, "backyard");
    public static final ThingTypeUID THING_TYPE_BOW = new ThingTypeUID(BINDING_ID, "bow");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_CHLORINATOR = new ThingTypeUID(BINDING_ID, "chlorinator");
    public static final ThingTypeUID THING_TYPE_COLORLOGIC = new ThingTypeUID(BINDING_ID, "colorlogic");
    public static final ThingTypeUID THING_TYPE_CSAD = new ThingTypeUID(BINDING_ID, "csad");
    public static final ThingTypeUID THING_TYPE_FILTER = new ThingTypeUID(BINDING_ID, "filter");
    public static final ThingTypeUID THING_TYPE_HEATER = new ThingTypeUID(BINDING_ID, "heater");
    public static final ThingTypeUID THING_TYPE_PUMP = new ThingTypeUID(BINDING_ID, "pump");
    public static final ThingTypeUID THING_TYPE_RELAY = new ThingTypeUID(BINDING_ID, "relay");
    public static final ThingTypeUID THING_TYPE_SENSOR = new ThingTypeUID(BINDING_ID, "sensor");
    public static final ThingTypeUID THING_TYPE_VIRTUALHEATER = new ThingTypeUID(BINDING_ID, "virtualHeater");

    // List of all Channel ids (bridge)
    // No Channels

    // List of all Channel ids (backyard)
    public static final String CHANNEL_BACKYARD_AIRTEMP = "backyardAirTemp";
    public static final String CHANNEL_BACKYARD_STATUS = "backyardStatus";
    public static final String CHANNEL_BACKYARD_STATE = "backyardState";
    public static final String CHANNEL_BACKYARD_ALARM1 = "backyardAlarm1";
    public static final String CHANNEL_BACKYARD_ALARM2 = "backyardAlarm2";
    public static final String CHANNEL_BACKYARD_ALARM3 = "backyardAlarm3";
    public static final String CHANNEL_BACKYARD_ALARM4 = "backyardAlarm4";
    public static final String CHANNEL_BACKYARD_ALARM5 = "backyardAlarm5";

    // List of all Channel ids (bow)
    public static final String CHANNEL_BOW_WATERTEMP = "bowWaterTemp";
    public static final String CHANNEL_BOW_FLOW = "bowFlow";

    // List of all Channel ids (chlorinator)
    public static final String CHANNEL_CHLORINATOR_ENABLE = "chlorEnable";
    public static final String CHANNEL_CHLORINATOR_OPERATINGMODE = "chlorOperatingMode";
    public static final String CHANNEL_CHLORINATOR_TIMEDPERCENT = "chlorTimedPercent";
    public static final String CHANNEL_CHLORINATOR_SCMODE = "chlorScMode";
    public static final String CHANNEL_CHLORINATOR_ERROR = "chlorError";
    public static final String CHANNEL_CHLORINATOR_ALERT = "chlorAlert";
    public static final String CHANNEL_CHLORINATOR_AVGSALTLEVEL = "chlorAvgSaltLevel";
    public static final String CHANNEL_CHLORINATOR_INSTANTSALTLEVEL = "chlorInstantSaltLevel";
    public static final String CHANNEL_CHLORINATOR_STATUS = "chlorStatus";

    // List of all Channel ids (colorlogic)
    public static final String CHANNEL_COLORLOGIC_ENABLE = "colorLogicLightEnable";
    public static final String CHANNEL_COLORLOGIC_LIGHTSTATE = "colorLogicLightState";
    public static final String CHANNEL_COLORLOGIC_CURRENTSHOW = "colorLogicLightCurrentShow";

    // List of all Channel ids (filter)
    public static final String CHANNEL_FILTER_ENABLE = "filterEnable";
    public static final String CHANNEL_FILTER_VALVEPOSITION = "filterValvePosition";
    public static final String CHANNEL_FILTER_SPEED = "filterSpeed";
    public static final String CHANNEL_FILTER_STATE = "filterState";
    public static final String CHANNEL_FILTER_LASTSPEED = "filterLastSpeed";

    public static final String PROPERTY_FILTER_MINPUMPSPEED = "minPumpSpeed";
    public static final String PROPERTY_FILTER_MAXPUMPSPEED = "maxPumpSpeed";
    public static final String PROPERTY_FILTER_MINPUMPRPM = "minPumpRPM";
    public static final String PROPERTY_FILTER_MAXPUMPRPM = "maxPumpRPM";

    // List of all Channel ids (heater)
    public static final String CHANNEL_HEATER_STATE = "heaterState";
    public static final String CHANNEL_HEATER_TEMP = "heaterTemp";
    public static final String CHANNEL_HEATER_ENABLE = "heaterEnable";

    // List of all Channel ids (pump)
    public static final String CHANNEL_PUMP_ENABLE = "pumpEnable";
    public static final String CHANNEL_PUMP_SPEED = "pumpSpeed";

    public static final String PROPERTY_PUMP_MINPUMPSPEED = "minPumpSpeed";
    public static final String PROPERTY_PUMP_MAXPUMPSPEED = "minPumpSpeed";
    public static final String PROPERTY_PUMP_MINPUMPRPM = "minPumpRPM";
    public static final String PROPERTY_PUMP_MAXPUMPRPM = "maxPumpRPM";

    // List of all Channel ids (relay)
    public static final String CHANNEL_RELAY_STATE = "relayState";

    // List of all Channel ids (sensor)
    public static final String CHANNEL_SENSOR_DATA = "sensorData";

    // List of all Channel ids (virtualHeater)
    public static final String CHANNEL_VIRTUALHEATER_CURRENTSETPOINT = "virtualHeaterCurrentSetpoint";
    public static final String CHANNEL_VIRTUALHEATER_ENABLE = "virtualHeaterEnable";

    // The properties associated with the thing
    public static final String PROPERTY_VERSION = "Property Version";
    public static final String PROPERTY_STATUSVERSION = "Property Status Version";
    public static final String PROPERTY_CONFIGUPDATEDTIME = "Property Configuration Last Updated Date/Time";
    public static final String MESSAGEVERSION = "Property Message Version";
    public static final String PROPERTY_SYSTEM_ID = "Property system ID";
    public static final String PROPERTY_TYPE = "propertyType";
    public static final String PROPERTY_BOWNAME = "BOW Name";
    public static final String PROPERTY_BOWID = "BOW ID";
}
