/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.awattar.internal.dto;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.i18n.TimeZoneProvider;

/**
 *
 * The {@link AwattarTimeProvider} provides a time provider for aWATTar
 *
 * @author Thomas Leber - Initial contribution
 */
public class AwattarTimeProvider {

    private TimeZoneProvider timeZoneProvider;

    public AwattarTimeProvider(TimeZoneProvider timeZoneProvider) {
        this.timeZoneProvider = timeZoneProvider;
    }

    /**
     * Get the current zone id.
     *
     * @return the current zone id
     */
    public @NonNull ZoneId getZoneId() {
        return timeZoneProvider.getTimeZone();
    }

    /**
     * Get the current instant.
     *
     * @return the current instant
     */
    public Instant getInstant() {
        return Clock.systemDefaultZone().instant();
    }

    /**
     * Get the current zoned date time.
     *
     * @return the current zoned date time
     */
    public ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.now(Clock.system(getZoneId()));
    }
}
