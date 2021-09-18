/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.miele.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MieleBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 * @author Martin Lepsy - added constants for support of WiFi devices & protocol
 * @author Jacob Laursen - Fixed multicast and protocol support (ZigBee/LAN)
 */
@NonNullByDefault
public class MieleBindingConstants {

    public static final String BINDING_ID = "miele";
    public static final String APPLIANCE_ID = "uid";
    public static final String DEVICE_CLASS = "dc";
    public static final String PROTOCOL_PROPERTY_NAME = "protocol";
    public static final String SERIAL_NUMBER_PROPERTY_NAME = "serialNumber";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_XGW3000 = new ThingTypeUID(BINDING_ID, "xgw3000");
    public static final ThingTypeUID THING_TYPE_DISHWASHER = new ThingTypeUID(BINDING_ID, "dishwasher");
    public static final ThingTypeUID THING_TYPE_OVEN = new ThingTypeUID(BINDING_ID, "oven");
    public static final ThingTypeUID THING_TYPE_FRIDGE = new ThingTypeUID(BINDING_ID, "fridge");
    public static final ThingTypeUID THING_TYPE_DRYER = new ThingTypeUID(BINDING_ID, "tumbledryer");
    public static final ThingTypeUID THING_TYPE_HOB = new ThingTypeUID(BINDING_ID, "hob");
    public static final ThingTypeUID THING_TYPE_FRIDGEFREEZER = new ThingTypeUID(BINDING_ID, "fridgefreezer");
    public static final ThingTypeUID THING_TYPE_HOOD = new ThingTypeUID(BINDING_ID, "hood");
    public static final ThingTypeUID THING_TYPE_WASHINGMACHINE = new ThingTypeUID(BINDING_ID, "washingmachine");
    public static final ThingTypeUID THING_TYPE_COFFEEMACHINE = new ThingTypeUID(BINDING_ID, "coffeemachine");

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String INTERFACE = "interface";
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";
}
