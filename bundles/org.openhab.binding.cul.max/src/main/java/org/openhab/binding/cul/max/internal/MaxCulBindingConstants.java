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
package org.openhab.binding.cul.max.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MaxCulBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
@NonNullByDefault
public class MaxCulBindingConstants {

    public static final String PROPERTY_THING_VERSION = "thing_version";
    public static final String CURRENT_THING_VERSION = "1.1";

    private static final String BINDING_ID = "maxcul";

    // List of main device types
    public static final String DEVICE_THERMOSTAT = "thermostat";
    public static final String DEVICE_THERMOSTATPLUS = "thermostatplus";
    public static final String DEVICE_WALLTHERMOSTAT = "wallthermostat";
    public static final String DEVICE_ECOSWITCH = "ecoswitch";
    public static final String DEVICE_SHUTTERCONTACT = "shuttercontact";
    public static final String BRIDGE_MAXCUL = "maxcul_bridge";
    public static final String BRIDGE_CUL_MORIZ = "cul_max_bridge";
    public static final String BRIDGE_CUN_MORIZ = "cun_max_bridge";
    // List of all Thing Type UIDs
    public static final ThingTypeUID HEATINGTHERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_THERMOSTAT);
    public static final ThingTypeUID HEATINGTHERMOSTATPLUS_THING_TYPE = new ThingTypeUID(BINDING_ID,
            DEVICE_THERMOSTATPLUS);
    public static final ThingTypeUID WALLTHERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_WALLTHERMOSTAT);
    public static final ThingTypeUID ECOSWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_ECOSWITCH);
    public static final ThingTypeUID SHUTTERCONTACT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_SHUTTERCONTACT);
    public static final ThingTypeUID MAXCULBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_MAXCUL);
    // List of all Channel ids
    public static final String CHANNEL_VALVE = "valve";
    public static final String CHANNEL_BATTERY = "battery_low";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_ACTUALTEMP = "actual_temp";
    public static final String CHANNEL_SETTEMP = "set_temp";
    public static final String CHANNEL_LOCKED = "locked";
    public static final String CHANNEL_CONTACT_STATE = "contact_state";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_DISPLAY_ACTUAL_TEMP = "display_actual_temp";

    public static final String CHANNEL_LISTEN_MODE = "listen_mode";
    public static final String CHANNEL_PAIR_MODE = "pair_mode";

    // Custom Properties
    public static final String PROPERTY_RFADDRESS = "rfAddress";
    public static final String PROPERTY_TIMEZONE = "timezone";

    // Thermostat settings properties
    public static final String PROPERTY_THERMO_COMFORT_TEMP = "comfortTemp";
    public static final String PROPERTY_THERMO_ECO_TEMP = "ecoTemp";
    public static final String PROPERTY_THERMO_MAX_TEMP_SETPOINT = "maxTempSetpoint";
    public static final String PROPERTY_THERMO_MIN_TEMP_SETPOINT = "minTempSetpoint";
    public static final String PROPERTY_THERMO_OFFSET_TEMP = "offsetTemp";
    public static final String PROPERTY_THERMO_WINDOW_OPEN_TEMP = "windowOpenTemp";
    public static final String PROPERTY_THERMO_WINDOW_OPEN_DURATION = "windowOpenDuration";
    public static final String PROPERTY_THERMO_WEEK_PROFILE = "weekProfile";
    public static final String PROPERTY_ASSOSIATE = "associate";

    // List of actions
    public static final String ACTION_DEVICE_RESET = "action-deviceReset";
    public static final String BUTTON_ACTION_VALUE = "1234";
    public static final int BUTTON_NOACTION_VALUE = -1;

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(HEATINGTHERMOSTAT_THING_TYPE, HEATINGTHERMOSTATPLUS_THING_TYPE, WALLTHERMOSTAT_THING_TYPE,
                    ECOSWITCH_THING_TYPE, SHUTTERCONTACT_THING_TYPE).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(MAXCULBRIDGE_THING_TYPE).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.concat(SUPPORTED_DEVICE_THING_TYPES_UIDS.stream(), SUPPORTED_BRIDGE_THING_TYPES_UIDS.stream())
                    .collect(Collectors.toSet()));
}
