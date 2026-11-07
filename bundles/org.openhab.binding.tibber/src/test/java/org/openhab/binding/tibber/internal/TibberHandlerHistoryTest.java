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
package org.openhab.binding.tibber.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tibber.internal.handler.TibberHandler;
import org.openhab.binding.tibber.internal.history.TibberHistory;
import org.openhab.binding.tibber.internal.history.TibberHistory.HistoryRequest;
import org.openhab.binding.tibber.internal.history.TibberHistory.TimeWindow;
import org.openhab.binding.tibber.internal.history.TibberHistorySeries;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.internal.ThingImpl;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonObject;

/**
 * The {@link TibberHandlerHistoryTest} checks the automatic history fetch and the history channel state updates.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TibberHandlerHistoryTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final String THING_ID = "test";
    private static final ThingTypeUID THING_TYPE = TIBBER_THING_TYPE;
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE, THING_ID);
    private static final List<String> HISTORY_CHANNEL_IDS = List.of(CHANNEL_YEARLY_CONSUMPTION, CHANNEL_YEARLY_COST,
            CHANNEL_YEARLY_PRODUCTION, CHANNEL_MONTHLY_CONSUMPTION, CHANNEL_MONTHLY_COST, CHANNEL_MONTHLY_PRODUCTION,
            CHANNEL_WEEKLY_CONSUMPTION, CHANNEL_WEEKLY_COST, CHANNEL_WEEKLY_PRODUCTION,
            CHANNEL_HISTORY_DAILY_CONSUMPTION, CHANNEL_HISTORY_DAILY_COST, CHANNEL_HISTORY_DAILY_PRODUCTION);

    private @Nullable HandlerContext context;

    @AfterEach
    void tearDown() {
        HandlerContext context = this.context;
        if (context != null) {
            context.handler().dispose();
        }
    }

    /**
     * After an openHAB restart the link notification is not replayed to a handler which is initialized later,
     * so the initial fetch must also be triggered by the initialization itself.
     */
    @Test
    void whenInitializedWithLinkedHistoryChannelsThenAllWindowsAreFetched() {
        HandlerContext context = startHandler(true);

        await(() -> windowCount(context.history()) >= TimeWindow.values().length);

        Set<TimeWindow> windows = context.history().getRequests().stream().map(HistoryRequest::window)
                .collect(Collectors.toSet());
        assertEquals(Set.of(TimeWindow.values()), windows);
        // empty store: every window is loaded completely
        assertTrue(context.history().getRequests().stream().allMatch(HistoryRequest::fullUpdate));
        verify(context.cronScheduler(), atLeastOnce()).schedule(any(), anyString());
    }

    @Test
    void whenInitializedWithoutLinkedHistoryChannelsThenNoHistoryFetchHappens() {
        HandlerContext context = startHandler(false);

        awaitUntil(300, () -> !context.history().getRequests().isEmpty());

        assertTrue(context.history().getRequests().isEmpty());
        verify(context.cronScheduler(), never()).schedule(any(), anyString());
    }

    @Test
    void whenHistoryChannelLinkedAfterInitializationThenHistoryIsFetched() {
        HandlerContext context = startHandler(false);

        ChannelUID monthlyCostChannel = historyChannelUID(CHANNEL_MONTHLY_COST);
        context.callback().setChannelLinked(monthlyCostChannel, true);
        context.handler().channelLinked(monthlyCostChannel);

        await(() -> windowCount(context.history()) >= TimeWindow.values().length);
        assertTrue(context.history().getRequests().stream()
                .anyMatch(request -> request.window() == TimeWindow.MONTHLY && !request.fullUpdate()));
        verify(context.cronScheduler(), atLeastOnce()).schedule(any(), anyString());
    }

    @Test
    void whenMonthlyHistoryContainsFutureEntryThenNewestPastEntryIsPushedAsState() {
        HandlerContext context = startHandler(true);
        TibberHistorySeries monthly = new TibberHistorySeries(null);
        Instant lastMonth = LocalDate.now(ZONE).withDayOfMonth(1).minusMonths(1).atStartOfDay(ZONE).toInstant();
        Instant nextMonth = LocalDate.now(ZONE).withDayOfMonth(1).plusMonths(1).atStartOfDay(ZONE).toInstant();
        monthly.put(lastMonth, historyEntry("369.279 kWh", null, null));
        monthly.put(nextMonth, historyEntry("12.0 kWh", null, null));
        context.history().put(TimeWindow.MONTHLY, monthly);

        context.handler().historyUpdated(new HistoryRequest(TimeWindow.MONTHLY, false), monthly);

        assertEquals(QuantityType.valueOf("369.279 kWh"),
                context.callback().getStateUpdate(historyChannelUID(CHANNEL_MONTHLY_CONSUMPTION)));
        assertNull(context.callback().getStateUpdate(historyChannelUID(CHANNEL_MONTHLY_COST)));
        assertNull(context.callback().getStateUpdate(historyChannelUID(CHANNEL_MONTHLY_PRODUCTION)));
        assertNotNull(context.callback().getTimeSeriesSend(historyChannelUID(CHANNEL_MONTHLY_CONSUMPTION)));
    }

    /**
     * The current month is aggregated from DAILY data: WEEKLY entries are dated at the start of a completed
     * ISO week and therefore do not cover the running month.
     */
    @Test
    void whenDailyCoversCurrentMonthThenMonthlyStateShowsCurrentMonthToDate() {
        HandlerContext context = startHandler(true);
        ZonedDateTime monthStart = LocalDate.now(ZONE).withDayOfMonth(1).atStartOfDay(ZONE);
        // the aggregator ignores entries at or after the current hour
        assumeTrue(Instant.now().truncatedTo(ChronoUnit.HOURS).isAfter(monthStart.toInstant()),
                "first hour of a month - nothing has been completed yet");

        TibberHistorySeries daily = new TibberHistorySeries(null);
        daily.put(monthStart.toInstant(), historyEntry("2.5000 kWh", null, null));
        context.history().put(TimeWindow.DAILY, daily);

        TibberHistorySeries monthly = new TibberHistorySeries(null);
        monthly.put(monthStart.minusMonths(1).toInstant(), historyEntry("369.279 kWh", null, null));
        context.history().put(TimeWindow.MONTHLY, monthly);

        context.handler().historyUpdated(new HistoryRequest(TimeWindow.MONTHLY, false), monthly);

        await(() -> QuantityType.valueOf("2.5 kWh")
                .equals(context.callback().getStateUpdate(historyChannelUID(CHANNEL_MONTHLY_CONSUMPTION))));
        assertNull(context.callback().getStateUpdate(historyChannelUID(CHANNEL_MONTHLY_COST)));
    }

    private HandlerContext startHandler(boolean linkHistoryChannels) {
        HandlerContext context = createContext(linkHistoryChannels);
        this.context = context;
        context.handler().initialize();
        context.callback().waitFor(ThingStatus.ONLINE);
        return context;
    }

    private static HandlerContext createContext(boolean linkHistoryChannels) {
        ThingImpl thing = new ThingImpl(THING_TYPE, THING_ID);
        thing.setChannels(historyChannels());
        Configuration configuration = new Configuration();
        configuration.put("token", "testToken");
        configuration.put("homeid", "testHomeId");
        thing.setConfiguration(configuration);

        CronScheduler cronScheduler = mock(CronScheduler.class);
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZONE);
        TibberHandlerCallbackMock callback = new TibberHandlerCallbackMock();
        if (linkHistoryChannels) {
            HISTORY_CHANNEL_IDS.forEach(id -> callback.setChannelLinked(historyChannelUID(id), true));
        }
        TibberHandlerMock handler = new TibberHandlerMock(thing,
                createHttpClient("src/test/resources/subscription-null-response.json"), cronScheduler,
                mock(BundleContext.class), timeZoneProvider, mock(StorageService.class));
        handler.setCallback(callback);
        HistoryStub history = new HistoryStub(handler);
        handler.setHistoryOverride(history);
        return new HandlerContext(handler, callback, history, cronScheduler);
    }

    private static HttpClient createHttpClient(String responseFileName) {
        ContentResponse responseMock = mock(ContentResponse.class);
        when(responseMock.getStatus()).thenReturn(HttpStatus.OK_200);
        when(responseMock.getContentAsString()).thenReturn(readResponse(responseFileName));
        Request requestMock = mock(Request.class);
        when(requestMock.timeout(anyLong(), any())).thenReturn(requestMock);
        when(requestMock.header(any(HttpHeader.class), anyString())).thenReturn(requestMock);
        when(requestMock.header(anyString(), anyString())).thenReturn(requestMock);
        try {
            when(requestMock.send()).thenReturn(responseMock);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            fail(e.getMessage());
        }
        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.POST(anyString())).thenReturn(requestMock);
        return httpClientMock;
    }

    private static String readResponse(String fileName) {
        try {
            return Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            fail(e.getMessage());
            return "";
        }
    }

    private static List<Channel> historyChannels() {
        List<Channel> channels = new ArrayList<>();
        for (String channelId : HISTORY_CHANNEL_IDS) {
            channels.add(ChannelBuilder.create(historyChannelUID(channelId), CoreItemFactory.NUMBER).build());
        }
        return channels;
    }

    private static ChannelUID historyChannelUID(String channelId) {
        return new ChannelUID(THING_UID, CHANNEL_GROUP_HISTORY, channelId);
    }

    private static JsonObject historyEntry(@Nullable String consumption, @Nullable String cost,
            @Nullable String production) {
        JsonObject entry = new JsonObject();
        if (consumption != null) {
            entry.addProperty(TibberHistorySeries.PURPOSE_CONSUMPTION, consumption);
        }
        if (cost != null) {
            entry.addProperty(TibberHistorySeries.PURPOSE_COST, cost);
        }
        if (production != null) {
            entry.addProperty(TibberHistorySeries.PURPOSE_PRODUCTION, production);
        }
        return entry;
    }

    private static long windowCount(HistoryStub history) {
        return history.getRequests().stream().map(HistoryRequest::window).distinct().count();
    }

    private static void await(BooleanSupplier condition) {
        Instant deadline = Instant.now().plusSeconds(5);
        while (!condition.getAsBoolean()) {
            assertTrue(Instant.now().isBefore(deadline), "condition not fulfilled within 5 seconds");
            sleep();
        }
    }

    private static void awaitUntil(long timeoutMs, BooleanSupplier condition) {
        Instant deadline = Instant.now().plusMillis(timeoutMs);
        while (!condition.getAsBoolean() && Instant.now().isBefore(deadline)) {
            sleep();
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(25);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail(e.getMessage());
        }
    }

    private record HandlerContext(TibberHandlerMock handler, TibberHandlerCallbackMock callback, HistoryStub history,
            CronScheduler cronScheduler) {
    }

    private static class HistoryStub extends TibberHistory {

        private final Map<TimeWindow, TibberHistorySeries> stored = new ConcurrentHashMap<>();
        private final List<HistoryRequest> requests = new CopyOnWriteArrayList<>();

        HistoryStub(TibberHandler handler) {
            super(mock(StorageService.class), "testHome", handler);
        }

        @Override
        public void updateHistory(TimeWindow window, boolean fullUpdate) {
            requests.add(new HistoryRequest(window, fullUpdate));
        }

        @Override
        public TibberHistorySeries getStoredSeries(TimeWindow window) {
            TibberHistorySeries series = stored.get(window);
            if (series == null) {
                series = new TibberHistorySeries(null);
                stored.put(window, series);
            }
            return series;
        }

        void put(TimeWindow window, TibberHistorySeries series) {
            stored.put(window, series);
        }

        List<HistoryRequest> getRequests() {
            return List.copyOf(requests);
        }
    }
}
