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
package org.openhab.binding.atmofrance.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AtmoFranceBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AtmoFranceBindingConstants {

    public static final String BINDING_ID = "atmofrance";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_API = new ThingTypeUID(BINDING_ID, "api");
    public static final ThingTypeUID THING_TYPE_CITY = new ThingTypeUID(BINDING_ID, "city");

    // List of all channel groups
    public static final String GROUP_AQ = "aq";
    public static final String GROUP_POLLENS = "pollens";

    // List of all channels
    public static final String CHANNEL_INDEX = "index";
    public static final String CHANNEL_EFFECTIVE_DATE = "effective-date";
    public static final String CHANNEL_DIFFUSION_DATE = "diffusion-date";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_API, THING_TYPE_CITY);
}
