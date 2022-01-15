/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.handler.channel;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Utility class handling type conversions from Java types to channel types.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class ChannelTypeUtil {
    private ChannelTypeUtil() {
        throw new IllegalStateException("ChannelTypeUtil cannot be instantiated.");
    }

    /**
     * Converts an {@link Optional} of {@link String} to {@link State}.
     */
    public static State stringToState(Optional<String> value) {
        return value.filter(v -> !v.isEmpty()).filter(v -> !v.equals("null")).map(v -> (State) new StringType(v))
                .orElse(UnDefType.UNDEF);
    }

    /**
     * Converts an {@link Optional} of {@link Boolean} to {@link State}.
     */
    public static State booleanToState(Optional<Boolean> value) {
        return value.map(v -> (State) OnOffType.from(v)).orElse(UnDefType.UNDEF);
    }

    /**
     * Converts an {@link Optional} of {@link Integer} to {@link State}.
     */
    public static State intToState(Optional<Integer> value) {
        return value.map(v -> (State) new DecimalType(v)).orElse(UnDefType.UNDEF);
    }

    /**
     * Converts an {@link Optional} of {@link Long} to {@link State}.
     */
    public static State longToState(Optional<Long> value) {
        return value.map(v -> (State) new DecimalType(v)).orElse(UnDefType.UNDEF);
    }

    /**
     * Converts an {@link Optional} of {@link Integer} to {@link State} representing a temperature.
     */
    public static State intToTemperatureState(Optional<Integer> value) {
        // The Miele 3rd Party API always provides temperatures in °C (even if the device uses another unit).
        return value.map(v -> (State) new QuantityType<>(v, SIUnits.CELSIUS)).orElse(UnDefType.UNDEF);
    }
}
