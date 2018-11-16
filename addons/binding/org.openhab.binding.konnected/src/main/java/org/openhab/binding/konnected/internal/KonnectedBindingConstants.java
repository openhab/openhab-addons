/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link KonnectedBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class KonnectedBindingConstants {

    public static final String BINDING_ID = "konnected";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MODULE = new ThingTypeUID(BINDING_ID, "module");

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String MAC_ADDR = "macAddress";
    public static final String CALLBACK_PATH = "callBackPath";

    // PIN_TO_ZONE array, this array maps an index location as a zone to the corresponding
    // pin location
    public static final Integer[] PIN_TO_ZONE = { 0, 1, 2, 5, 6, 7, 9, 8 };

    public static final String WEBHOOK_APP = "app_security";

    public static final String CHANNEL_ZONE = "channel_zone";

    // channeltypeids
    public static final String CHANNEL_SWITCH = "konnected:switch";
    public static final String CHANNEL_ACTUATOR = "konnected:actuator";
    public static final String CHANNEL_TEMPERATURE = "konnected:temperature";
    public static final String CHANNEL_HUMIDITY = "konnected:humidty";

    public static final String CHANNEL_TEMPERATURE_TYPE = "tempsensorType";
    public static final String CHANNEL_TEMPERATURE_DS18B20_ADDRESS = "ds18b20_address";
    public static final String CHANNEL_TEMPERATRUE_POLL = "pollinterval";

    public static final String CHANNEL_ACTUATOR_TIMES = "times";
    public static final String CHANNEL_ACTUATOR_MOMENTARY = "momentary";
    public static final String CHANNEL_ACTUATOR_PAUSE = "pause";

}
