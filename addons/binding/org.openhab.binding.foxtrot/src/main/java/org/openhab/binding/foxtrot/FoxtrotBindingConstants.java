/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FoxtrotBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Radovan Sninsky - Initial contribution
 */
@NonNullByDefault
public class FoxtrotBindingConstants {

    private static final String BINDING_ID = "foxtrot";

    // List of properties
    public static final String PROPERTY_PLCCOMS_HOST = "plccomsHost";
    public static final String PROPERTY_PLCCOMS_PORT = "plccomsPort";
    public static final String PROPERTY_PLCCOMS_VERSION = "plccomsVersion";
    public static final String PROPERTY_PLCCOM_EPSNET_VERSION = "plccomsEpsnetVersion";
    public static final String PROPERTY_PLCCOM_INI_VERSION = "plccomsIniVersion";
    public static final String PROPERTY_PLC_VERSION = "plcVersion";
    public static final String PROPERTY_PLC_IP = "plcIp";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PLC = new ThingTypeUID(BINDING_ID, "plc");

    public static final ThingTypeUID THING_TYPE_VARIABLE = new ThingTypeUID(BINDING_ID, "variable");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");

    // List of all Channel ids
    public static final String CHANNEL_NUMBER = "number";
    public static final String CHANNEL_STRING = "string";
    public static final String CHANNEL_BOOL = "bool";
}
