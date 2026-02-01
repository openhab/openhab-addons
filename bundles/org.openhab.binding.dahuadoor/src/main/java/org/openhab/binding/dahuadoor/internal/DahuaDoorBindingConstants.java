/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dahuadoor.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DahuaDoorBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class DahuaDoorBindingConstants {

    private static final String BINDING_ID = "dahuadoor";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VTO = new ThingTypeUID(BINDING_ID, "dahua_vth");

    // List of all Channel ids
    public static final String CHANNEL_BELL_BUTTON = "bell-button";
    public static final String CHANNEL_DOOR_IMAGE = "door-image";
    public static final String CHANNEL_OPEN_DOOR_1 = "open-door-1";
    public static final String CHANNEL_OPEN_DOOR_2 = "open-door-2";
}
