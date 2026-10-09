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

import static org.openhab.automation.optimalwindow.internal.OptimalWindowConstants.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.optimalwindow.internal.calc.TimeRange;
import org.openhab.automation.optimalwindow.internal.calc.WindowConfiguration;
import org.openhab.automation.optimalwindow.internal.calc.WindowResult;
import org.openhab.core.automation.ModuleHandlerCallback;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.BaseTriggerModuleHandler;
import org.openhab.core.automation.handler.TriggerHandlerCallback;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Triggers a rule when the optimal window starts and when it ends, and optionally updates status items.
 *
 * The window is checked every minute. When it is recalculated and which window is kept is decided by the
 * {@link WindowTracker}.
 *
 * When the trigger starts, e.g. after a restart of openHAB, it fires once with the current state, so a device is
 * switched off if the end of a window was missed.
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Thomas Leber - Adapted for the Optimal Window automation
 */
@NonNullByDefault
public class WindowTriggerHandler extends BaseTriggerModuleHandler {
    private static final Duration REFRESH_INTERVAL = Duration.ofMinutes(1);

    private final Logger logger = LoggerFactory.getLogger(WindowTriggerHandler.class);
    private final WindowTracker tracker;
    private final EventPublisher eventPublisher;
    private final Supplier<ZoneId> zoneSupplier;
    private final Clock clock;

    private final @Nullable String activeItem;
    private final @Nullable String startItem;
    private final @Nullable String endItem;
    private final @Nullable String countdownItem;
    private final @Nullable String remainingItem;
    private final @Nullable String windowTextItem;
    private final Map<String, State> lastStates = new HashMap<>();

    private @Nullable ScheduledFuture<?> initialRefreshJob;
    private @Nullable ScheduledFuture<?> refreshJob;
    // a planned window persisted before a restart is unknown, so it is replaced up to the longest possible range
    private @Nullable List<TimeRange> plannedRanges = List.of();
    private Instant plannedUntil;
    private boolean active = false;
    private boolean started = false;
    private boolean disposed = false;

    public WindowTriggerHandler(Trigger module, WindowTracker tracker, EventPublisher eventPublisher,
            Supplier<ZoneId> zoneSupplier, Clock clock) {
        super(module);
        this.tracker = tracker;
        this.eventPublisher = eventPublisher;
        this.zoneSupplier = zoneSupplier;
        this.clock = clock;
        plannedUntil = clock.instant().plus(WindowConfiguration.MAX_RANGE_DURATION);

        Configuration configuration = module.getConfiguration();
        activeItem = getItemName(configuration, CONFIG_ACTIVE_ITEM);
        startItem = getItemName(configuration, CONFIG_START_ITEM);
        endItem = getItemName(configuration, CONFIG_END_ITEM);
        countdownItem = getItemName(configuration, CONFIG_COUNTDOWN_ITEM);
        remainingItem = getItemName(configuration, CONFIG_REMAINING_ITEM);
        windowTextItem = getItemName(configuration, CONFIG_WINDOW_TEXT_ITEM);
    }

    @Override
    public void setCallback(ModuleHandlerCallback callback) {
        super.setCallback(callback);
        if (!(callback instanceof TriggerHandlerCallback triggerCallback)) {
            return;
        }

        // the job is required to run exactly at minute borders, hence we can't use scheduleWithFixedDelay
        Instant now = clock.instant();
        long delay = Duration.between(now, now.truncatedTo(ChronoUnit.MINUTES).plus(REFRESH_INTERVAL)).toMillis();
        ScheduledExecutorService scheduler = triggerCallback.getScheduler();
        initialRefreshJob = scheduler.schedule(this::refresh, 0, TimeUnit.MILLISECONDS);
        refreshJob = scheduler.scheduleAtFixedRate(this::refresh, delay, REFRESH_INTERVAL.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void dispose() {
        // a refresh that is already waiting for the lock must not run anymore
        disposed = true;
        cancel(initialRefreshJob);
        initialRefreshJob = null;
        cancel(refreshJob);
        refreshJob = null;
        // remove the planned window, so the active item is not switched by an obsolete plan
        clearPlannedWindow(clock.instant());
        super.dispose();
    }

    private static void cancel(@Nullable ScheduledFuture<?> job) {
        if (job != null) {
            job.cancel(true);
        }
    }

    synchronized void refresh() {
        if (disposed) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(zoneSupplier.get());
        WindowResult result;
        try {
            result = tracker.getResult(now);
        } catch (RuntimeException e) {
            // any exception would stop the scheduled refresh, so it is only logged
            logger.warn("Cannot calculate optimal window for '{}': {}", tracker.getForecastItem(), e.getMessage());
            result = null;
        }

        boolean nowActive = result != null && result.isActive(now.toInstant());

        // fire once on start, so a device is switched off if the end of a window was missed
        if (nowActive != active || !started) {
            active = nowActive;
            started = true;
            fire(result);
        }
        updateItems(result, now);
    }

    private void fire(@Nullable WindowResult result) {
        if (!(callback instanceof TriggerHandlerCallback triggerCallback)) {
            return;
        }

        Map<String, Object> outputs = new HashMap<>();
        outputs.put(OUTPUT_EVENT, active ? EVENT_START : EVENT_END);
        outputs.put(OUTPUT_COMMAND, OnOffType.from(active));
        if (result != null) {
            ZoneId zone = zoneSupplier.get();
            outputs.put(OUTPUT_START, result.getStart().atZone(zone));
            outputs.put(OUTPUT_END, result.getEnd().atZone(zone));
            outputs.put(OUTPUT_AVERAGE, BigDecimal.valueOf(result.getAverage()));
        }
        triggerCallback.triggered(module, outputs);
    }

    private void updateItems(@Nullable WindowResult result, ZonedDateTime now) {
        updateItem(activeItem, OnOffType.from(active));
        if (result == null) {
            updateItem(startItem, UnDefType.UNDEF);
            updateItem(endItem, UnDefType.UNDEF);
            updateItem(countdownItem, UnDefType.UNDEF);
            updateItem(remainingItem, UnDefType.UNDEF);
            updateItem(windowTextItem, UnDefType.UNDEF);
            clearPlannedWindow(now.toInstant());
            return;
        }

        Instant instant = now.toInstant();
        updateItem(startItem, new DateTimeType(result.getStart()));
        updateItem(endItem, new DateTimeType(result.getEnd()));
        updateItem(countdownItem, toMinutes(Duration.between(instant, getNextStart(result, instant))));
        updateItem(remainingItem, toMinutes(Duration.between(instant, getCurrentEnd(result, instant))));
        updateItem(windowTextItem, new StringType(result.getText(zoneSupplier.get())));
        sendPlannedWindow(result, now);
    }

    /**
     * @return the start of the next part of the window that has not started yet, or now if none is left
     */
    private static Instant getNextStart(WindowResult result, Instant now) {
        for (TimeRange range : result.getRanges()) {
            if (range.start().isAfter(now)) {
                return range.start();
            }
        }
        return now;
    }

    /**
     * @return the end of the part of the window that contains now, or now if the window is not active
     */
    private static Instant getCurrentEnd(WindowResult result, Instant now) {
        for (TimeRange range : result.getRanges()) {
            if (range.contains(now)) {
                return range.end();
            }
        }
        return now;
    }

    /**
     * Send the planned window as time series to the active item, so charts can show it. It is only sent when the
     * window changes and covers the time from now until the end of the search range.
     */
    private void sendPlannedWindow(WindowResult result, ZonedDateTime now) {
        String itemName = activeItem;
        List<TimeRange> ranges = result.getRanges();
        if (itemName == null || ranges.equals(plannedRanges)) {
            return;
        }
        plannedRanges = ranges;

        Instant instant = now.toInstant();
        TimeSeries timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
        timeSeries.add(instant, OnOffType.from(active));
        for (TimeRange range : ranges) {
            if (!range.end().isAfter(instant)) {
                continue;
            }
            if (range.start().isAfter(instant)) {
                timeSeries.add(range.start(), OnOffType.ON);
            }
            timeSeries.add(range.end(), OnOffType.OFF);
        }
        // the window is always within the search range
        plannedUntil = result.getSearchRange().end();
        if (plannedUntil.isAfter(result.getEnd()) && plannedUntil.isAfter(instant)) {
            timeSeries.add(plannedUntil, OnOffType.OFF);
        }
        eventPublisher.post(ItemEventFactory.createTimeSeriesEvent(itemName, timeSeries, null));
    }

    /**
     * Replace a previously sent planned window with {@code OFF}, so no obsolete future states remain.
     */
    private void clearPlannedWindow(Instant now) {
        String itemName = activeItem;
        if (itemName == null || plannedRanges == null) {
            return;
        }
        plannedRanges = null;

        TimeSeries timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
        timeSeries.add(now, OnOffType.OFF);
        if (plannedUntil.isAfter(now)) {
            timeSeries.add(plannedUntil, OnOffType.OFF);
        }
        eventPublisher.post(ItemEventFactory.createTimeSeriesEvent(itemName, timeSeries, null));
    }

    private static State toMinutes(Duration duration) {
        return new QuantityType<>(Math.max(0, duration.toMinutes()), Units.MINUTE);
    }

    private void updateItem(@Nullable String itemName, State state) {
        if (itemName == null || state.equals(lastStates.get(itemName))) {
            return;
        }
        lastStates.put(itemName, state);
        eventPublisher.post(ItemEventFactory.createStateEvent(itemName, state));
    }

    private static @Nullable String getItemName(Configuration configuration, String key) {
        return WindowConfiguration.getValue(configuration, key) instanceof String name ? name : null;
    }
}
