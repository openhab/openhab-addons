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

        // Add READERS for each capability - Not all are supported by all AC devices
        READERS.put(CapabilityId.ANION, List.of(new Reader("anion", getValue.apply(1))));
        READERS.put(CapabilityId.AUX_ELECTRIC_HEAT, List.of(new Reader("auxElectricHeat", getValue.apply(1))));
        READERS.put(CapabilityId.BREEZE_AWAY, List.of(new Reader("breezeAway", getValue.apply(1))));
        READERS.put(CapabilityId.BREEZE_CONTROL, List.of(new Reader("breezeControl", getValue.apply(1))));
        READERS.put(CapabilityId.BREEZELESS, List.of(new Reader("breezeless", getValue.apply(1))));
        READERS.put(CapabilityId.BUZZER, List.of(new Reader("buzzer", getValue.apply(1))));

        READERS.put(CapabilityId.DISPLAY_CONTROL,
                List.of(new Reader("displayControl", (value) -> List.of(1, 2, 100).contains(value))));

        READERS.put(CapabilityId.ENERGY,
                List.of(new Reader("energyStats", (value) -> List.of(2, 3, 4, 5).contains(value)),
                        new Reader("energySetting", (value) -> List.of(3, 5).contains(value)),
                        new Reader("energyBCD", (value) -> List.of(2, 3).contains(value))));

        READERS.put(CapabilityId.FAHRENHEIT, List.of(new Reader("fahrenheit", getValue.apply(0))));

        READERS.put(CapabilityId.FAN_SPEED_CONTROL,
                List.of(new Reader("fanSilent", getValue.apply(6)),
                        new Reader("fanLow", (value) -> List.of(3, 4, 5, 6, 7).contains(value)),
                        new Reader("fanMedium", (value) -> List.of(5, 6, 7).contains(value)),
                        new Reader("fanHigh", (value) -> List.of(3, 4, 5, 6, 7).contains(value)),
                        new Reader("fanAuto", (value) -> List.of(4, 5, 6).contains(value)),
                        new Reader("fanCustom", getValue.apply(1))));

        READERS.put(CapabilityId.FILTER_REMIND,
                List.of(new Reader("filterNotice", (value) -> List.of(1, 2, 4).contains(value)),
                        new Reader("filterClean", (value) -> List.of(3, 4).contains(value))));

        READERS.put(CapabilityId.HUMIDITY,
                List.of(new Reader("humidityAutoSet", (value) -> List.of(1, 2).contains(value)),
                        new Reader("humidityManualSet", (value) -> List.of(2, 3).contains(value))));

        READERS.put(CapabilityId.MODES,
                List.of(new Reader("modeHeat", (value) -> List.of(1, 2, 4, 6, 7, 9, 10, 11, 12, 13).contains(value)),
                        new Reader("modeCool", (value) -> !List.of(2, 10, 12).contains(value)),
                        new Reader("modeDry", (value) -> List.of(0, 1, 5, 6, 9, 11, 13).contains(value)),
                        new Reader("modeAuto", (value) -> List.of(0, 1, 2, 7, 8, 9, 13).contains(value)),
                        new Reader("modeAuxHeat", getValue.apply(9)),
                        new Reader("modeAux", (value) -> List.of(9, 10, 11, 13).contains(value))));

        READERS.put(CapabilityId.PRESET_ECO, List.of(new Reader("ecoCool", (value) -> List.of(1, 2).contains(value))));
        READERS.put(CapabilityId.PRESET_FREEZE_PROTECTION, List.of(new Reader("freezeProtection", getValue.apply(1))));
        READERS.put(CapabilityId.PRESET_IECO, List.of(new Reader("ieco", getValue.apply(1))));

        READERS.put(CapabilityId.PRESET_TURBO,
                List.of(new Reader("turboHeat", (value) -> List.of(1, 3).contains(value)),
                        new Reader("turboCool", (value) -> value < 2)));

        READERS.put(CapabilityId.RATE_SELECT, List.of(new Reader("rate_select_2_level", getValue.apply(1)),
                new Reader("rateSelect5Level", (value) -> List.of(2, 3).contains(value))));

        READERS.put(CapabilityId.SELF_CLEAN, List.of(new Reader("selfClean", getValue.apply(1))));
        READERS.put(CapabilityId.SMART_EYE, List.of(new Reader("smartEye", getValue.apply(1))));
        READERS.put(CapabilityId.SWING_LR_ANGLE, List.of(new Reader("swingHorizontalAngle", getValue.apply(1))));
        READERS.put(CapabilityId.SWING_UD_ANGLE, List.of(new Reader("swingVerticalAngle", getValue.apply(1))));

        READERS.put(CapabilityId.SWING_MODES,
                List.of(new Reader("swingHorizontal", (value) -> List.of(1, 3).contains(value)),
                        new Reader("swingVertical", (value) -> value < 2)));

        READERS.put(CapabilityId.WIND_OFF_ME, List.of(new Reader("windOffMe", getValue.apply(1))));
        READERS.put(CapabilityId.WIND_ON_ME, List.of(new Reader("windOnMe", getValue.apply(1))));
    }

    /**
     * Validates if Reader exists for the capability
     * 
     * @param id capability id
     * @return true or false
     */
    public static boolean hasReader(CapabilityId id) {
        return READERS.containsKey(id);
    }

    /**
     * Applies the appropriate Reader
     * 
     * @param id id
     * @param value value from reader
     * @param capabilities summary
     */
    public static void apply(CapabilityId id, int value, Map<CapabilityId, Map<String, Boolean>> capabilities) {
        Optional.ofNullable(READERS.get(id)).ifPresent(readersList -> {
            Map<String, Boolean> result = readersList.stream()
                    .map(reader -> Map.entry(reader.name, reader.predicate.test(value)))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            capabilities.put(id, result);
        });
    }
}
