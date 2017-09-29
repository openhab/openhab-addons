/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystrom;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MystromBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stéphane Raemy - Initial contribution
 */
public class MystromBindingConstants {

    // REST URI constants
    public static final String MYSTROM_URI = "https://mystrom.ch/mobile/";
    public static final String MYSTROM_AUTH_PATH = "auth";
    public static final String MYSTROM_DEVICES_PATH = "devices";
    public static final String MYSTROM_DEVICE_PATH = "device";
    public static final String MYSTROM_DEVICE_SWITCH_PATH = "device/switch";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String AUTH_TOKEN = "authToken";
    public static final String ID = "id";

    public static final String BINDING_ID = "mystrom";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_WIFISWITCH = new ThingTypeUID(BINDING_ID, "wifiswitch");

    // List of all Channel ids
    public static final String STATE = "state";
    public static final String CONSUMPTION = "consumption";

    // List of all Mystrom Device types
    public static final String DEVICE_TYPE_WIFISWITCH = "wsw";

}
