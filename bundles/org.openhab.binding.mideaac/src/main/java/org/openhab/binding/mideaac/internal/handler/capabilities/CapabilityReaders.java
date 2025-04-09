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
package org.openhab.binding.mideaac.internal.handler.capabilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.handler.capabilities.CapabilityParser.CapabilityId;

/**
 * The {@link CapabilityReaders} reads the raw capability message and
 * breaks them into detailed capabilities.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CapabilityReaders {
    private static final Map<CapabilityId, List<Reader>> READERS = new HashMap<>();

    static {
        // Helper to simplify creation of READERS
        Function<Integer, Predicate<Integer>> getValue = (expected) -> (value) -> Objects.equals(value, expected);

        // Add READERS for each capability - Not all are supported
        READERS.put(CapabilityId.ANION, List.of(new Reader("anion", getValue.apply(1))));
        READERS.put(CapabilityId.AUX_ELECTRIC_HEAT, List.of(new Reader("aux_electric_heat", getValue.apply(1))));
        READERS.put(CapabilityId.BREEZE_AWAY, List.of(new Reader("breeze_away", getValue.apply(1))));
        READERS.put(CapabilityId.BREEZE_CONTROL, List.of(new Reader("breeze_control", getValue.apply(1))));
        READERS.put(CapabilityId.BREEZELESS, List.of(new Reader("breezeless", getValue.apply(1))));
        READERS.put(CapabilityId.BUZZER, List.of(new Reader("buzzer", getValue.apply(1))));

        READERS.put(CapabilityId.DISPLAY_CONTROL,
                List.of(new Reader("display_control", (value) -> List.of(1, 2, 100).contains(value))));

        READERS.put(CapabilityId.ENERGY,
                List.of(new Reader("energy_stats", (value) -> List.of(2, 3, 4, 5).contains(value)),
                        new Reader("energy_setting", (value) -> List.of(3, 5).contains(value)),
                        new Reader("energy_bcd", (value) -> List.of(2, 3).contains(value))));

        READERS.put(CapabilityId.FAHRENHEIT, List.of(new Reader("fahrenheit", getValue.apply(0))));

        READERS.put(CapabilityId.FAN_SPEED_CONTROL,
                List.of(new Reader("fan_silent", getValue.apply(6)),
                        new Reader("fan_low", (value) -> List.of(3, 4, 5, 6, 7).contains(value)),
                        new Reader("fan_medium", (value) -> List.of(5, 6, 7).contains(value)),
                        new Reader("fan_high", (value) -> List.of(3, 4, 5, 6, 7).contains(value)),
                        new Reader("fan_auto", (value) -> List.of(4, 5, 6).contains(value)),
                        new Reader("fan_custom", getValue.apply(1))));

        READERS.put(CapabilityId.FILTER_REMIND,
                List.of(new Reader("filter_notice", (value) -> List.of(1, 2, 4).contains(value)),
                        new Reader("filter_clean", (value) -> List.of(3, 4).contains(value))));

        READERS.put(CapabilityId.HUMIDITY,
                List.of(new Reader("humidity_auto_set", (value) -> List.of(1, 2).contains(value)),
                        new Reader("humidity_manual_set", (value) -> List.of(2, 3).contains(value))));

        READERS.put(CapabilityId.MODES,
                List.of(new Reader("mode_heat", (value) -> List.of(1, 2, 4, 6, 7, 9, 10, 11, 12, 13).contains(value)),
                        new Reader("mode_cool", (value) -> !List.of(2, 10, 12).contains(value)),
                        new Reader("mode_dry", (value) -> List.of(0, 1, 5, 6, 9, 11, 13).contains(value)),
                        new Reader("mode_auto", (value) -> List.of(0, 1, 2, 7, 8, 9, 13).contains(value)),
                        new Reader("mode_aux_heat", getValue.apply(9)),
                        new Reader("mode_aux", (value) -> List.of(9, 10, 11, 13).contains(value))));

        READERS.put(CapabilityId.PRESET_ECO, List.of(new Reader("eco_cool", (value) -> List.of(1, 2).contains(value))));
        READERS.put(CapabilityId.PRESET_FREEZE_PROTECTION, List.of(new Reader("freeze_protection", getValue.apply(1))));
        READERS.put(CapabilityId.PRESET_IECO, List.of(new Reader("ieco", getValue.apply(1))));

        READERS.put(CapabilityId.PRESET_TURBO,
                List.of(new Reader("turbo_heat", (value) -> List.of(1, 3).contains(value)),
                        new Reader("turbo_cool", (value) -> value < 2)));

        READERS.put(CapabilityId.RATE_SELECT, List.of(new Reader("rate_select_2_level", getValue.apply(1)),
                new Reader("rate_select_5_level", (value) -> List.of(2, 3).contains(value))));

        READERS.put(CapabilityId.SELF_CLEAN, List.of(new Reader("self_clean", getValue.apply(1))));
        READERS.put(CapabilityId.SMART_EYE, List.of(new Reader("smart_eye", getValue.apply(1))));
        READERS.put(CapabilityId.SWING_LR_ANGLE, List.of(new Reader("swing_horizontal_angle", getValue.apply(1))));
        READERS.put(CapabilityId.SWING_UD_ANGLE, List.of(new Reader("swing_vertical_angle", getValue.apply(1))));

        READERS.put(CapabilityId.SWING_MODES,
                List.of(new Reader("swing_horizontal", (value) -> List.of(1, 3).contains(value)),
                        new Reader("swing_vertical", (value) -> value < 2)));

        READERS.put(CapabilityId.WIND_OFF_ME, List.of(new Reader("wind_off_me", getValue.apply(1))));
        READERS.put(CapabilityId.WIND_ON_ME, List.of(new Reader("wind_on_me", getValue.apply(1))));
    }

    public static boolean hasReader(CapabilityId id) {
        return READERS.containsKey(id);
    }

    public static void apply(CapabilityId id, int value, Map<CapabilityId, Map<String, Boolean>> capabilities) {
        Optional.ofNullable(READERS.get(id)).ifPresent(readersList -> {
            Map<String, Boolean> result = readersList.stream()
                    .map(reader -> Map.entry(reader.name, reader.predicate.test(value)))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            capabilities.put(id, result);
        });
    }
}
