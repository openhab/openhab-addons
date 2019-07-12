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
package org.openhab.binding.teleinfo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link TeleinfoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoBindingConstants {

    private static final String BINDING_ID = "teleinfo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERIAL_CONTROLLER = new ThingTypeUID(BINDING_ID, "serialcontroller");
    // public static final ThingTypeUID THING_TYPE_REMOTE_IP = new ThingTypeUID(BINDING_ID, "remoteip");

    // List of all Channel ids
    public static final String CHANNEL_ISOUSC = "isousc";
    public static final String CHANNEL_PTEC = "ptec";
    public static final String CHANNEL_IMAX = "imax";
    public static final String CHANNEL_ADPS = "adps";
    public static final String CHANNEL_PAPP = "papp";
    public static final String CHANNEL_IINST = "iinst";
    public static final String CHANNEL_LAST_UPDATE = "lastUpdate";
    public static final String CHANNEL_CURRENT_POWER = "currentPower";
    public static final String CHANNEL_CURRENT_POWER_CONFIG_PARAMETER_POWERFACTOR = "powerFactor";

    public static final ThingTypeUID THING_HCHP_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "hchp_electricitymeter");
    public static final String THING_HCHP_ELECTRICITY_METER_PROPERTY_ADCO = "adco";
    public static final String THING_HCHP_ELECTRICITY_METER_CHANNEL_HCHC = "hchc";
    public static final String THING_HCHP_ELECTRICITY_METER_CHANNEL_HCHP = "hchp";
    public static final String THING_HCHP_ELECTRICITY_METER_CHANNEL_HHPHC = "hhphc";

    public static final ThingTypeUID THING_BASE_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "base_electricitymeter");
    public static final String THING_BASE_ELECTRICITY_METER_PROPERTY_ADCO = THING_HCHP_ELECTRICITY_METER_PROPERTY_ADCO;
    public static final String THING_BASE_ELECTRICITY_METER_CHANNEL_BASE = "base";

    public static final ThingTypeUID THING_EJP_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "ejp_electricitymeter");
    public static final String THING_EJP_ELECTRICITY_METER_PROPERTY_ADCO = THING_HCHP_ELECTRICITY_METER_PROPERTY_ADCO;

    public static final ThingTypeUID THING_TEMPO_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "tempo_electricitymeter");
    public static final String THING_TEMPO_ELECTRICITY_METER_PROPERTY_ADCO = THING_HCHP_ELECTRICITY_METER_PROPERTY_ADCO;

    public final static String ERROR_OFFLINE_SERIAL_NOT_FOUND = "@text/teleinfo.thingstate.serial_notfound";
    public final static String ERROR_OFFLINE_SERIAL_INUSE = "@text/teleinfo.thingstate.serial_inuse";
    public final static String ERROR_OFFLINE_SERIAL_UNSUPPORTED = "@text/teleinfo.thingstate.serial_unsupported";
    public final static String ERROR_OFFLINE_SERIAL_LISTENERS = "@text/teleinfo.thingstate.serial_listeners";
    public final static String ERROR_OFFLINE_CONTROLLER_OFFLINE = "@text/teleinfo.thingstate.controller_offline";

}
