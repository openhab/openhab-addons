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
package org.openhab.binding.netatmo.internal.api.dto;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link LocationEx} is the common interface for dto holding an extra location data
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public interface LocationEx extends Location {
    Optional<String> getCountry();

    @Nullable
    String getTimezone();

    public default ZoneId getZoneId(ZoneId fallback) {
        String local = getTimezone();
        if (local != null) {
            try {
                return ZoneId.of(local);
            } catch (DateTimeException ignore) {
            }
        }
        return fallback;
    }
}
