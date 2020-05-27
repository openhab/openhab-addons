/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.eclipse.smarthome.core.thing.ThingStatus.*;
import static org.eclipse.smarthome.core.thing.type.ChannelKind.TRIGGER;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;

import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.eclipse.smarthome.core.scheduler.CronScheduler;
import org.eclipse.smarthome.core.scheduler.ScheduledCompletableFuture;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.config.AstroThingConfig;
import org.openhab.binding.astro.internal.job.Job;
import org.openhab.binding.astro.internal.job.PositionalJob;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.util.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base ThingHandler for all Astro handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
public abstract class AstroThingHandler extends BaseThingHandler {

    private static final String DAILY_MIDNIGHT = "30 0 0 * * ? *";

    /** Logger Instance */
    protected final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /** Scheduler to schedule jobs */
    private final CronScheduler cronScheduler;

    private int linkedPositionalChannels = 0;
    protected AstroThingConfig thingConfig;
    private final Lock monitor = new ReentrantLock();

    private ScheduledCompletableFuture<?> dailyJob;
    private final Set<ScheduledFuture<?>> scheduledFutures = new HashSet<>();

    public AstroThingHandler(Thing thing, CronScheduler scheduler) {
        super(thing);
        this.cronScheduler = scheduler;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());
        String thingUid = getThing().getUID().toString();
        thingConfig = getConfigAs(AstroThingConfig.class);
        thingConfig.setThingUid(thingUid);
        boolean validConfig = true;

        if (StringUtils.trimToNull(thingConfig.getGeolocation()) == null) {
            logger.error("Astro parameter geolocation is mandatory and must be configured, disabling thing '{}'",
                    thingUid);
            validConfig = false;
        } else {
            thingConfig.parseGeoLocation();
        }

        if (thingConfig.getLatitude() == null || thingConfig.getLongitude() == null) {
            logger.error(
                    "Astro parameters geolocation could not be split into latitude and longitude, disabling thing '{}'",
                    thingUid);
            validConfig = false;
        }
        if (thingConfig.getInterval() == null || thingConfig.getInterval() < 1 || thingConfig.getInterval() > 86400) {
            logger.error("Astro parameter interval must be in the range of 1-86400, disabling thing '{}'", thingUid);
            validConfig = false;
        }

        if (validConfig) {
            logger.debug("{}", thingConfig);
            updateStatus(ONLINE);
            restartJobs();
        } else {
            updateStatus(OFFLINE);
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
        logger.debug("Publishing planet {} for thing {}", getPlanet().getClass().getSimpleName(), getThing().getUID());
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
        if (isLinked(channelUID.getId()) && getPlanet() != null) {
            final Channel channel = getThing().getChannel(channelUID.getId());
            if (channel == null) {
                logger.error("Cannot find channel for {}", channelUID);
                return;
            }
            try {
                AstroChannelConfig config = channel.getConfiguration().as(AstroChannelConfig.class);
                updateState(channelUID, PropertyUtils.getState(channelUID, config, getPlanet()));
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
                if (cronScheduler == null) {
                    logger.warn("Thread Pool Executor is not available");
                    return;
                }
                // Daily Job
                Job runnable = getDailyJob();
                dailyJob = cronScheduler.schedule(runnable, DAILY_MIDNIGHT);
                logger.debug("Scheduled {} at midnight", dailyJob);
                // Execute daily startup job immediately
                runnable.run();

                // Repeat positional job every configured seconds
                // Use scheduleAtFixedRate to avoid time drift associated with scheduleWithFixedDelay
                if (isPositionalChannelLinked()) {
                    Job positionalJob = new PositionalJob(thingUID);
                    ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(positionalJob, 0,
                            thingConfig.getInterval(), TimeUnit.SECONDS);
                    scheduledFutures.add(future);
                    logger.info("Scheduled {} every {} seconds", positionalJob, thingConfig.getInterval());
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
            if (cronScheduler != null) {
                if (dailyJob != null) {
                    dailyJob.cancel(true);
                }
                dailyJob = null;
            }
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
        linkedChannelChange(channelUID, 1);
        publishChannelIfLinked(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        linkedChannelChange(channelUID, -1);
    }

    /**
     * Counts positional channels and restarts Astro jobs.
     */
    private void linkedChannelChange(ChannelUID channelUID, int step) {
        if (ArrayUtils.contains(getPositionalChannelIds(), channelUID.getId())) {
            int oldValue = linkedPositionalChannels;
            linkedPositionalChannels += step;
            if (oldValue == 0 && linkedPositionalChannels > 0 || oldValue > 0 && linkedPositionalChannels == 0) {
                restartJobs();
            }
        }
    }

    /**
     * Returns {@code true}, if at least one positional channel is linked.
     */
    private boolean isPositionalChannelLinked() {
        for (Channel channel : getThing().getChannels()) {
            if (ArrayUtils.contains(getPositionalChannelIds(), channel.getUID().getId())
                    && isLinked(channel.getUID().getId())) {
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
            String formattedDate = DateFormatUtils.ISO_DATETIME_FORMAT.format(eventAt);
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
    public abstract void publishDailyInfo();

    /**
     * Calculates and publishes the interval Astro data.
     */
    public abstract void publishPositionalInfo();

    /**
     * Returns the {@link Planet} instance (cannot be {@code null})
     */
    public abstract Planet getPlanet();

    /**
     * Returns the channelIds for positional calculation (cannot be {@code null})
     */
    protected abstract String[] getPositionalChannelIds();

    /**
     * Returns the daily calculation {@link Job} (cannot be {@code null})
     */
    protected abstract Job getDailyJob();
}
