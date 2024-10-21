/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.entsoe.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.TimeSeries.Policy;

/**
 * The {@link EntsoeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jørgen Melhus - Initial contribution
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class EntsoeBindingConstants {

    private static final String BINDING_ID = "entsoe";

    public static final Policy TIMESERIES_POLICY = Policy.REPLACE;

    public static final String CHANNEL_SPOT_PRICE = "spot-price";

    public static final String CHANNEL_TRIGGER_PRICES_RECEIVED = "prices-received";

    // Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DAY_AHEAD = new ThingTypeUID(BINDING_ID, "day-ahead");

    // List of all Thing Type UIDs
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Set.of(THING_TYPE_DAY_AHEAD);
}
