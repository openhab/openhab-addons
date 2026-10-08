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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.automation.optimalwindow.internal.OptimalWindowConstants;
import org.openhab.automation.optimalwindow.internal.calc.ForecastSource;
import org.openhab.automation.optimalwindow.internal.calc.WindowCalculator;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.TriggerHandlerCallback;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.items.events.ItemTimeSeriesEvent;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;

/**
 * Tests the {@link WindowTriggerHandler}.
 *
 * @author Thomas Leber - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class WindowTriggerHandlerTest {
    private static final ZoneId ZONE = ZoneId.of("GMT");
    private static final ZonedDateTime DAY = ZonedDateTime.of(2026, 10, 7, 0, 0, 0, 0, ZONE);

    private @Mock @NonNullByDefault({}) TriggerHandlerCallback callback;
    private @Mock @NonNullByDefault({}) EventPublisher eventPublisher;
    private @Mock @NonNullByDefault({}) ScheduledExecutorService scheduler;

    private final MutableClock clock = new MutableClock(DAY.toInstant());
    private SortedMap<Long, Double> forecast = new TreeMap<>();
    private final ForecastSource source = (item, service, begin, end) -> forecast;

    /**
     * Hourly forecast for two days, cheap from {@code cheapFrom} to {@code cheapTo} on the first day.
     */
    private static SortedMap<Long, Double> forecast(int cheapFrom, int cheapTo) {
        SortedMap<Long, Double> values = new TreeMap<>();
        for (int hour = 0; hour < 48; hour++) {
            values.put(DAY.plusHours(hour).toInstant().toEpochMilli(),
                    hour >= cheapFrom && hour < cheapTo ? 5.0 : 20.0);
        }
        return values;
    }

    private WindowTriggerHandler createHandler(Map<String, Object> extraConfig) {
        Map<String, Object> config = new HashMap<>(Map.of("forecastItem", "Price", "length", "2h", "activeItem",
                "Active", "windowTextItem", "WindowText", "countdownItem", "Countdown", "remainingItem", "Remaining"));
        config.putAll(extraConfig);
        Trigger trigger = ModuleBuilder.createTrigger().withId("1").withTypeUID(OptimalWindowConstants.TRIGGER_TYPE_ID)
                .withConfiguration(new Configuration(config)).build();
        WindowTriggerHandler handler = new WindowTriggerHandler(trigger, new WindowCalculator(source), eventPublisher,
                () -> ZONE, clock);
        // the scheduler is mocked, so the test calls refresh() itself
        when(callback.getScheduler()).thenReturn(scheduler);
        handler.setCallback(callback);
        return handler;
    }

    private void at(int hour) {
        clock.instant = DAY.plusHours(hour).toInstant();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, ?>> triggered() {
        ArgumentCaptor<Map<String, ?>> captor = ArgumentCaptor.forClass(Map.class);
        verify(callback, atLeast(0)).triggered(any(Trigger.class), captor.capture());
        return captor.getAllValues();
    }

    private State lastState(String itemName) {
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventPublisher, atLeast(0)).post(captor.capture());
        State state = null;
        for (Event event : captor.getAllValues()) {
            if (event instanceof ItemStateEvent stateEvent && stateEvent.getItemName().equals(itemName)) {
                state = stateEvent.getItemState();
            }
        }
        assertNotNull(state, "no state for " + itemName);
        return state;
    }

    @BeforeEach
    void setUp() {
        forecast = forecast(2, 4);
    }

    @Test
    void firesStartAndEnd() {
        WindowTriggerHandler handler = createHandler(Map.of());

        at(1);
        handler.refresh();
        // the first refresh fires the current state
        assertEquals(1, triggered().size());
        assertEquals(OptimalWindowConstants.EVENT_END, triggered().get(0).get(OptimalWindowConstants.OUTPUT_EVENT));
        assertEquals(OnOffType.OFF, lastState("Active"));
        assertEquals(new StringType("02:00\u201304:00"), lastState("WindowText"));

        at(2);
        handler.refresh();
        assertEquals(2, triggered().size());
        assertEquals(OptimalWindowConstants.EVENT_START, triggered().get(1).get(OptimalWindowConstants.OUTPUT_EVENT));
        assertEquals(OnOffType.ON, triggered().get(1).get(OptimalWindowConstants.OUTPUT_COMMAND));
        assertEquals(OnOffType.ON, lastState("Active"));

        at(3);
        handler.refresh();
        assertEquals(2, triggered().size());

        at(4);
        handler.refresh();
        assertEquals(3, triggered().size());
        assertEquals(OptimalWindowConstants.EVENT_END, triggered().get(2).get(OptimalWindowConstants.OUTPUT_EVENT));
        assertEquals(OnOffType.OFF, triggered().get(2).get(OptimalWindowConstants.OUTPUT_COMMAND));
        assertEquals(OnOffType.OFF, lastState("Active"));
    }

    @Test
    void startedWindowIsKeptWhenForecastChanges() {
        WindowTriggerHandler handler = createHandler(Map.of());

        at(2);
        handler.refresh();
        assertEquals(1, triggered().size());

        // new forecast values make 10:00 - 12:00 cheaper, but the running window must not be interrupted
        forecast = forecast(10, 12);
        handler.forecastUpdated();
        at(3);
        handler.refresh();
        assertEquals(1, triggered().size());
        assertEquals(OnOffType.ON, lastState("Active"));

        at(4);
        handler.refresh();
        assertEquals(2, triggered().size());

        // no second window within the same range
        at(10);
        handler.refresh();
        assertEquals(2, triggered().size());
    }

    @Test
    void startsWhenCreatedWithinWindow() {
        WindowTriggerHandler handler = createHandler(Map.of());

        clock.instant = DAY.plusHours(2).plusMinutes(30).toInstant();
        handler.refresh();
        assertEquals(1, triggered().size());
        assertEquals(OptimalWindowConstants.EVENT_START, triggered().get(0).get(OptimalWindowConstants.OUTPUT_EVENT));
    }

    @Test
    void missingForecastOnlySendsEndOnStart() {
        forecast = new TreeMap<>();
        WindowTriggerHandler handler = createHandler(Map.of());

        at(2);
        handler.refresh();
        at(3);
        handler.refresh();
        assertEquals(1, triggered().size());
        assertEquals(OptimalWindowConstants.EVENT_END, triggered().get(0).get(OptimalWindowConstants.OUTPUT_EVENT));
        assertEquals(OnOffType.OFF, lastState("Active"));
        assertEquals(UnDefType.UNDEF, lastState("WindowText"));
    }

    @Test
    void missedEndIsSentAfterRestart() {
        // e.g. openHAB was down from 03:00 to 05:00, the device must be switched off
        WindowTriggerHandler handler = createHandler(Map.of());

        at(5);
        handler.refresh();
        assertEquals(1, triggered().size());
        assertEquals(OnOffType.OFF, triggered().get(0).get(OptimalWindowConstants.OUTPUT_COMMAND));
    }

    @Test
    void resultIsCachedUntilForecastIsUpdated() {
        WindowTriggerHandler handler = createHandler(Map.of());

        at(0);
        handler.refresh();
        assertEquals(new StringType("02:00\u201304:00"), lastState("WindowText"));

        // without a time series update the cached window is used
        forecast = forecast(6, 8);
        clock.instant = DAY.plusMinutes(10).toInstant();
        handler.refresh();
        assertEquals(new StringType("02:00\u201304:00"), lastState("WindowText"));

        handler.forecastUpdated();
        clock.instant = DAY.plusMinutes(11).toInstant();
        handler.refresh();
        assertEquals(new StringType("06:00\u201308:00"), lastState("WindowText"));
    }

    @Test
    void cachedResultExpires() {
        WindowTriggerHandler handler = createHandler(Map.of());

        at(0);
        handler.refresh();
        forecast = forecast(6, 8);
        clock.instant = DAY.plus(WindowTriggerHandler.MAX_RESULT_AGE).toInstant();
        handler.refresh();
        assertEquals(new StringType("06:00\u201308:00"), lastState("WindowText"));
    }

    @Test
    void countdownAndRemaining() {
        WindowTriggerHandler handler = createHandler(Map.of());

        clock.instant = DAY.plusMinutes(75).toInstant();
        handler.refresh();
        assertEquals(new QuantityType<>(45, Units.MINUTE), lastState("Countdown"));
        assertEquals(new QuantityType<>(0, Units.MINUTE), lastState("Remaining"));

        clock.instant = DAY.plusMinutes(150).toInstant();
        handler.refresh();
        assertEquals(new QuantityType<>(0, Units.MINUTE), lastState("Countdown"));
        assertEquals(new QuantityType<>(90, Units.MINUTE), lastState("Remaining"));
    }

    @Test
    void plannedWindowIsSentAsTimeSeries() {
        WindowTriggerHandler handler = createHandler(Map.of());

        at(1);
        handler.refresh();
        handler.refresh();

        List<ItemTimeSeriesEvent> events = postedEvents().stream().filter(ItemTimeSeriesEvent.class::isInstance)
                .map(ItemTimeSeriesEvent.class::cast).toList();
        // only sent when the window changes
        assertEquals(1, events.size());
        assertEquals("Active", events.get(0).getItemName());

        List<TimeSeries.Entry> entries = events.get(0).getTimeSeries().getStates().toList();
        assertEquals(List.of(new TimeSeries.Entry(DAY.plusHours(1).toInstant(), OnOffType.OFF),
                new TimeSeries.Entry(DAY.plusHours(2).toInstant(), OnOffType.ON),
                new TimeSeries.Entry(DAY.plusHours(4).toInstant(), OnOffType.OFF),
                new TimeSeries.Entry(DAY.plusHours(24).toInstant(), OnOffType.OFF)), entries);
    }

    @Test
    void unresolvedTemplateReferencesUseDefaults() {
        // a rule created from the template keeps the references of optional parameters that were left empty
        WindowTriggerHandler handler = createHandler(Map.of("persistenceService", "{{persistenceService}}",
                "rangeStart", "{{rangeStart}}", "consecutive", "{{consecutive}}", "endItem", "{{endItem}}"));

        at(2);
        handler.refresh();
        assertEquals(1, triggered().size());
        verify(eventPublisher, never()).post(argThat(event -> event.getTopic().contains("{{")));
    }

    @Test
    void itemsAreOnlyUpdatedOnChange() {
        WindowTriggerHandler handler = createHandler(Map.of());

        at(0);
        handler.refresh();
        handler.refresh();
        long activeUpdates = new ArrayList<>(postedEvents()).stream()
                .filter(e -> e instanceof ItemStateEvent s && s.getItemName().equals("Active")).count();
        assertEquals(1, activeUpdates);
    }

    private List<Event> postedEvents() {
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventPublisher, atLeast(0)).post(captor.capture());
        return captor.getAllValues();
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZONE;
        }

        @Override
        public Clock withZone(@NonNullByDefault({}) ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
