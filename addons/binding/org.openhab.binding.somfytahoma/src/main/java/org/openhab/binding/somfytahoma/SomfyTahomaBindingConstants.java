/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SomfyTahomaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaBindingConstants {

    public static final String BINDING_ID = "somfytahoma";

    // Things
    // Bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // Gateway
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    // Rollershutter
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID, "rollershutter");

    // Awning
    public static final ThingTypeUID THING_TYPE_AWNING = new ThingTypeUID(BINDING_ID, "awning");
    
    // Actiongroup
    public static final ThingTypeUID THING_TYPE_ACTIONGROUP = new ThingTypeUID(BINDING_ID, "actiongroup");

    // On Off
    public static final ThingTypeUID THING_TYPE_ONOFF = new ThingTypeUID(BINDING_ID, "onoff");

    // List of all Channel ids
    // Gateway
    public static final String VERSION = "version";

    // Roller shutter
    public static final String POSITION = "position";
    public static final String CONTROL = "control";

    // Action group
    public static final String TRIGGER = "trigger";

    // OnOff
    public static final String SWITCH = "switch";

    //Constants
    final public static String TAHOMA_URL = "https://www.tahomalink.com/enduser-mobile-web/externalAPI/json/";
    final public static String SETUP_URL = "https://www.tahomalink.com/enduser-mobile-web/enduserAPI/setup/gateways/";
    final public static String DELETE_URL = "https://www.tahomalink.com/enduser-mobile-web/enduserAPI/exec/current/setup/";
    final public static String TAHOMA_AGENT = "TaHoma/3640 CFNetwork/711.1.16 Darwin/14.0.0";
    final public static String UNAUTHORIZED = "Server returned HTTP response code: 401";
    final public static int TYPE_PERCENT = 1;
    final public static int TYPE_ONOFF = 3;
    final public static String COMMAND_MY = "my";
    final public static String COMMAND_SET_CLOSURE = "setClosure";
    final public static String COMMAND_UP = "up";
    final public static String COMMAND_DOWN = "down";
}
