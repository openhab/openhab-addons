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
package org.openhab.binding.energycharts.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EnergyChartsBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
@NonNullByDefault
public class EnergyChartsBindingConstants {
    public static final String BINDING_ID = "energycharts";

    public static final ThingTypeUID THING_TYPE_PRICES = new ThingTypeUID(BINDING_ID, "prices");

    public static final String CHANNEL_MARKET_NET = "market-net";
    public static final String CHANNEL_MARKET_GROSS = "market-gross";
    public static final String CHANNEL_TOTAL_NET = "total-net";
    public static final String CHANNEL_TOTAL_GROSS = "total-gross";

    /**
     * Bidding zones whose prices Energy-Charts publishes under CC BY 4.0.
     */
    public static final Set<String> BIDDING_ZONES = Set.of("AT", "BE", "CH", "CZ", "DE-LU", "DK1", "DK2", "FR", "HU",
            "IT-North", "NL", "NO2", "PL", "SE4", "SI");
}
