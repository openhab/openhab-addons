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
package org.openhab.binding.publicholiday.internal;

import static org.openhab.binding.publicholiday.internal.PublicHolidayBindingConstants.CHANNEL_HOLIDAY_NAME;
import static org.openhab.binding.publicholiday.internal.PublicHolidayBindingConstants.CHANNEL_IS_DAY_BEFORE_PUBLIC_HOLIDAY;
import static org.openhab.binding.publicholiday.internal.PublicHolidayBindingConstants.CHANNEL_IS_PUBLIC_HOLIDAY;
import static org.openhab.core.thing.ThingStatus.ONLINE;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.publicholiday.internal.publicholiday.HolidayFactory;
import org.openhab.binding.publicholiday.internal.publicholiday.HolidayJob;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PublicHolidayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Güthle - Initial contribution
 */
@NonNullByDefault
public class PublicHolidayHandler extends BaseThingHandler {
    private static final Logger logger = LoggerFactory.getLogger(PublicHolidayHandler.class);

    private final CronScheduler scheduler;

    private @Nullable ScheduledCompletableFuture<Void> updateJob;

    private @Nullable PublicHolidayConfiguration config;

    private HolidayJob jobExecutor;

    public PublicHolidayHandler(Thing thing, CronScheduler scheduler) {
        super(thing);
        this.scheduler = scheduler;
        jobExecutor = new HolidayJob(this, List.of());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (CHANNEL_IS_PUBLIC_HOLIDAY.equals(channelUID.getId())) {
                jobExecutor.refreshValues(true, false);
            } else if (CHANNEL_IS_DAY_BEFORE_PUBLIC_HOLIDAY.equals(channelUID.getId())) {
                jobExecutor.refreshValues(false, true);
            } else if (CHANNEL_HOLIDAY_NAME.equals(channelUID.getId())) {
                jobExecutor.refreshValues(true, false);
            }
        }
    }

    @Override
    public void initialize() {
        synchronized (this) {
            config = getConfigAs(PublicHolidayConfiguration.class);
            jobExecutor = new HolidayJob(this, HolidayFactory.generateHolidayList(config));
        }
        restartJobs();
        updateStatus(ONLINE);
    }

    @Override
    public void dispose() {
        stopJobs();
    }

    public void updateValue(boolean isHoliday, boolean isNextDayHoliday, String holidayName) {
        Channel isHolidayChannel = this.thing.getChannel(PublicHolidayBindingConstants.CHANNEL_IS_PUBLIC_HOLIDAY);
        Channel isNextDayHolidayChannel = this.thing
                .getChannel(PublicHolidayBindingConstants.CHANNEL_IS_DAY_BEFORE_PUBLIC_HOLIDAY);

        if (isNextDayHolidayChannel != null) {
            updateState(isHolidayChannel.getUID(), OnOffType.from(isHoliday));
        }
        if (isNextDayHolidayChannel != null) {
            updateState(isNextDayHolidayChannel.getUID(), OnOffType.from(isNextDayHoliday));
        }
    }

    private void restartJobs() {
        stopJobs();
        startJobs();
    }

    private void startJobs() {
        if (config != null) {
            jobExecutor.refreshValues(true, true);
            synchronized (this) {
                updateJob = scheduler.schedule(jobExecutor, config.updateValueCron);
            }
        } else {
            // The initialization has not worked accordingly. Scheduling will be useless, too
            logger.warn("Initial refreshing of the values was not possible."
                    + "Further handling might not work as expected, too.");
        }
    }

    private synchronized void stopJobs() {
        synchronized (this) {
            if (updateJob != null) {
                updateJob.cancel(true);
                updateJob = null;
            }
        }
    }
}
