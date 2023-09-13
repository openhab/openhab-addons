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
package org.openhab.binding.remoteopenhab.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RemoteopenhabBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RemoteopenhabBindingConstants {

    public static final String BINDING_ID = "remoteopenhab";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");
    public static final ThingTypeUID THING_TYPE_THING = new ThingTypeUID(BINDING_ID, "thing");

    // All supported Bridge types
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = Set.of(BRIDGE_TYPE_SERVER);

    // All supported Thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_THING);

    // List of all channel types
    public static final String CHANNEL_TYPE_TRIGGER = "trigger";
}
