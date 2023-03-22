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
package org.openhab.binding.epsonprojector.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EpsonProjectorBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Yannick Schaus - Initial contribution
 * @author Michael Lobstein - Updated for OH3
 */
@NonNullByDefault
public class EpsonProjectorBindingConstants {

    public static final String BINDING_ID = "epsonprojector";
    public static final int DEFAULT_PORT = 3629;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PROJECTOR_SERIAL = new ThingTypeUID(BINDING_ID, "projector-serial");
    public static final ThingTypeUID THING_TYPE_PROJECTOR_TCP = new ThingTypeUID(BINDING_ID, "projector-tcp");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_PROJECTOR_SERIAL,
            THING_TYPE_PROJECTOR_TCP);

    // Some Channel types
    public static final String CHANNEL_TYPE_POWER = "power";
    public static final String CHANNEL_TYPE_POWERSTATE = "powerstate";
    public static final String CHANNEL_TYPE_LAMPTIME = "lamptime";

    // Config properties
    public static final String THING_PROPERTY_HOST = "host";
    public static final String THING_PROPERTY_PORT = "port";
}
