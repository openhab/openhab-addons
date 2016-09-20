/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.bravia;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.sony.SonyBindingConstants;

/**
 * The class provides all the constants specific to the Bravia system
 *
 * @author Tim Roberts
 * @version $Id: $Id
 */
public class BraviaConstants {

    // The thing constants
    public final static ThingTypeUID THING_TYPE_BRAVIA = new ThingTypeUID(SonyBindingConstants.BINDING_ID, "bravia");

    // All the channel constants
    public final static String CHANNEL_IR = "ir";
    public final static String CHANNEL_POWER = "power";
    public final static String CHANNEL_VOLUME = "volume";
    public final static String CHANNEL_AUDIOMUTE = "audiomute";
    public final static String CHANNEL_CHANNEL = "channel";
    public final static String CHANNEL_TRIPLETCHANNEL = "tripletchannel";
    public final static String CHANNEL_INPUTSOURCE = "inputsource";
    public final static String CHANNEL_INPUT = "input";
    public final static String CHANNEL_PICTUREMUTE = "picturemute";
    public final static String CHANNEL_TOGGLEPICTUREMUTE = "togglepicturemute";
    public final static String CHANNEL_PICTUREINPICTURE = "pip";
    public final static String CHANNEL_TOGGLEPICTUREINPICTURE = "togglepip";
    public final static String CHANNEL_TOGGLEPIPPOSITION = "togglepipposition";
    public final static String CHANNEL_BROADCASTADDRESS = "broadcastaddress";
    public final static String CHANNEL_MACADDRESS = "macaddress";
}
