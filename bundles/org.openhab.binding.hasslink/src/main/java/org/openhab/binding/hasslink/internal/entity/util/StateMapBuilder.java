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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.util.EntityUtils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;

import com.google.gson.JsonElement;

/**
 * A fluent builder for mapping Home Assistant {@link EntityState} values and attributes
 * into openHAB channel {@link State} instances.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class StateMapBuilder {

    private final Map<String, ParsedData> map = new HashMap<>();
    private final EntityState entityState;
    private final EntityContext context;

    private StateMapBuilder(EntityState entityState, EntityContext context) {
        this.entityState = entityState;
        this.context = context;
    }

    /**
     * Creates a new instance of {@link StateMapBuilder} bound to the specified {@link EntityState} and
     * {@link EntityContext}.
     *
     * @param entityState the Home Assistant entity state to parse
     * @param context execution context supplying optional bridge handler and channel link predicate
     * @return a new builder instance
     */
    public static StateMapBuilder create(EntityState entityState, EntityContext context) {
        return new StateMapBuilder(entityState, context);
    }

    /**
     * Evaluates whether the primary state string is valid and available.
     * <p>
     * If the entity state is unavailable/unknown, or if mapping fails,
     * it stores {@link UnDefType#UNDEF} under the primary attribute key.
     *
     * @param consumer function to produce a valid openHAB State from the raw primary state string
     * @return this builder instance for fluent chaining
     */
    private StateMapBuilder withValidPrimaryState(Function<String, @Nullable State> consumer) {
        if (entityState.isUnavailableOrUnknown()) {
            return putPrimaryState(UnDefType.UNDEF);
        }
        State mapped = consumer.apply(entityState.state());
        return putPrimaryState(mapped);
    }

    /**
     * Stores an openHAB {@link State} under the primary attribute key.
     * <p>
     * Skips adding to the map if the provided state is {@code null} or if the primary channel is not linked.
     *
     * @param state openHAB state to store under primary attribute
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putPrimaryState(@Nullable State state) {
        if (state != null && context.isLinked(EntityType.PRIMARY_ATTR)) {
            map.put(EntityType.PRIMARY_ATTR, ParsedData.of(state));
        }
        return this;
    }

    /**
     * Parses the primary state string as a {@link StringType} under the primary attribute key.
     *
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putPrimaryStringState() {
        return withValidPrimaryState(StringType::new);
    }

    /**
     * Parses the primary state string as a {@link DateTimeType} under the primary attribute key.
     *
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putPrimaryDateTimeState() {
        return withValidPrimaryState(rawState -> {
            try {
                return DateTimeType.valueOf(rawState);
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    /**
     * Parses the primary state string as an {@link OnOffType} under the primary attribute key.
     *
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putPrimaryOnOffState() {
        return withValidPrimaryState(rawState -> "on".equalsIgnoreCase(rawState) ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * Parses the primary state string as an {@link OpenClosedType} under the primary attribute key.
     *
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putPrimaryOpenClosedState() {
        return withValidPrimaryState(rawState -> {
            boolean open = "on".equalsIgnoreCase(rawState) || "open".equalsIgnoreCase(rawState);
            return open ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        });
    }

    /**
     * Parses the primary numeric state as
     * a {@link QuantityType} (if unit of measurement or device class config is present)
     * or {@link DecimalType} under the primary attribute key.
     *
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putPrimaryNumeric() {
        return withValidPrimaryState(strVal -> {
            BigDecimal val = entityState.getStateAsBigDecimal();
            if (val != null) {
                String unit = entityState.getUnitOfMeasurement();
                String deviceClass = entityState.getAttributeAsString("device_class");
                if (unit == null || unit.isBlank()) {
                    unit = EntityUnitResolver.resolveDeviceClassUnit(entityState, context.getHaConfig(), deviceClass);
                }

                if (unit != null && !unit.isBlank() && UnitUtils.parseUnit(unit) != null) {
                    try {
                        return QuantityType.valueOf(strVal + " " + unit);
                    } catch (IllegalArgumentException e) {
                        return new DecimalType(val);
                    }
                } else {
                    return new DecimalType(val);
                }
            }
            return null;
        });
    }

    /**
     * Infers the openHAB state type for the primary entity state based on state value,
     * domain, and device class.
     *
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putPrimaryInferredState() {
        putPrimaryState(inferPrimaryState());
        return this;
    }

    private @Nullable State inferPrimaryState() {
        if (entityState.isUnavailableOrUnknown()) {
            return UnDefType.UNDEF;
        }

        String rawState = entityState.state();
        String domain = entityState.getDomain();
        String deviceClass = entityState.getAttributeAsString("device_class");

        // Temporal Check
        if (deviceClass != null && !deviceClass.isBlank()) {
            String dcLower = deviceClass.toLowerCase();
            if ("date".equals(dcLower) || "timestamp".equals(dcLower)) {
                try {
                    return DateTimeType.valueOf(rawState);
                } catch (IllegalArgumentException e) {
                    return UnDefType.UNDEF;
                }
            }
        }

        // Handle Binary / Switch States
        if ("binary_sensor".equalsIgnoreCase(domain) || "switch".equalsIgnoreCase(domain) //
                || "on".equalsIgnoreCase(rawState) || "off".equalsIgnoreCase(rawState) //
                || "open".equalsIgnoreCase(rawState) || "closed".equalsIgnoreCase(rawState)) {

            boolean isContact = TypeInferrer.isContactDeviceClass(deviceClass) //
                    || "open".equalsIgnoreCase(rawState) //
                    || "closed".equalsIgnoreCase(rawState);

            boolean isOn = "on".equalsIgnoreCase(rawState) || "open".equalsIgnoreCase(rawState);

            if (isContact) {
                return isOn ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            } else {
                return isOn ? OnOffType.ON : OnOffType.OFF;
            }
        }

        // Numeric Check
        BigDecimal bdState = entityState.getStateAsBigDecimal();
        if (bdState != null) {
            String unit = entityState.getUnitOfMeasurement();
            if (unit == null || unit.isBlank()) {
                unit = EntityUnitResolver.resolveDeviceClassUnit(entityState, context.getHaConfig(), deviceClass);
            }

            if (unit != null && !unit.isBlank()) {
                try {
                    return QuantityType.valueOf(rawState + " " + unit);
                } catch (IllegalArgumentException e) {
                    return new DecimalType(bdState);
                }
            }
            return new DecimalType(bdState);
        }

        return new StringType(rawState);
    }

    /**
     * Extracts and maps a secondary entity attribute.
     * <p>
     * Skips parsing if the attribute channel is not currently linked in openHAB.
     * Skips adding to the map if the attribute is missing/null in the JSON payload.
     * Maps to {@link UnDefType#UNDEF} if the attribute exists but is set to "unavailable" or "unknown".
     *
     * @param attribute target attribute name
     * @param mapper conversion function from raw attribute value to openHAB State
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putAttribute(String attribute, Function<JsonElement, @Nullable State> mapper) {
        if (!context.isLinked(attribute)) {
            return this;
        }

        JsonElement element = entityState.getAttribute(attribute);
        if (element == null || element.isJsonNull()) {
            return this;
        }

        if (entityState.isAttributeUnavailableOrUnknown(attribute)) {
            map.put(attribute, ParsedData.of(UnDefType.UNDEF));
            return this;
        }

        State mapped = mapper.apply(element);
        if (mapped != null) {
            map.put(attribute, ParsedData.of(mapped));
        }
        return this;
    }

    /**
     * Directly maps an openHAB {@link State} to an attribute key.
     * <p>
     * Skips adding to the map if state is {@code null} or attribute is not linked.
     *
     * @param attribute target attribute key
     * @param state openHAB {@link State} to assign
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder put(String attribute, @Nullable State state) {
        if (state != null && context.isLinked(attribute)) {
            map.put(attribute, ParsedData.of(state));
        }
        return this;
    }

    /**
     * Extracts a string attribute and stores it as a {@link StringType}.
     *
     * @param attribute target attribute name
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putString(String attribute) {
        return putAttribute(attribute, element -> {
            String str = element.getAsString();
            return !str.isBlank() ? new StringType(str) : null;
        });
    }

    /**
     * Directly stores a string value as a {@link StringType} if non-null and non-blank.
     *
     * @param attribute target attribute name
     * @param value string value to store
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putStringValue(String attribute, @Nullable String value) {
        if (value != null && !value.isBlank() && context.isLinked(attribute)) {
            map.put(attribute, ParsedData.of(new StringType(value)));
        }
        return this;
    }

    /**
     * Extracts a numeric attribute and stores it as a {@link QuantityType} with the specified unit.
     *
     * @param attribute target attribute name
     * @param unit target unit of measurement
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putQuantity(String attribute, Unit<?> unit) {
        return putAttribute(attribute, element -> {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                return new QuantityType<>(element.getAsBigDecimal(), unit);
            }
            return null;
        });
    }

    /**
     * Extracts a numeric attribute and stores it as a {@link QuantityType} with the specified unit string.
     *
     * @param attribute target attribute name
     * @param unit target unit string (e.g., "°C", "kWh", "%")
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putQuantity(String attribute, @Nullable String unit) {
        if (UnitUtils.parseUnit(unit) == null) {
            return this;
        }
        return putAttribute(attribute, val -> {
            JsonElement element = entityState.getAttribute(attribute);
            if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                return new QuantityType<>(element.getAsString() + " " + unit);
            }
            return null;
        });
    }

    /**
     * Extracts a numeric attribute and stores it as a {@link QuantityType} using the unit string
     * found in another attribute key.
     *
     * @param attribute target attribute name
     * @param unitAttribute attribute name containing the unit string
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putQuantityWithUnitAttr(String attribute, String unitAttribute) {
        String unit = entityState.getAttributeAsString(unitAttribute);
        return putQuantity(attribute, unit);
    }

    /**
     * Extracts a numeric attribute and stores it as a {@link DecimalType}.
     *
     * @param attribute target attribute name
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putDecimal(String attribute) {
        return putAttribute(attribute, element -> {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                return new DecimalType(element.getAsBigDecimal());
            }
            return null;
        });
    }

    /**
     * Directly stores a numeric value as a {@link DecimalType} if non-null.
     *
     * @param attribute target attribute name
     * @param value numeric value to store
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putDecimalValue(String attribute, @Nullable Number value) {
        if (value != null && context.isLinked(attribute)) {
            map.put(attribute, ParsedData.of(new DecimalType(value.doubleValue())));
        }
        return this;
    }

    /**
     * Extracts a boolean attribute and stores it as an {@link OnOffType}.
     *
     * @param attribute target attribute name
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putOnOff(String attribute) {
        return putAttribute(attribute, element -> {
            if (element.isJsonPrimitive()) {
                String val = element.getAsString().trim();
                if ("on".equalsIgnoreCase(val) || "true".equalsIgnoreCase(val) || "1".equals(val)) {
                    return OnOffType.ON;
                }
                if ("off".equalsIgnoreCase(val) || "false".equalsIgnoreCase(val) || "0".equals(val)) {
                    return OnOffType.OFF;
                }
                if (EntityState.isUnavailableOrUnknown(val)) {
                    return UnDefType.UNDEF;
                }
            }
            return null;
        });
    }

    /**
     * Directly stores a boolean value as an {@link OnOffType} if non-null.
     *
     * @param attribute target attribute name
     * @param value boolean value to store
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putOnOffValue(String attribute, @Nullable Boolean value) {
        if (value != null && context.isLinked(attribute)) {
            map.put(attribute, ParsedData.of(OnOffType.from(value)));
        }
        return this;
    }

    /**
     * Extracts an integer percentage attribute and stores it as a {@link PercentType}.
     *
     * @param attribute target attribute name
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putPercent(String attribute) {
        return putAttribute(attribute, val -> {
            Long l = entityState.getAttributeAsLong(attribute);
            return l != null ? new PercentType(l.intValue()) : null;
        });
    }

    /**
     * Converts a 0–255 byte attribute (e.g., light brightness) to an openHAB {@link PercentType} (0–100%).
     *
     * @param attribute target attribute name
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putBytePercent(String attribute) {
        return putScaledPercent(attribute, 0.0, 255.0);
    }

    /**
     * Inverts a 0–100 percentage attribute (e.g., converting cover position where 100 is fully open
     * to openHAB position where 100 is fully closed).
     *
     * @param attribute target attribute name
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putInvertedPercent(String attribute) {
        return putScaledPercent(attribute, 100.0, 0.0);
    }

    /**
     * Scales a numerical attribute value from range [{@code min}, {@code max}] to an openHAB {@link PercentType}
     * (0–100%).
     *
     * @param attribute target attribute name
     * @param min input value corresponding to 0% output (or 100% if min > max)
     * @param max input value corresponding to 100% output (or 0% if min > max)
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putScaledPercent(String attribute, double min, double max) {
        return putAttribute(attribute, val -> {
            Double value = entityState.getAttributeAsDouble(attribute);
            if (value != null) {
                double lowerBound = Math.min(min, max);
                double upperBound = Math.max(min, max);
                double clamped = Math.max(lowerBound, Math.min(upperBound, value));

                double percent = ((clamped - min) / (max - min)) * 100.0;
                return new PercentType(BigDecimal.valueOf(percent));
            }
            return null;
        });
    }

    /**
     * Extracts latitude, longitude, and optional altitude attributes to construct a combined openHAB {@link PointType}.
     *
     * @param channelKey target location channel key
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putLocation(String channelKey) {
        if (!context.isLinked(channelKey)) {
            return this;
        }
        BigDecimal lat = entityState.getAttributeAsBigDecimal("latitude");
        BigDecimal lon = entityState.getAttributeAsBigDecimal("longitude");
        if (lat != null && lon != null) {
            BigDecimal alt = entityState.getAttributeAsBigDecimal("altitude");
            if (alt != null) {
                put(channelKey, new PointType(new DecimalType(lat), new DecimalType(lon), new DecimalType(alt)));
            } else {
                put(channelKey, new PointType(new DecimalType(lat), new DecimalType(lon)));
            }
        }
        return this;
    }

    /**
     * Maps Home Assistant playback states ("playing" / "paused") to an openHAB {@link PlayPauseType}.
     *
     * @param attribute target attribute key
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putPlayer(String attribute) {
        if (!context.isLinked(attribute)) {
            return this;
        }
        switch (entityState.state()) {
            case "playing" -> put(attribute, PlayPauseType.PLAY);
            case "paused" -> put(attribute, PlayPauseType.PAUSE);
        }
        return this;
    }

    /**
     * Adds a time series to the state map.
     *
     * @param attribute target attribute key
     * @param timeSeries the time series data
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putTimeSeries(String attribute, @Nullable TimeSeries timeSeries) {
        if (timeSeries != null && context.isLinked(attribute)) {
            map.put(attribute, ParsedData.of(timeSeries));
        }
        return this;
    }

    /**
     * Automatically extracts and converts all remaining custom JSON attributes from {@link EntityState}
     * into openHAB {@link State} objects if they are currently linked and not internal metadata.
     *
     * @return this builder instance for fluent chaining
     */
    public StateMapBuilder putGenericAttributes() {
        if (entityState.attributes().isEmpty()) {
            return this;
        }

        entityState.attributes().keySet().stream() //
                .filter(attr -> !EntityState.isMetadataAttribute(attr)) //
                .filter(EntityUtils::isValidUID) //
                .filter(attr -> !map.containsKey(attr)) //
                .filter(context::isLinked) //
                .forEach(attr -> {
                    State inferredState = parseCustomAttributeState(attr);
                    if (inferredState != null) {
                        put(attr, inferredState);
                    }
                });

        return this;
    }

    private @Nullable State parseCustomAttributeState(String attribute) {
        // 1. Fetch raw string early and handle UNDEF states globally
        String str = entityState.getAttributeAsString(attribute);
        if (str != null && EntityState.isUnavailableOrUnknown(str)) {
            return UnDefType.UNDEF;
        }

        // 2. Parse DateTime attributes
        if (TypeInferrer.isDateTimeAttribute(attribute) && str != null) {
            try {
                return DateTimeType.valueOf(str);
            } catch (IllegalArgumentException e) {
                return new StringType(str);
            }
        }

        // 3. Try primitive numeric & boolean conversions
        Double num = entityState.getAttributeAsDouble(attribute);
        if (num != null) {
            return new DecimalType(num);
        }

        Boolean bool = entityState.getAttributeAsBoolean(attribute);
        if (bool != null) {
            return bool ? OnOffType.ON : OnOffType.OFF;
        }

        // 4. Fallback to raw string
        if (str != null) {
            return new StringType(str);
        }

        // 5. Fallback to raw JSON payload
        String json = entityState.getAttributeAsJson(attribute);
        if (json != null) {
            return new StringType(json);
        }

        return null;
    }

    /**
     * Builds and returns an unmodifiable map of channel key to openHAB state updates.
     *
     * @return unmodifiable map of parsed state data
     */
    public Map<String, ParsedData> build() {
        return Map.copyOf(map);
    }
}
