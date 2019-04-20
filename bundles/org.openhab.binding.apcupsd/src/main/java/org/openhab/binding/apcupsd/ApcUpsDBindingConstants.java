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
package org.openhab.binding.apcupsd;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ApcUpsDBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Aitor Iturrioz - Initial contribution
 */
@NonNullByDefault
public class ApcUpsDBindingConstants {

    private static final String BINDING_ID = "apcupsd";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_APCUPSTCP = new ThingTypeUID(BINDING_ID, "apcupsd-tcp");

    // Config parameters
    public static final String PARAMETER_IP = "ip";
    public static final String PARAMETER_PORT = "port";
    public static final String PARAMETER_REFRESH = "refresh";
    public static final String PARAMETER_UPDATE_ONLY_ON_CHANGE = "updateOnlyOnChange";
    
    // Properties
    public static final String PROPERTY_FIRMWARE = "firmware";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_SERIAL_NO = "serialNumber";
    
    // Channels
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_LINE_V = "line_v";
    public static final String CHANNEL_BATT_TIMELEFT = "batt_timeleft";
    public static final String CHANNEL_BATT_CHARGE = "batt_charge";
    public static final String CHANNEL_BATT_V = "batt_v";
    public static final String CHANNEL_BATT_DATE = "batt_date";
    public static final String CHANNEL_LOAD_PCT = "load_pct";
    public static final String CHANNEL_START_TIME = "start_time";
}
