/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.phc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PHCBinding} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Jonas Hohaus - Initial contribution
 */
@NonNullByDefault
public class PHCBindingConstants {

    public static final String BINDING_ID = "phc";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    public static final ThingTypeUID THING_TYPE_AM = new ThingTypeUID(BINDING_ID, "AM");
    public static final ThingTypeUID THING_TYPE_EM = new ThingTypeUID(BINDING_ID, "EM");
    public static final ThingTypeUID THING_TYPE_JRM = new ThingTypeUID(BINDING_ID, "JRM");
    public static final ThingTypeUID THING_TYPE_DIM = new ThingTypeUID(BINDING_ID, "DIM");

    // List of all Channel Group IDs
    public static final String CHANNELS_AM = "am";
    public static final String CHANNELS_EM = "em";
    public static final String CHANNELS_EM_LED = "emLed";
    public static final String CHANNELS_JRM = "jrm";
    public static final String CHANNELS_JRM_TIME = "jrmT";
    public static final String CHANNELS_DIM = "dim";

    // List of all configuration parameters
    public static final String PORT = "port";
    public static final String ADDRESS = "address";
    public static final String UP_DOWN_TIME_1 = "upDownTime1";
    public static final String UP_DOWN_TIME_2 = "upDownTime2";
    public static final String UP_DOWN_TIME_3 = "upDownTime3";
    public static final String UP_DOWN_TIME_4 = "upDownTime4";
    public static final String DIM_TIME_1 = "dimTime1";
    public static final String DIM_TIME_2 = "dimTime2";
}
