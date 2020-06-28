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
package org.openhab.binding.elgatokeylight.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ElgatoKeyLightBindingConstants} class defines common constants,
 * which are used across the whole binding.
 *
 * @author Gunnar Wagenknecht - Initial contribution
 */
@NonNullByDefault
public class ElgatoKeyLightBindingConstants {

    private static final String BINDING_ID = "elgatokeylight";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_KEY_LIGHT = new ThingTypeUID(BINDING_ID, "keylight");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_KEY_LIGHT);

    // List of all Channel ids
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR_TEMPREATURE = "color-temperature";

}
