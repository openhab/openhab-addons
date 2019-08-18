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
package org.openhab.binding.heliosventilation.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HeliosVentilationBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class HeliosVentilationBindingConstants {

    public static final String BINDING_ID = "heliosventilation";

    public static final String DATAPOINT_FILE = "datapoints.properties";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HELIOS_VENTILATION = new ThingTypeUID(BINDING_ID, "ventilation");

    // List of all Channel ids
    // TODO: remove channel ids
    public static final String CHANNEL_OUTSIDE_TEMP = "outsideTemp";
    public static final String CHANNEL_OUTGOING_TEMP = "outgoingTemp";
    public static final String CHANNEL_EXTRACT_TEMP = "extractTemp";
    public static final String CHANNEL_SUPPLY_TEMP = "supplyTemp";
    public static final String CHANNEL_SET_TEMP = "setTemp";
    public static final String CHANNEL_BYPASS_TEMP = "bypassTemp";
    public static final String CHANNEL_SUPPLY_STOP_TEMP = "supplyStopTemp";
    public static final String CHANNEL_PREHEAT_TEMP = "preheatTemp";
    public static final String CHANNEL_RH_LIMIT = "rhLimit";
    public static final String CHANNEL_HYSTERESIS = "hysteresis";
    public static final String CHANNEL_FANSPEED = "fanspeed";
    public static final String CHANNEL_MIN_FANSPEED = "minFanspeed";
    public static final String CHANNEL_MAX_FANSPEED = "maxFanspeed";
    public static final String CHANNEL_DC_FAN_EXTRACT = "DCFanExtract";
    public static final String CHANNEL_DC_FAN_SUPPLY = "DCFanSupply";
}
