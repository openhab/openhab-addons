/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.timer.handler;

import static org.openhab.binding.timer.TimerBindingConstants.*;

import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TimerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Neil Renaud - Initial contribution
 */
public class TimerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TimerHandler.class);

    private int onTimeHours = 0;
    private int onTimeMinutes = 0;
    private int onTimeSeconds = 0;
    private int offTimeHours = 0;
    private int offTimeMinutes = 0;
    private int offTimeSeconds = 0;
    private boolean enabled = false;
    private boolean runsOnMon = false;
    private boolean runsOnTue = false;
    private boolean runsOnWed = false;
    private boolean runsOnThur = false;
    private boolean runsOnFri = false;
    private boolean runsOnSat = false;
    private boolean runsOnSun = false;

    private OnCallable onCallable = null;
    private OffCallable offCallable = null;

    public TimerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_ON_TIME_HOURS)) {
            if (command instanceof RefreshType) {
                updateState(CHANNEL_OFF_TIME_HOURS, new DecimalType(onTimeHours));
            }
            if (command instanceof DecimalType) {
                onTimeHours = ((DecimalType) command).intValue();
            }
        }
        if (channelUID.getId().equals(CHANNEL_ON_TIME_MINUTES)) {
            if (command instanceof DecimalType) {
                onTimeMinutes = ((DecimalType) command).intValue();
            }
        }
        if (channelUID.getId().equals(CHANNEL_ON_TIME_SECONDS)) {
            if (command instanceof DecimalType) {
                onTimeSeconds = ((DecimalType) command).intValue();
            }
        }
        if (channelUID.getId().equals(CHANNEL_OFF_TIME_HOURS)) {
            if (command instanceof DecimalType) {
                offTimeHours = ((DecimalType) command).intValue();
            }
        }
        if (channelUID.getId().equals(CHANNEL_OFF_TIME_MINUTES)) {
            if (command instanceof DecimalType) {
                offTimeMinutes = ((DecimalType) command).intValue();
            }
        }
        if (channelUID.getId().equals(CHANNEL_OFF_TIME_SECONDS)) {
            if (command instanceof DecimalType) {
                offTimeSeconds = ((DecimalType) command).intValue();
            }
        }
        if (channelUID.getId().equals(CHANNEL_ENABLED)) {
            if (command instanceof OnOffType) {
                enabled = ((OnOffType) command).equals(OnOffType.ON);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_MON)) {
            if (command instanceof OnOffType) {
                runsOnMon = ((OnOffType) command).equals(OnOffType.ON);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_TUE)) {
            if (command instanceof OnOffType) {
                runsOnTue = ((OnOffType) command).equals(OnOffType.ON);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_WED)) {
            if (command instanceof OnOffType) {
                runsOnWed = ((OnOffType) command).equals(OnOffType.ON);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_THU)) {
            if (command instanceof OnOffType) {
                runsOnThur = ((OnOffType) command).equals(OnOffType.ON);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_FRI)) {
            if (command instanceof OnOffType) {
                runsOnFri = ((OnOffType) command).equals(OnOffType.ON);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_SAT)) {
            if (command instanceof OnOffType) {
                runsOnSat = ((OnOffType) command).equals(OnOffType.ON);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_SUN)) {
            if (command instanceof OnOffType) {
                runsOnSun = ((OnOffType) command).equals(OnOffType.ON);
            }
        }
        onCallable = scheduleOn(onTimeHours, onTimeMinutes, onTimeSeconds, new OnCallable());
        offCallable = scheduleOff(offTimeHours, offTimeMinutes, offTimeSeconds, new OffCallable());
        String description = getDescription();
        updateState(CHANNEL_DESCRIPTION, new StringType(description));
    }

    private String getDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(enabled ? "ON" : "OFF").append(':');
        stringBuilder.append(runsOnMon ? "M" : ".");
        stringBuilder.append(runsOnTue ? "T" : ".");
        stringBuilder.append(runsOnWed ? "W" : ".");
        stringBuilder.append(runsOnThur ? "Th" : ".");
        stringBuilder.append(runsOnFri ? "F" : ".");
        stringBuilder.append(runsOnSat ? "Sa" : ".");
        stringBuilder.append(runsOnSun ? "Su" : ".");
        stringBuilder.append(' ');
        stringBuilder.append("ON Time: ").append(onTimeHours).append(':').append(onTimeMinutes).append(':')
                .append(onTimeSeconds);
        stringBuilder.append(",OFF Time: ").append(offTimeHours).append(':').append(offTimeMinutes).append(':')
                .append(offTimeSeconds);
        return stringBuilder.toString();
    }

    @Override
    public void initialize() {
        // TODO: Need to initalise from previous state...
        updateStatus(ThingStatus.ONLINE);
    }

    private ScheduledFuture<Boolean> scheduleOn(int hours, int minutes, int seconds, Callable<Boolean> callable) {
        ScheduledFuture<Boolean> job = scheduler.schedule(callable, delayFromNow(hours, minutes, seconds),
                TimeUnit.MILLISECONDS);
        return job;

    }

    private ScheduledFuture<Boolean> scheduleOff(int hours, int minutes, int seconds, Callable<Boolean> callable) {
        ScheduledFuture<Boolean> job = scheduler.schedule(callable, delayFromNow(hours, minutes, seconds),
                TimeUnit.MILLISECONDS);
        return job;

    }

    private long delayFromNow(int hours, int minutes, int seconds) {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);

        long withTime = cal.getTimeInMillis();

        if (now > withTime) {
            cal.roll(Calendar.DAY_OF_YEAR, true);
        }

        long toSchedule = cal.getTimeInMillis();

        return System.currentTimeMillis() - toSchedule;
    }

    private boolean runsToday() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1 && runsOnSun) {
            return true;
        }

        return false;
    }

    private final class OnCallable implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            if (enabled && runsToday()) {
                updateState(CHANNEL_STATUS, OnOffType.ON);
            }
            return true;
        }
    }

    private final class OffCallable implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            if (enabled && runsToday()) {
                updateState(CHANNEL_STATUS, OnOffType.OFF);
                offCallable = scheduleOff(offTimeHours, offTimeMinutes, offTimeSeconds, offCallable);
            }
            return true;
        }
    }
}
