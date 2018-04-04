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

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PLC = new ThingTypeUID(BINDING_ID, "plccoms");

    public static final ThingTypeUID THING_TYPE_STRING = new ThingTypeUID(BINDING_ID, "string");
    public static final ThingTypeUID THING_TYPE_NUMBER = new ThingTypeUID(BINDING_ID, "number");
    public static final ThingTypeUID THING_TYPE_BOOL = new ThingTypeUID(BINDING_ID, "bool");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_BLIND = new ThingTypeUID(BINDING_ID, "blind");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");

    // List of all Channel ids
    public static final String CHANNEL_STRING = "string";
    public static final String CHANNEL_NUMBER = "number";
    public static final String CHANNEL_BOOL = "bool";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_DIMMER = "dimmer";
    public static final String CHANNEL_BLIND = "blind";
}
