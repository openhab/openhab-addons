/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link YeelightBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Coaster Li (lixin@yeelink.net) - Initial contribution
 */
public class YeelightBindingConstants {

    public static final String BINDING_ID = "yeelight";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_CEILING = new ThingTypeUID(BINDING_ID, "Ceiling");
    public final static ThingTypeUID THING_TYPE_DOLPHIN = new ThingTypeUID(BINDING_ID, "Dolphin");
    public final static ThingTypeUID THING_TYPE_WONDER = new ThingTypeUID(BINDING_ID, "Wonder");
    public final static ThingTypeUID THING_TYPE_STRIPE = new ThingTypeUID(BINDING_ID, "Stripe");

    // List of all Channel ids
    public final static String CHANNEL_BRIGHTNESS = "Brightness";
    public final static String CHANNEL_COLOR = "Color";
    public final static String CHANNEL_COLOR_TEMPERATURE = "Color_Temperature";

}