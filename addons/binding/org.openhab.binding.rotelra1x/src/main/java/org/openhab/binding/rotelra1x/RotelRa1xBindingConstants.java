/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rotelra1x;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RotelRa1xBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marius Bjørnstad - Initial contribution
 */
public class RotelRa1xBindingConstants {

    public static final String BINDING_ID = "rotelra1x";

    public static final ThingTypeUID THING_TYPE_AMP = new ThingTypeUID(BINDING_ID, "amp");

    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_FREQUENCY = "frequency";
    public static final String CHANNEL_SOURCE = "source";

}
