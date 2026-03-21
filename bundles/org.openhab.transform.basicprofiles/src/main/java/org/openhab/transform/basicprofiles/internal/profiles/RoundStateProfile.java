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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * Applies rounding with the specified scale and the rounding mode to a {@link QuantityType} or {@link DecimalType}
 * state. Default rounding mode is {@link RoundingMode#HALF_UP}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class RoundStateProfile implements TimeSeriesProfile {

    private final Logger logger = LoggerFactory.getLogger(RoundStateProfile.class);

    public static final String PARAM_SCALE = "scale";
    public static final String PARAM_PRECISION = "precision";
    public static final String PARAM_MODE = "mode";

    private final ProfileCallback callback;

    final @Nullable Integer scale;
    final @Nullable Integer precision;
    final RoundingMode roundingMode;

    public RoundStateProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;

        RoundStateProfileConfig config = context.getConfiguration().as(RoundStateProfileConfig.class);
        logger.debug("Configuring profile with parameters: [scale='{}', precision='{}', mode='{}']", config.scale,
                config.precision, config.mode);

        Integer localScale = null;
        if (config.scale != null) {
            localScale = config.scale;
        } else {
            logger.error("Parameter 'scale' is not of type String or Number.");
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

        this.scale = localScale;
        this.precision = localPrecision;
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
}
