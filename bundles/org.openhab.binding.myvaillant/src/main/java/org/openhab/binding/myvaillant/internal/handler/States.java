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
package org.openhab.binding.myvaillant.internal.handler;

import java.time.Instant;
import java.time.ZoneId;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Conversion between API values and openHAB states and commands.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
final class States {

    private States() {
    }

    static State temperature(@Nullable Double celsius) {
        return quantity(celsius, SIUnits.CELSIUS);
    }

    static State quantity(@Nullable Number value, Unit<?> unit) {
        return value == null ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    static State decimal(@Nullable Number value) {
        return value == null ? UnDefType.UNDEF : new DecimalType(value.doubleValue());
    }

    static State string(@Nullable String value) {
        return value == null ? UnDefType.UNDEF : new StringType(value);
    }

    static State onOff(@Nullable Boolean value) {
        return value == null ? UnDefType.UNDEF : OnOffType.from(value);
    }

    static State dateTime(@Nullable Instant instant, ZoneId zone) {
        return instant == null ? UnDefType.UNDEF : new DateTimeType(instant.atZone(zone));
    }

    /** Temperature in °C from a number or quantity command, {@code null} for other commands */
    static @Nullable Double toCelsius(Command command) {
        return toDouble(command, SIUnits.CELSIUS);
    }

    /** Value in the given unit from a quantity command, plain numbers are taken as that unit */
    static @Nullable Double toDouble(Command command, @Nullable Unit<?> unit) {
        if (command instanceof QuantityType<?> quantity) {
            QuantityType<?> converted = unit == null ? quantity : quantity.toInvertibleUnit(unit);
            return converted == null ? null : converted.doubleValue();
        }
        if (command instanceof DecimalType decimal) {
            return decimal.doubleValue();
        }
        return null;
    }

    static @Nullable Instant toInstant(Command command) {
        return command instanceof DateTimeType dateTime ? dateTime.getInstant() : null;
    }
}
