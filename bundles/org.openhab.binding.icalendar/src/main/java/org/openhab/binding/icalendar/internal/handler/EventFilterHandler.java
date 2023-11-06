/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.icalendar.internal.config.EventFilterConfiguration;
import org.openhab.binding.icalendar.internal.handler.PullJob.CalendarUpdateListener;
import org.openhab.binding.icalendar.internal.logic.AbstractPresentableCalendar;
import org.openhab.binding.icalendar.internal.logic.Event;
import org.openhab.binding.icalendar.internal.logic.EventTextFilter;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EventFilterHandler} filters events from a calendar and presents them in a dynamic way.
 *
 * @author Michael Wodniok - Initial Contribution
 * @author Michael Wodniok - Fixed subsecond search if rounding to unit
 */
@NonNullByDefault
public class EventFilterHandler extends BaseThingHandler implements CalendarUpdateListener {

    private @Nullable EventFilterConfiguration configuration;
    private final Logger logger = LoggerFactory.getLogger(EventFilterHandler.class);
    private final List<ResultChannelSet> resultChannels;
    private final TimeZoneProvider tzProvider;
    private @Nullable ScheduledFuture<?> updateFuture;

    public EventFilterHandler(Thing thing, TimeZoneProvider tzProvider) {
        super(thing);
        resultChannels = new CopyOnWriteArrayList<>();
        this.tzProvider = tzProvider;
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
            updateStates();
        }
    }

    @Override
    public void initialize() {
        Bridge iCalendarBridge = getBridge();
        if (iCalendarBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "This thing requires a bridge configured to work.");
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

        updateChannelSet(config);
        if (iCalendarBridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
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
        synchronized (resultChannels) {
            if (resultChannels.size() == resultCount) {
                return;
            }
            resultChannels.clear();
            for (int position = 0; position < resultCount; position++) {
                ChannelGroupUID currentGroup = new ChannelGroupUID(getThing().getUID(),
                        RESULT_GROUP_ID_PREFIX + position);
                ResultChannelSet current = new ResultChannelSet(currentGroup,
                        new ChannelUID(currentGroup, RESULT_BEGIN_ID), new ChannelUID(currentGroup, RESULT_END_ID),
                        new ChannelUID(currentGroup, RESULT_TITLE_ID));
                resultChannels.add(current);
            }
        }
    }

    /**
     * Checks existing channels, adds missing and removes extraneous channels from the Thing.
     *
     * @param config The validated Configuration of the Thing.
     */
    private void updateChannelSet(EventFilterConfiguration config) {
        final ThingHandlerCallback handlerCallback = getCallback();
        if (handlerCallback == null) {
            return;
        }

        final List<Channel> currentChannels = getThing().getChannels();
        final ThingBuilder thingBuilder = editThing();
        BigDecimal maxEvents = config.maxEvents;
        if (maxEvents == null || maxEvents.compareTo(BigDecimal.ZERO) < 1) {
            thingBuilder.withoutChannels(currentChannels);
            updateThing(thingBuilder.build());
            return;
        }
        generateExpectedChannelList(maxEvents.intValue());

        synchronized (resultChannels) {
            currentChannels.stream().filter((Channel current) -> {
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

            resultChannels
                    .stream().filter((ResultChannelSet current) -> (getThing()
                            .getChannelsOfGroup(current.resultGroup.toString()).isEmpty()))
                    .forEach((ResultChannelSet current) -> {
                        for (ChannelBuilder builder : handlerCallback.createChannelBuilders(current.resultGroup,
                                GROUP_TYPE_UID)) {
                            Channel currentChannel = builder.build();
                            Channel existingChannel = getThing().getChannel(currentChannel.getUID());
                            if (existingChannel == null) {
                                thingBuilder.withChannel(currentChannel);
                            }
                        }
                    });
        }
        updateThing(thingBuilder.build());
    }

    /**
     * Updates all states and channels. Reschedules an update if no error occurs.
     */
    private void updateStates() {
        if (!ThingHandlerHelper.isHandlerInitialized(this)) {
            logger.debug("Ignoring call for updating states as this instance is not initialized yet.");
            return;
        }
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
        final AbstractPresentableCalendar cal = iCalendarHandler.getRuntimeCalendar();
        if (cal != null) {
            updateStatus(ThingStatus.ONLINE);

            Instant reference = Instant.now();
            TimeMultiplicator multiplicator = null;
            EventTextFilter filter = null;
            int maxEvents;
            Instant begin = Instant.EPOCH;
            Instant end = Instant.ofEpochMilli(Long.MAX_VALUE);

            try {
                String textFilterValue = config.textEventValue;
                if (textFilterValue != null) {
                    String textEventField = config.textEventField;
                    String textValueType = config.textValueType;
                    if (textEventField == null || textValueType == null) {
                        throw new ConfigBrokenException("Text filter settings are not set properly.");
                    }
                    try {
                        EventTextFilter.Field textFilterField = EventTextFilter.Field.valueOf(textEventField);
                        EventTextFilter.Type textFilterType = EventTextFilter.Type.valueOf(textValueType);

                        filter = new EventTextFilter(textFilterField, textFilterValue, textFilterType);
                    } catch (IllegalArgumentException e2) {
                        throw new ConfigBrokenException("textEventField or textValueType are not set properly.");
                    }
                }

                BigDecimal maxEventsBD = config.maxEvents;
                if (maxEventsBD == null) {
                    throw new ConfigBrokenException("maxEvents is not set.");
                }
                maxEvents = maxEventsBD.intValue();
                if (maxEvents < 0) {
                    throw new ConfigBrokenException("maxEvents is less than 0. This is not allowed.");
                }

                try {
                    final String datetimeUnit = config.datetimeUnit;
                    if (datetimeUnit != null) {
                        multiplicator = TimeMultiplicator.valueOf(datetimeUnit);
                    }
                } catch (IllegalArgumentException e) {
                    throw new ConfigBrokenException("datetimeUnit is not set properly.");
                }

                final Boolean datetimeRound = config.datetimeRound;
                if (datetimeRound != null && datetimeRound.booleanValue()) {
                    if (multiplicator == null) {
                        throw new ConfigBrokenException("datetimeUnit is missing but required for datetimeRound.");
                    }
                    ZonedDateTime refDT = reference.atZone(tzProvider.getTimeZone());
                    switch (multiplicator) {
                        case WEEK:
                            refDT = refDT.with(ChronoField.DAY_OF_WEEK, 1);
                        case DAY:
                            refDT = refDT.with(ChronoField.HOUR_OF_DAY, 0);
                        case HOUR:
                            refDT = refDT.with(ChronoField.MINUTE_OF_HOUR, 0);
                        case MINUTE:
                            refDT = refDT.with(ChronoField.SECOND_OF_MINUTE, 0).with(ChronoField.NANO_OF_SECOND, 0);
                    }
                    reference = refDT.toInstant();
                }

                BigDecimal datetimeStart = config.datetimeStart;
                if (datetimeStart != null) {
                    if (multiplicator == null) {
                        throw new ConfigBrokenException("datetimeUnit is missing but required for datetimeStart.");
                    }
                    begin = reference.plusSeconds(datetimeStart.longValue() * multiplicator.getMultiplier());
                }
                BigDecimal datetimeEnd = config.datetimeEnd;
                if (datetimeEnd != null) {
                    if (multiplicator == null) {
                        throw new ConfigBrokenException("datetimeUnit is missing but required for datetimeEnd.");
                    }
                    end = reference.plusSeconds(datetimeEnd.longValue() * multiplicator.getMultiplier());
                }
            } catch (ConfigBrokenException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                return;
            }

            synchronized (resultChannels) {
                List<Event> results = cal.getFilteredEventsBetween(begin, end, filter, maxEvents);
                for (int position = 0; position < resultChannels.size(); position++) {
                    ResultChannelSet channels = resultChannels.get(position);
                    if (position < results.size()) {
                        Event result = results.get(position);
                        updateState(channels.titleChannel, new StringType(result.title));
                        updateState(channels.beginChannel,
                                new DateTimeType(result.start.atZone(tzProvider.getTimeZone())));
                        updateState(channels.endChannel, new DateTimeType(result.end.atZone(tzProvider.getTimeZone())));
                    } else {
                        updateState(channels.titleChannel, UnDefType.UNDEF);
                        updateState(channels.beginChannel, UnDefType.UNDEF);
                        updateState(channels.endChannel, UnDefType.UNDEF);
                    }
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Calendar has not been retrieved yet.");
        }

        int refreshTime = DEFAULT_FILTER_REFRESH;
        if (config.refreshTime != null) {
            refreshTime = config.refreshTime.intValue();
            if (refreshTime < 1) {
                logger.debug("refreshTime is set to invalid value. Using default.");
                refreshTime = DEFAULT_FILTER_REFRESH;
            }
        }
        ScheduledFuture<?> currentUpdateFuture = updateFuture;
        if (currentUpdateFuture != null) {
            currentUpdateFuture.cancel(true);
        }
        updateFuture = scheduler.scheduleWithFixedDelay(this::updateStates, refreshTime, refreshTime, TimeUnit.MINUTES);
    }
}
