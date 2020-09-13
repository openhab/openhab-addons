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
package org.openhab.binding.icalendar.internal.handler;

import static org.openhab.binding.icalendar.internal.ICalendarBindingConstants.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelGroupUID;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.icalendar.internal.config.EventFilterConfiguration;
import org.openhab.binding.icalendar.internal.handler.PullJob.CalendarUpdateListener;
import org.openhab.binding.icalendar.internal.logic.AbstractPresentableCalendar;
import org.openhab.binding.icalendar.internal.logic.Event;
import org.openhab.binding.icalendar.internal.logic.EventTextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EventFilterHandler} filters events from a calendar and presents them in a dynamic way.
 *
 * @author Michael Wodniok - Initial Contribution
 */
@NonNullByDefault
public class EventFilterHandler extends BaseThingHandler implements CalendarUpdateListener {

    private @Nullable EventFilterConfiguration configuration;
    private final Logger logger = LoggerFactory.getLogger(EventFilterHandler.class);
    private final List<ResultChannelSet> resultChannels;
    private @Nullable ScheduledFuture<?> updateFuture;
    private boolean initFinished;

    public EventFilterHandler(Thing thing) {
        super(thing);
        resultChannels = new CopyOnWriteArrayList<>();
        initFinished = false;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStates();
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> currentUpdateFuture = updateFuture;
        if (currentUpdateFuture != null) {
            currentUpdateFuture.cancel(true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (initFinished) {
                updateStates();
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        @Nullable
        Bridge iCalendarBridge = getBridge();
        if (iCalendarBridge == null) {
            logger.warn("This thing requires a bridge configured to work.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

        final EventFilterConfiguration config = getConfigAs(EventFilterConfiguration.class);
        if (config.datetimeUnit == null && (config.datetimeEnd != null || config.datetimeStart != null)) {
            logger.warn("Start/End date-time is set but no unit. This will ignore the filter.");
        }
        if (config.textEventField != null && config.textValueType == null) {
            logger.warn("Event field is set but not match type. This will ignore the filter.");
        }
        configuration = config;

        if (iCalendarBridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        } else {
            updateChannelSet(config);
            updateStates();
        }
        initFinished = true;
    }

    @Override
    public void onCalendarUpdated() {
        updateStates();
    }

    /**
     * Consists of a set of channels and their group for describing a filtered event. *
     */
    private class ResultChannelSet {
        ChannelGroupUID resultGroup;
        ChannelUID beginChannel;
        ChannelUID endChannel;
        ChannelUID titleChannel;

        public ResultChannelSet(ChannelGroupUID group, ChannelUID begin, ChannelUID end, ChannelUID title) {
            resultGroup = group;
            beginChannel = begin;
            endChannel = end;
            titleChannel = title;
        }
    }

    /**
     * Describes some fixed time factors for unit selection.
     */
    private enum TimeMultiplicator {
        MINUTE(60),
        HOUR(3600),
        DAY(86400),
        WEEK(604800);

        private final int secondsPerUnit;

        private TimeMultiplicator(int secondsPerUnit) {
            this.secondsPerUnit = secondsPerUnit;
        }

        /**
         * Returns the count of seconds per unit.
         *
         * @return Seconds per unit.
         */
        public int getMultiplier() {
            return secondsPerUnit;
        }
    }

    /**
     * Generates a list of channel sets according to the required amount.
     *
     * @param resultCount The required amount of results.
     */
    private void generateExpectedChannelList(int resultCount) {
        if (resultChannels.size() == resultCount) {
            return;
        }
        resultChannels.clear();
        for (int position = 0; position < resultCount; position++) {
            ChannelGroupUID currentGroup = new ChannelGroupUID(getThing().getUID(), RESULT_GROUP_ID_PREFIX + position);
            ResultChannelSet current = new ResultChannelSet(currentGroup, new ChannelUID(currentGroup, RESULT_BEGIN_ID),
                    new ChannelUID(currentGroup, RESULT_END_ID), new ChannelUID(currentGroup, RESULT_TITLE_ID));
            resultChannels.add(current);
        }
    }

    /**
     * Checks existing channels, adds missing and removes extraneous channels from the Thing.
     *
     * @param config The validated Configuration of the Thing.
     */
    private void updateChannelSet(EventFilterConfiguration config) {
        @Nullable
        final ThingHandlerCallback handlerCallback = getCallback();
        if (handlerCallback == null) {
            return;
        }

        final List<Channel> currentChannels = getThing().getChannels();
        final ThingBuilder thingBuilder = editThing();
        if (config.maxEvents == null || config.maxEvents.compareTo(new BigDecimal(0)) < 1) {
            thingBuilder.withoutChannels(currentChannels);
            updateThing(thingBuilder.build());
            return;
        }
        generateExpectedChannelList(config.maxEvents.intValue());

        currentChannels.stream().filter((Channel current) -> {
            @Nullable
            String currentGroupId = current.getUID().getGroupId();
            if (currentGroupId == null) {
                return true;
            }
            for (ResultChannelSet channelSet : resultChannels) {
                if (channelSet.resultGroup.getId().contentEquals(currentGroupId)) {
                    return false;
                }
            }
            return true;
        }).forEach((Channel toDelete) -> {
            thingBuilder.withoutChannel(toDelete.getUID());
        });

        resultChannels.stream().filter((ResultChannelSet current) -> {
            return (getThing().getChannelsOfGroup(current.resultGroup.toString()).size() == 0);
        }).forEach((ResultChannelSet current) -> {
            for (ChannelBuilder builder : handlerCallback.createChannelBuilders(current.resultGroup, GROUP_TYPE_UID)) {
                Channel currentChannel = builder.build();
                @Nullable
                Channel existingChannel = getThing().getChannel(currentChannel.getUID());
                if (existingChannel == null) {
                    thingBuilder.withChannel(currentChannel);
                }
            }
        });

        updateThing(thingBuilder.build());
    }

    /**
     * Updates all states and channels.
     */
    private void updateStates() {
        final Bridge iCalendarBridge = getBridge();
        if (iCalendarBridge == null) {
            logger.debug("Bridge not instantiated!");
            return;
        }
        final ICalendarHandler iCalendarHandler = (ICalendarHandler) iCalendarBridge.getHandler();
        if (iCalendarHandler == null) {
            logger.debug("ICalendarHandler not instantiated!");
            return;
        }
        final EventFilterConfiguration config = configuration;
        if (config == null) {
            logger.debug("Configuration not instantiated!");
            return;
        }
        @Nullable
        final AbstractPresentableCalendar cal = iCalendarHandler.getRuntimeCalendar();
        if (cal != null) {
            updateStatus(ThingStatus.ONLINE);

            String textFilterValue = config.textEventValue;
            EventTextFilter filter = null;
            if (textFilterValue != null) {
                try {
                    if (config.textEventField == null || config.textValueType == null) {
                        throw new IllegalArgumentException("Config is broken.");
                    }
                    EventTextFilter.Field textFilterField = EventTextFilter.Field.valueOf(config.textEventField);
                    EventTextFilter.Type textFilterType = EventTextFilter.Type.valueOf(config.textValueType);

                    filter = new EventTextFilter(textFilterField, textFilterValue, textFilterType);
                } catch (IllegalArgumentException e) {
                    logger.warn("Text-filter settings are not set properly.");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                    return;
                }
            }

            Instant reference = Instant.now();
            TimeMultiplicator multiplicator;

            try {
                if (config.datetimeUnit == null) {
                    throw new IllegalArgumentException("Config is broken.");
                }
                multiplicator = TimeMultiplicator.valueOf(config.datetimeUnit);
            } catch (IllegalArgumentException e) {
                logger.warn("Time-filter settings are not set properly.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                return;
            }

            if (config.datetimeRound) {
                ZonedDateTime refDT = reference.atZone(ZoneId.systemDefault());
                switch (multiplicator) {
                    case WEEK:
                        refDT = refDT.with(ChronoField.DAY_OF_WEEK, 1);
                    case DAY:
                        refDT = refDT.with(ChronoField.HOUR_OF_DAY, 0);
                    case HOUR:
                        refDT = refDT.with(ChronoField.MINUTE_OF_HOUR, 0);
                    case MINUTE:
                        refDT = refDT.with(ChronoField.SECOND_OF_MINUTE, 0);
                }
                reference = refDT.toInstant();
            }
            Instant begin = Instant.EPOCH;
            Instant end = Instant.ofEpochMilli(Long.MAX_VALUE);

            if (config.datetimeStart != null) {
                begin = reference.plusSeconds(config.datetimeStart.longValue() * multiplicator.getMultiplier());
            }
            if (config.datetimeEnd != null) {
                end = reference.plusSeconds(config.datetimeEnd.longValue() * multiplicator.getMultiplier());
            }

            List<Event> results = cal.getFilteredEventsBetween(begin, end, filter, config.maxEvents.intValue());
            for (int position = 0; position < config.maxEvents.intValue(); position++) {
                ResultChannelSet channels = resultChannels.get(position);
                if (position < results.size()) {
                    Event result = results.get(position);
                    updateState(channels.titleChannel, new StringType(result.title));
                    updateState(channels.beginChannel, new DateTimeType(result.start.atZone(ZoneId.systemDefault())));
                    updateState(channels.endChannel, new DateTimeType(result.end.atZone(ZoneId.systemDefault())));
                } else {
                    updateState(channels.titleChannel, UnDefType.UNDEF);
                    updateState(channels.beginChannel, UnDefType.UNDEF);
                    updateState(channels.endChannel, UnDefType.UNDEF);
                }
            }

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

        int refreshTime = DEFAULT_FILTER_REFRESH;
        if (config.refreshTime != null) {
            refreshTime = config.refreshTime.intValue();
        }
        ScheduledFuture<?> currentUpdateFuture = updateFuture;
        if (currentUpdateFuture != null) {
            currentUpdateFuture.cancel(true);
        }
        updateFuture = scheduler.scheduleWithFixedDelay(() -> {
            updateStates();
        }, refreshTime, refreshTime, TimeUnit.MINUTES);
    }
}
