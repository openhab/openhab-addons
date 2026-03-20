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
package org.openhab.persistence.timescaledb.internal;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.CallItem;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.ImageItem;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.RollershutterItem;
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
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts between openHAB {@link State} objects and the three-column schema
 * ({@code value DOUBLE PRECISION}, {@code string TEXT}, {@code unit TEXT}).
 *
 * <p>
 * Mapping rules (store direction):
 * <ul>
 * <li>{@link DecimalType} → value, unit=null, string=null</li>
 * <li>{@link QuantityType} → value=numeric, unit=unit-string, string=null</li>
 * <li>{@link OnOffType} → value (ON=1.0, OFF=0.0)</li>
 * <li>{@link OpenClosedType} → value (OPEN=1.0, CLOSED=0.0)</li>
 * <li>{@link PercentType} → value (0.0–100.0)</li>
 * <li>{@link UpDownType} → value (UP=0.0, DOWN=1.0)</li>
 * <li>{@link HSBType} → string="H,S,B"</li>
 * <li>{@link DateTimeType} → string=ISO-8601</li>
 * <li>{@link PointType} → string="lat,lon[,alt]"</li>
 * <li>{@link PlayPauseType} → string=enum name</li>
 * <li>{@link StringListType} → string=comma-separated values</li>
 * <li>{@link RawType} → string=Base64-encoded bytes, unit=MIME type</li>
 * <li>{@link StringType} → string=raw value</li>
 * </ul>
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault
public class TimescaleDBMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimescaleDBMapper.class);

    private TimescaleDBMapper() {
        // utility class
    }

    /**
     * SQL row representation of a persisted state.
     *
     * @param value Numeric value, or null for string-only states.
     * @param string String value, or null for numeric states.
     * @param unit Unit string for {@link QuantityType}, null otherwise.
     */
    public record Row(@Nullable Double value, @Nullable String string, @Nullable String unit) {
    }

    /**
     * Converts an openHAB {@link State} to a SQL {@link Row}.
     *
     * @param state The state to convert.
     * @return The row to be stored, or null if the state type is unsupported.
     */
    public static @Nullable Row toRow(State state) {
        if (state instanceof QuantityType<?> qty) {
            return new Row(qty.doubleValue(), null, qty.getUnit().toString());
        } else if (state instanceof HSBType hsb) {
            // HSBType extends PercentType extends DecimalType — must be checked before both
            return new Row(null, hsb.toString(), null);
        } else if (state instanceof OnOffType onOff) {
            return new Row(onOff == OnOffType.ON ? 1.0 : 0.0, null, null);
        } else if (state instanceof OpenClosedType openClosed) {
            return new Row(openClosed == OpenClosedType.OPEN ? 1.0 : 0.0, null, null);
        } else if (state instanceof PercentType pct) {
            return new Row(pct.doubleValue(), null, null);
        } else if (state instanceof DecimalType dec) {
            return new Row(dec.doubleValue(), null, null);
        } else if (state instanceof UpDownType upDown) {
            return new Row(upDown == UpDownType.UP ? 0.0 : 1.0, null, null);
        } else if (state instanceof DateTimeType dt) {
            return new Row(null, dt.getZonedDateTime(ZoneId.systemDefault()).toString(), null);
        } else if (state instanceof PointType point) {
            return new Row(null, point.toString(), null);
        } else if (state instanceof PlayPauseType playPause) {
            return new Row(null, playPause.toString(), null);
        } else if (state instanceof StringListType stringList) {
            return new Row(null, stringList.toString(), null);
        } else if (state instanceof RawType raw) {
            return new Row(null, Base64.getEncoder().encodeToString(raw.getBytes()), raw.getMimeType());
        } else if (state instanceof StringType str) {
            return new Row(null, str.toString(), null);
        } else {
            LOGGER.warn("Unsupported state type for TimescaleDB persistence: {}", state.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Reconstructs an openHAB {@link State} from a SQL row, using the item type for disambiguation.
     *
     * @param item The openHAB item (used for type-based disambiguation of numeric states).
     * @param value The {@code value} column, may be null.
     * @param string The {@code string} column, may be null.
     * @param unit The {@code unit} column, may be null.
     * @return The reconstructed state, or {@link UnDefType#UNDEF} if reconstruction fails.
     */
    public static State toState(Item item, @Nullable Double value, @Nullable String string, @Nullable String unit) {
        // Unwrap GroupItem to its base item for type dispatch
        Item realItem = item;
        if (item instanceof GroupItem groupItem) {
            Item baseItem = groupItem.getBaseItem();
            if (baseItem != null) {
                realItem = baseItem;
            }
        }

        // QuantityType: unit column present together with a numeric value
        if (unit != null && value != null) {
            try {
                return new QuantityType<>(value + " " + unit);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Failed to parse QuantityType for item '{}' with value={} unit={}: {}", item.getName(),
                        value, unit, e.getMessage());
                return UnDefType.UNDEF;
            }
        }

        // String-based states
        if (string != null) {
            if (realItem instanceof ColorItem) {
                try {
                    return new HSBType(string);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Failed to parse HSBType for item '{}': {}", item.getName(), e.getMessage());
                    return UnDefType.UNDEF;
                }
            }
            if (realItem instanceof DateTimeItem) {
                try {
                    return new DateTimeType(ZonedDateTime.parse(string));
                } catch (Exception e) {
                    LOGGER.warn("Failed to parse DateTimeType for item '{}': {}", item.getName(), e.getMessage());
                    return UnDefType.UNDEF;
                }
            }
            if (realItem instanceof LocationItem) {
                try {
                    return new PointType(string);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Failed to parse PointType for item '{}': {}", item.getName(), e.getMessage());
                    return UnDefType.UNDEF;
                }
            }
            if (realItem instanceof PlayerItem) {
                try {
                    return PlayPauseType.valueOf(string);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Failed to parse PlayPauseType for item '{}': {}", item.getName(), e.getMessage());
                    return UnDefType.UNDEF;
                }
            }
            if (realItem instanceof CallItem) {
                return new StringListType(string);
            }
            if (realItem instanceof ImageItem) {
                try {
                    byte[] bytes = Base64.getDecoder().decode(string);
                    String mimeType = unit != null ? unit : "application/octet-stream";
                    return new RawType(bytes, mimeType);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Failed to decode RawType for item '{}': {}", item.getName(), e.getMessage());
                    return UnDefType.UNDEF;
                }
            }
            // StringItem, GenericItem, and anything else with a string value
            return new StringType(string);
        }

        // Numeric states without unit
        if (value != null) {
            if (realItem instanceof DimmerItem || realItem instanceof RollershutterItem) {
                // DimmerItem extends SwitchItem — must be checked before SwitchItem
                return new PercentType((int) Math.round(value));
            }
            if (realItem instanceof SwitchItem) {
                return value >= 0.5 ? OnOffType.ON : OnOffType.OFF;
            }
            if (realItem instanceof ContactItem) {
                return value >= 0.5 ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            }
            // NumberItem, GenericItem, UpDownType stored as decimal, etc.
            return new DecimalType(value);
        }

        LOGGER.warn("Cannot reconstruct state for item '{}': value=null, string=null, unit=null", item.getName());
        return UnDefType.UNDEF;
    }

    /**
     * Converts a SQL operator from {@link org.openhab.core.persistence.FilterCriteria.Operator}
     * to a JDBC-compatible SQL operator string.
     *
     * @param operator The filter operator.
     * @return The SQL operator string, or null if not supported.
     */
    public static @Nullable String toSqlOperator(org.openhab.core.persistence.FilterCriteria.Operator operator) {
        return switch (operator) {
            case EQ -> "=";
            case NEQ -> "<>";
            case LT -> "<";
            case LTE -> "<=";
            case GT -> ">";
            case GTE -> ">=";
            default -> null;
        };
    }
}
