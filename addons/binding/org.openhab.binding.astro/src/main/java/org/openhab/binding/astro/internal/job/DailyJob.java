/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.job;

import static org.openhab.binding.astro.AstroBindingConstants.*;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Calendar;

import org.apache.commons.lang.time.DateFormatUtils;
import org.openhab.binding.astro.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.AstroHandlerFactory;
import org.openhab.binding.astro.internal.model.Eclipse;
import org.openhab.binding.astro.internal.model.Moon;
import org.openhab.binding.astro.internal.model.MoonPhase;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunEclipse;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates and publishes the planet data and also schedules the events for the current day.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DailyJob extends AbstractBaseJob {
    private static final Logger logger = LoggerFactory.getLogger(DailyJob.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeJob(String thingUid, JobDataMap jobDataMap) {
        AstroThingHandler handler = AstroHandlerFactory.getHandler(thingUid);
        if (handler != null) {
            handler.publishDailyInfo();

            Planet planet = handler.getPlanet();
            if (planet instanceof Moon) {
                // schedule moon events
                Moon moon = (Moon) planet;
                scheduleEvent(thingUid, handler, moon.getRise().getStart(), EVENT_START, EVENT_CHANNEL_ID_RISE);
                scheduleEvent(thingUid, handler, moon.getSet().getEnd(), EVENT_END, EVENT_CHANNEL_ID_SET);

                MoonPhase moonPhase = moon.getPhase();
                scheduleEvent(thingUid, handler, moonPhase.getFirstQuarter(), EVENT_PHASE_FIRST_QUARTER,
                        EVENT_CHANNEL_ID_MOON_PHASE);
                scheduleEvent(thingUid, handler, moonPhase.getThirdQuarter(), EVENT_PHASE_THIRD_QUARTER,
                        EVENT_CHANNEL_ID_MOON_PHASE);
                scheduleEvent(thingUid, handler, moonPhase.getFull(), EVENT_PHASE_FULL, EVENT_CHANNEL_ID_MOON_PHASE);
                scheduleEvent(thingUid, handler, moonPhase.getNew(), EVENT_PHASE_NEW, EVENT_CHANNEL_ID_MOON_PHASE);

                Eclipse eclipse = moon.getEclipse();
                scheduleEvent(thingUid, handler, eclipse.getPartial(), EVENT_ECLIPSE_PARTIAL, EVENT_CHANNEL_ID_ECLIPSE);
                scheduleEvent(thingUid, handler, eclipse.getTotal(), EVENT_ECLIPSE_TOTAL, EVENT_CHANNEL_ID_ECLIPSE);

                scheduleEvent(thingUid, handler, moon.getPerigee().getDate(), EVENT_PERIGEE, EVENT_CHANNEL_ID_PERIGEE);
                scheduleEvent(thingUid, handler, moon.getApogee().getDate(), EVENT_APOGEE, EVENT_CHANNEL_ID_APOGEE);
            } else {
                // schedule sun events
                Sun sun = (Sun) planet;
                scheduleRange(thingUid, handler, sun.getRise(), EVENT_CHANNEL_ID_RISE);
                scheduleRange(thingUid, handler, sun.getSet(), EVENT_CHANNEL_ID_SET);
                scheduleRange(thingUid, handler, sun.getNoon(), EVENT_CHANNEL_ID_NOON);
                scheduleRange(thingUid, handler, sun.getNight(), EVENT_CHANNEL_ID_NIGHT);
                scheduleRange(thingUid, handler, sun.getMorningNight(), EVENT_CHANNEL_ID_MORNING_NIGHT);
                scheduleRange(thingUid, handler, sun.getAstroDawn(), EVENT_CHANNEL_ID_ASTRO_DAWN);
                scheduleRange(thingUid, handler, sun.getNauticDawn(), EVENT_CHANNEL_ID_NAUTIC_DAWN);
                scheduleRange(thingUid, handler, sun.getCivilDawn(), EVENT_CHANNEL_ID_CIVIL_DAWN);
                scheduleRange(thingUid, handler, sun.getAstroDusk(), EVENT_CHANNEL_ID_ASTRO_DUSK);
                scheduleRange(thingUid, handler, sun.getNauticDusk(), EVENT_CHANNEL_ID_NAUTIC_DUSK);
                scheduleRange(thingUid, handler, sun.getCivilDusk(), EVENT_CHANNEL_ID_CIVIL_DUSK);
                scheduleRange(thingUid, handler, sun.getEveningNight(), EVENT_CHANNEL_ID_EVENING_NIGHT);
                scheduleRange(thingUid, handler, sun.getDaylight(), EVENT_CHANNEL_ID_DAYLIGHT);

                SunEclipse eclipse = sun.getEclipse();
                scheduleEvent(thingUid, handler, eclipse.getPartial(), EVENT_ECLIPSE_PARTIAL, EVENT_CHANNEL_ID_ECLIPSE);
                scheduleEvent(thingUid, handler, eclipse.getTotal(), EVENT_ECLIPSE_TOTAL, EVENT_CHANNEL_ID_ECLIPSE);
                scheduleEvent(thingUid, handler, eclipse.getRing(), EVENT_ECLIPSE_RING, EVENT_CHANNEL_ID_ECLIPSE);

                // schedule republish jobs
                schedulePublishPlanet(thingUid, handler, "zodiac", sun.getZodiac().getEnd());
                schedulePublishPlanet(thingUid, handler, "season", sun.getSeason().getNextSeason());
            }

            logger.info("Scheduled astro event-jobs for thing {}", thingUid);
        }
    }

    private void scheduleRange(String thingUid, AstroThingHandler astroHandler, Range range, String channelId) {
        scheduleEvent(thingUid, astroHandler, range.getStart(), EVENT_START, channelId);
        scheduleEvent(thingUid, astroHandler, range.getEnd(), EVENT_END, channelId);
    }

    private void scheduleEvent(String thingUid, AstroThingHandler astroHandler, Calendar eventAt, String event,
            String channelId) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(KEY_THING_UID, thingUid);
        jobDataMap.put(EventJob.KEY_EVENT, event);
        jobDataMap.put(KEY_CHANNEL_ID, channelId);

        schedule(astroHandler, EventJob.class, jobDataMap, "event-" + event.toLowerCase() + "-" + channelId, eventAt);
    }

    private void schedulePublishPlanet(String thingUid, AstroThingHandler astroHandler, String jobKey,
            Calendar eventAt) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(KEY_THING_UID, thingUid);

        schedule(astroHandler, PublishPlanetJob.class, jobDataMap, "publish-" + jobKey, eventAt);
    }

    private void schedule(AstroThingHandler astroHandler, Class<? extends AbstractBaseJob> clazz, JobDataMap jobDataMap,
            String jobKey, Calendar eventAt) {
        try {
            Calendar today = Calendar.getInstance();
            if (eventAt != null && DateTimeUtils.isSameDay(eventAt, today)
                    && DateTimeUtils.isTimeGreaterEquals(eventAt, today)) {
                jobDataMap.put(KEY_JOB_NAME, "job-" + jobKey);
                String thingUid = jobDataMap.getString(KEY_THING_UID);
                Trigger trigger = newTrigger().withIdentity("trigger-" + jobKey, thingUid).startAt(eventAt.getTime())
                        .build();
                JobDetail jobDetail = newJob(clazz).withIdentity("job-" + jobKey, thingUid).usingJobData(jobDataMap)
                        .build();
                astroHandler.getScheduler().scheduleJob(jobDetail, trigger);
                logger.debug("Scheduled astro job-{} for thing {} at {}", jobKey, thingUid,
                        DateFormatUtils.ISO_DATETIME_FORMAT.format(eventAt));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
