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

package org.openhab.binding.haywardomnilogic.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HaywardBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matt Myers - Initial contribution
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
    public static final ThingTypeUID THING_TYPE_FILTER = new ThingTypeUID(BINDING_ID, "filter");
    public static final ThingTypeUID THING_TYPE_HEATER = new ThingTypeUID(BINDING_ID, "heater");
    public static final ThingTypeUID THING_TYPE_PUMP = new ThingTypeUID(BINDING_ID, "pump");
    public static final ThingTypeUID THING_TYPE_RELAY = new ThingTypeUID(BINDING_ID, "relay");
    public static final ThingTypeUID THING_TYPE_SENSOR = new ThingTypeUID(BINDING_ID, "sensor");
    public static final ThingTypeUID THING_TYPE_VIRTUALHEATER = new ThingTypeUID(BINDING_ID, "virtualHeater");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE);

    public static final Set<ThingTypeUID> THING_TYPES_UIDS = Set.of(HaywardBindingConstants.THING_TYPE_BACKYARD,
            HaywardBindingConstants.THING_TYPE_BOW, HaywardBindingConstants.THING_TYPE_BRIDGE,
            HaywardBindingConstants.THING_TYPE_CHLORINATOR, HaywardBindingConstants.THING_TYPE_COLORLOGIC,
            HaywardBindingConstants.THING_TYPE_FILTER, HaywardBindingConstants.THING_TYPE_HEATER,
            HaywardBindingConstants.THING_TYPE_PUMP, HaywardBindingConstants.THING_TYPE_RELAY,
            HaywardBindingConstants.THING_TYPE_SENSOR, HaywardBindingConstants.THING_TYPE_VIRTUALHEATER);

    // List of all Channel ids (bridge)
    // No Channels

    // List of all Channel ids (backyard)
    public static final String CHANNEL_BACKYARD_AIRTEMP = "backyardAirTemp";
    public static final String CHANNEL_BACKYARD_STATUS = "backyardStatus";
    public static final String CHANNEL_BACKYARD_STATE = "backyardState";

    // List of all Channel ids (bow)
    public static final String CHANNEL_BOW_WATERTEMP = "bowWaterTemp";
    public static final String CHANNEL_BOW_FLOW = "bowFlow";

    public static final String PROPERTY_BOW_TYPE = "type";
    public static final String PROPERTY_BOW_SHAREDTYPE = "sharedType";
    public static final String PROPERTY_BOW_SHAREDPRIORITY = "sharedPriority";
    public static final String PROPERTY_BOW_SHAREDEQUIPID = "sharedEquipmentSystemID";
    public static final String PROPERTY_BOW_SUPPORTSSPILLOVER = "supportsSpillover";
    public static final String PROPERTY_BOW_SIZEINGALLONS = "sizeInGallons";

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

    public static final String PROPERTY_CHLORINATOR_SHAREDTYPE = "chlorSharedType";
    public static final String PROPERTY_CHLORINATOR_MODE = "chlorMode";
    public static final String PROPERTY_CHLORINATOR_CELLTYPE = "cellType";
    public static final String PROPERTY_CHLORINATOR_DISPENSERTYPE = "dispenserType";

    // List of all Channel ids (colorlogic)
    public static final String CHANNEL_COLORLOGIC_ENABLE = "colorLogicLightEnable";
    public static final String CHANNEL_COLORLOGIC_LIGHTSTATE = "colorLogicLightState";
    public static final String CHANNEL_COLORLOGIC_CURRENTSHOW = "colorLogicLightCurrentShow";

    public static final String PROPERTY_COLORLOGIC_TYPE = "colorlogicType";

    // List of all Channel ids (filter)
    public static final String CHANNEL_FILTER_ENABLE = "filterEnable";
    public static final String CHANNEL_FILTER_VALVEPOSITION = "filterValvePosition";
    public static final String CHANNEL_FILTER_SPEEDPERCENT = "filterSpeedPercent";
    public static final String CHANNEL_FILTER_SPEEDRPM = "filterSpeedRpm";
    public static final String CHANNEL_FILTER_SPEEDSELECT = "filterSpeedSelect";
    public static final String CHANNEL_FILTER_STATE = "filterState";
    public static final String CHANNEL_FILTER_LASTSPEED = "filterLastSpeed";

    public static final String PROPERTY_FILTER_SHAREDTYPE = "filterSharedType";
    public static final String PROPERTY_FILTER_FILTERTYPE = "filterType";
    public static final String PROPERTY_FILTER_PRIMINGENABLED = "primingEnabled";
    public static final String PROPERTY_FILTER_MINSPEED = "minFilterPercent";
    public static final String PROPERTY_FILTER_MAXSPEED = "maxFilterPercent";
    public static final String PROPERTY_FILTER_MINRPM = "minFilterRPM";
    public static final String PROPERTY_FILTER_MAXRPM = "maxFilterRPM";
    public static final String PROPERTY_FILTER_LOWSPEED = "lowFilterSpeed";
    public static final String PROPERTY_FILTER_MEDSPEED = "mediumFilterSpeed";
    public static final String PROPERTY_FILTER_HIGHSPEED = "highFilterSpeed";
    public static final String PROPERTY_FILTER_CUSTOMSPEED = "customFilterSpeed";
    public static final String PROPERTY_FILTER_FREEZEPROTECTOVERRIDEINTERVAL = "freezeProtectOverrideInterval";

    // List of all Channel ids (heater)
    public static final String CHANNEL_HEATER_STATE = "heaterState";
    public static final String CHANNEL_HEATER_TEMP = "heaterTemp";
    public static final String CHANNEL_HEATER_ENABLE = "heaterEnable";

    public static final String PROPERTY_HEATER_TYPE = "type";
    public static final String PROPERTY_HEATER_HEATERTYPE = "heaterType";
    public static final String PROPERTY_HEATER_SHAREDEQUIPID = "sharedEquipmentSystemID";

    // List of all Channel ids (pump)
    public static final String CHANNEL_PUMP_ENABLE = "pumpEnable";
    public static final String CHANNEL_PUMP_SPEEDPERCENT = "pumpSpeedPercent";
    public static final String CHANNEL_PUMP_SPEEDRPM = "pumpSpeedRpm";
    public static final String CHANNEL_PUMP_SPEEDSELECT = "pumpSpeedSelect";
    public static final String CHANNEL_PUMP_STATE = "pumpState";
    public static final String CHANNEL_PUMP_LASTSPEED = "pumpLastSpeed";

    public static final String PROPERTY_PUMP_TYPE = "pumpType";
    public static final String PROPERTY_PUMP_FUNCTION = "pumpFunction";
    public static final String PROPERTY_PUMP_PRIMINGENABLED = "pumpPrimingEnabled";
    public static final String PROPERTY_PUMP_MINSPEED = "minPumpPercent";
    public static final String PROPERTY_PUMP_MAXSPEED = "maxPumpPercent";
    public static final String PROPERTY_PUMP_MINRPM = "minPumpRPM";
    public static final String PROPERTY_PUMP_MAXRPM = "maxPumpRPM";
    public static final String PROPERTY_PUMP_LOWSPEED = "lowPumpSpeed";
    public static final String PROPERTY_PUMP_MEDSPEED = "mediumPumpSpeed";
    public static final String PROPERTY_PUMP_HIGHSPEED = "highPumpSpeed";
    public static final String PROPERTY_PUMP_CUSTOMSPEED = "customPumpSpeed";

    // List of all Channel ids (relay)
    public static final String CHANNEL_RELAY_STATE = "relayState";

    public static final String PROPERTY_RELAY_TYPE = "relayType";
    public static final String PROPERTY_RELAY_FUNCTION = "relayFunction";

    // List of all Channel ids (sensor)
    public static final String CHANNEL_SENSOR_DATA = "sensorData";

    public static final String PROPERTY_SENSOR_TYPE = "sensorType";
    public static final String PROPERTY_SENSOR_UNITS = "sensorUnits";

    // List of all Channel ids (virtualHeater)
    public static final String CHANNEL_VIRTUALHEATER_CURRENTSETPOINT = "virtualHeaterCurrentSetpoint";
    public static final String CHANNEL_VIRTUALHEATER_ENABLE = "virtualHeaterEnable";

    public static final String PROPERTY_VIRTUALHEATER_SHAREDTYPE = "sharedType";
    public static final String PROPERTY_VIRTUALHEATER_MINSETTABLEWATERTEMP = "minSettableWaterTemp";
    public static final String PROPERTY_VIRTUALHEATER_MAXSETTABLEWATERTEMP = "maxSettableWaterTemp";
    public static final String PROPERTY_VIRTUALHEATER_MAXWATERTEMP = "maxWaterTemp";

    // The properties associated with all things
    public static final String PROPERTY_SYSTEM_ID = "systemID";
    public static final String PROPERTY_TYPE = "thingType";
    public static final String PROPERTY_BOWNAME = "bowName";
    public static final String PROPERTY_BOWID = "bowID";

    // Hayward Command html
    public static final String COMMAND_PARAMETERS = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request>";

    public static final String COMMAND_SCHEDULE = "<Parameter name=\"IsCountDownTimer\" dataType=\"bool\">false</Parameter>"
            + "<Parameter name=\"StartTimeHours\" dataType=\"int\">0</Parameter>"
            + "<Parameter name=\"StartTimeMinutes\" dataType=\"int\">0</Parameter>"
            + "<Parameter name=\"EndTimeHours\" dataType=\"int\">0</Parameter>"
            + "<Parameter name=\"EndTimeMinutes\" dataType=\"int\">0</Parameter>"
            + "<Parameter name=\"DaysActive\" dataType=\"int\">0</Parameter>"
            + "<Parameter name=\"Recurring\" dataType=\"bool\">false</Parameter>";
}
