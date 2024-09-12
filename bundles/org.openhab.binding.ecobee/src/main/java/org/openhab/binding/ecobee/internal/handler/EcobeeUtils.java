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
package org.openhab.binding.ecobee.internal.handler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link EcobeeUtils} contains utility methods used by the
 * thing handler and the bridge handler.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public final class EcobeeUtils {

    private static final int UNKNOWN_VALUE = -5002;

    /*
     * Checks to see if a bridge is online.
     */
    public static boolean isBridgeOnline(@Nullable Bridge bridge) {
        boolean bridgeStatus = false;
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            bridgeStatus = true;
        }
        return bridgeStatus;
    }

    /*
     * Set the state to the passed value. If value is null, set the state to UNDEF
     */
    public static State undefOrOnOff(@Nullable Boolean value) {
        return value == null ? UnDefType.UNDEF : OnOffType.from(value);
    }

    public static State undefOrString(@Nullable String value) {
        return value == null ? UnDefType.UNDEF : new StringType(value);
    }

    public static State undefOrDecimal(@Nullable Number value) {
        return (value == null || isUnknown(value)) ? UnDefType.UNDEF : new DecimalType(value.doubleValue());
    }

    public static State undefOrLong(@Nullable Number value) {
        return (value == null || isUnknown(value)) ? UnDefType.UNDEF : new DecimalType(value.longValue());
    }

    public static State undefOrQuantity(@Nullable Number value, Unit<?> unit) {
        return (value == null || isUnknown(value)) ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    public static State undefOrTemperature(@Nullable Number value) {
        return (value == null || isUnknown(value)) ? UnDefType.UNDEF
                : new QuantityType<>(value.doubleValue() / 10.0, ImperialUnits.FAHRENHEIT);
    }

    public static State undefOrPoint(@Nullable String value) {
        return value == null ? UnDefType.UNDEF : new PointType(value);
    }

    public static State undefOrDate(@Nullable Instant instant, TimeZoneProvider timeZoneProvider) {
        return instant == null ? UnDefType.UNDEF
                : new DateTimeType(ZonedDateTime.ofInstant(instant, timeZoneProvider.getTimeZone()));
    }

    public static State undefOrDate(@Nullable LocalDateTime ldt, TimeZoneProvider timeZoneProvider) {
        return ldt == null ? UnDefType.UNDEF : new DateTimeType(ldt.atZone(timeZoneProvider.getTimeZone()));
    }

    private static boolean isUnknown(Number value) {
        return value.intValue() == UNKNOWN_VALUE;
    }

    /*
     * Convert a QuantityType<Temperature> to the internal format used by the Ecobee API.
     */
    @SuppressWarnings("unchecked")
    public static Integer convertQuantityTypeToEcobeeTemp(Object value) {
        if (value instanceof QuantityType<?>) {
            QuantityType<Temperature> convertedTemp = ((QuantityType<Temperature>) value)
                    .toUnit(ImperialUnits.FAHRENHEIT);
            if (convertedTemp != null) {
                return Integer.valueOf((int) (convertedTemp.doubleValue() * 10));
            }
        }
        throw new IllegalArgumentException("temperature is not a QuantityType");
    }
}
