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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.automation.optimalwindow.internal.OptimalWindowConstants;
import org.openhab.automation.optimalwindow.internal.factory.OptimalWindowModuleHandlerFactory;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.items.events.ItemTimeSeriesUpdatedEvent;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.types.TimeSeries;

/**
 * Tests the {@link OptimalWindowModuleHandlerFactory}. It is in the handler package to check the forwarding of
 * forecast updates with {@link WindowTriggerHandler#refresh()}.
 *
 * @author Thomas Leber - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class OptimalWindowModuleHandlerFactoryTest {
    private @Mock @NonNullByDefault({}) EventPublisher eventPublisher;
    private @Mock @NonNullByDefault({}) TimeZoneProvider timeZoneProvider;
    private @Mock @NonNullByDefault({}) PersistenceServiceRegistry registry;
    private @Mock @NonNullByDefault({}) QueryablePersistenceService persistenceService;

    private @NonNullByDefault({}) OptimalWindowModuleHandlerFactory factory;

    @BeforeEach
    void setUp() {
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.of("GMT"));
        when(registry.getDefault()).thenReturn(persistenceService);
        when(persistenceService.query(any(FilterCriteria.class))).thenReturn(List.of());
        factory = new OptimalWindowModuleHandlerFactory(eventPublisher, timeZoneProvider, registry);
    }

    private static Configuration config(String item) {
        return new Configuration(Map.of("forecastItem", item, "length", "1h"));
    }

    private Trigger trigger(String item) {
        return ModuleBuilder.createTrigger().withId("1").withTypeUID(OptimalWindowConstants.TRIGGER_TYPE_ID)
                .withConfiguration(config(item)).build();
    }

    private static ItemTimeSeriesUpdatedEvent timeSeriesUpdated(String item) {
        return ItemEventFactory.createTimeSeriesUpdatedEvent(item, new TimeSeries(TimeSeries.Policy.REPLACE), null);
    }

    @Test
    void typesAndEvents() {
        assertEquals(2, factory.getTypes().size());
        assertTrue(factory.getTypes().contains(OptimalWindowConstants.TRIGGER_TYPE_ID));
        assertTrue(factory.getTypes().contains(OptimalWindowConstants.CONDITION_TYPE_ID));
        assertEquals(Set.of(ItemTimeSeriesUpdatedEvent.TYPE), factory.getSubscribedEventTypes());
    }

    @Test
    void createsHandlers() {
        Condition condition = ModuleBuilder.createCondition().withId("2")
                .withTypeUID(OptimalWindowConstants.CONDITION_TYPE_ID).withConfiguration(config("Price")).build();
        Trigger unknown = ModuleBuilder.createTrigger().withId("3").withTypeUID("core.GenericCronTrigger").build();

        assertInstanceOf(WindowTriggerHandler.class, factory.getHandler(trigger("Price"), "rule1"));
        assertInstanceOf(InWindowConditionHandler.class, factory.getHandler(condition, "rule1"));
        assertNull(factory.getHandler(unknown, "rule1"));
    }

    @Test
    void forecastUpdateRecalculatesMatchingTrigger() {
        WindowTriggerHandler handler = (WindowTriggerHandler) factory.getHandler(trigger("Price"), "rule1");
        assertNotNull(handler);

        handler.refresh();
        handler.refresh();
        // the second refresh uses the cached result
        verify(persistenceService, times(1)).query(any(FilterCriteria.class));

        // an update of another item is ignored
        factory.receive(timeSeriesUpdated("OtherItem"));
        handler.refresh();
        verify(persistenceService, times(1)).query(any(FilterCriteria.class));

        factory.receive(timeSeriesUpdated("Price"));
        handler.refresh();
        verify(persistenceService, times(2)).query(any(FilterCriteria.class));
    }

    @Test
    void removedTriggerGetsNoUpdates() {
        Trigger trigger = trigger("Price");
        ModuleHandler moduleHandler = factory.getHandler(trigger, "rule1");
        assertNotNull(moduleHandler);
        WindowTriggerHandler handler = (WindowTriggerHandler) moduleHandler;
        handler.refresh();

        factory.ungetHandler(trigger, "rule1", handler);
        factory.receive(timeSeriesUpdated("Price"));
        handler.refresh();
        verify(persistenceService, times(1)).query(any(FilterCriteria.class));
    }
}
