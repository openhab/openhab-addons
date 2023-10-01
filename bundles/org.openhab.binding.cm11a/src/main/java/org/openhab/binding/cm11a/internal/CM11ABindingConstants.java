/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.cm11a.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link CM11ABindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class CM11ABindingConstants {

    public static final String BINDING_ID = "cm11a";

    /**
     * Bridge Type UIDs
     */
    public static final ThingTypeUID THING_TYPE_CM11A = new ThingTypeUID(BINDING_ID, "cm11a");

    /**
     * List of all Thing Type UIDs
     */
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");

    /**
     * List of all Channel ids
     */
    public static final String CHANNEL_LIGHTLEVEL = "lightlevel";
    public static final String CHANNEL_SWITCH = "switchstatus";
}
