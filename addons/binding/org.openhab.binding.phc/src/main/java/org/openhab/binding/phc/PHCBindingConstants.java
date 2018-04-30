/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.phc;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PHCBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jonas Hohaus - Initial contribution
 */
public class PHCBindingConstants {

    public static final String BINDING_ID = "phc";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    public static final ThingTypeUID THING_TYPE_AM = new ThingTypeUID(BINDING_ID, "AM");
    public static final ThingTypeUID THING_TYPE_EM = new ThingTypeUID(BINDING_ID, "EM");
    public static final ThingTypeUID THING_TYPE_JRM = new ThingTypeUID(BINDING_ID, "JRM");

    // List of all Channel Group IDs
    public static final String CHANNELS_AM = "am";
    public static final String CHANNELS_EM = "em";
    public static final String CHANNELS_EM_LED = "emLed";
    public static final String CHANNELS_JRM = "jrm";
    public static final String CHANNELS_JRM_TIME = "jrmT";

    // List of all configuration parameters
    public static final String PORT = "port";
    public static final String ADDRESS = "address";
    public static final String UP_DOWN_TIME_1 = "upDownTime1";
    public static final String UP_DOWN_TIME_2 = "upDownTime2";
    public static final String UP_DOWN_TIME_3 = "upDownTime3";
    public static final String UP_DOWN_TIME_4 = "upDownTime4";
}
