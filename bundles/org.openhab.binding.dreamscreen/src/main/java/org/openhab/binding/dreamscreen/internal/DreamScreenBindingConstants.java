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
package org.openhab.binding.dreamscreen.internal;

import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DreamScreenBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public class DreamScreenBindingConstants {

    private static final String BINDING_ID = "dreamscreen";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_4K = new ThingTypeUID(BINDING_ID, "4k");
    public static final ThingTypeUID THING_TYPE_HD = new ThingTypeUID(BINDING_ID, "hd");
    public static final ThingTypeUID THING_TYPE_SIDEKICK = new ThingTypeUID(BINDING_ID, "sidekick");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_HD, THING_TYPE_4K, THING_TYPE_SIDEKICK).collect(toSet());

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_SCENE = "scene";
    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_COLOR = "color";
}
