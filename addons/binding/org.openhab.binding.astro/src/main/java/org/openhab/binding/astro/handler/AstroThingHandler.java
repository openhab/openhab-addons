/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.handler;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.config.AstroThingConfig;
import org.openhab.binding.astro.internal.job.AbstractBaseJob;
import org.openhab.binding.astro.internal.job.AbstractDailyJob;
import org.openhab.binding.astro.internal.job.PositionalJob;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.util.PropertyUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base ThingHandler for all Astro handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public abstract class AstroThingHandler extends BaseThingHandler {
    private static final Logger logger = LoggerFactory.getLogger(AstroThingHandler.class);
    private Scheduler quartzScheduler;
    private ScheduledFuture<?> schedulerFuture;
    private int linkedPositionalChannels = 0;
    protected AstroThingConfig thingConfig;
    private Object schedulerLock = new Object();

    public AstroThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     */
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
            logger.debug(thingConfig.toString());
            updateStatus(ThingStatus.ONLINE);
            restartJobs();
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
        logger.debug("Thing {} initialized {}", getThing().getUID(), getThing().getStatus());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        logger.debug("Disposing thing {}", getThing().getUID());
        if (schedulerFuture != null && !schedulerFuture.isCancelled()) {
            schedulerFuture.cancel(true);
            schedulerFuture = null;
        }
        stopJobs();
        quartzScheduler = null;
        logger.debug("Thing {} disposed", getThing().getUID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing {}", channelUID);
            publishChannelIfLinked(channelUID);
        } else {
            logger.warn("The Astro-Binding is a read-only binding and can not handle commands");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        logger.warn("The Astro-Binding is a read-only binding and can not handle channel updates");
        super.handleUpdate(channelUID, newState);
    }

    /**
     * Iterates all channels of the thing and updates their states.
     */
    public void publishPlanet() {
        logger.debug("Publishing planet {} for thing {}", getPlanet().getClass().getSimpleName(), getThing().getUID());
        for (Channel channel : getThing().getChannels()) {
            if (channel.getKind() != ChannelKind.TRIGGER) {
                publishChannelIfLinked(channel.getUID());
            }
        }
    }

    /**
     * Publishes the channel with data if it's linked.
     */
    public void publishChannelIfLinked(ChannelUID channelUID) {
        if (isLinked(channelUID.getId()) && getPlanet() != null) {
            try {
                AstroChannelConfig config = getThing().getChannel(channelUID.getId()).getConfiguration()
                        .as(AstroChannelConfig.class);
                updateState(channelUID, PropertyUtils.getState(channelUID, config, getPlanet()));
            } catch (Exception ex) {
                logger.error("Can't update state for channel " + channelUID + ": " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Schedules a positional and a daily job at midnight for astro calculation and starts it immediately too. Removes
     * already scheduled jobs first.
     */
    private void restartJobs() {
        logger.debug("Restarting jobs for thing {}", getThing().getUID());

        if (schedulerFuture != null && !schedulerFuture.isCancelled()) {
            schedulerFuture.cancel(true);
        }

        schedulerFuture = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                stopJobs();

                try {
                    synchronized (schedulerLock) {
                        if (quartzScheduler == null) {
                            quartzScheduler = StdSchedulerFactory.getDefaultScheduler();
                        }

                        if (getThing().getStatus() == ThingStatus.ONLINE) {
                            String thingUid = getThing().getUID().toString();
                            String typeId = getThing().getThingTypeUID().getId();
                            JobDataMap jobDataMap = new JobDataMap();
                            jobDataMap.put(AbstractBaseJob.KEY_THING_UID, thingUid);
                            jobDataMap.put(AbstractBaseJob.KEY_JOB_NAME, "job-daily");

                            // dailyJob
                            Trigger trigger = newTrigger().withIdentity("trigger-daily-" + typeId, thingUid)
                                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?")).build();
                            JobDetail jobDetail = newJob(getDailyJobClass())
                                    .withIdentity("job-daily-" + typeId, thingUid).usingJobData(jobDataMap).build();
                            quartzScheduler.scheduleJob(jobDetail, trigger);
                            logger.info("Scheduled astro job-daily-{} at midnight for thing {}", typeId, thingUid);

                            // startupJob
                            trigger = newTrigger().withIdentity("trigger-daily-startup-" + typeId, thingUid).startNow()
                                    .build();
                            jobDetail = newJob(getDailyJobClass()).withIdentity("job-daily-startup-" + typeId, thingUid)
                                    .usingJobData(jobDataMap).build();
                            quartzScheduler.scheduleJob(jobDetail, trigger);

                            if (isPositionalChannelLinked()) {
                                // positional intervalJob
                                jobDataMap = new JobDataMap();
                                jobDataMap.put(AbstractBaseJob.KEY_THING_UID, thingUid);
                                jobDataMap.put(AbstractBaseJob.KEY_JOB_NAME, "job-positional");

                                Date start = new Date(System.currentTimeMillis() + (thingConfig.getInterval()) * 1000);
                                trigger = newTrigger().withIdentity("trigger-positional", thingUid).startAt(start)
                                        .withSchedule(simpleSchedule().repeatForever()
                                                .withIntervalInSeconds(thingConfig.getInterval()))
                                        .build();
                                jobDetail = newJob(PositionalJob.class).withIdentity("job-positional", thingUid)
                                        .usingJobData(jobDataMap).build();
                                quartzScheduler.scheduleJob(jobDetail, trigger);
                                logger.info("Scheduled astro job-positional with interval of {} seconds for thing {}",
                                        thingConfig.getInterval(), thingUid);
                            }
                        }
                    }
                } catch (SchedulerException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }, 2000, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops all jobs for this thing.
     */
    private void stopJobs() {
        logger.debug("Stopping jobs for thing {}", getThing().getUID());
        synchronized (schedulerLock) {
            if (quartzScheduler != null) {
                try {
                    String thingUid = getThing().getUID().toString();
                    for (JobKey jobKey : quartzScheduler.getJobKeys(jobGroupEquals(thingUid))) {
                        logger.debug("Deleting astro {} for thing '{}'", jobKey.getName(), thingUid);
                        quartzScheduler.deleteJob(jobKey);
                    }
                } catch (SchedulerException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void channelLinked(ChannelUID channelUID) {
        linkedChannelChange(channelUID, 1);
        publishChannelIfLinked(channelUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        linkedChannelChange(channelUID, -1);
    }

    /**
     * Counts positional channels and restarts astro jobs.
     */
    private void linkedChannelChange(ChannelUID channelUID, int step) {
        if (ArrayUtils.contains(getPositionalChannelIds(), channelUID.getId())) {
            int oldValue = linkedPositionalChannels;
            linkedPositionalChannels += step;
            if ((oldValue == 0 && linkedPositionalChannels > 0) || (oldValue > 0 && linkedPositionalChannels == 0)) {
                restartJobs();
            }
        }
    }

    /**
     * Returns true, if at least one positional channel is linked.
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
        if (getThing().getChannel(channelId) != null) {
            triggerChannel(getThing().getChannel(channelId).getUID(), event);
        } else {
            logger.warn("Event {} in thing {} does not exist, please recreate the thing", event, getThing().getUID());
        }
    }

    /**
     * Returns the scheduler for the astro jobs.
     */
    public Scheduler getScheduler() {
        return quartzScheduler;
    }

    /**
     * Calculates and publishes the daily astro data.
     */
    public abstract void publishDailyInfo();

    /**
     * Calculates and publishes the interval astro data.
     */
    public abstract void publishPositionalInfo();

    /**
     * Returns the planet.
     */
    public abstract Planet getPlanet();

    /**
     * Returns the channelIds for positional calculation.
     */
    protected abstract String[] getPositionalChannelIds();

    /**
     * Returns the class for the daily calculation job.
     */
    protected abstract Class<? extends AbstractDailyJob> getDailyJobClass();
}
