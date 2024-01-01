/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.automation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Map;

import org.openhab.core.automation.ModuleHandlerCallback;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.BaseTriggerModuleHandler;
import org.openhab.core.automation.handler.TriggerHandlerCallback;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.scheduler.Scheduler;
import org.openhab.core.scheduler.SchedulerRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the core provided time related module type by an absolute day/time trigger.
 * <p>
 * It allows to set a date and a time as separate configuration values (easier to manipulate from
 * other actions / rules etc) and also allows the user to setup a random factor
 * (presence simulation).
 *
 * @author David Graeff - Initial contribution
 */
public class AbsoluteDateTimeTriggerHandler extends BaseTriggerModuleHandler implements SchedulerRunnable {

    private final Logger logger = LoggerFactory.getLogger(AbsoluteDateTimeTriggerHandler.class);

    public static final String MODULE_TYPE_ID = "timer.AbsoluteDateTimeTrigger";
    public static final String CALLBACK_CONTEXT_NAME = "CALLBACK";
    public static final String MODULE_CONTEXT_NAME = "MODULE";

    public static final String CFG_DATE = "date";
    public static final String CFG_TIME = "time";
    public static final String CFG_TIME_RND = "randomizeTime";

    private final Scheduler scheduler;
    private final Instant dateTime;
    private ScheduledCompletableFuture<?> schedule;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATETIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;
    private final DateTimeFormatter dateTimeformatter;

    public AbsoluteDateTimeTriggerHandler(Trigger module, Scheduler scheduler) {
        super(module);
        this.scheduler = scheduler;
        dateTimeformatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT);

        // Take optional date into account
        String cfgDate = (String) module.getConfiguration().get(CFG_DATE);
        LocalDateTime dateTime = cfgDate == null || cfgDate.isEmpty() ? LocalDate.now().atStartOfDay()
                : LocalDateTime.from(dateFormatter.parse(cfgDate));

        // Take optional time into account
        String cfgDTime = (String) module.getConfiguration().get(CFG_TIME);
        if (cfgDTime != null && !cfgDTime.isEmpty()) {
            TemporalAccessor temporalAccessor = timeFormatter.parse(cfgDTime);
            dateTime.plusHours(temporalAccessor.getLong(ChronoField.HOUR_OF_DAY));
            dateTime.plusMinutes(temporalAccessor.getLong(ChronoField.MINUTE_OF_HOUR));
        }

        this.dateTime = dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    @Override
    public synchronized void setCallback(ModuleHandlerCallback callback) {
        super.setCallback(callback);
        scheduleJob();
    }

    private void scheduleJob() {
        schedule = scheduler.at(this, dateTime);
        logger.debug("Scheduled absolute date/time '{}' for trigger '{}'.", dateTimeformatter.format(dateTime),
                module.getId());
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        if (schedule != null) {
            schedule.cancel(true);
            logger.debug("cancelled job for trigger '{}'.", module.getId());
        }
    }

    @Override
    public void run() {
        ((TriggerHandlerCallback) callback).triggered(module, Map.of());
        schedule = null;
    }
}
