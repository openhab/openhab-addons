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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.ROUND_UID;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.TimeSeriesProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.openhab.transform.basicprofiles.internal.config.RoundStateProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies rounding with the specified scale and the rounding mode to a {@link QuantityType}, {@link DecimalType},
 * or {@link DateTimeType} state. Default rounding mode is {@link RoundingMode#HALF_UP}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class RoundStateProfile implements TimeSeriesProfile {

    private final Logger logger = LoggerFactory.getLogger(RoundStateProfile.class);

    public static final String PARAM_PRECISION = "precision";
    public static final String PARAM_SCALE = "scale";
    public static final String PARAM_MODE = "mode";

    private static final int DATETIME_DEFAULT_SCALE = 3;

    private final ProfileCallback callback;

    final @Nullable Integer precision;
    final @Nullable Integer scale;
    final RoundingMode roundingMode;

    public RoundStateProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;

        RoundStateProfileConfig config = context.getConfiguration().as(RoundStateProfileConfig.class);
        logger.debug("Configuring profile with parameters: [scale='{}', precision='{}', mode='{}']", config.scale,
                config.precision, config.mode);

        Integer localScale = null;
        if (config.scale != null) {
            localScale = config.scale;
        }

        Integer localPrecision = null;
        if (config.precision != null) {
            if (config.precision.intValue() > 0) {
                localPrecision = config.precision;
            } else {
                logger.warn("Parameter 'precision' must be > 0: '{}'. Ignoring it.", config.precision);
            }
        }

        RoundingMode localRoundingMode = RoundingMode.HALF_UP;
        if (config.mode instanceof String) {
            try {
                localRoundingMode = RoundingMode.valueOf(config.mode);
            } catch (IllegalArgumentException e) {
                logger.warn("Parameter 'mode' is not a supported rounding mode: '{}'. Using default.", config.mode);
            }
        } else {
            logger.error("Parameter 'mode' is not of type String.");
        }

        this.precision = localPrecision;
        this.scale = localScale;
        this.roundingMode = localRoundingMode;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return ROUND_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // do nothing
    }

    @Override
    public void onCommandFromItem(Command command) {
        callback.handleCommand((Command) applyRound(command));
    }

    @Override
    public void onCommandFromHandler(Command command) {
        callback.sendCommand((Command) applyRound(command));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        callback.sendUpdate((State) applyRound(state));
    }

    @Override
    public void onTimeSeriesFromHandler(TimeSeries timeSeries) {
        TimeSeries transformedTimeSeries = new TimeSeries(timeSeries.getPolicy());
        timeSeries.getStates()
                .forEach(entry -> transformedTimeSeries.add(entry.timestamp(), (State) applyRound(entry.state())));
        callback.sendTimeSeries(transformedTimeSeries);
    }

    private Type applyRound(Type state) {
        if (state instanceof UnDefType) {
            return state;
        }

        if (state instanceof QuantityType<?> qtState) {
            BigDecimal rounded = roundNumber(qtState.toBigDecimal());
            return new QuantityType<>(rounded, qtState.getUnit());
        } else if (state instanceof DateTimeType dtState) {
            return roundDateTime(dtState);
        } else if (state instanceof DecimalType dtState) {
            BigDecimal rounded = roundNumber(dtState.toBigDecimal());
            return new DecimalType(rounded);
        } else {
            logger.warn(
                    "Round cannot be applied to the incompatible state '{}' sent from the binding. Returning original state.",
                    state);
            return state;
        }
    }

    private BigDecimal roundNumber(BigDecimal value) {
        BigDecimal result = value;

        if (precision != null) {
            result = result.round(new MathContext(precision.intValue(), roundingMode));
        }
        if (scale != null) {
            result = result.setScale(scale.intValue(), roundingMode);
        }
        return result;
    }

    private DateTimeType roundDateTime(DateTimeType value) {
        final int configuredScale = scale != null ? scale.intValue() : DATETIME_DEFAULT_SCALE;

        if (precision != null) {
            logger.debug("Ignoring precision '{}' for DateTime value rounding.", precision);
        }

        final Long unitNanos = getDateTimeUnitNanos(configuredScale);
        if (unitNanos == null) {
            logger.warn(
                    "Scale '{}' is not supported for DateTime values. Supported scales are 0 (days), 1 (hours), 2 (minutes), 3 (seconds), and 4 (milliseconds). Returning original state.",
                    configuredScale);
            return value;
        }

        return new DateTimeType(roundInstant(value.getInstant(), unitNanos.longValue()));
    }

    private Instant roundInstant(Instant value, long unitNanos) {
        long epochNanos = Math.addExact(Math.multiplyExact(value.getEpochSecond(), 1_000_000_000L), value.getNano());
        long floor = Math.multiplyExact(Math.floorDiv(epochNanos, unitNanos), unitNanos);
        if (floor == epochNanos) {
            return instantFromEpochNanos(floor);
        }
        long ceiling = Math.addExact(floor, unitNanos);
        return instantFromEpochNanos(selectRoundedEpochNanos(epochNanos, floor, ceiling, unitNanos));
    }

    private long selectRoundedEpochNanos(long value, long floor, long ceiling, long unitNanos) {
        return switch (roundingMode) {
            case UP -> value >= 0 ? ceiling : floor;
            case DOWN -> value >= 0 ? floor : ceiling;
            case CEILING -> ceiling;
            case FLOOR -> floor;
            case HALF_UP -> selectHalfRoundedEpochNanos(value, floor, ceiling, true);
            case HALF_DOWN -> selectHalfRoundedEpochNanos(value, floor, ceiling, false);
            case HALF_EVEN -> selectHalfEvenRoundedEpochNanos(value, floor, ceiling, unitNanos);
            case UNNECESSARY -> throw new ArithmeticException("Rounding necessary");
        };
    }

    private long selectHalfRoundedEpochNanos(long value, long floor, long ceiling, boolean tiesAwayFromZero) {
        int distanceComparison = Long.compare(Math.subtractExact(value, floor), Math.subtractExact(ceiling, value));
        if (distanceComparison < 0) {
            return floor;
        } else if (distanceComparison > 0) {
            return ceiling;
        }
        if (tiesAwayFromZero) {
            return value >= 0 ? ceiling : floor;
        }
        return value >= 0 ? floor : ceiling;
    }

    private long selectHalfEvenRoundedEpochNanos(long value, long floor, long ceiling, long unitNanos) {
        int distanceComparison = Long.compare(Math.subtractExact(value, floor), Math.subtractExact(ceiling, value));
        if (distanceComparison < 0) {
            return floor;
        } else if (distanceComparison > 0) {
            return ceiling;
        }
        return Math.floorMod(Math.floorDiv(floor, unitNanos), 2) == 0 ? floor : ceiling;
    }

    private @Nullable Long getDateTimeUnitNanos(int configuredScale) {
        return switch (configuredScale) {
            case 0 -> 86_400_000_000_000L;
            case 1 -> 3_600_000_000_000L;
            case 2 -> 60_000_000_000L;
            case 3 -> 1_000_000_000L;
            case 4 -> 1_000_000L;
            default -> null;
        };
    }

    private Instant instantFromEpochNanos(long epochNanos) {
        return Instant.ofEpochSecond(Math.floorDiv(epochNanos, 1_000_000_000L),
                Math.floorMod(epochNanos, 1_000_000_000L));
    }
}
