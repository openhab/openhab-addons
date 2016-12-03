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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.openhab.binding.astro.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.AstroHandlerFactory;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.binding.astro.internal.util.PropertyUtils;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates and publishes the Sun data.
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

        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(thingUid);
        if (astroHandler != null) {
            astroHandler.publishDailyInfo();
            scheduleEvents(thingUid, astroHandler, astroHandler.getPlanet());
            logger.info("Scheduled astro EventJobs for thing {}", thingUid);
        }
    }

    /**
     * Schedules events for the current day.
     */
    private void scheduleEvents(String thingUid, AstroThingHandler astroHandler, Planet planet) {
        Map<String, String> planetEvents = planet instanceof Sun ? SUN_EVENTS : MOON_EVENTS;
        Calendar today = Calendar.getInstance();
        for (Entry<String, String> entry : planetEvents.entrySet()) {
            try {
                String channelId = entry.getKey();
                String event = entry.getValue();

                // special zodiac handling
                if (channelId.startsWith("zodiac#") && planet instanceof Sun) {
                    event = StringUtils.replace(event, "{}", ((Sun) planet).getZodiac().getSign().toString());
                }

                Calendar eventAt = (Calendar) PropertyUtils.getPropertyValue(
                        astroHandler.getThing().getChannel(channelId).getUID(), astroHandler.getPlanet());

                if (eventAt != null && DateTimeUtils.isSameDay(eventAt, today)
                        && DateTimeUtils.isTimeGreaterEquals(eventAt, today)) {
                    JobDataMap jobDataMap = new JobDataMap();
                    jobDataMap.put(KEY_THING_UID, thingUid);
                    jobDataMap.put(EventJob.KEY_EVENT, event);

                    String jobIdentity = EventJob.class.getSimpleName() + "-" + event;
                    Trigger trigger = newTrigger().withIdentity("eventTrigger-" + event, thingUid)
                            .startAt(eventAt.getTime()).build();
                    JobDetail jobDetail = newJob(EventJob.class).withIdentity(jobIdentity, thingUid)
                            .usingJobData(jobDataMap).build();
                    astroHandler.getScheduler().scheduleJob(jobDetail, trigger);
                    logger.debug("Scheduled astro {} for thing {} at {} for event {}", jobIdentity, thingUid,
                            DateFormatUtils.ISO_DATETIME_FORMAT.format(eventAt), event);
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
}
