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
package org.openhab.persistence.inmemory.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.items.GenericItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;

/**
 * The {@link InMemoryPersistenceTests} contains tests for the {@link InMemoryPersistenceService}
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class InMemoryPersistenceTests {
    private static final String ITEM_NAME = "testItem";
    private static final String ALIAS = "alias";

    private @NonNullByDefault({}) InMemoryPersistenceService service;
    private @NonNullByDefault({}) @Mock GenericItem item;

    private @NonNullByDefault({}) FilterCriteria filterCriteria;

    @BeforeEach
    public void setup() {
        when(item.getName()).thenReturn(ITEM_NAME);

        filterCriteria = new FilterCriteria();
        filterCriteria.setItemName(ITEM_NAME);

        service = new InMemoryPersistenceService();
    }

    @Test
    public void storeDirect() {
        State state = new DecimalType(1);
        when(item.getState()).thenReturn(state);

        ZonedDateTime expectedTime = ZonedDateTime.now();
        service.store(item);

        TreeSet<HistoricItem> storedStates = new TreeSet<>(Comparator.comparing(HistoricItem::getTimestamp));
        service.query(filterCriteria).forEach(storedStates::add);

        assertThat(storedStates, hasSize(1));
        assertThat(storedStates.first().getName(), is(ITEM_NAME));
        assertThat(storedStates.first().getState(), is(state));
        assertThat((double) storedStates.first().getTimestamp().toEpochSecond(),
                is(closeTo(expectedTime.toEpochSecond(), 2)));
    }

    @Test
    public void storeAlias() {
        State state = new PercentType(1);
        when(item.getState()).thenReturn(state);

        ZonedDateTime expectedTime = ZonedDateTime.now();
        service.store(item, ALIAS);

        TreeSet<HistoricItem> storedStates = new TreeSet<>(Comparator.comparing(HistoricItem::getTimestamp));

        // query with item name should return nothing
        service.query(filterCriteria).forEach(storedStates::add);
        assertThat(storedStates, is(empty()));

        filterCriteria.setItemName(ALIAS);
        service.query(filterCriteria).forEach(storedStates::add);

        assertThat(storedStates.size(), is(1));
        assertThat(storedStates.first().getName(), is(ALIAS));
        assertThat(storedStates.first().getState(), is(state));
        assertThat((double) storedStates.first().getTimestamp().toEpochSecond(),
                is(closeTo(expectedTime.toEpochSecond(), 2)));
    }

    @Test
    public void storeHistoric() {
        State state = new HSBType("120,100,100");
        when(item.getState()).thenReturn(state);

        State historicState = new HSBType("40,50,50");
        ZonedDateTime expectedTime = ZonedDateTime.of(2022, 05, 31, 10, 0, 0, 0, ZoneId.systemDefault());
        service.store(item, expectedTime, historicState);

        TreeSet<HistoricItem> storedStates = new TreeSet<>(Comparator.comparing(HistoricItem::getTimestamp));
        service.query(filterCriteria).forEach(storedStates::add);

        assertThat(storedStates, hasSize(1));
        assertThat(storedStates.first().getName(), is(ITEM_NAME));
        assertThat(storedStates.first().getState(), is(historicState));
        assertThat(storedStates.first().getTimestamp(), is(expectedTime));
    }

    @Test
    public void queryWithoutItemNameReturnsEmptyList() {
        TreeSet<HistoricItem> storedStates = new TreeSet<>(Comparator.comparing(HistoricItem::getTimestamp));
        service.query(new FilterCriteria()).forEach(storedStates::add);

        assertThat(storedStates, is(empty()));
    }

    @Test
    public void queryUnknownItemReturnsEmptyList() {
        TreeSet<HistoricItem> storedStates = new TreeSet<>(Comparator.comparing(HistoricItem::getTimestamp));
        service.query(filterCriteria).forEach(storedStates::add);

        assertThat(storedStates, is(empty()));
    }

    @Test
    public void querySupportsAscendingOrdering() {
        ZonedDateTime start = ZonedDateTime.of(2020, 12, 1, 12, 0, 0, 0, ZoneId.systemDefault());
        service.store(item, start, new DecimalType(1));
        service.store(item, start.plusHours(1), new DecimalType(2));
        service.store(item, start.plusHours(2), new DecimalType(3));

        filterCriteria.setOrdering(FilterCriteria.Ordering.ASCENDING);
        filterCriteria.setBeginDate(start);

        List<Integer> resultSet = new ArrayList<>();
        service.query(filterCriteria).forEach(h -> resultSet.add(((DecimalType) h.getState()).intValue()));

        assertThat(resultSet, contains(1, 2, 3));
    }

    @Test
    public void querySupportsDescendingOrdering() {
        ZonedDateTime start = ZonedDateTime.of(2020, 12, 1, 12, 0, 0, 0, ZoneId.systemDefault());
        service.store(item, start, new DecimalType(1));
        service.store(item, start.plusHours(1), new DecimalType(2));
        service.store(item, start.plusHours(2), new DecimalType(3));

        filterCriteria.setOrdering(FilterCriteria.Ordering.DESCENDING);
        filterCriteria.setBeginDate(start);

        List<Integer> resultSet = new ArrayList<>();
        service.query(filterCriteria).forEach(h -> resultSet.add(((DecimalType) h.getState()).intValue()));

        assertThat(resultSet, contains(3, 2, 1));
    }

    @Test
    public void removeBetweenTimes() {
        State historicState1 = new StringType("value1");
        State historicState2 = new StringType("value2");
        State historicState3 = new StringType("value3");

        ZonedDateTime expectedTime = ZonedDateTime.of(2022, 05, 31, 10, 0, 0, 0, ZoneId.systemDefault());
        service.store(item, expectedTime, historicState1);
        service.store(item, expectedTime.plusHours(2), historicState2);
        service.store(item, expectedTime.plusHours(4), historicState3);

        // ensure both are stored
        TreeSet<HistoricItem> storedStates = new TreeSet<>(Comparator.comparing(HistoricItem::getTimestamp));
        service.query(filterCriteria).forEach(storedStates::add);

        assertThat(storedStates, hasSize(3));

        filterCriteria.setBeginDate(expectedTime.plusHours(1));
        filterCriteria.setEndDate(expectedTime.plusHours(3));
        service.remove(filterCriteria);

        filterCriteria = new FilterCriteria();
        filterCriteria.setItemName(ITEM_NAME);
        storedStates.clear();
        service.query(filterCriteria).forEach(storedStates::add);

        assertThat(storedStates, hasSize(2));

        assertThat(storedStates.first().getName(), is(ITEM_NAME));
        assertThat(storedStates.first().getState(), is(historicState1));
        assertThat(storedStates.first().getTimestamp(), is(expectedTime));

        assertThat(storedStates.last().getName(), is(ITEM_NAME));
        assertThat(storedStates.last().getState(), is(historicState3));
        assertThat(storedStates.last().getTimestamp(), is(expectedTime.plusHours(4)));
    }
}
