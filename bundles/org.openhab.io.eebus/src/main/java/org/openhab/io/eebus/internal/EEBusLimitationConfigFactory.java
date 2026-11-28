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
package org.openhab.io.eebus.internal;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openmuc.jeebus.spine.utils.datatypes.ScaledNumberWrapper;
import org.openmuc.jeebus.usecase.powerlimitation.controllablesystem.SimpleLimitationConfig;

/**
 * Builds the {@link SimpleLimitationConfig} passed to {@code LpcCs}/{@code LppCs} from an item's
 * {@code eebus="lpc"}/{@code eebus="lpp"} metadata configuration map (the {@code [ ... ]} bracket
 * parameters), e.g. {@code eebus="lpc" [ nominalMax=11000, failsafeLimit=4200,
 * failsafeDuration="PT2H" ]}.
 * <p>
 * {@link SimpleLimitationConfig}'s constructor is {@code (failsafeDurationMin, failsafeLimit,
 * loadControlLimit, nominalMax)} - NOT "nominal then failsafe" - getting that positional order
 * wrong was a real, confirmed bug in an earlier version of this add-on (see
 * {@code EEBusLimitationConfigFactoryTest}), which is why this mapping is centralised and tested
 * here rather than inlined at each call site.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class EEBusLimitationConfigFactory {

    private static final int SCALED_NUMBER_SCALE = 0;
    static final long DEFAULT_NOMINAL_MAX_WATTS = 4200;
    static final String DEFAULT_FAILSAFE_DURATION = "PT2H";

    private EEBusLimitationConfigFactory() {
    }

    public static SimpleLimitationConfig lpc(Map<String, Object> config) {
        long nominalMaxWatts = wattsOrDefault(config, "nominalMax", DEFAULT_NOMINAL_MAX_WATTS);
        long failsafeWatts = wattsOrDefault(config, "failsafeLimit", nominalMaxWatts);
        String failsafeDuration = failsafeDuration(config);

        ScaledNumberWrapper nominalMax = new ScaledNumberWrapper(nominalMaxWatts, SCALED_NUMBER_SCALE);
        ScaledNumberWrapper failsafe = new ScaledNumberWrapper(failsafeWatts, SCALED_NUMBER_SCALE);
        // loadControlLimit (the initial/default active limit) starts at nominalMax, i.e.
        // unrestricted until the CEM says otherwise.
        return new SimpleLimitationConfig(failsafeDuration, failsafe, nominalMax, nominalMax);
    }

    public static SimpleLimitationConfig lpp(Map<String, Object> config) {
        long nominalMaxWatts = wattsOrDefault(config, "nominalMax", 0);
        long failsafeWatts = wattsOrDefault(config, "failsafeLimit", nominalMaxWatts);
        String failsafeDuration = failsafeDuration(config);

        ScaledNumberWrapper nominalMax = new ScaledNumberWrapper(nominalMaxWatts, SCALED_NUMBER_SCALE);
        ScaledNumberWrapper failsafe = new ScaledNumberWrapper(failsafeWatts, SCALED_NUMBER_SCALE);
        // Per the jEEBus reference usage: for LPP the load-control (default) limit is negative,
        // while nominal max and failsafe remain positive.
        return new SimpleLimitationConfig(failsafeDuration, failsafe, nominalMax.negate(), nominalMax);
    }

    private static long wattsOrDefault(Map<String, Object> config, String key, long defaultValue) {
        if (!config.containsKey(key)) {
            return defaultValue;
        }
        Object value = config.get(key);
        if (value instanceof Number || value instanceof String) {
            try {
                long watts = new BigDecimal(value.toString().trim()).longValueExact();
                if (watts >= 0) {
                    return watts;
                }
            } catch (NumberFormatException | ArithmeticException e) {
                throw new IllegalArgumentException(key + " must be a non-negative whole number of watts", e);
            }
        }
        throw new IllegalArgumentException(key + " must be a non-negative whole number of watts");
    }

    private static String failsafeDuration(Map<String, Object> config) {
        String key = "failsafeDuration";
        if (!config.containsKey(key)) {
            return DEFAULT_FAILSAFE_DURATION;
        }
        if (config.get(key) instanceof String value) {
            try {
                Duration duration = Duration.parse(value.trim());
                if (duration.isPositive()) {
                    return duration.toString();
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(key + " must be a positive ISO 8601 duration", e);
            }
        }
        throw new IllegalArgumentException(key + " must be a positive ISO 8601 duration");
    }
}
