/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.haassohnpelletoven.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HaassohnpelletstoveBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Feininger - Initial contribution
 */
@NonNullByDefault
public class HaassohnpelletstoveBindingConstants {

    private static final String BINDING_ID = "haassohnpelletoven";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OVEN = new ThingTypeUID(BINDING_ID, "oven");

    // List of all Channel ids
    public static final String CHANNEL_isTemp = "channelIsTemp"; // is Temperature
    public static final String CHANNEL_mode = "channelMode"; // Stove Mode
    public static final String CHANNEL_spTemp = "channelSpTemp"; // Target Temperature
    public static final String CHANNEL_prg = "channelPrg"; // Programming the Stove
}
