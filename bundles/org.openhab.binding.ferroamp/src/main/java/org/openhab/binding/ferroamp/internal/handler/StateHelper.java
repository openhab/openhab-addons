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

package org.openhab.binding.ferroamp.internal.handler;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ferroamp.internal.api.FerroampMqttCommunication;
import org.openhab.binding.ferroamp.internal.config.ChannelMapping;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link FerroampMqttCommunication} is responsible for communication with Ferroamp-system's Mqtt-broker.
 *
 * @author Leo Siepel - Initial contribution
 *
 */

@NonNullByDefault
public class StateHelper {

    public static State convertToState(ChannelMapping mapping, @Nullable String value) {
        if (value == null || value.isEmpty()) {
            return UnDefType.NULL;
        }
        if (Units.HERTZ.equals(mapping.unit) || //
                Units.PERCENT.equals(mapping.unit) || //
                Units.VOLT.equals(mapping.unit) || //
                Units.WATT.equals(mapping.unit) || //
                Units.WATT_HOUR.equals(mapping.unit) || //
                Units.AMPERE.equals(mapping.unit)) {
            return new QuantityType<>(Double.valueOf(value), mapping.unit);
        }
        if (mapping.unit.equals(Units.ONE)) {
            if (isIso8601(value)) {
                return new DateTimeType(Instant.parse(value));
            } else {
                return new StringType(value);
            }
        }
        return UnDefType.UNDEF;
    }

    private static boolean isIso8601(String value) {
        try {
            Instant.parse(value);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}