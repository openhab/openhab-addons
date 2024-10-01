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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.ROUND_UID;

import java.math.RoundingMode;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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
public class RoundStateProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(RoundStateProfile.class);

    public static final String PARAM_SCALE = "scale";
    public static final String PARAM_MODE = "mode";

    private final ProfileCallback callback;

    final int scale;
    final RoundingMode roundingMode;

    public RoundStateProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        RoundStateProfileConfig config = context.getConfiguration().as(RoundStateProfileConfig.class);
        logger.debug("Configuring profile with parameters: [{scale='{}', mode='{}']", config.scale, config.mode);

        int localScale = 0;
        Integer configScale = config.scale;
        if (configScale != null) {
            localScale = ((Number) configScale).intValue();
        } else {
            logger.error("Parameter 'scale' is not of type String or Number.");
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

    private Type applyRound(Type state) {
        if (state instanceof UnDefType) {
            // we cannot round UNDEF or NULL values, thus we simply return them without reporting an error or warning
            return state;
        }

        Type result = UnDefType.UNDEF;
        if (state instanceof QuantityType<?> qtState) {
            result = new QuantityType<>(qtState.toBigDecimal().setScale(scale, roundingMode), qtState.getUnit());
        } else if (state instanceof DecimalType dtState) {
            result = new DecimalType(dtState.toBigDecimal().setScale(scale, roundingMode));
        } else {
            logger.warn(
                    "Round cannot be applied to the incompatible state '{}' sent from the binding. Returning original state.",
                    state);
            result = state;
        }
        return result;
    }
}
