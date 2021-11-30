/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeathomesystem.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FreeAtHomeSystemBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andras Uhrin - Initial contribution
 */
@NonNullByDefault
public class FreeAtHomeSystemBindingConstants {

    private static final String BINDING_ID = "freeathomesystem";

    // List of all Thing Type UIDs
    public static final String UNKNOWN_TYPE_ID = "unknown";
    public static final String BRIDGE_TYPE_ID = "bridge";
    public static final String ACTUATOR_TYPE_ID = "actuator";
    public static final String SCENE_TYPE_ID = "scene";
    public static final String RULE_TYPE_ID = "rule";
    public static final String THERMOSTAT_TYPE_ID = "thermostat";
    public static final String WINDOWSENSOR_TYPE_ID = "windowsensor";
    public static final String DOORRINGSENSOR_TYPE_ID = "doorringsensor";
    public static final String DOOROPENER_TYPE_ID = "dooropener";
    public static final String CORRIDORLIGHTSWITCH_TYPE_ID = "corridorlight";
    public static final String MUTEACTUATOR_TYPE_ID = "muteactuator";
    public static final String SHUTTERACTUATOR_TYPE_ID = "shutteractuator";
    public static final String DIMMINGACTUATOR_TYPE_ID = "dimmingactuator";

    // List of all Thing Type UIDs
    public static final ThingTypeUID UNKNOWN_TYPE_UID = new ThingTypeUID(BINDING_ID, UNKNOWN_TYPE_ID);
    public static final ThingTypeUID BRIDGE_TYPE_UID = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_ID);
    public static final ThingTypeUID ACTUATOR_TYPE_UID = new ThingTypeUID(BINDING_ID, ACTUATOR_TYPE_ID);
    public static final ThingTypeUID THERMOSTAT_TYPE_UID = new ThingTypeUID(BINDING_ID, THERMOSTAT_TYPE_ID);
    public static final ThingTypeUID WINDOWSENSOR_TYPE_UID = new ThingTypeUID(BINDING_ID, WINDOWSENSOR_TYPE_ID);
    public static final ThingTypeUID SCENE_TYPE_UID = new ThingTypeUID(BINDING_ID, SCENE_TYPE_ID);
    public static final ThingTypeUID RULE_TYPE_UID = new ThingTypeUID(BINDING_ID, RULE_TYPE_ID);
    public static final ThingTypeUID DOORRINGSENSOR_TYPE_UID = new ThingTypeUID(BINDING_ID, DOORRINGSENSOR_TYPE_ID);
    public static final ThingTypeUID DOOROPENER_TYPE_UID = new ThingTypeUID(BINDING_ID, DOOROPENER_TYPE_ID);
    public static final ThingTypeUID CORRIDORLIGHTSWITCH_TYPE_UID = new ThingTypeUID(BINDING_ID,
            CORRIDORLIGHTSWITCH_TYPE_ID);
    public static final ThingTypeUID MUTEACTUATOR_TYPE_UID = new ThingTypeUID(BINDING_ID, MUTEACTUATOR_TYPE_ID);
    public static final ThingTypeUID SHUTTERACTUATOR_TYPE_UID = new ThingTypeUID(BINDING_ID, SHUTTERACTUATOR_TYPE_ID);
    public static final ThingTypeUID DIMMINGACTUATOR_TYPE_UID = new ThingTypeUID(BINDING_ID, DIMMINGACTUATOR_TYPE_ID);

    // Switch/Actuator Channel ids
    public static final String SWITCH_CHANNEL_ID = "switchChannel";

    // Thermostat Channel ids
    public static final String THERMOSTAT_CHANNEL_SETPOINTTEMP_ID = "thermostatSetpointTemperature";
    public static final String THERMOSTAT_CHANNEL_MEASUREDTEMP_ID = "thermostatMeasuredTemperature";
    public static final String THERMOSTAT_CHANNEL_HEATDEMAND_ID = "thermostatHeatingDemand";
    public static final String THERMOSTAT_CHANNEL_HEATINGACTIVE_ID = "thermostatHeatingActive";
    public static final String THERMOSTAT_CHANNEL_STATE_ID = "thermostatState";
    public static final String THERMOSTAT_CHANNEL_ONOFFWITCH_ID = "thermostatOnoffSwitch";
    public static final String THERMOSTAT_CHANNEL_ECOSWITCH_ID = "thermostatEcoSwitch";

    // Window Sensor Channel ids
    public static final String WINDOWSENSOR_CHANNEL_STATE_ID = "windowState";
    public static final String WINDOWSENSOR_CHANNEL_POS_ID = "windowPosition";

    // Door Ring Channel id
    public static final String DOORDINGSENSOR_CHANNEL_STATE_ID = "doorringSensor";

    // Shutter Channel id
    public static final String SHUTTER_TRIGGER_CHANNEL_ID = "shutterUpDownTrigger";
    public static final String SHUTTER_POS_CHANNEL_ID = "shutterPosition";

    // Dimming Channel id
    public static final String DIMMING_VALUE_CHANNEL_ID = "dimmerValue";
    public static final String DIMMING_SWITCH_CHANNEL_ID = "dimmerSwitchChannel";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(UNKNOWN_TYPE_UID, BRIDGE_TYPE_UID,
            WINDOWSENSOR_TYPE_UID, THERMOSTAT_TYPE_UID, SCENE_TYPE_UID, RULE_TYPE_UID, ACTUATOR_TYPE_UID,
            DOORRINGSENSOR_TYPE_UID, DOOROPENER_TYPE_UID, CORRIDORLIGHTSWITCH_TYPE_UID, MUTEACTUATOR_TYPE_UID,
            SHUTTERACTUATOR_TYPE_UID, DIMMINGACTUATOR_TYPE_UID);
}
