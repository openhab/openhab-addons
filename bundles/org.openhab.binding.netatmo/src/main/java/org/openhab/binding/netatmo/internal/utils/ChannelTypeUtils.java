/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.Measure;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureClass;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * This class holds various channel values conversion methods
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Rob Nielsen - Added day, week, and month measurements to the weather station and modules
 *
 */
@NonNullByDefault
public class ChannelTypeUtils {

    public static @Nullable QuantityType<?> commandToQuantity(Command command, MeasureClass measureClass) {
        Measure measureDef = NetatmoConstants.NA_MEASURES.get(measureClass);
        if (measureDef != null) {
            if (command instanceof QuantityType<?>) {
                return ((QuantityType<?>) command).toUnit(measureDef.unit);
            }
            try {
                double value = Double.parseDouble(command.toString());
                return QuantityType.valueOf(value, measureDef.unit);
            } catch (NumberFormatException ignore) {
            }
        }
        return null;
    }

    public static State toStringType(@Nullable Enum<?> value) {
        return (value == null) ? UnDefType.NULL : new StringType(value.name());
    }

    public static State toStringType(@Nullable String value) {
        return (value == null) ? UnDefType.NULL : new StringType(value);
    }

    public static ZonedDateTime toZonedDateTime(long netatmoTS, ZoneId zoneId) {
        Instant i = Instant.ofEpochSecond(netatmoTS);
        return ZonedDateTime.ofInstant(i, zoneId);
    }

    public static State toDateTimeType(@Nullable Double netatmoTS, ZoneId zoneId) {
        return netatmoTS == null ? UnDefType.NULL : toDateTimeType(toZonedDateTime(netatmoTS.intValue(), zoneId));
    }

    public static State toDateTimeType(long netatmoTS, ZoneId zoneId) {
        return toDateTimeType(toZonedDateTime(netatmoTS, zoneId));
    }

    public static State toDateTimeType(@Nullable ZonedDateTime zonedDateTime) {
        return (zonedDateTime == null) ? UnDefType.NULL : new DateTimeType(zonedDateTime);
    }

    public static State toDecimalType(@Nullable BigDecimal value) {
        return value == null ? UnDefType.NULL : new DecimalType(value.setScale(2, RoundingMode.HALF_UP));
    }

    public static State toDecimalType(@Nullable String textualDecimal) {
        return textualDecimal == null ? UnDefType.NULL : new DecimalType(textualDecimal);
    }

    public static State toQuantityType(@Nullable Double value, @Nullable MeasureClass measureClass) {
        if (value != null && !value.isNaN()) {
            if (measureClass != null) {
                Measure measureDef = NetatmoConstants.NA_MEASURES.get(measureClass);
                if (measureDef != null) {
                    BigDecimal measure = new BigDecimal(Math.min(Math.max(measureDef.minValue, value), value))
                            .setScale(measureDef.scale, RoundingMode.HALF_UP);
                    return new QuantityType<>(measure, measureDef.unit);
                }
            } else {
                return new DecimalType(value);
            }
        }
        return UnDefType.NULL;
    }

    public static State toQuantityType(@Nullable Double value, @Nullable Unit<?> unit) {
        return value == null || value.isNaN() ? UnDefType.NULL
                : unit == null ? new DecimalType(value)
                        : toQuantityType(new BigDecimal(value).setScale(2, RoundingMode.HALF_UP), unit);
    }

    public static State toQuantityType(@Nullable Number value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, unit);
    }

    public static State toRawType(@Nullable String pictureUrl) {
        if (pictureUrl != null) {
            RawType picture = HttpUtil.downloadImage(pictureUrl);
            if (picture != null) {
                return picture;
            }
        }
        return UnDefType.UNDEF;
    }
}
