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
package org.openhab.binding.awattar.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AwattarBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
@NonNullByDefault
public class AwattarBindingConstants {
    public static final String BINDING_ID = "awattar";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_PRICE = new ThingTypeUID(BINDING_ID, "prices");
    public static final ThingTypeUID THING_TYPE_BESTPRICE = new ThingTypeUID(BINDING_ID, "bestprice");

    public static final String CHANNEL_GROUP_CURRENT = "current";

    // List of all Channel ids
    public static final String CHANNEL_TOTAL_NET = "total-net";
    public static final String CHANNEL_TOTAL_GROSS = "total-gross";
    public static final String CHANNEL_MARKET_NET = "market-net";
    public static final String CHANNEL_MARKET_GROSS = "market-gross";

    public static final String CHANNEL_ACTIVE = "active";
    public static final String CHANNEL_START = "start";
    public static final String CHANNEL_END = "end";
    public static final String CHANNEL_COUNTDOWN = "countdown";
    public static final String CHANNEL_REMAINING = "remaining";
    public static final String CHANNEL_HOURS = "hours";
}
