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
package org.openhab.binding.resol.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ResolBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class ResolBindingConstants {

    private static final String BRIDGE_VBUSLAN = "vbuslan";

    public static final String BINDING_ID = "resol";

    // List of all ChannelTypeUIDs is empty, as we got totally rid of static channel types.
    // ChannelTypeUIDs are constructed from the BINDING_ID and the UnitCodeTextIndex from the VSF

    // List of all Thing Type
    public static final String THING_ID_DEVICE = "device";
    public static final String THING_ID_EMU_EM = "emulatedEM";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_UID_BRIDGE = new ThingTypeUID(BINDING_ID, BRIDGE_VBUSLAN);

    public static final ThingTypeUID THING_TYPE_UID_DEVICE = new ThingTypeUID(BINDING_ID, THING_ID_DEVICE);

    public static final ThingTypeUID THING_TYPE_UID_EMU_EM = new ThingTypeUID(BINDING_ID, THING_ID_EMU_EM);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_UID_BRIDGE,
            THING_TYPE_UID_DEVICE, THING_TYPE_UID_EMU_EM);

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Set.of(THING_TYPE_UID_BRIDGE);
}
