/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.herzborg.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HerzborgBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pavel Fedin - Initial contribution
 */
@NonNullByDefault
public class HerzborgBindingConstants {

    private static final String BINDING_ID = "herzborg";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERIAL_BUS = new ThingTypeUID(BINDING_ID, "serialBus");
    public static final ThingTypeUID THING_TYPE_CURTAIN = new ThingTypeUID(BINDING_ID, "curtain");

    // List of all Channel ids
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_REVERSE = "reverse";
    public static final String CHANNEL_HAND_START = "handStart";
    public static final String CHANNEL_EXT_SWITCH = "extSwitch";
    public static final String CHANNEL_HV_SWITCH = "hvSwitch";
}
