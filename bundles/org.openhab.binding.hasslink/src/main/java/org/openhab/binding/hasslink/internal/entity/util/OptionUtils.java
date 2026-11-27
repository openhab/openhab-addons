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
package org.openhab.binding.hasslink.internal.entity.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.util.StringUtils;

/**
 * Utility class providing helper methods for building dynamic openHAB {@link StateDescriptionFragment}
 * options and {@link CommandOption} lists from Home Assistant {@link EntityState} payloads.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class OptionUtils {

    private OptionUtils() {
        // Utility class
    }

    /**
     * Capitalizes snake_case strings into human-readable labels (e.g. "return_to_base" -> "Return to base").
     */
    public static String defaultLabelMapper(String rawValue) {
        return Objects.requireNonNull(StringUtils.capitalizeByWhitespace(rawValue.replace('_', ' ')));
    }

    // ============================================================================================
    // Numeric Ranges (StateDescription)
    // ============================================================================================

    /**
     * Extracts numeric bounds (min, max, step) from entity state attributes.
     */
    public static @Nullable StateDescriptionFragment extractRange(EntityState entityState, @Nullable String minAttr,
            @Nullable String maxAttr, @Nullable String stepAttr) {
        BigDecimal min = minAttr != null ? entityState.getAttributeAsBigDecimal(minAttr) : null;
        BigDecimal max = maxAttr != null ? entityState.getAttributeAsBigDecimal(maxAttr) : null;
        BigDecimal step = stepAttr != null ? entityState.getAttributeAsBigDecimal(stepAttr) : null;

        if (min == null && max == null && step == null) {
            return null;
        }

        StateDescriptionFragmentBuilder builder = StateDescriptionFragmentBuilder.create();
        if (min != null) {
            builder.withMinimum(min);
        }
        if (max != null) {
            builder.withMaximum(max);
        }
        if (step != null) {
            builder.withStep(step);
        }
        return builder.build();
    }

    // ============================================================================================
    // State Options (StateDescriptionFragment)
    // ============================================================================================

    public static @Nullable StateDescriptionFragment extractStateOptions(EntityState entityState,
            String optionsAttribute, Function<String, String> labelMapper) {
        List<StateOption> options = extractRawOptions(entityState, optionsAttribute, labelMapper, StateOption::new);
        return options.isEmpty() ? null : StateDescriptionFragmentBuilder.create().withOptions(options).build();
    }

    public static @Nullable StateDescriptionFragment extractStateOptions(EntityState entityState,
            String optionsAttribute) {
        return extractStateOptions(entityState, optionsAttribute, OptionUtils::defaultLabelMapper);
    }

    public static StateDescriptionFragment extractStateBitmaskOptions(EntityState entityState, String attributeName,
            List<StateOption> defaultOptions, Map<Long, StateOption> featureMap) {
        List<StateOption> options = extractRawBitmaskOptions(entityState, attributeName, defaultOptions, featureMap);
        return StateDescriptionFragmentBuilder.create().withOptions(options).build();
    }

    // ============================================================================================
    // Command Options (List<CommandOption>)
    // ============================================================================================

    public static List<CommandOption> extractCommandOptions(EntityState entityState, String optionsAttribute,
            Function<String, String> labelMapper) {
        return extractRawOptions(entityState, optionsAttribute, labelMapper, CommandOption::new);
    }

    public static List<CommandOption> extractCommandOptions(EntityState entityState, String optionsAttribute) {
        return extractCommandOptions(entityState, optionsAttribute, OptionUtils::defaultLabelMapper);
    }

    public static List<CommandOption> extractCommandBitmaskOptions(EntityState entityState, String attributeName,
            List<CommandOption> defaultOptions, Map<Long, CommandOption> featureMap) {
        return extractRawBitmaskOptions(entityState, attributeName, defaultOptions, featureMap);
    }

    // ============================================================================================
    // Private Generic Helpers
    // ============================================================================================

    private static <T> List<T> extractRawOptions(EntityState entityState, String optionsAttribute,
            Function<String, String> labelMapper, BiFunction<String, @Nullable String, T> optionFactory) {
        List<String> rawOptions = entityState.getAttributeAsStringList(optionsAttribute);
        if (rawOptions.isEmpty()) {
            return List.of();
        }

        List<T> result = new ArrayList<>(rawOptions.size());
        for (String option : rawOptions) {
            result.add(optionFactory.apply(option, labelMapper.apply(option)));
        }
        return result;
    }

    private static <T> List<T> extractRawBitmaskOptions(EntityState entityState, String attributeName,
            List<T> defaultOptions, Map<Long, T> featureMap) {
        List<T> options = new ArrayList<>(defaultOptions);

        Long features = entityState.getAttributeAsLong(attributeName);
        if (features != null) {
            long flags = features;
            for (Map.Entry<Long, T> entry : featureMap.entrySet()) {
                if ((flags & entry.getKey()) != 0) {
                    options.add(entry.getValue());
                }
            }
        }

        return options;
    }
}
