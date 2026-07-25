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

    public static boolean findBoolean(List<MelCloudHomeSetting> settings, String name) {
        Optional<String> value = findString(settings, name);
        return value.isPresent() && Boolean.parseBoolean(value.get());
    }

    public static Optional<Double> findDouble(List<MelCloudHomeSetting> settings, String name) {
        return findString(settings, name).flatMap(MelCloudHomeSettingsParser::parseDoubleSafely);
    }

    private static Optional<Double> parseDoubleSafely(String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
