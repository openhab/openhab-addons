/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jenocean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link JEnOceanBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan Kemmler - Initial contribution
 */
@NonNullByDefault
public class JEnOceanBindingConstants {

    private static final String BINDING_ID = "jenocean";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ROCKER_SWITCH = new ThingTypeUID(BINDING_ID, "RockerSwitch");

    // List of all Channel ids
    public static final String CHANNEL_A_ON_OFF = "channel_a_on_off";
    public static final String CHANNEL_B_ON_OFF = "channel_b_on_off";
    public static final String CHANNEL_A_ROCKER = "channel_a_rocker";
    public static final String CHANNEL_B_ROCKER = "channel_b_rocker";
    public static final String CHANNEL_A_UP_BUTTON = "channel_a_up_button";
    public static final String CHANNEL_A_DOWN_BUTTON = "channel_a_down_button";
    public static final String CHANNEL_B_UP_BUTTON = "channel_b_up_button";
    public static final String CHANNEL_B_DOWN_BUTTON = "channel_b_down_button";

}
