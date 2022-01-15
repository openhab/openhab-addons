/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.measure.quantity.Angle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.action.AstroActions;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.config.AstroThingConfig;
import org.openhab.binding.astro.internal.job.Job;
import org.openhab.binding.astro.internal.job.PositionalJob;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Position;
import org.openhab.binding.astro.internal.util.PropertyUtils;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
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
    private final SimpleDateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /** Scheduler to schedule jobs */
    private final CronScheduler cronScheduler;

    protected final TimeZoneProvider timeZoneProvider;

    private final Lock monitor = new ReentrantLock();

    private final Set<ScheduledFuture<?>> scheduledFutures = new HashSet<>();

    private boolean linkedPositionalChannels;

    protected AstroThingConfig thingConfig = new AstroThingConfig();

    private @Nullable ScheduledCompletableFuture<?> dailyJob;

    public AstroThingHandler(Thing thing, final CronScheduler scheduler, final TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.cronScheduler = scheduler;
        this.timeZoneProvider = timeZoneProvider;
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
        if (isLinked(channelUID.getId()) && planet != null) {
            final Channel channel = getThing().getChannel(channelUID.getId());
            if (channel == null) {
                logger.error("Cannot find channel for {}", channelUID);
                return;
            }
            try {
                AstroChannelConfig config = channel.getConfiguration().as(AstroChannelConfig.class);
                updateState(channelUID,
                        PropertyUtils.getState(channelUID, config, planet, timeZoneProvider.getTimeZone()));
            } catch (Exception ex) {
                logger.error("Can't update state for channel {} : {}", channelUID, ex.getMessage(), ex);
            }
        }
    }

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
                String thingUID = getThing().getUID().toString();
                // Daily Job
                Job runnable = getDailyJob();
                dailyJob = cronScheduler.schedule(runnable, DAILY_MIDNIGHT);
                logger.debug("Scheduled {} at midnight", dailyJob);
                // Execute daily startup job immediately
                runnable.run();

                // Repeat positional job every configured seconds
                // Use scheduleAtFixedRate to avoid time drift associated with scheduleWithFixedDelay
                linkedPositionalChannels = isPositionalChannelLinked();
                if (linkedPositionalChannels) {
                    Job positionalJob = new PositionalJob(thingUID);
                    ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(positionalJob, 0, thingConfig.interval,
                            TimeUnit.SECONDS);
                    scheduledFutures.add(future);
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
            ScheduledCompletableFuture<?> job = dailyJob;
            if (job != null) {
                job.cancel(true);
            }
            dailyJob = null;
            for (ScheduledFuture<?> future : scheduledFutures) {
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
            boolean oldValue = linkedPositionalChannels;
            linkedPositionalChannels = isPositionalChannelLinked();
            if (oldValue != linkedPositionalChannels) {
                restartJobs();
            }
        }
    }

    /**
     * Returns {@code true}, if at least one positional channel is linked.
     */
    private boolean isPositionalChannelLinked() {
        List<String> positionalChannels = Arrays.asList(getPositionalChannelIds());
        for (Channel channel : getThing().getChannels()) {
            String id = channel.getUID().getId();
            if (isLinked(id) && positionalChannels.contains(id)) {
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
     *
     * @return {@code true} if the {@code job} is added to the queue, otherwise {@code false}
     */
    public void schedule(Job job, Calendar eventAt) {
        long sleepTime;
        monitor.lock();
        try {
            tidyScheduledFutures();
            sleepTime = eventAt.getTimeInMillis() - new Date().getTime();
            ScheduledFuture<?> future = scheduler.schedule(job, sleepTime, TimeUnit.MILLISECONDS);
            scheduledFutures.add(future);
        } finally {
            monitor.unlock();
        }
        if (logger.isDebugEnabled()) {
            String formattedDate = this.isoFormatter.format(eventAt);
            logger.debug("Scheduled {} in {}ms (at {})", job, sleepTime, formattedDate);
        }
    }

    private void tidyScheduledFutures() {
        for (Iterator<ScheduledFuture<?>> iterator = scheduledFutures.iterator(); iterator.hasNext();) {
            ScheduledFuture<?> future = iterator.next();
            if (future.isDone()) {
                logger.trace("Tidying up done future {}", future);
                iterator.remove();
            }
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
    protected abstract Job getDailyJob();

    public abstract @Nullable Position getPositionAt(ZonedDateTime date);

    public @Nullable QuantityType<Angle> getAzimuth(ZonedDateTime date) {
        Position position = getPositionAt(date);
        return position != null ? position.getAzimuth() : null;
    }

    public @Nullable QuantityType<Angle> getElevation(ZonedDateTime date) {
        Position position = getPositionAt(date);
        return position != null ? position.getElevation() : null;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(AstroActions.class);
    }
}
