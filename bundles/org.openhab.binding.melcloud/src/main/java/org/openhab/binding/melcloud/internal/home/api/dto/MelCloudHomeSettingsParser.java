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
package org.openhab.binding.melcloud.internal.home.api.dto;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Looks up typed values by name in a unit's {@code settings} array (see {@link MelCloudHomeSetting}).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class MelCloudHomeSettingsParser {

    private MelCloudHomeSettingsParser() {
        // Utility class
    }

    public static Optional<String> findString(List<MelCloudHomeSetting> settings, String name) {
        return settings.stream().filter(setting -> name.equals(setting.name)).map(setting -> setting.value)
                .filter(value -> !value.isEmpty()).findFirst();
    }

    /**
     * Reads a boolean setting without collapsing "absent" into {@code false}, so callers can publish an unknown
     * state instead of a valid-looking {@code OFF}.
     *
     * @param settings the unit's settings array
     * @param name the setting's name
     * @return the setting's value, or empty if it is absent or not an explicit {@code true}/{@code false}
     */
    public static Optional<Boolean> findBoolean(List<MelCloudHomeSetting> settings, String name) {
        return findString(settings, name).flatMap(MelCloudHomeSettingsParser::parseBooleanSafely);
    }

    public static Optional<Double> findDouble(List<MelCloudHomeSetting> settings, String name) {
        return findString(settings, name).flatMap(MelCloudHomeSettingsParser::parseDoubleSafely);
    }

    private static Optional<Boolean> parseBooleanSafely(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return Optional.of(Boolean.TRUE);
        }
        if ("false".equalsIgnoreCase(value)) {
            return Optional.of(Boolean.FALSE);
        }
        return Optional.empty();
    }

    private static Optional<Double> parseDoubleSafely(String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
