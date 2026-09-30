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
package org.openhab.binding.gme.internal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Converts raw GME API price records into validated price entries.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public final class GmePriceEntryMapper {

    private static final DateTimeFormatter FLOW_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private GmePriceEntryMapper() {
    }

    public static Optional<GmePriceEntry> map(GmePriceEntryRaw raw) {
        return map(raw, GmeGranularity.PT60);
    }

    public static Optional<GmePriceEntry> map(GmePriceEntryRaw raw, GmeGranularity granularity) {
        if ("Auction cancelled".equalsIgnoreCase(raw.notes())) {
            return Optional.empty();
        }

        try {
            LocalDate flowDate = LocalDate.parse(raw.flowDate(), FLOW_DATE_FORMAT);
            int hour = Integer.parseInt(raw.hour());
            int period = Integer.parseInt(raw.period());
            BigDecimal priceMWh = new BigDecimal(raw.price());

            if (hour < 1 || hour > 25) {
                throw new IllegalArgumentException("Invalid GME hour: " + hour);
            }

            return Optional.of(new GmePriceEntry(flowDate, hour, raw.market(), raw.zone(), priceMWh, period, granularity,
                    raw.notes()));
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid GME price entry", e);
        }
    }
}
