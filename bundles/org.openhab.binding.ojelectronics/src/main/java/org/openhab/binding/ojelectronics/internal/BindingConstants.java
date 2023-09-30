/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ojelectronics.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class BindingConstants {

    private static final String BINDING_ID = "ojelectronics";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OJCLOUD = new ThingTypeUID(BINDING_ID, "ojcloud");
    public static final ThingTypeUID THING_TYPE_OWD5 = new ThingTypeUID(BINDING_ID, "owd5");

    // List of all Channel ids
    public static final String CHANNEL_OWD5_FLOORTEMPERATURE = "floorTemperature";
    public static final String CHANNEL_OWD5_GROUPNAME = "groupName";
    public static final String CHANNEL_OWD5_GROUPID = "groupId";
    public static final String CHANNEL_OWD5_ONLINE = "online";
    public static final String CHANNEL_OWD5_HEATING = "heating";
    public static final String CHANNEL_OWD5_ROOMTEMPERATURE = "roomTemperature";
    public static final String CHANNEL_OWD5_THERMOSTATNAME = "thermostatName";
    public static final String CHANNEL_OWD5_REGULATIONMODE = "regulationMode";
    public static final String CHANNEL_OWD5_COMFORTSETPOINT = "comfortSetpoint";
    public static final String CHANNEL_OWD5_COMFORTENDTIME = "comfortEndTime";
    public static final String CHANNEL_OWD5_BOOSTENDTIME = "boostEndTime";
    public static final String CHANNEL_OWD5_MANUALSETPOINT = "manualSetpoint";
    public static final String CHANNEL_OWD5_VACATIONENABLED = "vacationEnabled";
    public static final String CHANNEL_OWD5_VACATIONBEGINDAY = "vacationBeginDay";
    public static final String CHANNEL_OWD5_VACATIONENDDAY = "vacationEndDay";
}
