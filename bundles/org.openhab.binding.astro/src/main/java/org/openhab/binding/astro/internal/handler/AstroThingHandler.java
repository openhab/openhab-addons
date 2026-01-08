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
package org.openhab.binding.astro.internal.handler;

import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.thing.type.ChannelKind.TRIGGER;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.InstantSource;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.AstroBindingConstants;
import org.openhab.binding.astro.internal.action.AstroActions;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.config.AstroThingConfig;
import org.openhab.binding.astro.internal.job.Job;
import org.openhab.binding.astro.internal.job.PositionalJob;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Position;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base ThingHandler for all Astro handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
@NonNullByDefault
public abstract class AstroThingHandler extends BaseThingHandler {
    private static final String DAILY_MIDNIGHT = "30 0 0 * * ? *";

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SimpleDateFormat loggerFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ROOT);

    /** Scheduler to schedule jobs */
    private final CronScheduler cronScheduler;

    protected final TimeZoneProvider timeZoneProvider;

    protected final LocaleProvider localeProvider;

    private final Lock monitor = new ReentrantLock();

    // All access must be guarded by "monitor"
    private final Map<String, ScheduledFuture<?>> scheduledFutures = new HashMap<>();

    // All access must be guarded by "monitor"
    private boolean linkedPositionalChannels;

    protected AstroThingConfig thingConfig = new AstroThingConfig();

    /** The source of the current time */
    protected final InstantSource instantSource;

    public AstroThingHandler(Thing thing, final CronScheduler scheduler, final TimeZoneProvider timeZoneProvider,
            LocaleProvider localeProvider, InstantSource instantSource) {
        super(thing);
        this.cronScheduler = scheduler;
        this.timeZoneProvider = timeZoneProvider;
        this.localeProvider = localeProvider;
        this.instantSource = instantSource;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());
        String thingUid = getThing().getUID().toString();
        thingConfig = getConfigAs(AstroThingConfig.class);
        boolean validConfig = true;
        String geoLocation = thingConfig.geolocation;
        if (geoLocation == null || geoLocation.trim().isEmpty()) {
            logger.error("Astro parameter geolocation is mandatory and must be configured, disabling thing '{}'",
                    thingUid);
            validConfig = false;
        } else {
            thingConfig.parseGeoLocation();
        }

        if (thingConfig.latitude == null || thingConfig.longitude == null) {
            logger.error(
                    "Astro parameters geolocation could not be split into latitude and longitude, disabling thing '{}'",
                    thingUid);
            validConfig = false;
        }
        if (thingConfig.interval < 1 || thingConfig.interval > 86400) {
            logger.error("Astro parameter interval must be in the range of 1-86400, disabling thing '{}'", thingUid);
            validConfig = false;
        }

        if (validConfig) {
            logger.debug("{}", thingConfig);
            updateStatus(ONLINE);
            restartJobs();
        } else {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
        logger.debug("Thing {} initialized {}", getThing().getUID(), getThing().getStatus());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing thing {}", getThing().getUID());
        stopJobs();
        logger.debug("Thing {} disposed", getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (REFRESH == command) {
            logger.debug("Refreshing {}", channelUID);
            publishChannelIfLinked(channelUID);
        } else {
            logger.warn("The Astro-Binding is a read-only binding and can not handle commands");
        }
    }

    /**
     * Iterates all channels of the thing and updates their states.
     */
    public void publishPlanet() {
        Planet planet = getPlanet();
        if (planet == null) {
            return;
        }
        logger.debug("Publishing planet {} for thing {}", planet.getClass().getSimpleName(), getThing().getUID());
        for (Channel channel : getThing().getChannels()) {
            if (channel.getKind() != TRIGGER) {
                publishChannelIfLinked(channel.getUID());
            }
        }
    }

    /**
     * Publishes the channel with data if it's linked.
     */
    public void publishChannelIfLinked(ChannelUID channelUID) {
        Planet planet = getPlanet();
        if (isLinked(channelUID) && planet != null) {
            final Channel channel = getThing().getChannel(channelUID);
            if (channel == null) {
                logger.error("Cannot find channel for {}", channelUID);
                return;
            }
            if (channel.getKind() == TRIGGER) {
                // if the channel is a trigger channel, there is no state to publish
                return;
            }
            try {
                updateState(channelUID, getState(channel));
            } catch (IllegalArgumentException e) {
                logger.warn("Can't retrieve the state for channel '{}': {}", channelUID, e.getMessage());
                logger.trace("", e);
            }
        }
    }

    /**
     * Retrieve the channel state and convert it to an appropriate {@link State} instance.
     *
     * @param channel the {@link Channel} whose {@link State} to get.
     * @return The resulting channel {@link State}.
     * @throws IllegalArgumentException If the channel has a state of an unsupported type.
     */
    protected abstract State getState(Channel channel);

    /**
     * Schedules a positional and a daily job at midnight for Astro calculation and starts it immediately too. Removes
     * already scheduled jobs first.
     */
    private void restartJobs() {
        logger.debug("Restarting jobs for thing {}", getThing().getUID());
        monitor.lock();
        try {
            stopJobs();
            if (getThing().getStatus() == ONLINE) {
                TimeZone zone = TimeZone.getTimeZone(timeZoneProvider.getTimeZone());
                Locale locale = localeProvider.getLocale();
                // Daily Job
                Job runnable = getDailyJob(zone, locale);
                scheduledFutures.put(AstroBindingConstants.DAILY_JOB, cronScheduler.schedule(runnable, DAILY_MIDNIGHT));
                logger.debug("Scheduled daily '{}' job at midnight", getThing().getUID());
                // Execute daily startup job immediately
                runnable.run();

                // Repeat positional job every configured seconds
                // Use scheduleAtFixedRate to avoid time drift associated with scheduleWithFixedDelay
                linkedPositionalChannels = isPositionalChannelLinked();
                if (linkedPositionalChannels) {
                    Job positionalJob = new PositionalJob(this);
                    ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(positionalJob, 0, thingConfig.interval,
                            TimeUnit.SECONDS);
                    scheduledFutures.put(AstroBindingConstants.POSITIONAL_JOB, future);
                    logger.info("Scheduled {} every {} seconds", positionalJob, thingConfig.interval);
                }
            }
        } finally {
            monitor.unlock();
        }
    }

    /**
     * Stops all jobs for this thing.
     */
    private void stopJobs() {
        logger.debug("Stopping scheduled jobs for thing {}", getThing().getUID());
        monitor.lock();
        try {
            for (ScheduledFuture<?> future : scheduledFutures.values()) {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
            scheduledFutures.clear();
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        } finally {
            monitor.unlock();
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        linkedChannelChange(channelUID);
        publishChannelIfLinked(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        linkedChannelChange(channelUID);
    }

    /**
     * Counts positional channels and restarts Astro jobs.
     */
    private void linkedChannelChange(ChannelUID channelUID) {
        if (Arrays.asList(getPositionalChannelIds()).contains(channelUID.getId())) {
            boolean newValue = isPositionalChannelLinked();
            monitor.lock();
            try {
                boolean oldValue = linkedPositionalChannels;
                linkedPositionalChannels = newValue;
                if (oldValue != linkedPositionalChannels) {
                    restartJobs();
                }
            } finally {
                monitor.unlock();
            }
        }
    }

    /**
     * Returns {@code true}, if at least one positional channel is linked.
     */
    private boolean isPositionalChannelLinked() {
        List<String> positionalChannels = Arrays.asList(getPositionalChannelIds());
        for (Channel channel : getThing().getChannels()) {
            ChannelUID id = channel.getUID();
            if (isLinked(id) && positionalChannels.contains(id.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Emits an event for the given channel.
     */
    public void triggerEvent(String channelId, String event) {
        final Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.warn("Event {} in thing {} does not exist, please recreate the thing", event, getThing().getUID());
            return;
        }
        triggerChannel(channel.getUID(), event);
    }

    /**
     * Adds the provided {@link Job} to the queue (cannot be {@code null})
     */
    private void schedule(String identifier, Job job, long sleepTimeMs) {
        monitor.lock();
        try {
            tidyScheduledFutures();
            ScheduledFuture<?> future = scheduledFutures.get(identifier);
            if (future != null && !future.isDone()) {
                // The event is already scheduled
                long delay;
                if ((delay = future.getDelay(TimeUnit.MILLISECONDS)) < 10L && Math.abs(delay - sleepTimeMs) <= 20L) {
                    // if the previously scheduled event is about to run very soon and their scheduled are similar,
                    // we don't know if we can cancel it in time, so we let it run and don't schedule the new one.
                    return;
                }
                future.cancel(true);
            }
            future = scheduler.schedule(job, sleepTimeMs, TimeUnit.MILLISECONDS);
            scheduledFutures.put(identifier, future);
        } finally {
            monitor.unlock();
        }
    }

    /**
     * Adds the provided {@link Job} to the queue (cannot be {@code null})
     */
    public void schedule(String identifier, Job job, Calendar eventAt) {
        // We don't use instantSource here, because we always want to schedule relative to the system clock
        long sleepTime = eventAt.getTimeInMillis() - System.currentTimeMillis();
        if (sleepTime >= 0L) {
            schedule(identifier, job, sleepTime);
            if (logger.isDebugEnabled()) {
                final String formattedDate = this.loggerFormatter.format(eventAt.getTime());
                logger.debug("Scheduled {} in {}ms (at {})", job, sleepTime, formattedDate);
            }
        } else if (logger.isDebugEnabled()) {
            final String formattedDate = this.loggerFormatter.format(eventAt.getTime());
            logger.debug("Failed to schedule {} in {}ms (at {}) since it's in the past", job, sleepTime, formattedDate);
        }
    }

    public void schedule(String identifier, Job job, Instant eventAt) {
        // We don't use instantSource here, because we always want to schedule relative to the system clock
        long sleepTime = eventAt.toEpochMilli() + 1L - System.currentTimeMillis();
        if (sleepTime >= 0L) {
            schedule(identifier, job, sleepTime);
            if (logger.isDebugEnabled()) {
                logger.debug("Scheduled {} in {}ms (at {})", job, sleepTime, eventAt.atZone(ZoneId.systemDefault()));
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("Failed to schedule {} in {}ms (at {}) since it's in the past", job, sleepTime,
                    eventAt.atZone(ZoneId.systemDefault()));
        }
    }

    private void tidyScheduledFutures() {
        monitor.lock();
        try {
            ScheduledFuture<?> future;
            for (Iterator<ScheduledFuture<?>> iterator = scheduledFutures.values().iterator(); iterator.hasNext();) {
                future = iterator.next();
                if (future.isDone()) {
                    logger.trace("Tidying up done future {}", future);
                    iterator.remove();
                }
            }
        } finally {
            monitor.unlock();
        }
    }

    /**
     * Calculates and publishes the daily Astro data.
     */
    public void publishDailyInfo() {
        publishPositionalInfo();
    }

    /**
     * Calculates and publishes the interval Astro data.
     */
    public abstract void publishPositionalInfo();

    /**
     * Returns the {@link Planet} instance (cannot be {@code null})
     */
    public abstract @Nullable Planet getPlanet();

    /**
     * Returns the channelIds for positional calculation (cannot be {@code null})
     */
    protected abstract String[] getPositionalChannelIds();

    /**
     * Returns the daily calculation {@link Job} (cannot be {@code null})
     */
    protected abstract Job getDailyJob(TimeZone zone, Locale locale);

    public abstract Position getPositionAt(ZonedDateTime date);

    public State getAzimuth(ZonedDateTime date) {
        return getPositionAt(date).getAzimuth();
    }

    public State getElevation(ZonedDateTime date) {
        return getPositionAt(date).getElevation();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(AstroActions.class);
    }

    /**
     * Convert an untyped value into the appropriate {@link State} type for the specified {@link Channel}.
     *
     * @param value the {@link Object} to convert.
     * @param channel the {@link Channel} whose type to convert to.
     * @return The appropriate {@link State} instance.
     * @throws IllegalArgumentException If {@code value} is of an unsupported type.
     */
    protected State toState(@Nullable Object value, Channel channel) {
        if (value == null) {
            return UnDefType.UNDEF;
        } else if (value instanceof State state) {
            return state;
        } else if (value instanceof Calendar cal) {
            cal.setTimeZone(TimeZone.getTimeZone(timeZoneProvider.getTimeZone()));
            GregorianCalendar gregorianCal = (GregorianCalendar) DateTimeUtils.applyConfig(cal,
                    channel.getConfiguration().as(AstroChannelConfig.class));
            return new DateTimeType(gregorianCal.toInstant());
        } else if (value instanceof Instant instant) {
            return new DateTimeType(
                    DateTimeUtils.applyConfig(instant, channel.getConfiguration().as(AstroChannelConfig.class)));
        } else if (value instanceof Number) {
            BigDecimal decimalValue = new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP);
            return new DecimalType(decimalValue);
        } else if (value instanceof String || value instanceof Enum) {
            return new StringType(value.toString());
        } else {
            throw new IllegalArgumentException("Unsupported value type " + value.getClass().getSimpleName());
        }
    }
}
