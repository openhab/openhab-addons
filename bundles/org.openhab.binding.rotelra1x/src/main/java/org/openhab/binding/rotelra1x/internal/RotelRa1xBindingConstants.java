/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.rotelra1x.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RotelRa1xBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marius Bjørnstad - Initial contribution
 */
@NonNullByDefault
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
