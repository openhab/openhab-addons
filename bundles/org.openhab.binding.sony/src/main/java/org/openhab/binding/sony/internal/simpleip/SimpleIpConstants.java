/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.simpleip;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.sony.internal.SonyBindingConstants;

/**
 * The class provides all the constants specific to the Simple IP system.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SimpleIpConstants {
    // The thing constants
    public static final ThingTypeUID THING_TYPE_SIMPLEIP = new ThingTypeUID(SonyBindingConstants.BINDING_ID,
            SonyBindingConstants.SIMPLEIP_THING_TYPE_PREFIX);

    // Default port for simple IP
    public static final int PORT = 20060;

    // All the channel constants
    static final String CHANNEL_IR = "ir";
    static final String CHANNEL_POWER = "power";
    static final String CHANNEL_TOGGLEPOWER = "togglepower";
    static final String CHANNEL_VOLUME = "volume";
    static final String CHANNEL_AUDIOMUTE = "audiomute";
    static final String CHANNEL_CHANNEL = "channel";
    static final String CHANNEL_TRIPLETCHANNEL = "tripletchannel";
    static final String CHANNEL_INPUTSOURCE = "inputsource";
    static final String CHANNEL_INPUT = "input";
    static final String CHANNEL_SCENE = "scene";
    static final String CHANNEL_PICTUREMUTE = "picturemute";
    static final String CHANNEL_TOGGLEPICTUREMUTE = "togglepicturemute";
    static final String CHANNEL_PICTUREINPICTURE = "pip";
    static final String CHANNEL_TOGGLEPICTUREINPICTURE = "togglepip";
    static final String CHANNEL_TOGGLEPIPPOSITION = "togglepipposition";

    // All the custom property keys
    static final String PROP_BROADCASTADDRESS = "broadcastaddress";
    static final String PROP_MACADDRESS = "macaddress";
}
