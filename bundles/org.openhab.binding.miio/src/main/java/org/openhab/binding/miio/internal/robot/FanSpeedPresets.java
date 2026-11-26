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
package org.openhab.binding.miio.internal.robot;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.StateOption;

/**
 * Fan speed presets (the values of {@code set_custom_mode}) of the vacuum models. Models that do not have their own
 * preset set use {@link #DEFAULT}, which is also what the static options of the {@code fan} channel list.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum FanSpeedPresets {

    DEFAULT(new Preset(38, "Silent"), new Preset(60, "Standard"), new Preset(75, "Turbo"), new Preset(77, "Power"),
            new Preset(90, "Full"), new Preset(100, "Max"), new Preset(101, "Quiet"), new Preset(102, "Balanced"),
            new Preset(103, "Turbo"), new Preset(104, "Max"), new Preset(105, "Mob"), new Preset(106, "Auto"),
            new Preset(108, "Max+")),
    E2(new Preset(41, "Gentle"), new Preset(50, "Silent"), new Preset(68, "Standard"), new Preset(79, "Strong"),
            new Preset(100, "Max"));

    /** Value reported for a fan power that is not one of the presets. */
    public static final int CUSTOM_VALUE = -1;
    public static final String CUSTOM_LABEL = "Custom";

    private static final String MODEL_E2 = "roborock.vacuum.e2";

    private final List<Preset> presets;

    FanSpeedPresets(Preset... presets) {
        this.presets = List.of(presets);
    }

    public static FanSpeedPresets forModel(String model) {
        return MODEL_E2.equalsIgnoreCase(model) ? E2 : DEFAULT;
    }

    /**
     * @return the preset value matching the fan power, or {@link #CUSTOM_VALUE} if it is not a preset of this set
     */
    public int getValue(int fanPower) {
        return presets.stream().anyMatch(p -> p.value == fanPower) ? fanPower : CUSTOM_VALUE;
    }

    public String getLabel(int fanPower) {
        for (Preset preset : presets) {
            if (preset.value == fanPower) {
                return preset.label;
            }
        }
        return CUSTOM_LABEL;
    }

    public List<StateOption> getOptions() {
        return Stream.concat(presets.stream(), Stream.of(new Preset(CUSTOM_VALUE, CUSTOM_LABEL)))
                .map(p -> new StateOption(Integer.toString(p.value), p.label)).toList();
    }

    private record Preset(int value, String label) {
    }
}
