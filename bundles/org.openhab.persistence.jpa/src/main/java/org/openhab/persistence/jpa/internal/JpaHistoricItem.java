/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.persistence.jpa.internal;

import java.text.DateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.persistence.jpa.internal.model.JpaPersistentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The historic item as returned when querying the service.
 *
 * @author Manfred Bergmann - Initial contribution
 *
 */
@NonNullByDefault
public class JpaHistoricItem implements HistoricItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaHistoricItem.class);

    private final String name;
    private final State state;
    private final Instant instant;

    public JpaHistoricItem(String name, State state, Instant instant) {
        this.name = name;
        this.state = state;
        this.instant = instant;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ZonedDateTime getTimestamp() {
        return instant.atZone(ZoneId.systemDefault());
    }

    @Override
    public Instant getInstant() {
        return instant;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return DateFormat.getDateTimeInstance().format(getTimestamp()) + ": " + name + " -> " + state;
    }

    /**
     * This method maps {@link JpaPersistentItem}s to {@link HistoricItem}s.
     *
     * @param jpaQueryResult the result with jpa items
     * @param item used for query information, like the state (State)
     * @return list of historic items
     */
    public static List<HistoricItem> fromResultList(List<JpaPersistentItem> jpaQueryResult, Item item) {
        return jpaQueryResult.stream().map(pItem -> fromPersistedItem(pItem, item)).filter(Objects::nonNull)
                .map(Objects::requireNonNull).collect(Collectors.toList());
    }

    /**
     * Converts the string value of the persisted item to the state of a {@link HistoricItem}.
     *
     * @param pItem the persisted {@link JpaPersistentItem}
     * @param item the source reference Item
     * @return historic item
     */
    public static @Nullable HistoricItem fromPersistedItem(JpaPersistentItem pItem, Item item) {
        State state;
        if (item instanceof NumberItem numberItem) {
            Unit<?> unit = numberItem.getUnit();
            QuantityType<?> value = QuantityType.valueOf(pItem.getValue());
            if (unit == null) {
                // Item has no unit; drop any persisted unit
                state = Objects.requireNonNull(value.as(DecimalType.class));
            } else if (value.getUnit() == Units.ONE) {
                // No persisted unit; assume the item's unit
                state = new QuantityType<>(value.toBigDecimal(), unit);
            } else {
                // Ensure we return in the item's unit
                state = value.toUnit(unit);
                if (state == null) {
                    LOGGER.warn("Persisted state {} for item {} is incompatible with item's unit {}; ignoring", value,
                            item.getName(), unit);
                    return null;
                }
            }
        } else if (item instanceof DimmerItem) {
            state = new PercentType(Integer.parseInt(pItem.getValue()));
        } else if (item instanceof SwitchItem) {
            state = OnOffType.valueOf(pItem.getValue());
        } else if (item instanceof ContactItem) {
            state = OpenClosedType.valueOf(pItem.getValue());
        } else if (item instanceof RollershutterItem) {
            state = PercentType.valueOf(pItem.getValue());
        } else if (item instanceof DateTimeItem) {
            state = new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(pItem.getValue())),
                    ZoneId.systemDefault()));
        } else if (item instanceof LocationItem) {
            PointType pType = null;
            String[] comps = pItem.getValue().split(";");
            if (comps.length >= 2) {
                pType = new PointType(new DecimalType(comps[0]), new DecimalType(comps[1]));

                if (comps.length == 3) {
                    pType.setAltitude(new DecimalType(comps[2]));
                }
            }
            state = pType == null ? UnDefType.UNDEF : pType;
        } else if (item instanceof StringListType) {
            state = new StringListType(pItem.getValue());
        } else {
            state = new StringType(pItem.getValue());
        }

        return new JpaHistoricItem(item.getName(), state, pItem.getInstant());
    }
}
