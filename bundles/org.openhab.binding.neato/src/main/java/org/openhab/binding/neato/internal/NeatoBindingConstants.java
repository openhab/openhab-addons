/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.neato.internal;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link NeatoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class NeatoBindingConstants {

    public static final String BINDING_ID = "neato";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_NEATOACCOUNT = new ThingTypeUID(BINDING_ID, "neatoaccount");
    public static final ThingTypeUID THING_TYPE_VACUUMCLEANER = new ThingTypeUID(BINDING_ID, "vacuumcleaner");

    // List of all Channel ids
    public static final String CHANNEL_BATTERY = "battery-level";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_ERROR = "error";
    public static final String CHANNEL_ACTION = "action";
    public static final String CHANNEL_DOCKHASBEENSEEN = "dock-has-been-seen";
    public static final String CHANNEL_ISDOCKED = "is-docked";
    public static final String CHANNEL_ISSCHEDULED = "is-scheduled";
    public static final String CHANNEL_ISCHARGING = "is-charging";
    public static final String COMMAND = "command";
    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_CLEANINGCATEGORY = "cleaning-category";
    public static final String CHANNEL_CLEANINGMODE = "cleaning-mode";
    public static final String CHANNEL_CLEANINGMODIFIER = "cleaning-modifier";
    public static final String CHANNEL_CLEANINGSPOTWIDTH = "cleaning-spotwidth";
    public static final String CHANNEL_CLEANINGSPOTHEIGHT = "cleaning-spotheight";

    public static final String CONFIG_SECRET = "secret";
    public static final String CONFIG_SERIAL = "serial";
    public static final String CONFIG_REFRESHTIME = "refresh";

    public static final String PROPERTY_NAME = "robot-name";
}
