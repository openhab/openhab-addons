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
 * @author openHAB EEBus Add-on Contributors - Initial contribution
 */
@NonNullByDefault
public final class EEBusLimitationConfigFactory {

    private static final int SCALED_NUMBER_SCALE = 0;
    static final double DEFAULT_NOMINAL_MAX_WATTS = 4200;
    static final String DEFAULT_FAILSAFE_DURATION = "PT2H";

    private EEBusLimitationConfigFactory() {
    }

    public static SimpleLimitationConfig lpc(Map<String, Object> config) {
        double nominalMaxWatts = numberOrDefault(config, "nominalMax", DEFAULT_NOMINAL_MAX_WATTS);
        double failsafeWatts = numberOrDefault(config, "failsafeLimit", nominalMaxWatts);
        String failsafeDuration = stringOrDefault(config, "failsafeDuration", DEFAULT_FAILSAFE_DURATION);

        ScaledNumberWrapper nominalMax = new ScaledNumberWrapper((long) nominalMaxWatts, SCALED_NUMBER_SCALE);
        ScaledNumberWrapper failsafe = new ScaledNumberWrapper((long) failsafeWatts, SCALED_NUMBER_SCALE);
        // loadControlLimit (the initial/default active limit) starts at nominalMax, i.e.
        // unrestricted until the CEM says otherwise.
        return new SimpleLimitationConfig(failsafeDuration, failsafe, nominalMax, nominalMax);
    }

    public static SimpleLimitationConfig lpp(Map<String, Object> config) {
        double nominalMaxWatts = numberOrDefault(config, "nominalMax", 0);
        double failsafeWatts = numberOrDefault(config, "failsafeLimit", nominalMaxWatts);
        String failsafeDuration = stringOrDefault(config, "failsafeDuration", DEFAULT_FAILSAFE_DURATION);

        ScaledNumberWrapper nominalMax = new ScaledNumberWrapper((long) nominalMaxWatts, SCALED_NUMBER_SCALE);
        ScaledNumberWrapper failsafe = new ScaledNumberWrapper((long) failsafeWatts, SCALED_NUMBER_SCALE);
        // Per the jEEBus reference usage: for LPP the load-control (default) limit is negative,
        // while nominal max and failsafe remain positive.
        return new SimpleLimitationConfig(failsafeDuration, failsafe, nominalMax.negate(), nominalMax);
    }

    private static double numberOrDefault(Map<String, Object> config, String key, double defaultValue) {
        Object value = config.get(key);
        return value instanceof Number number ? number.doubleValue() : defaultValue;
    }

    private static String stringOrDefault(Map<String, Object> config, String key, String defaultValue) {
        Object value = config.get(key);
        return value instanceof String string && !string.isBlank() ? string : defaultValue;
    }
}
