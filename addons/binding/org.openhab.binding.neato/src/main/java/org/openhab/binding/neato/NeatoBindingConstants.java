/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NeatoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class NeatoBindingConstants {

    public static final String BINDING_ID = "neato";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VACUUMCLEANER = new ThingTypeUID(BINDING_ID, "vacuumcleaner");

    // List of all Channel ids
    public static final String CHANNEL_BATTERY = "battery-level";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_ERROR = "error";
    public static final String CHANNEL_AVAILABLESERVICES = "available-services";
    public static final String CHANNEL_VERSION = "version";
    public static final String CHANNEL_MODELNAME = "model-name";
    public static final String CHANNEL_FIRMWARE = "firmware";
    public static final String CHANNEL_ACTION = "action";
    public static final String CHANNEL_DOCKHASBEENSEEN = "dock-has-been-seen";
    public static final String CHANNEL_ISDOCKED = "is-docked";
    public static final String CHANNEL_ISSCHEDULED = "is-scheduled";
    public static final String CHANNEL_ISCHARGING = "is-charging";
    public static final String CHANNEL_AVAILABLECOMMANDS = "available-commands";
    public static final String COMMAND = "command";
    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_CLEANINGCATEGORY = "cleaning-category";
    public static final String CHANNEL_CLEANINGMODE = "cleaning-mode";
    public static final String CHANNEL_CLEANINGMODIFIER = "cleaning-modifier";
    public static final String CHANNEL_CLEANINGSPOTWIDTH = "cleaning-spotwidth";
    public static final String CHANNEL_CLEANINGSPOTHEIGHT = "cleaning-spotheight";

    public static final String CONFIG_SECRET = "secret";
    public static final String CONFIG_NAME = "name";
    public static final String CONFIG_SERIAL = "serial";
    public static final String CONFIG_REFRESHTIME = "refresh";

    public static final int NEATO_STATE_INVALID = 0;
    public static final int NEATO_STATE_IDLE = 1;
    public static final int NEATO_STATE_BUSY = 2;
    public static final int NEATO_STATE_PAUSED = 3;
    public static final int NEATO_STATE_ERROR = 4;

    public static final int NEATO_ACTION_INVALID = 0;
    public static final int NEATO_ACTION_HOUSECLEANING = 1;
    public static final int NEATO_ACTION_SPOTCLEANING = 2;
    public static final int NEATO_ACTION_MANUALCLEANING = 3;
    public static final int NEATO_ACTION_DOCKING = 4;
    public static final int NEATO_ACTION_USERMENUACTIVE = 5;
    public static final int NEATO_ACTION_SUSPENDEDCLEANING = 6;
    public static final int NEATO_ACTION_UPDATING = 7;
    public static final int NEATO_ACTION_COPYINGLOGS = 8;
    public static final int NEATO_ACTION_RECOVERINGLOCATION = 9;
    public static final int NEATO_ACTION_IECTEST = 10;

    public static final int NEATO_CLEAN_CATEGORY_MANUAL = 1;
    public static final int NEATO_CLEAN_CATEGORY_HOUSE = 2;
    public static final int NEATO_CLEAN_CATEGORY_SPOT = 3;

    public static final int NEATO_CLEAN_MODE_ECO = 1;
    public static final int NEATO_CLEAN_MODE_TURBO = 2;

    public static final int NEATO_CLEAN_MODIFIER_NORMAL = 1;
    public static final int NEATO_CLEAN_MODIFIER_DOUBLE = 2;

}
