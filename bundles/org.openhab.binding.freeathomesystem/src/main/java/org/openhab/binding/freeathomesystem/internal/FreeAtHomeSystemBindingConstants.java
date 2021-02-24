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
    public static final String SWITCH_CHANNEL_ID = "switch_channel";
    public static final String ACTUATOR_CHANNEL_ID = "actuator_channel";

    // Thermostat Channel ids
    public static final String THERMOSTAT_CHANNEL_SETPOINTTEMP_ID = "thermostat_setpoint_temperature";
    public static final String THERMOSTAT_CHANNEL_MEASUREDTEMP_ID = "thermostat_measured_temperature";
    public static final String THERMOSTAT_CHANNEL_HEATDEMAND_ID = "thermostat_heating_demand";
    public static final String THERMOSTAT_CHANNEL_HEATINGACTIVE_ID = "thermostat_heating_active";
    public static final String THERMOSTAT_CHANNEL_STATE_ID = "thermostat_state";
    public static final String THERMOSTAT_CHANNEL_ONOFFWITCH_ID = "thermostat_onoff_switch";
    public static final String THERMOSTAT_CHANNEL_ECOSWITCH_ID = "thermostat_eco_switch";

    // Window Sensor Channel ids
    public static final String WINDOWSENSOR_CHANNEL_STATE_ID = "window_state_channel";
    public static final String WINDOWSENSOR_CHANNEL_POS_ID = "window_pos_channel";

    // Door Ring Channel id
    public static final String DOORDINGSENSOR_CHANNEL_STATE_ID = "doorringsensor_channel";

    // Shutter Channel id
    public static final String SHUTTER_UP_TRIGGER_CHANNEL_ID = "shutter_up_tigger_channel";
    public static final String SHUTTER_DOWN_TRIGGER_CHANNEL_ID = "shutter_down_tigger_channel";
    public static final String SHUTTER_POS_CHANNEL_ID = "shutter_pos_channel";

    // Dimming Channel id
    public static final String DIMMING_VALUE_CHANNEL_ID = "dim_value_channel";
    public static final String DIMMING_SWITCH_CHANNEL_ID = "dim_switch_channel";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(UNKNOWN_TYPE_UID, BRIDGE_TYPE_UID,
            WINDOWSENSOR_TYPE_UID, THERMOSTAT_TYPE_UID, SCENE_TYPE_UID, RULE_TYPE_UID, ACTUATOR_TYPE_UID,
            DOORRINGSENSOR_TYPE_UID, DOOROPENER_TYPE_UID, CORRIDORLIGHTSWITCH_TYPE_UID, MUTEACTUATOR_TYPE_UID,
            SHUTTERACTUATOR_TYPE_UID, DIMMINGACTUATOR_TYPE_UID);
}
