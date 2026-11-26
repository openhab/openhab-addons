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
package org.openhab.binding.tibber.internal.pulse.sml;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;

/**
 * The applicable values of one valid SML frame.
 *
 * @param values the values by channel id ({@code power}, {@code energy-import}, ...), never empty; absent
 *            quantities are not in the map (they are never zero)
 * @param meterId the meter's own server id in lower-case hexadecimal, or null if the frame did not carry it
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public record SmlReading(Map<String, QuantityType<?>> values, @Nullable String meterId) {

    /** Omits the meter id: it must not reach a log. */
    @Override
    public String toString() {
        return "SmlReading[values=" + values + "]";
    }
}
