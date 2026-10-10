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

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.core.types.util.UnitUtils;

import com.google.gson.JsonElement;

/**
 * Infers the openHAB {@link ItemType} for Home Assistant primary entity states
 * and secondary attributes based on domain, device class, naming conventions, and JSON data types.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class TypeInferrer {

    private static final Set<String> CONTACT_DEVICE_CLASSES = Set.of(
            // Binary sensor contact classes
            "door", "garage_door", "opening", "window", "lock",
            // Cover device classes (positional / open-closed states)
            "awning", "blind", "curtain", "damper", "garage", "gate", "shade", "shutter");

    /**
     * Maps official Home Assistant sensor device classes to standard openHAB UoM dimension names
     * defined in javax.measure.quantity or org.openhab.core.library.dimension.
     */
    public static final Map<String, String> DEVICE_CLASS_DIMENSIONS = Map.ofEntries( //
            Map.entry("area", "Area"), //
            Map.entry("temperature", "Temperature"), //

            Map.entry("power", "Power"), //
            Map.entry("apparent_power", "Power"), //
            Map.entry("reactive_power", "Power"), //

            Map.entry("energy", "Energy"), //
            Map.entry("energy_storage", "Energy"), //

            Map.entry("pressure", "Pressure"), //
            Map.entry("atmospheric_pressure", "Pressure"), //

            Map.entry("voltage", "ElectricPotential"), //
            Map.entry("current", "ElectricCurrent"), //

            Map.entry("humidity", "Dimensionless"), //
            Map.entry("battery", "Dimensionless"), //
            Map.entry("aqi", "Dimensionless"), //
            Map.entry("carbon_dioxide", "Dimensionless"), //
            Map.entry("carbon_monoxide", "Dimensionless"), //
            Map.entry("nitrogen_dioxide", "Dimensionless"), //
            Map.entry("nitrogen_monoxide", "Dimensionless"), //
            Map.entry("nitrous_oxide", "Dimensionless"), //
            Map.entry("ozone", "Dimensionless"), //
            Map.entry("pm1", "Dimensionless"), //
            Map.entry("pm10", "Dimensionless"), //
            Map.entry("pm25", "Dimensionless"), //
            Map.entry("ph", "Dimensionless"), //
            Map.entry("power_factor", "Dimensionless"), //
            Map.entry("signal_strength", "Dimensionless"), //
            Map.entry("sulphur_dioxide", "Dimensionless"), //
            Map.entry("volatile_organic_compounds", "Dimensionless"), //
            Map.entry("volatile_organic_compounds_parts", "Dimensionless"), //
            Map.entry("moisture", "Dimensionless"), //
            Map.entry("blood_glucose_concentration", "Dimensionless"), //

            Map.entry("distance", "Length"), //
            Map.entry("precipitation", "Length"), //

            Map.entry("speed", "Speed"), //
            Map.entry("wind_speed", "Speed"), //
            Map.entry("precipitation_intensity", "Speed"), //

            Map.entry("illuminance", "Illuminance"), //
            Map.entry("irradiance", "Intensity"), //
            Map.entry("duration", "Time"), //

            Map.entry("volume", "Volume"), //
            Map.entry("volume_storage", "Volume"), //
            Map.entry("gas", "Volume"), //
            Map.entry("water", "Volume"), //

            Map.entry("volume_flow_rate", "VolumetricFlowRate"), //
            Map.entry("weight", "Mass"), //
            Map.entry("data_rate", "DataTransferRate"), //
            Map.entry("data_size", "DataAmount"), //
            Map.entry("monetary", "Currency"), //
            Map.entry("sound_pressure", "SoundPressure"), //
            Map.entry("angle", "Angle"), //
            Map.entry("frequency", "Frequency"));

    private static final Set<String> TIMESTAMP_ATTRIBUTES = Set.of("last_triggered", "last_changed", "last_updated",
            "next_dawn", "next_dusk", "next_midnight", "next_noon", "next_rising", "next_setting", "sunrise", "sunset");

    private static final Pattern TIMESTAMP_SUFFIX_PATTERN = Pattern.compile(".*(_at|_time|_timestamp|_date)$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern BOOLEAN_PATTERN = Pattern
            .compile("^(is_|has_|can_).+|.+(_enabled|_active|_on|_supported|_available)$", Pattern.CASE_INSENSITIVE);

    private static final Set<String> NUMERIC_ATTRIBUTES = Set.of("position", "latitude", "longitude", "altitude",
            "rssi", "linkquality", "brightness");

    private static final Pattern NUMERIC_SUFFIX_PATTERN = Pattern
            .compile(".*(_count|_amount|_total|_level|_ratio|_index|_value|_factor|_rate)$", Pattern.CASE_INSENSITIVE);

    private TypeInferrer() {
        // Utility class
    }

    /**
     * Master entry point: Infers the openHAB {@link ItemType} for any channel target (primary or secondary attribute).
     *
     * @param entityState the Home Assistant entity state
     * @param attributeName attribute key or primary attribute identifier
     * @return the inferred openHAB ItemType
     */
    public static ItemType inferItemType(EntityState entityState, String attributeName) {
        if (EntityType.isPrimary(attributeName)) {
            return inferPrimaryItemType(entityState, null);
        }
        return inferAttributeItemType(entityState, attributeName);
    }

    /**
     * Infers the openHAB {@link ItemType} for a secondary entity attribute using naming heuristics,
     * dimension resolution, and JSON primitive inspection.
     *
     * @param entityState the Home Assistant entity state
     * @param attributeName secondary attribute key
     * @return the inferred openHAB ItemType
     */
    public static ItemType inferAttributeItemType(EntityState entityState, String attributeName) {
        if (isDateTime(null, attributeName)) {
            return ItemType.DATETIME;
        }

        String cleanName = attributeName.toLowerCase();

        if (BOOLEAN_PATTERN.matcher(cleanName).matches()) {
            return ItemType.SWITCH;
        }

        String dimension = resolveDimension(null, null, attributeName);
        if (dimension != null) {
            return ItemType.number(dimension);
        }
        if (NUMERIC_ATTRIBUTES.contains(cleanName) || NUMERIC_SUFFIX_PATTERN.matcher(cleanName).matches()) {
            return ItemType.NUMBER;
        }

        JsonElement element = entityState.getAttribute(attributeName);
        if (element != null && element.isJsonPrimitive()) {
            var primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return ItemType.NUMBER;
            } else if (primitive.isBoolean()) {
                return ItemType.SWITCH;
            }
        }

        return ItemType.STRING;
    }

    /**
     * Infers the openHAB {@link ItemType} for a primary entity state using domain rules, device class, and resolved
     * units.
     *
     * @param entityState the entity state context
     * @param resolvedUnit optional pre-resolved unit string (e.g. from {@code EntityUnitResolver})
     * @return the inferred openHAB ItemType
     */
    public static ItemType inferPrimaryItemType(EntityState entityState, @Nullable String resolvedUnit) {
        String domain = entityState.getDomain();
        String deviceClass = entityState.getAttributeAsString("device_class");

        if (isDateTime(deviceClass, null)) {
            return ItemType.DATETIME;
        }

        if ("binary_sensor".equalsIgnoreCase(domain) || "switch".equalsIgnoreCase(domain)) {
            return isContactDeviceClass(deviceClass) ? ItemType.CONTACT : ItemType.SWITCH;
        }

        String unit = (resolvedUnit != null && !resolvedUnit.isBlank()) ? resolvedUnit
                : entityState.getUnitOfMeasurement();
        if (unit != null && unit.isBlank()) {
            unit = null;
        }

        String stateClass = entityState.getAttributeAsString("state_class");
        boolean hasStateClass = stateClass != null && !stateClass.isBlank();
        boolean hasNumericControlAttrs = entityState.getAttribute("min") != null
                || entityState.getAttribute("step") != null;

        boolean isNumericDomain = "number".equalsIgnoreCase(domain) || "input_number".equalsIgnoreCase(domain)
                || "counter".equalsIgnoreCase(domain);

        boolean isNumeric = entityState.getStateAsBigDecimal() != null || unit != null || isNumericDomain
                || hasStateClass || hasNumericControlAttrs || isNumericDeviceClass(deviceClass);

        if (isNumeric) {
            String dimension = resolveDimension(unit, deviceClass, null);
            return dimension != null ? ItemType.number(dimension) : ItemType.NUMBER;
        }

        String rawState = entityState.state();
        if (!rawState.isBlank()) {
            if ("open".equalsIgnoreCase(rawState) || "closed".equalsIgnoreCase(rawState)) {
                return ItemType.CONTACT;
            }
            if ("on".equalsIgnoreCase(rawState) || "off".equalsIgnoreCase(rawState)) {
                return isContactDeviceClass(deviceClass) ? ItemType.CONTACT : ItemType.SWITCH;
            }
        }

        return ItemType.STRING;
    }

    public static ItemType inferPrimaryItemType(EntityState entityState) {
        return inferPrimaryItemType(entityState, null);
    }

    // ==========================================
    // Shared Resolution Helpers
    // ==========================================

    /**
     * Checks if a device class or attribute name indicates a date/time value.
     *
     * @param deviceClass optional Home Assistant device class
     * @param attributeName optional attribute or state identifier
     * @return true if the inputs indicate a DateTime value
     */
    public static boolean isDateTime(@Nullable String deviceClass, @Nullable String attributeName) {
        if (deviceClass != null && !deviceClass.isBlank()) {
            String dcLower = deviceClass.toLowerCase();
            if ("date".equals(dcLower) || "timestamp".equals(dcLower)) {
                return true;
            }
        }
        return attributeName != null && isDateTimeAttribute(attributeName);
    }

    public static boolean isDateTimeAttribute(String attributeName) {
        if (attributeName.isBlank()) {
            return false;
        }
        String cleanName = attributeName.toLowerCase();
        return TIMESTAMP_ATTRIBUTES.contains(cleanName) || TIMESTAMP_SUFFIX_PATTERN.matcher(cleanName).matches();
    }

    /**
     * Resolves the openHAB dimension name by evaluating parsed units, device classes, or attribute naming patterns.
     *
     * @param unit optional pre-resolved unit or unit of measurement
     * @param deviceClass optional Home Assistant device class
     * @param attributeName optional secondary attribute key
     * @return openHAB dimension name or null if non-dimensional
     */
    public static @Nullable String resolveDimension(@Nullable String unit, @Nullable String deviceClass,
            @Nullable String attributeName) {
        if (unit != null && !unit.isBlank()) {
            Unit<?> parsedUnit = UnitUtils.parseUnit(unit);
            if (parsedUnit != null) {
                String unitDimension = UnitUtils.getDimensionName(parsedUnit);
                if (unitDimension != null && !unitDimension.isBlank()) {
                    return unitDimension;
                }
            }
        }

        String dcDimension = getDimensionFromDeviceClass(deviceClass);
        if (dcDimension != null) {
            return dcDimension;
        }

        return getDimensionFromAttributeName(attributeName);
    }

    public static boolean isContactDeviceClass(@Nullable String deviceClass) {
        if (deviceClass == null || deviceClass.isBlank()) {
            return false;
        }
        return CONTACT_DEVICE_CLASSES.contains(deviceClass.toLowerCase());
    }

    public static boolean isNumericDeviceClass(@Nullable String deviceClass) {
        return getDimensionFromDeviceClass(deviceClass) != null;
    }

    /**
     * Maps Home Assistant sensor device classes to standard openHAB UoM dimensions
     * defined in javax.measure.quantity or org.openhab.core.library.dimension.
     *
     * @param deviceClass Home Assistant device class key
     * @return openHAB UoM dimension string or null if unmapped
     */
    public static @Nullable String getDimensionFromDeviceClass(@Nullable String deviceClass) {
        if (deviceClass == null || deviceClass.isBlank()) {
            return null;
        }
        return DEVICE_CLASS_DIMENSIONS.get(deviceClass.toLowerCase());
    }

    /**
     * Extracts and normalizes suffix tokens from secondary attribute names to perform
     * dimensional lookup against official device classes.
     *
     * @param attributeName secondary attribute key
     * @return openHAB UoM dimension string or null if unmapped
     */
    public static @Nullable String getDimensionFromAttributeName(@Nullable String attributeName) {
        if (attributeName == null || attributeName.isBlank()) {
            return null;
        }

        String cleanName = attributeName.toLowerCase();
        int lastUnderscore = cleanName.lastIndexOf('_');
        String suffix = (lastUnderscore != -1) ? cleanName.substring(lastUnderscore + 1) : cleanName;

        String normalizedDeviceClass = switch (suffix) {
            case "temp" -> "temperature";
            case "moisture" -> "humidity";
            case "velocity" -> "speed";
            case "length", "width", "height", "depth", "radius", "altitude" -> "distance";
            case "mass" -> "weight";
            case "azimuth", "elevation", "latitude", "longitude" -> "angle";
            case "position", "brightness", "rssi", "percent", "percentage" -> "battery";
            default -> suffix;
        };

        return getDimensionFromDeviceClass(normalizedDeviceClass);
    }
}
