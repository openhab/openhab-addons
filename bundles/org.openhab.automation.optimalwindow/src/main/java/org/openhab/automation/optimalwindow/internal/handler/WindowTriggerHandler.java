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
import org.openhab.automation.optimalwindow.internal.calc.WindowCalculator;
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
 * The window is checked every minute. It is recalculated when the forecast item receives a new time series, when the
 * search range moves on, and at least every {@link #MAX_RESULT_AGE}. Once a window has started, it is kept until the
 * end of its search range, so new forecast values cannot interrupt a running window.
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
    static final Duration MAX_RESULT_AGE = Duration.ofMinutes(15);

    private final Logger logger = LoggerFactory.getLogger(WindowTriggerHandler.class);
    private final WindowCalculator calculator;
    private final EventPublisher eventPublisher;
    private final Supplier<ZoneId> zoneSupplier;
    private final Clock clock;
    private final WindowConfiguration config;

    private final @Nullable String activeItem;
    private final @Nullable String startItem;
    private final @Nullable String endItem;
    private final @Nullable String countdownItem;
    private final @Nullable String remainingItem;
    private final @Nullable String windowTextItem;
    private final Map<String, State> lastStates = new HashMap<>();

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable WindowResult lockedResult;
    private @Nullable WindowResult cachedResult;
    private @Nullable TimeRange cachedRange;
    private Instant cachedAt = Instant.EPOCH;
    private @Nullable List<TimeRange> plannedRanges;
    private boolean active = false;
    private boolean started = false;

    public WindowTriggerHandler(Trigger module, WindowCalculator calculator, EventPublisher eventPublisher,
            Supplier<ZoneId> zoneSupplier, Clock clock) {
        super(module);
        this.calculator = calculator;
        this.eventPublisher = eventPublisher;
        this.zoneSupplier = zoneSupplier;
        this.clock = clock;

        Configuration configuration = module.getConfiguration();
        config = WindowConfiguration.from(configuration);
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
        scheduler.execute(this::refresh);
        refreshJob = scheduler.scheduleAtFixedRate(this::refresh, delay, REFRESH_INTERVAL.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void dispose() {
        ScheduledFuture<?> localJob = refreshJob;
        if (localJob != null) {
            localJob.cancel(true);
        }
        refreshJob = null;
        super.dispose();
    }

    /**
     * @return the name of the forecast item
     */
    public String getForecastItem() {
        return config.forecastItem;
    }

    /**
     * Called when the forecast item received a new time series. The window is recalculated on the next refresh,
     * which also gives the persistence service time to store the new values.
     */
    public synchronized void forecastUpdated() {
        cachedResult = null;
        cachedRange = null;
    }

    synchronized void refresh() {
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(zoneSupplier.get());
        WindowResult result;
        try {
            result = getResult(now);
        } catch (IllegalStateException e) {
            logger.warn("Cannot calculate optimal window for '{}': {}", config.forecastItem, e.getMessage());
            result = null;
        }

        boolean nowActive = result != null && result.isActive(now.toInstant());
        if (nowActive && result != null) {
            lockedResult = result;
        }

        // fire once on start, so a device is switched off if the end of a window was missed
        if (nowActive != active || !started) {
            active = nowActive;
            started = true;
            fire(result);
        }
        updateItems(result, now);
    }

    private @Nullable WindowResult getResult(ZonedDateTime now) {
        TimeRange range = WindowCalculator.getRange(config.rangeStart, config.rangeDuration, now);
        WindowResult localLockedResult = lockedResult;
        if (localLockedResult != null && localLockedResult.getSearchRange().equals(range)) {
            return localLockedResult;
        }
        lockedResult = null;

        if (range.equals(cachedRange) && cachedAt.plus(MAX_RESULT_AGE).isAfter(now.toInstant())) {
            return cachedResult;
        }

        WindowResult result = calculator.calculate(config, now);
        logger.trace("Optimal window for {}: {}", config, result);
        cachedResult = result;
        cachedRange = range;
        cachedAt = now.toInstant();
        return result;
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
            outputs.put(OUTPUT_START, ZonedDateTime.ofInstant(Instant.ofEpochMilli(result.getStart()), zone));
            outputs.put(OUTPUT_END, ZonedDateTime.ofInstant(Instant.ofEpochMilli(result.getEnd()), zone));
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
            return;
        }

        long nowMillis = now.toInstant().toEpochMilli();
        updateItem(startItem, new DateTimeType(Instant.ofEpochMilli(result.getStart())));
        updateItem(endItem, new DateTimeType(Instant.ofEpochMilli(result.getEnd())));
        updateItem(countdownItem, toMinutes(getNextStart(result, nowMillis) - nowMillis));
        updateItem(remainingItem, toMinutes(active ? result.getEnd() - nowMillis : 0));
        updateItem(windowTextItem, new StringType(result.getText(zoneSupplier.get())));
        sendPlannedWindow(result, now);
    }

    /**
     * @return the start of the next part of the window that has not started yet, or now if none is left
     */
    private static long getNextStart(WindowResult result, long now) {
        return result.getRanges().stream().mapToLong(TimeRange::start).filter(start -> start > now).min().orElse(now);
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

        long nowMillis = now.toInstant().toEpochMilli();
        TimeSeries timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
        timeSeries.add(now.toInstant(), OnOffType.from(active));
        for (TimeRange range : ranges) {
            if (range.end() <= nowMillis) {
                continue;
            }
            if (range.start() > nowMillis) {
                timeSeries.add(Instant.ofEpochMilli(range.start()), OnOffType.ON);
            }
            timeSeries.add(Instant.ofEpochMilli(range.end()), OnOffType.OFF);
        }
        long rangeEnd = result.getSearchRange().end();
        if (rangeEnd > result.getEnd() && rangeEnd > nowMillis) {
            timeSeries.add(Instant.ofEpochMilli(rangeEnd), OnOffType.OFF);
        }
        eventPublisher.post(ItemEventFactory.createTimeSeriesEvent(itemName, timeSeries, null));
    }

    private static State toMinutes(long millis) {
        return new QuantityType<>(Math.max(0, millis / 60000), Units.MINUTE);
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
