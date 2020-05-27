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
package org.openhab.binding.neohub.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NeoHubBindingConstants} class defines common constants
 *
 * @author Sebastian Prehn - Initial contribution (NeoHub command codes)
 * @author Andrew Fiddian-Green - Initial contribution (OpenHAB v2.x binding
 *         code)
 * 
 */
public class NeoHubBindingConstants {

    /*
     * binding id
     */
    public static final String BINDING_ID = "neohub";

    /*
     * device id's
     */
    public static final String DEVICE_ID_NEOHUB = "neohub";
    public static final String DEVICE_ID_NEOSTAT = "neostat";
    public static final String DEVICE_ID_NEOPLUG = "neoplug";
    public static final String DEVICE_ID_NEOCONTACT = "neocontact";
    public static final String DEVICE_ID_NEOTEMPERATURESENSOR = "neotemperaturesensor";

    /*
     * Thing Type UIDs
     */
    public static final ThingTypeUID THING_TYPE_NEOHUB = new ThingTypeUID(BINDING_ID, DEVICE_ID_NEOHUB);
    public static final ThingTypeUID THING_TYPE_NEOSTAT = new ThingTypeUID(BINDING_ID, DEVICE_ID_NEOSTAT);
    public static final ThingTypeUID THING_TYPE_NEOPLUG = new ThingTypeUID(BINDING_ID, DEVICE_ID_NEOPLUG);
    public static final ThingTypeUID THING_TYPE_NEOCONTACT = new ThingTypeUID(BINDING_ID, DEVICE_ID_NEOCONTACT);
    public static final ThingTypeUID THING_TYPE_NEOTEMPERATURESENSOR = new ThingTypeUID(BINDING_ID,
            DEVICE_ID_NEOTEMPERATURESENSOR);

    /*
     * Channel IDs common for several device types
     */
    public static final String CHAN_BATTERY_LOW_ALARM = "batteryLowAlarm";

    /*
     * Channel IDs for NeoStat thermostats
     */
    public static final String CHAN_ROOM_TEMP = "roomTemperature";
    public static final String CHAN_TARGET_TEMP = "targetTemperature";
    public static final String CHAN_FLOOR_TEMP = "floorTemperature";
    public static final String CHAN_OCC_MODE_PRESENT = "occupancyModePresent";
    public static final String CHAN_STAT_OUTPUT_STATE = "thermostatOutputState";

    /*
     * Channel IDs for NeoPlug smart plugs
     */
    public static final String CHAN_PLUG_OUTPUT_STATE = "plugOutputState";
    public static final String CHAN_PLUG_AUTO_MODE = "plugAutoMode";

    /*
     * Channel IDs for NeoContact (wireless) contact sensors
     */
    public static final String CHAN_CONTACT_STATE = "contactState";

    /*
     * Channel IDs for NeoTemperatureSensor (wireless) temperature sensors
     */
    public static final String CHAN_TEMPERATURE_SENSOR = "sensorTemperature";

    /*
     * enumerator for results of method calls
     */
    public static enum NeoHubReturnResult {
        SUCCEEDED,
        ERR_COMMUNICATION,
        ERR_INITIALIZATION
    }

    /*
     * the property IdD for the name of a thing in the NeoHub note: names may differ
     * between the NeoHub and the OpenHAB framework
     */
    public static final String DEVICE_NAME = "deviceNameInHub";

    /*
     * socket timeout in seconds for the TCP connection to the hub
     */
    public static final int TCP_SOCKET_TIMEOUT = 5;

    /*
     * setup parameters for de-bouncing of state changes (time in seconds) so state
     * changes that occur within this time window are ignored
     */
    public static final long DEBOUNCE_DELAY = 15;

    /*
     * setup parameters for lazy polling
     */
    public static final int LAZY_POLL_INTERVAL = 60;

    /*
     * setup parameters for fast polling bursts a burst comprises FAST_POLL_CYCLES
     * polling calls spaced at FAST_POLL_INTERVAL for example 5 polling calls made
     * at 4 second intervals (e.g. 5 x 4 => 20 seconds)
     */
    public static final int FAST_POLL_CYCLES = 5;
    public static final int FAST_POLL_INTERVAL = 4;

    /*
     * setup parameters for device discovery
     */
    public static final int DISCOVERY_TIMEOUT = 5;
    public static final int DISCOVERY_START_DELAY = 30;
    public static final int DISCOVERY_REFRESH_PERIOD = 600;

    /*
     * NeoHub JSON command codes strings: Thanks to Sebastian Prehn !!
     */
    public static final String CMD_CODE_INFO = "{\"INFO\":0}";
    public static final String CMD_CODE_TEMP = "{\"SET_TEMP\":[%s, \"%s\"]}";
    public static final String CMD_CODE_AWAY = "{\"FROST_%s\":\"%s\"}";
    public static final String CMD_CODE_TIMER = "{\"TIMER_%s\":\"%s\"}";
    public static final String CMD_CODE_MANUAL = "{\"MANUAL_%s\":\"%s\"}";
    public static final String CMD_CODE_READ_DCB = "{\"READ_DCB\":100}";

    /*
     * openHAB status strings
     */
    public static final String VAL_OFF = "Off";
    public static final String VAL_HEATING = "Heating";

    /*
     * logger message strings
     */
    public static final String PLEASE_REPORT_BUG = "Unexpected situation - please report a bug: ";
    public static final String MSG_HUB_CONFIG = PLEASE_REPORT_BUG + "hub needs to be initialized!";
    public static final String MSG_HUB_COMM = PLEASE_REPORT_BUG + "error communicating with the hub!";
    public static final String MSG_FMT_INFO_POLL_ERR = "INFO polling error: {}";
    public static final String MSG_FMT_DCB_POLL_ERR = "READ_DCB polling error: {}";
    public static final String MSG_FMT_SET_VALUE_ERR = "{} set value error: {}";
}
