/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

    /*
     * Thing Type UIDs
     */
    public static final ThingTypeUID THING_TYPE_NEOHUB = new ThingTypeUID(BINDING_ID, DEVICE_ID_NEOHUB);
    public static final ThingTypeUID THING_TYPE_NEOSTAT = new ThingTypeUID(BINDING_ID, DEVICE_ID_NEOSTAT);
    public static final ThingTypeUID THING_TYPE_NEOPLUG = new ThingTypeUID(BINDING_ID, DEVICE_ID_NEOPLUG);

    /*
     * Channel IDs for NeoStats
     */
    public static final String CHAN_ROOM_TEMP = "roomTemperature";
    public static final String CHAN_TARGET_TEMP = "targetTemperature";
    public static final String CHAN_FLOOR_TEMP = "floorTemperature";
    public static final String CHAN_OCC_MODE_PRESENT = "occupancyModePresent";
    public static final String CHAN_STAT_OUTPUT_STATE = "thermostatOutputState";

    /*
     * Channel IDs for NeoPlugs
     */
    public static final String CHAN_PLUG_OUTPUT_STATE = "plugOutputState";
    public static final String CHAN_PLUG_AUTO_MODE = "plugAutoMode";

    /*
     * enumerator for results of method calls
     */
    public static enum NeoHubReturnResult {
        SUCCEEDED, ERR_COMMUNICATION, ERR_INITIALIZATION
    };

    /*
     * the property IdD for the name of a thing in the NeoHub note: names may differ
     * between the NeoHub and the OpenHAB framework
     */
    public static final String DEVICE_NAME = "deviceNameInHub";

    /*
     * socket timeout in seconds for the TCP connection to the hub
     */
    public static final int TCP_SOCKET_IMEOUT = 5;

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

    /*
     * openHAB status strings
     */
    public static final String VAL_OFF = "Off";
    public static final String VAL_HEATING = "Heating";

}
