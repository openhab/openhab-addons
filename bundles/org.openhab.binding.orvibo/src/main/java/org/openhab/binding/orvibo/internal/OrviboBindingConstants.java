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
package org.openhab.binding.orvibo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OrviboBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Walters - Initial contribution
 */
@NonNullByDefault
public class OrviboBindingConstants {

    public static final String BINDING_ID = "orvibo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_S20 = new ThingTypeUID(BINDING_ID, "s20");

    // List of all Channel ids
    public static final String CHANNEL_S20_SWITCH = "power";

    // List of all Config properties
    public static final String CONFIG_PROPERTY_DEVICE_ID = "deviceId";
}
