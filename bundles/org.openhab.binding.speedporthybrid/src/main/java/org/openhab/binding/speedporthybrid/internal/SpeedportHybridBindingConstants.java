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
package org.openhab.binding.speedporthybrid.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SpeedportHybridBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Henning Treu - Initial contribution
 */
@NonNullByDefault
public class SpeedportHybridBindingConstants {

    private static final String BINDING_ID = "speedporthybrid";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SPEEDPORT_HYBRID = new ThingTypeUID(BINDING_ID, "speedporthybrid");

    // List of all Channel ids
    public static final String CHANNEL_LTE = "lte";
}
