/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max;

import java.util.Collection;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * The {@link MaxBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MaxBinding {

    public static final String BINDING_ID = "max";

    // List of main device types
    public static final String DEVICE_THERMOSTAT = "thermostat";
    public static final String DEVICE_THERMOSTATPLUS = "thermostatplus";
    public static final String DEVICE_WALLTHERMOSTAT = "wallthermostat";
    public static final String DEVICE_ECOSWITCH = "ecoswitch";
    public static final String DEVICE_SHUTTERCONTACT = "shuttercontact";
    public static final String BRIDGE_MAXCUBE = "bridge";

    // List of all Thing Type UIDs
    public static final ThingTypeUID HEATINGTHERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_THERMOSTAT);
    public static final ThingTypeUID HEATINGTHERMOSTATPLUS_THING_TYPE = new ThingTypeUID(BINDING_ID,
            DEVICE_THERMOSTATPLUS);
    public static final ThingTypeUID WALLTHERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_WALLTHERMOSTAT);
    public static final ThingTypeUID ECOSWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_ECOSWITCH);
    public static final ThingTypeUID SHUTTERCONTACT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_SHUTTERCONTACT);
    public static final ThingTypeUID CUBEBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_MAXCUBE);

    // List of all Channel ids
    public static final String CHANNEL_VALVE = "valve";
    public static final String CHANNEL_BATTERY = "battery_low";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_ACTUALTEMP = "actual_temp";
    public static final String CHANNEL_SETTEMP = "set_temp";
    public static final String CHANNEL_LOCKED = "locked";
    public static final String CHANNEL_CONTACT_STATE = "contact_state";
    public static final String CHANNEL_FREE_MEMORY = "free_mem";
    public static final String CHANNEL_DUTY_CYCLE = "duty_cycle";

    // Custom Properties
    public static final String PROPERTY_SERIAL_NUMBER = "serialNumber";
    public static final String PROPERTY_IP_ADDRESS = "ipAddress";
    public static final String PROPERTY_VENDOR_NAME = "eQ-3 AG";
    public static final String PROPERTY_RFADDRESS = "rfAddress";
    public static final String PROPERTY_ROOMNAME = "room";
    public static final String PROPERTY_ROOMID = "roomId";
    public static final String PROPERTY_DEVICENAME = "name";
    public static final String PROPERTY_REFRESH_ACTUAL_RATE = "refreshActualRate";
    public static final String PROPERTY_NTP_SERVER1 = "ntpServer1";
    public static final String PROPERTY_NTP_SERVER2 = "ntpServer2";

    // Thermostat settings properties
    public static final String PROPERTY_THERMO_COMFORT_TEMP = "comfortTemp";
    public static final String PROPERTY_THERMO_ECO_TEMP = "ecoTemp";
    public static final String PROPERTY_THERMO_MAX_TEMP_SETPOINT = "maxTempSetpoint";
    public static final String PROPERTY_THERMO_MIN_TEMP_SETPOINT = "minTempSetpoint";
    public static final String PROPERTY_THERMO_OFFSET_TEMP = "offsetTemp";
    public static final String PROPERTY_THERMO_WINDOW_OPEN_TEMP = "windowOpenTemp";
    public static final String PROPERTY_THERMO_WINDOW_OPEN_DURATION = "windowOpenDuration";
    public static final String PROPERTY_THERMO_DECALCIFICATION = "decalcification";
    public static final String PROPERTY_THERMO_VALVE_MAX = "valveMaximum";
    public static final String PROPERTY_THERMO_VALVE_OFFSET = "valveOffset";
    public static final String PROPERTY_THERMO_BOOST_DURATION = "boostDuration";
    public static final String PROPERTY_THERMO_BOOST_VALVEPOS = "boostValvePos";
    public static final String PROPERTY_THERMO_PROGRAM_DATA = "programData";

    // List of actions
    public static final String ACTION_CUBE_REBOOT = "action-cubeReboot";
    public static final String ACTION_CUBE_RESET = "action-cubeReset";
    public static final String ACTION_DEVICE_DELETE = "action-deviceDelete";
    public static final String BUTTON_ACTION_VALUE = "1234";
    public static final int BUTTON_NOACTION_VALUE = -1;

    public static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(
            HEATINGTHERMOSTAT_THING_TYPE, HEATINGTHERMOSTATPLUS_THING_TYPE, WALLTHERMOSTAT_THING_TYPE,
            ECOSWITCH_THING_TYPE, SHUTTERCONTACT_THING_TYPE, CUBEBRIDGE_THING_TYPE);

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = ImmutableSet.of(
            HEATINGTHERMOSTAT_THING_TYPE, HEATINGTHERMOSTATPLUS_THING_TYPE, WALLTHERMOSTAT_THING_TYPE,
            ECOSWITCH_THING_TYPE, SHUTTERCONTACT_THING_TYPE);

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(CUBEBRIDGE_THING_TYPE);
}
