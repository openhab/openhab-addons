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
package org.openhab.persistence.victoriametrics.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conversion logic between openHAB {@link State} types and VictoriaMetrics sample values.
 * <p>
 * VictoriaMetrics samples are 64-bit floating point numbers, so only states with a numeric representation can be
 * stored. Player states are stored as numbers ({@code PAUSE}=0, {@code PLAY}=1, {@code REWIND}=2,
 * {@code FASTFORWARD}=3). {@link HSBType} and {@link PointType} are stored as one sample per component, see
 * {@link #stateToComponents(State)}. {@link StringType} is only stored for items that opted in, as a label, see
 * {@link VictoriaMetricsPoint}. Unlike InfluxDB, other states that InfluxDB stores as strings (e.g.
 * {@code RawType}, {@code StringListType}) are not supported.
 *
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 * @author Michael Weger - Restrict to numeric states, store player, color and location states as numbers,
 *         restore string states
 */
@NonNullByDefault
public class VictoriaMetricsStateConvertUtils {
    static final Number DIGITAL_VALUE_OFF = 0; // Visible for testing
    static final Number DIGITAL_VALUE_ON = 1; // Visible for testing
    static final Number PLAYER_VALUE_PAUSE = 0; // Visible for testing
    static final Number PLAYER_VALUE_PLAY = 1; // Visible for testing
    static final Number PLAYER_VALUE_REWIND = 2; // Visible for testing
    static final Number PLAYER_VALUE_FASTFORWARD = 3; // Visible for testing
    public static final String COMPONENT_HUE = "hue";
    public static final String COMPONENT_SATURATION = "saturation";
    public static final String COMPONENT_BRIGHTNESS = "brightness";
    public static final String COMPONENT_LATITUDE = "latitude";
    public static final String COMPONENT_LONGITUDE = "longitude";
    public static final String COMPONENT_ALTITUDE = "altitude";
    private static final Logger LOGGER = LoggerFactory.getLogger(VictoriaMetricsStateConvertUtils.class);

    /**
     * Converts {@link State} to a number fitting into a VictoriaMetrics sample.
     *
     * @param state to be converted
     * @return decimal value for DecimalType and QuantityType, 0 or 1 for OnOffType and OpenClosedType,
     *         epoch milliseconds for DateTimeType, 0 to 3 for PlayPauseType and RewindFastforwardType,
     *         <code>null</code> for all others
     */
    public static @Nullable Number stateToObject(State state) {
        Number value;
        if (state instanceof HSBType) { // HSBType is a DecimalType, but has no single numeric value
            value = null;
        } else if (state instanceof DecimalType type) {
            value = type.toBigDecimal();
        } else if (state instanceof QuantityType<?> type) {
            value = type.toBigDecimal();
        } else if (state instanceof OnOffType) {
            value = state == OnOffType.ON ? DIGITAL_VALUE_ON : DIGITAL_VALUE_OFF;
        } else if (state instanceof OpenClosedType) {
            value = state == OpenClosedType.OPEN ? DIGITAL_VALUE_ON : DIGITAL_VALUE_OFF;
        } else if (state instanceof DateTimeType type) {
            value = type.getInstant().toEpochMilli();
        } else if (state instanceof PlayPauseType) {
            value = state == PlayPauseType.PLAY ? PLAYER_VALUE_PLAY : PLAYER_VALUE_PAUSE;
        } else if (state instanceof RewindFastforwardType) {
            value = state == RewindFastforwardType.REWIND ? PLAYER_VALUE_REWIND : PLAYER_VALUE_FASTFORWARD;
        } else {
            value = null;
        }
        return value;
    }

    /**
     * Converts a {@link State} consisting of several numbers to its components, each stored as a separate sample.
     *
     * @param state to be converted
     * @return hue, saturation and brightness for HSBType, latitude, longitude and altitude for PointType,
     *         <code>null</code> for all others
     */
    public static @Nullable Map<String, BigDecimal> stateToComponents(State state) {
        Map<String, BigDecimal> components = new LinkedHashMap<>();
        if (state instanceof HSBType hsb) {
            components.put(COMPONENT_HUE, hsb.getHue().toBigDecimal());
            components.put(COMPONENT_SATURATION, hsb.getSaturation().toBigDecimal());
            components.put(COMPONENT_BRIGHTNESS, hsb.getBrightness().toBigDecimal());
        } else if (state instanceof PointType point) {
            components.put(COMPONENT_LATITUDE, point.getLatitude().toBigDecimal());
            components.put(COMPONENT_LONGITUDE, point.getLongitude().toBigDecimal());
            components.put(COMPONENT_ALTITUDE, point.getAltitude().toBigDecimal());
        } else {
            return null;
        }
        return components;
    }

    /**
     * Converts a value to a {@link State} which is suitable for the given {@link Item}. This is
     * needed for querying a {@link VictoriaMetricsHistoricItem}.
     *
     * @param value to be converted to a {@link State}
     * @param itemName name of the {@link Item} to get the {@link State} for
     * @return the state of the item represented by the itemName parameter, else the string value of
     *         the Object parameter
     */
    public static State objectToState(Object value, String itemName, ItemRegistry itemRegistry) {
        try {
            Item item = itemRegistry.getItem(itemName);
            return objectToState(value, item);
        } catch (ItemNotFoundException e) {
            LOGGER.info("Could not find item '{}' in registry", itemName);
        }

        return new StringType(String.valueOf(value));
    }

    /**
     * Converts an exported value of a string state series to the string state
     *
     * @param value the exported value
     * @return the label value, the empty string for a sample without the label (see {@link VictoriaMetricsPoint}), or
     *         <code>null</code> if the value is not a string state
     */
    public static @Nullable String toStringState(@Nullable Object value) {
        if (value instanceof String string) {
            return string;
        } else if (value instanceof BigDecimal number
                && number.compareTo(new BigDecimal(VictoriaMetricsPoint.STRING_STATE_VALUE.toString())) == 0) {
            return "";
        }
        return null;
    }

    public static State objectToState(@Nullable Object value, Item itemToSetState) {
        String valueStr = String.valueOf(value);

        @Nullable
        Item item = itemToSetState;
        if (item instanceof GroupItem groupItem) {
            item = groupItem.getBaseItem();
        }
        if (item instanceof ColorItem) { // before DimmerItem, which ColorItem extends
            return value instanceof Map<?, ?> components ? toHSBType(components) : UnDefType.UNDEF;
        } else if (item instanceof LocationItem) {
            return value instanceof Map<?, ?> components ? toPointType(components) : UnDefType.UNDEF;
        } else if (item instanceof NumberItem numberItem) {
            Unit<?> unit = numberItem.getUnit();
            if (unit == null) {
                return new DecimalType(valueStr);
            } else {
                return new QuantityType<>(new BigDecimal(valueStr), unit);
            }
        } else if (item instanceof DimmerItem) {
            return new PercentType(valueStr);
        } else if (item instanceof SwitchItem) {
            return OnOffType.from(toBoolean(valueStr));
        } else if (item instanceof ContactItem) {
            return toBoolean(valueStr) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        } else if (item instanceof RollershutterItem) {
            return new PercentType(valueStr);
        } else if (item instanceof DateTimeItem) {
            return new DateTimeType(Instant.ofEpochMilli(new BigDecimal(valueStr).longValue()));
        } else if (item instanceof PlayerItem) {
            return switch (new BigDecimal(valueStr).intValue()) {
                case 0 -> PlayPauseType.PAUSE;
                case 1 -> PlayPauseType.PLAY;
                case 2 -> RewindFastforwardType.REWIND;
                case 3 -> RewindFastforwardType.FASTFORWARD;
                default -> UnDefType.UNDEF;
            };
        } else if (item instanceof StringItem) {
            String stringState = toStringState(value);
            return stringState != null ? new StringType(stringState) : UnDefType.UNDEF;
        } else if (item == null) {
            return new StringType(valueStr);
        }
        return UnDefType.UNDEF;
    }

    private static State toHSBType(Map<?, ?> components) {
        if (components.get(COMPONENT_HUE) instanceof BigDecimal hue
                && components.get(COMPONENT_SATURATION) instanceof BigDecimal saturation
                && components.get(COMPONENT_BRIGHTNESS) instanceof BigDecimal brightness) {
            return new HSBType(new DecimalType(hue), new PercentType(saturation), new PercentType(brightness));
        }
        return UnDefType.UNDEF;
    }

    private static State toPointType(Map<?, ?> components) {
        if (components.get(COMPONENT_LATITUDE) instanceof BigDecimal latitude
                && components.get(COMPONENT_LONGITUDE) instanceof BigDecimal longitude) {
            Object altitude = components.get(COMPONENT_ALTITUDE);
            return new PointType(new DecimalType(latitude), new DecimalType(longitude),
                    new DecimalType(altitude instanceof BigDecimal decimal ? decimal : BigDecimal.ZERO));
        }
        return UnDefType.UNDEF;
    }

    private static boolean toBoolean(@Nullable Object object) {
        if (object instanceof Boolean boolean1) {
            return boolean1;
        } else if (object != null) {
            if ("1".equals(object) || "1.0".equals(object)) {
                return true;
            } else {
                return Boolean.parseBoolean(String.valueOf(object));
            }
        } else {
            return false;
        }
    }
}
