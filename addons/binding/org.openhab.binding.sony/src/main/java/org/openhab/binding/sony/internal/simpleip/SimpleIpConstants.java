/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.simpleip;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.sony.SonyBindingConstants;

// TODO: Auto-generated Javadoc
/**
 * The class provides all the constants specific to the Simple IP system.
 *
 * @author Tim Roberts - Initial contribution
 */
public class SimpleIpConstants {

    /** The Constant THING_TYPE_SIMPLEIP. */
    // The thing constants
    public final static ThingTypeUID THING_TYPE_SIMPLEIP = new ThingTypeUID(SonyBindingConstants.BINDING_ID,
            "simpleip");

    /** The Constant PORT. */
    public final static int PORT = 20060;

    /** The Constant CHANNEL_IR. */
    // All the channel constants
    public final static String CHANNEL_IR = "ir";

    /** The Constant CHANNEL_POWER. */
    public final static String CHANNEL_POWER = "power";

    /** The Constant CHANNEL_VOLUME. */
    public final static String CHANNEL_VOLUME = "volume";

    /** The Constant CHANNEL_AUDIOMUTE. */
    public final static String CHANNEL_AUDIOMUTE = "audiomute";

    /** The Constant CHANNEL_CHANNEL. */
    public final static String CHANNEL_CHANNEL = "channel";

    /** The Constant CHANNEL_TRIPLETCHANNEL. */
    public final static String CHANNEL_TRIPLETCHANNEL = "tripletchannel";

    /** The Constant CHANNEL_INPUTSOURCE. */
    public final static String CHANNEL_INPUTSOURCE = "inputsource";

    /** The Constant CHANNEL_INPUT. */
    public final static String CHANNEL_INPUT = "input";

    /** The Constant CHANNEL_PICTUREMUTE. */
    public final static String CHANNEL_PICTUREMUTE = "picturemute";

    /** The Constant CHANNEL_TOGGLEPICTUREMUTE. */
    public final static String CHANNEL_TOGGLEPICTUREMUTE = "togglepicturemute";

    /** The Constant CHANNEL_PICTUREINPICTURE. */
    public final static String CHANNEL_PICTUREINPICTURE = "pip";

    /** The Constant CHANNEL_TOGGLEPICTUREINPICTURE. */
    public final static String CHANNEL_TOGGLEPICTUREINPICTURE = "togglepip";

    /** The Constant CHANNEL_TOGGLEPIPPOSITION. */
    public final static String CHANNEL_TOGGLEPIPPOSITION = "togglepipposition";

    /** The Constant PROP_BROADCASTADDRESS. */
    public final static String PROP_BROADCASTADDRESS = "broadcastaddress";

    /** The Constant PROP_MACADDRESS. */
    public final static String PROP_MACADDRESS = "macaddress";
}
