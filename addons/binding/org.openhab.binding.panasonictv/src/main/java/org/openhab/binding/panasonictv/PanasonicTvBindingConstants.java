/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PanasonicTvBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@NonNullByDefault
public class PanasonicTvBindingConstants {

    public static final String BINDING_ID = "panasonictv";

    public static final ThingTypeUID PANASONIC_TV_THING_TYPE = new ThingTypeUID(BINDING_ID, "tv");

    // List of all remote controller thing channel id's
    public static final String KEY_CODE = "keyCode";
    public static final String POWER = "power";
    public static final String SOURCE_NAME = "sourceName";
    public static final String SOURCE_ID = "sourceId";

    // List of all media renderer thing channel id's
    public static final String VOLUME = "volume";
    public static final String MUTE = "mute";

}
