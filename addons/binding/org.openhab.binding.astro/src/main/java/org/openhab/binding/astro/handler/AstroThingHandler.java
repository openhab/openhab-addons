/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import java.util.TimerTask;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.astro.internal.config.AstroThingConfig;
import org.openhab.binding.astro.internal.job.DailyJob;
import org.openhab.binding.astro.internal.job.PositionalJob;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.util.DelayedExecutor;
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
    private DelayedExecutor delayedExecutor = new DelayedExecutor();
    private int linkedPositionalChannels = 0;
    protected AstroThingConfig thingConfig;

    public AstroThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        String thingUid = getThing().getUID().toString();
        thingConfig = getConfigAs(AstroThingConfig.class);
        thingConfig.setThingUid(thingUid);
        boolean validConfig = true;

        if (StringUtils.trimToNull(thingConfig.getGeolocation()) == null) {
            logger.error("Astro parameter geolocation is mandatory and must be configured, disabling thing '{}'", thingUid);
            validConfig = false;
        } else {
            thingConfig.parseGeoLocation();
        }

        if (thingConfig.getLatitude() == null || thingConfig.getLongitude() == null) {
            logger.error("Astro parameters geolocation could not be split into latitude and longitude, disabling thing '{}'", thingUid);
            validConfig = false;
        }
        if (thingConfig.getInterval() == null || thingConfig.getInterval() < 1 || thingConfig.getInterval() > 86400) {
            logger.error("Astro parameter interval must be in the range of 1-86400, disabling thing '{}'", thingUid);
            validConfig = false;
        }

        if (validConfig) {
            logger.debug(thingConfig.toString());
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        stopJobs();
        quartzScheduler = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing {}", channelUID);
            publishChannelIfLinked(getThing().getChannel(channelUID.getId()));
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
    protected void publishPlanet() {
        logger.debug("Publishing planet {} for thing {}", getPlanet().getClass().getSimpleName(), getThing().getUID());
        for (Channel channel : getThing().getChannels()) {
            publishChannelIfLinked(channel);
        }
    }

    /**
     * Publishes the channel with data if it's linked.
     */
    private void publishChannelIfLinked(Channel channel) {
        if (channel.isLinked()) {
            try {
                updateState(channel.getUID(), PropertyUtils.getState(channel.getUID(), getPlanet()));
            } catch (Exception ex) {
                logger.error("Can't update state for channel " + channel.getUID() + ": " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Schedules a positional and a daily job at midnight for astro calculation and starts it immediately too. Removes
     * already scheduled jobs first.
     */
    private void restartJobs() {
        delayedExecutor.cancel();
        delayedExecutor.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    if (quartzScheduler == null) {
                        quartzScheduler = StdSchedulerFactory.getDefaultScheduler();
                    }

                    stopJobs();

                    if (getThing().getStatus() == ThingStatus.ONLINE) {
                        String thingUid = getThing().getUID().toString();
                        JobDataMap jobDataMap = new JobDataMap();
                        jobDataMap.put("thingUid", thingUid);

                        // dailyJob
                        String jobIdentity = DailyJob.class.getSimpleName();
                        Trigger trigger = newTrigger().withIdentity("dailyJobTrigger", thingUid)
                                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?")).build();
                        JobDetail jobDetail = newJob(DailyJob.class).withIdentity(jobIdentity, thingUid).usingJobData(jobDataMap).build();
                        quartzScheduler.scheduleJob(jobDetail, trigger);
                        logger.info("Scheduled astro {} at midnight for thing {}", jobIdentity, thingUid);

                        // startupJob
                        trigger = newTrigger().withIdentity("dailyJobStartupTrigger", thingUid).startNow().build();
                        jobDetail = newJob(DailyJob.class).withIdentity("dailyJobStartup", thingUid).usingJobData(jobDataMap).build();
                        quartzScheduler.scheduleJob(jobDetail, trigger);

                        if (linkedPositionalChannels > 0) {
                            // positional intervalJob
                            jobIdentity = PositionalJob.class.getSimpleName();
                            Date start = new Date(System.currentTimeMillis() + (thingConfig.getInterval()) * 1000);
                            trigger = newTrigger().withIdentity("positionalJobTrigger", thingUid).startAt(start)
                                    .withSchedule(simpleSchedule().repeatForever().withIntervalInSeconds(thingConfig.getInterval())).build();
                            jobDetail = newJob(PositionalJob.class).withIdentity(jobIdentity, thingUid).usingJobData(jobDataMap).build();
                            quartzScheduler.scheduleJob(jobDetail, trigger);
                            logger.info("Scheduled astro {} with interval of {} seconds for thing {}", jobIdentity, thingConfig.getInterval(),
                                    thingUid);
                        }
                    }
                } catch (SchedulerException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }, 2000);
    }

    private void stopJobs() {
        if (quartzScheduler != null) {
            try {
                String thingUid = getThing().getUID().toString();
                for (JobKey jobKey : quartzScheduler.getJobKeys(jobGroupEquals(thingUid))) {
                    logger.info("Deleting astro {} for thing '{}'", jobKey.getName(), thingUid);
                    quartzScheduler.deleteJob(jobKey);
                }
            } catch (SchedulerException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        linkedChannelChange(channelUID, 1);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        linkedChannelChange(channelUID, -1);
    }

    /**
     * Counts positional channels and restarts astro jobs.
     */
    private void linkedChannelChange(ChannelUID channelUID, int step) {
        if (ArrayUtils.contains(getPositionalChannelIds(), channelUID.getId())) {
            linkedPositionalChannels += step;
        }
        restartJobs();
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
}
