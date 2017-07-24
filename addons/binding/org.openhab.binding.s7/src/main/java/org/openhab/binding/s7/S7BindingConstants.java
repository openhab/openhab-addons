/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.s7;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import Moka7.S7;

/**
 * The {@link S7Binding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent Sibilla - Initial contribution
 */
public class S7BindingConstants {

    public static final String BINDING_ID = "s7";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");
    public static final ThingTypeUID THING_TYPE_CONTACT = new ThingTypeUID(BINDING_ID, "contact");
    public static final ThingTypeUID THING_TYPE_PUSHBUTTON = new ThingTypeUID(BINDING_ID, "pushbutton");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");

    // List of all Channel ids
    public static final String CHANNEL_REFRESH_DURATION = "refreshDuration";
    public static final String CHANNEL_MAX_REFRESH_DURATION = "maxRefreshDuration";
    public static final String CHANNEL_STATE = "state";

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String LOCAL_TSAP = "localTSAP";
    public static final String REMOTE_TSAP = "remoteTSAP";
    public static final String POLLING_INTERVAL = "pollingInterval";

    // Thing config properties
    public static final String ACCESS_MODE = "accessMode";
    public static final String INPUT_DBAREA = "inputDBArea";
    public static final String INPUT_ADDRESS = "inputAddress";
    public static final String OUTPUT_DBAREA = "outputDBArea";
    public static final String OUTPUT_ADDRESS = "outputAddress";

    // Access mode options properties
    public static final String MODE_READ_ONLY = "ReadOnly";
    public static final String MODE_TOGGLE = "ToggleMode";
    public static final String MODE_READ_WRITE = "ReadWrite";
    public static final String MODE_PUSHBUTTON = "Pushbutton";

    // DB option definitions
    public static final String[] AREA_NAME = { "Area PE", "Area PA", "Area MK", "Area CT", "Area TM", "Area DB" };
    public static final int[] AREA_ID = { S7.S7AreaPE, S7.S7AreaPA, S7.S7AreaMK, S7.S7AreaCT, S7.S7AreaTM,
            S7.S7AreaDB };
}
