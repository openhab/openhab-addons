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
package org.openhab.binding.e3dc.internal.cloud;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The AI 360 settings of one power plant, as published by the {@code ai360} channel group.
 *
 * <p>
 * The cloud keeps these values in two places: the installation holds whether the battery takes part and
 * how it treats the feed in tariff, the storage holds the price limit, the state of charge to stop at and
 * the months the limit applies to. This record joins both so a channel update needs one value only.
 *
 * @param enabled price based charging of the battery is active
 * @param batteryPriceCharging the battery takes part in dynamic prices
 * @param sgReady the battery is controlled through an SG Ready signal
 * @param feedInTariffType how charging reacts to the feed in tariff, empty when none is set
 * @param priceLimit electricity price in ct/kWh below which the battery is charged from the grid
 * @param chargeUntilSoc state of charge in percent up to which the battery is charged from the grid
 * @param activeMonths months the price limit applies to, bit 0 is January
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record E3DCAi360State(boolean enabled, boolean batteryPriceCharging, boolean sgReady, String feedInTariffType,
        double priceLimit, int chargeUntilSoc, int activeMonths) {

    /** The feed in tariff type the cloud reports when no tariff is configured. */
    public static final String NO_FEED_IN_TARIFF = "";

    /** No month is enabled. */
    public static final int NO_MONTHS = 0;

    /** Every month is enabled. */
    public static final int ALL_MONTHS = 0xFFF;
}
