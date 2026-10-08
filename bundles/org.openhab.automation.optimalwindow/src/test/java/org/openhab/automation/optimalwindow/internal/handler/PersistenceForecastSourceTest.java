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
package org.openhab.automation.optimalwindow.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.types.State;

/**
 * Tests the {@link PersistenceForecastSource}.
 *
 * @author Thomas Leber - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class PersistenceForecastSourceTest {
    private static final ZonedDateTime BEGIN = ZonedDateTime.of(2026, 10, 7, 0, 0, 0, 0, ZoneId.of("GMT"));
    private static final ZonedDateTime END = BEGIN.plusDays(2);

    private @Mock @NonNullByDefault({}) PersistenceServiceRegistry registry;
    private @Mock @NonNullByDefault({}) QueryablePersistenceService defaultService;
    private @Mock @NonNullByDefault({}) QueryablePersistenceService influxdb;
    private @Mock @NonNullByDefault({}) PersistenceService notQueryable;

    private static HistoricItem historicItem(ZonedDateTime timestamp, State state) {
        return new HistoricItem() {
            @Override
            public ZonedDateTime getTimestamp() {
                return timestamp;
            }

            @Override
            public State getState() {
                return state;
            }

            @Override
            public String getName() {
                return "Price";
            }
        };
    }

    @BeforeEach
    void setUp() {
        when(registry.getDefault()).thenReturn(defaultService);
        when(registry.get("influxdb")).thenReturn(influxdb);
        when(registry.get("rrd4j")).thenReturn(notQueryable);
        when(defaultService.query(any(FilterCriteria.class))).thenReturn(List.of());
        when(influxdb.query(any(FilterCriteria.class))).thenReturn(List.of());
    }

    @Test
    void readsNumbersAndQuantities() {
        when(defaultService.query(any(FilterCriteria.class)))
                .thenReturn(List.of(historicItem(BEGIN, new DecimalType(10.5)),
                        historicItem(BEGIN.plusHours(1), new QuantityType<>(0.2, Units.ONE)),
                        historicItem(BEGIN.plusHours(2), OnOffType.ON)));

        SortedMap<Long, Double> values = new PersistenceForecastSource(registry).getValues("Price", null, BEGIN, END);

        // states that are not numbers are skipped
        assertEquals(Map.of(BEGIN.toInstant().toEpochMilli(), 10.5, BEGIN.plusHours(1).toInstant().toEpochMilli(), 0.2),
                values);
    }

    @Test
    void queriesItemAndPeriodAscending() {
        new PersistenceForecastSource(registry).getValues("Price", null, BEGIN, END);

        ArgumentCaptor<FilterCriteria> captor = ArgumentCaptor.forClass(FilterCriteria.class);
        verify(defaultService).query(captor.capture());
        FilterCriteria filter = captor.getValue();
        assertEquals("Price", filter.getItemName());
        assertEquals(BEGIN, filter.getBeginDate());
        assertEquals(END, filter.getEndDate());
        assertEquals(FilterCriteria.Ordering.ASCENDING, filter.getOrdering());
    }

    @Test
    void usesConfiguredService() {
        new PersistenceForecastSource(registry).getValues("Price", "influxdb", BEGIN, END);

        verify(influxdb).query(any(FilterCriteria.class));
        verify(defaultService, never()).query(any(FilterCriteria.class));
    }

    @Test
    void failsForMissingOrNotQueryableService() {
        PersistenceForecastSource source = new PersistenceForecastSource(registry);

        assertThrows(IllegalStateException.class, () -> source.getValues("Price", "jdbc", BEGIN, END));
        assertThrows(IllegalStateException.class, () -> source.getValues("Price", "rrd4j", BEGIN, END));
    }
}
