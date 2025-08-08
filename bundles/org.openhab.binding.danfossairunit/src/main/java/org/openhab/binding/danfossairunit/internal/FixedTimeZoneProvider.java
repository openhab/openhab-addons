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
package org.openhab.binding.danfossairunit.internal;

import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.TimeZoneProvider;

/**
 * Provider for returning a fixed time zone.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class FixedTimeZoneProvider implements TimeZoneProvider {

    private final ZoneId timeZone;

    private FixedTimeZoneProvider(ZoneId timeZone) {
        this.timeZone = timeZone;
    }

    public static FixedTimeZoneProvider of(ZoneId timeZone) {
        return new FixedTimeZoneProvider(timeZone);
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }
}
