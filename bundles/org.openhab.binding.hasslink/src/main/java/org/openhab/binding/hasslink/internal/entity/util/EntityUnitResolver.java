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

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.api.dto.HomeAssistantConfig;
import org.openhab.binding.hasslink.internal.api.dto.HomeAssistantConfig.UnitSystem;

/**
 * Resolves target units for Home Assistant entities by evaluating explicit state attributes
 * and falling back to global Home Assistant system unit preferences.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class EntityUnitResolver {

    private EntityUnitResolver() {
        // Utility class
    }

    public static @Nullable String resolveUnit(@Nullable EntityState entityState,
            @Nullable HomeAssistantConfig haConfig, String attributeName,
            Function<UnitSystem, @Nullable String> configUnitExtractor) {

        if (entityState != null) {
            String unit = entityState.getAttributeAsString(attributeName);
            if (unit != null && !unit.isBlank()) {
                return unit;
            }
        }

        HomeAssistantConfig.UnitSystem unitSystem = haConfig != null ? haConfig.unitSystem : null;
        if (unitSystem != null) {
            return configUnitExtractor.apply(unitSystem);
        }

        return null;
    }

    public static @Nullable String resolveTemperatureUnit(@Nullable EntityState entityState,
            @Nullable HomeAssistantConfig haConfig) {
        return resolveUnit(entityState, haConfig, "temperature_unit", u -> u.temperature);
    }

    public static @Nullable String resolveLengthUnit(@Nullable EntityState entityState,
            @Nullable HomeAssistantConfig haConfig) {
        return resolveUnit(entityState, haConfig, "length_unit", u -> u.length);
    }

    public static @Nullable String resolveDeviceClassUnit(@Nullable EntityState entityState,
            @Nullable HomeAssistantConfig haConfig, @Nullable String deviceClass) {
        if (deviceClass == null || deviceClass.isBlank()) {
            return null;
        }

        deviceClass = deviceClass.toLowerCase();

        // Try explicit unit attribute first (e.g. "temperature_unit", "length_unit")
        String unitAttr = deviceClass + "_unit";
        if (entityState != null) {
            String explicitUnit = entityState.getAttributeAsString(unitAttr);
            if (explicitUnit != null && !explicitUnit.isBlank()) {
                return explicitUnit;
            }
        }

        // Fallback to Home Assistant system default unit for the device class
        return switch (deviceClass) {
            case "temperature" -> resolveTemperatureUnit(entityState, haConfig);
            case "distance", "length" -> resolveLengthUnit(entityState, haConfig);
            case "precipitation", "precipitation_intensity" ->
                resolveUnit(entityState, haConfig, "precipitation_unit", u -> u.accumulatedPrecipitation);
            case "pressure" -> resolveUnit(entityState, haConfig, "pressure_unit", u -> u.pressure);
            case "speed", "wind_speed" -> resolveUnit(entityState, haConfig, "speed_unit", u -> u.windSpeed);
            case "volume", "volume_storage" -> resolveUnit(entityState, haConfig, "volume_unit", u -> u.volume);
            case "weight", "mass" -> resolveUnit(entityState, haConfig, "mass_unit", u -> u.mass);
            default -> null;
        };
    }
}
