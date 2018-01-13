/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.timer.handler;

import static java.util.Calendar.*;
import static org.openhab.binding.timer.TimerBindingConstants.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
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
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DailyTimerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Neil Renaud - Initial contribution
 */
public class DailyTimerHandler extends BaseThingHandler {

    private static final int ENABLED = 0;
    // Sleep time before rescheduling timer - this is to ensure we don't accidently fire twice
    private static final int SLEEP_TIME = 10000;

    private final Logger logger = LoggerFactory.getLogger(DailyTimerHandler.class);

    // Boolean array used to store the enabled status and the enabledOnXXXDay status.
    // This is used as it makes it easier to avoid large if/elses and make the code more generic.
    private final boolean[] runsOn = new boolean[] { false, // enabled
            false, // Sunday
            false, // Monday
            false, // Tuesday
            false, // Wednesday
            false, // Thursday
            false, // Friday
            false // Saturday
    };
    // Map storing the ON and OFF hours, minutes, seconds - used instead of explicity variables
    // as it allows us to make the code more generic.
    private final Map<String, DecimalType> times = new HashMap<String, DecimalType>(6);

    private final OnCallable onCallable = new OnCallable();
    private final OffCallable offCallable = new OffCallable();

    private String description = "";

    private ScheduledFuture<Boolean> onSchedule = null;
    private ScheduledFuture<Boolean> offSchedule = null;

    public DailyTimerHandler(Thing thing) {
        super(thing);
    }

    private void handleTimeChange(ChannelUID channelUID, DecimalType command) {
        times.put(channelUID.getId(), command);
    }

    private void handleEnableChange(ChannelUID channelUID, OnOffType command, int index) {
        runsOn[index] = command.equals(OnOffType.ON);
    }

    private void handleEnabledRefresh(ChannelUID channelUID, int index) {
        OnOffType state = runsOn[index] ? OnOffType.ON : OnOffType.OFF;
        updateState(channelUID.getId(), state);
    }

    private void handleTimeRefresh(ChannelUID channelUID) {
        DecimalType state = times.get(channelUID.getId());
        if (state == null) {
            updateState(channelUID.getId(), UnDefType.UNDEF);
        } else {
            updateState(channelUID.getId(), state);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_ON_TIME_HOURS) || channelUID.getId().equals(CHANNEL_ON_TIME_MINUTES)
                || channelUID.getId().equals(CHANNEL_ON_TIME_SECONDS)) {

            if (command instanceof RefreshType) {
                handleTimeRefresh(channelUID);
            } else if (command instanceof DecimalType) {
                handleTimeChange(channelUID, (DecimalType) command);
                updateOnSchedule();
            }
        }
        if (channelUID.getId().equals(CHANNEL_OFF_TIME_HOURS) || channelUID.getId().equals(CHANNEL_OFF_TIME_MINUTES)
                || channelUID.getId().equals(CHANNEL_OFF_TIME_SECONDS)) {

            if (command instanceof RefreshType) {
                handleTimeRefresh(channelUID);
            } else if (command instanceof DecimalType) {
                handleTimeChange(channelUID, (DecimalType) command);
                updateOffSchedule();
            }
        }

        if (channelUID.getId().equals(CHANNEL_ENABLED)) {
            if (command instanceof OnOffType) {
                handleEnableChange(channelUID, (OnOffType) command, ENABLED);
            } else if (command instanceof RefreshType) {
                handleEnabledRefresh(channelUID, ENABLED);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_MON)) {
            if (command instanceof OnOffType) {
                handleEnableChange(channelUID, (OnOffType) command, Calendar.MONDAY);
            } else if (command instanceof RefreshType) {
                handleEnabledRefresh(channelUID, Calendar.MONDAY);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_TUE)) {
            if (command instanceof OnOffType) {
                handleEnableChange(channelUID, (OnOffType) command, Calendar.TUESDAY);
            } else if (command instanceof RefreshType) {
                handleEnabledRefresh(channelUID, Calendar.TUESDAY);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_WED)) {
            if (command instanceof OnOffType) {
                handleEnableChange(channelUID, (OnOffType) command, Calendar.WEDNESDAY);
            } else if (command instanceof RefreshType) {
                handleEnabledRefresh(channelUID, Calendar.WEDNESDAY);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_THU)) {
            if (command instanceof OnOffType) {
                handleEnableChange(channelUID, (OnOffType) command, Calendar.THURSDAY);
            } else if (command instanceof RefreshType) {
                handleEnabledRefresh(channelUID, Calendar.THURSDAY);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_FRI)) {
            if (command instanceof OnOffType) {
                handleEnableChange(channelUID, (OnOffType) command, Calendar.FRIDAY);
            } else if (command instanceof RefreshType) {
                handleEnabledRefresh(channelUID, Calendar.FRIDAY);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_SAT)) {
            if (command instanceof OnOffType) {
                handleEnableChange(channelUID, (OnOffType) command, Calendar.SATURDAY);
            } else if (command instanceof RefreshType) {
                handleEnabledRefresh(channelUID, Calendar.SATURDAY);
            }
        }
        if (channelUID.getId().equals(CHANNEL_RUN_ON_SUN)) {
            if (command instanceof OnOffType) {
                handleEnableChange(channelUID, (OnOffType) command, Calendar.SUNDAY);
            } else if (command instanceof RefreshType) {
                handleEnabledRefresh(channelUID, Calendar.SUNDAY);
            }
        }
        if (channelUID.getId().equals(CHANNEL_DESCRIPTION) && command instanceof RefreshType) {
            updateState(channelUID.getId(), new StringType(description));
        }

        updateDescription();
        updateState(CHANNEL_DESCRIPTION, new StringType(description));
    }

    private void updateDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(runsOn[MONDAY] ? "M" : ".");
        stringBuilder.append(runsOn[TUESDAY] ? "T" : ".");
        stringBuilder.append(runsOn[WEDNESDAY] ? "W" : ".");
        stringBuilder.append(runsOn[THURSDAY] ? "Th" : ".");
        stringBuilder.append(runsOn[FRIDAY] ? "F" : ".");
        stringBuilder.append(runsOn[SATURDAY] ? "Sa" : ".");
        stringBuilder.append(runsOn[SUNDAY] ? "Su" : ".");
        stringBuilder.append(' ');

        DecimalType onHours = times.get(CHANNEL_ON_TIME_HOURS);
        DecimalType onMinutes = times.get(CHANNEL_ON_TIME_MINUTES);
        DecimalType onSeconds = times.get(CHANNEL_ON_TIME_SECONDS);
        if (validHoursMinsSeconds(onHours, onMinutes, onSeconds)) {
            stringBuilder.append("ON: ");
            if (onHours.intValue() < 10) {
                stringBuilder.append("0");
            }
            stringBuilder.append(onHours.intValue()).append(':');
            if (onMinutes.intValue() < 10) {
                stringBuilder.append("0");
            }
            stringBuilder.append(onMinutes.intValue()).append(':');
            if (onSeconds.intValue() < 10) {
                stringBuilder.append("0");
            }
            stringBuilder.append(onSeconds.intValue());
        } else {
            stringBuilder.append("ON: --:--:--");
        }

        DecimalType offHours = times.get(CHANNEL_OFF_TIME_HOURS);
        DecimalType offMinutes = times.get(CHANNEL_OFF_TIME_MINUTES);
        DecimalType offSeconds = times.get(CHANNEL_OFF_TIME_SECONDS);
        if (validHoursMinsSeconds(offHours, offMinutes, offSeconds)) {
            stringBuilder.append(" OFF: ");
            if (offHours.intValue() < 10) {
                stringBuilder.append("0");
            }
            stringBuilder.append(offHours.intValue()).append(':');
            if (offMinutes.intValue() < 10) {
                stringBuilder.append("0");
            }
            stringBuilder.append(offMinutes.intValue()).append(':');
            if (offSeconds.intValue() < 10) {
                stringBuilder.append("0");
            }
            stringBuilder.append(offSeconds.intValue());
        } else {
            stringBuilder.append(" OFF: --:--:--");
        }

        description = stringBuilder.toString();
    }

    @Override
    public void initialize() {
        updateState(CHANNEL_ENABLED, UnDefType.UNDEF);
        updateState(CHANNEL_DESCRIPTION, UnDefType.UNDEF);
        updateState(CHANNEL_STATUS, UnDefType.UNDEF);
        updateState(CHANNEL_ON_TIME_HOURS, UnDefType.UNDEF);
        updateState(CHANNEL_ON_TIME_MINUTES, UnDefType.UNDEF);
        updateState(CHANNEL_ON_TIME_SECONDS, UnDefType.UNDEF);
        updateState(CHANNEL_OFF_TIME_HOURS, UnDefType.UNDEF);
        updateState(CHANNEL_OFF_TIME_MINUTES, UnDefType.UNDEF);
        updateState(CHANNEL_OFF_TIME_SECONDS, UnDefType.UNDEF);
        updateState(CHANNEL_RUN_ON_MON, UnDefType.UNDEF);
        updateState(CHANNEL_RUN_ON_TUE, UnDefType.UNDEF);
        updateState(CHANNEL_RUN_ON_WED, UnDefType.UNDEF);
        updateState(CHANNEL_RUN_ON_THU, UnDefType.UNDEF);
        updateState(CHANNEL_RUN_ON_FRI, UnDefType.UNDEF);
        updateState(CHANNEL_RUN_ON_SAT, UnDefType.UNDEF);
        updateState(CHANNEL_RUN_ON_SUN, UnDefType.UNDEF);
        updateStatus(ThingStatus.ONLINE);
    }

    private ScheduledFuture<Boolean> cancelAndReschedule(DecimalType hours, DecimalType minutes, DecimalType seconds,
            ScheduledFuture<Boolean> job, Callable<Boolean> callable) {
        cancel(job);
        return scheduler.schedule(callable, delayFromNow(hours.intValue(), minutes.intValue(), seconds.intValue()),
                TimeUnit.MILLISECONDS);
    }

    private void cancel(ScheduledFuture<Boolean> job) {
        if (job != null) {
            logger.info("Cancelling current job [{}]", getThing());
            // Cancel current job.
            job.cancel(false);
            job = null;
        }
    }

    private void updateOnSchedule() {
        DecimalType hours = times.get(CHANNEL_ON_TIME_HOURS);
        DecimalType minutes = times.get(CHANNEL_ON_TIME_MINUTES);
        DecimalType seconds = times.get(CHANNEL_ON_TIME_SECONDS);
        if (validHoursMinsSeconds(hours, minutes, seconds)) {
            onSchedule = cancelAndReschedule(hours, minutes, seconds, onSchedule, onCallable);
            logger.info("Scheduled ON job for h[{}] m[{}] s[{}]", hours, minutes, seconds);
        } else {
            cancel(onSchedule);
            logger.debug("Not rescheduling ON as time is in valid h[{}] m[{}] s[{}]", hours, minutes, seconds);
        }
    }

    private void updateOffSchedule() {
        DecimalType hours = times.get(CHANNEL_OFF_TIME_HOURS);
        DecimalType minutes = times.get(CHANNEL_OFF_TIME_MINUTES);
        DecimalType seconds = times.get(CHANNEL_OFF_TIME_SECONDS);
        if (validHoursMinsSeconds(hours, minutes, seconds)) {
            offSchedule = cancelAndReschedule(hours, minutes, seconds, offSchedule, offCallable);
            logger.info("Scheduled OFF job for h[{}] m[{}] s[{}]", hours, minutes, seconds);
        } else {
            cancel(offSchedule);
            logger.debug("Not rescheduling OFF as time is in valid h[{}] m[{}] s[{}]", hours, minutes, seconds);
        }
    }

    private boolean validHoursMinsSeconds(DecimalType hours, DecimalType minutes, DecimalType seconds) {
        return (hours != null && minutes != null && seconds != null);
    }

    private long delayFromNow(int hours, int minutes, int seconds) {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);

        long withTime = cal.getTimeInMillis();

        if (now > withTime) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        long toSchedule = cal.getTimeInMillis();
        long delay = toSchedule - System.currentTimeMillis();
        logger.debug("Scheduling in {} ms", delay);
        return delay;
    }

    private boolean runsToday() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return runsOn[dayOfWeek];
    }

    private final class OnCallable implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            if (runsOn[ENABLED] && runsToday()) {
                updateState(CHANNEL_STATUS, OnOffType.ON);
            } else {
                logger.debug("Timer[{}] Not turning ON as it is disabled [{}]", getThing(), runsOn);
            }
            Thread.sleep(SLEEP_TIME);
            updateOnSchedule();
            return true;
        }
    }

    private final class OffCallable implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            if (runsOn[ENABLED] && runsToday()) {
                updateState(CHANNEL_STATUS, OnOffType.OFF);
            } else {
                logger.debug("Timer[{}] Not turning OFF as it is disabled [{}]", getThing(), runsOn);
            }
            Thread.sleep(SLEEP_TIME);
            updateOffSchedule();
            return true;
        }
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        cancel(onSchedule);
        cancel(offSchedule);
    }
}
