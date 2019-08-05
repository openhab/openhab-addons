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
package org.openhab.binding.lutron.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LutronBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Allan Tong - Initial contribution
 */
@NonNullByDefault
public class LutronBindingConstants {

    public static final String BINDING_ID = "lutron";

    // Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_IPBRIDGE = new ThingTypeUID(BINDING_ID, "ipbridge");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_SHADE = new ThingTypeUID(BINDING_ID, "shade");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_OCCUPANCYSENSOR = new ThingTypeUID(BINDING_ID, "occupancysensor");
    public static final ThingTypeUID THING_TYPE_KEYPAD = new ThingTypeUID(BINDING_ID, "keypad");
    public static final ThingTypeUID THING_TYPE_TTKEYPAD = new ThingTypeUID(BINDING_ID, "ttkeypad");
    public static final ThingTypeUID THING_TYPE_INTLKEYPAD = new ThingTypeUID(BINDING_ID, "intlkeypad");
    public static final ThingTypeUID THING_TYPE_PICO = new ThingTypeUID(BINDING_ID, "pico");
    public static final ThingTypeUID THING_TYPE_VIRTUALKEYPAD = new ThingTypeUID(BINDING_ID, "virtualkeypad");
    public static final ThingTypeUID THING_TYPE_VCRX = new ThingTypeUID(BINDING_ID, "vcrx");
    public static final ThingTypeUID THING_TYPE_CCO = new ThingTypeUID(BINDING_ID, "cco");
    public static final ThingTypeUID THING_TYPE_CCO_PULSED = new ThingTypeUID(BINDING_ID, "ccopulsed");
    public static final ThingTypeUID THING_TYPE_CCO_MAINTAINED = new ThingTypeUID(BINDING_ID, "ccomaintained");
    public static final ThingTypeUID THING_TYPE_TIMECLOCK = new ThingTypeUID(BINDING_ID, "timeclock");
    public static final ThingTypeUID THING_TYPE_GREENMODE = new ThingTypeUID(BINDING_ID, "greenmode");
    public static final ThingTypeUID THING_TYPE_QSIO = new ThingTypeUID(BINDING_ID, "qsio");
    public static final ThingTypeUID THING_TYPE_GRAFIKEYEKEYPAD = new ThingTypeUID(BINDING_ID, "grafikeyekeypad");
    public static final ThingTypeUID THING_TYPE_BLIND = new ThingTypeUID(BINDING_ID, "blind");
    public static final ThingTypeUID THING_TYPE_PALLADIOMKEYPAD = new ThingTypeUID(BINDING_ID, "palladiomkeypad");
    public static final ThingTypeUID THING_TYPE_WCI = new ThingTypeUID(BINDING_ID, "wci");

    // List of all Channel ids
    public static final String CHANNEL_LIGHTLEVEL = "lightlevel";
    public static final String CHANNEL_SHADELEVEL = "shadelevel";
    public static final String CHANNEL_SWITCH = "switchstatus";
    public static final String CHANNEL_OCCUPANCYSTATUS = "occupancystatus";
    public static final String CHANNEL_CLOCKMODE = "clockmode";
    public static final String CHANNEL_SUNRISE = "sunrise";
    public static final String CHANNEL_SUNSET = "sunset";
    public static final String CHANNEL_EXECEVENT = "execevent";
    public static final String CHANNEL_ENABLEEVENT = "enableevent";
    public static final String CHANNEL_DISABLEEVENT = "disableevent";
    public static final String CHANNEL_STEP = "step";
    public static final String CHANNEL_BLINDLIFTLEVEL = "blindliftlevel";
    public static final String CHANNEL_BLINDTILTLEVEL = "blindtiltlevel";

    // Bridge config properties (used by discovery service)
    public static final String HOST = "ipAddress";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String SERIAL_NUMBER = "serialNumber";
    public static final String DISCOVERY_FILE = "discoveryFile";

    public static final String PROPERTY_PRODFAM = "productFamily";
    public static final String PROPERTY_PRODTYP = "productType";
    public static final String PROPERTY_CODEVER = "version";
    public static final String PROPERTY_MACADDR = "macAddress";

    // Thing config properties
    public static final String INTEGRATION_ID = "integrationId";

    // CCO config properties
    public static final String OUTPUT_TYPE = "outputType";
    public static final String OUTPUT_TYPE_PULSED = "Pulsed";
    public static final String OUTPUT_TYPE_MAINTAINED = "Maintained";
    public static final String DEFAULT_PULSE = "pulseLength";

    // GreenMode config properties
    public static final String POLL_INTERVAL = "pollInterval";

    // Blind types
    public static final String BLIND_TYPE_PARAMETER = "type";
    public static final String BLIND_TYPE_SHEER = "Sheer";
    public static final String BLIND_TYPE_VENETIAN = "Venetian";
}
