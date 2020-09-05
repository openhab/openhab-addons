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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
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
public class EventFilterHandler extends BaseThingHandler {

    private @Nullable EventFilterConfiguration configuration;
    private final Logger logger = LoggerFactory.getLogger(EventFilterHandler.class);
    private final List<ResultChannelSet> resultChannels;
    private @Nullable ScheduledFuture<?> updates;

    public EventFilterHandler(Thing thing) {
        super(thing);
        resultChannels = new ArrayList<>();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateStates();
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
            updateStates();
        }
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

    private class ResultChannelSet {
        ChannelGroupUID resultGroup;
        ChannelUID beginChannel;
        ChannelUID endChannel;
        ChannelUID titleChannel;
    }

    private enum TimeMultiplicator {
        MINUTE(60),
        HOUR(3600),
        DAY(86400),
        WEEK(604800);

        private final int secondsPerUnit;

        private TimeMultiplicator(int secondsPerUnit) {
            this.secondsPerUnit = secondsPerUnit;
        }

        public int getMultiplier() {
            return secondsPerUnit;
        }
    }

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
            updateChannelSet(config);

            String textFilterValue = config.textEventValue;
            EventTextFilter filter = null;
            if (textFilterValue != null) {
                try {
                    EventTextFilter.Field textFilterField = EventTextFilter.Field.valueOf(config.textEventField);
                    EventTextFilter.Type textFilterType = EventTextFilter.Type.valueOf(config.textValueType);

                    filter = new EventTextFilter(textFilterField, textFilterValue, textFilterType);
                } catch (NullPointerException | IllegalArgumentException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                    return;
                }
            }

            Instant now = Instant.now();
            TimeMultiplicator multiplicator;

            try {
                multiplicator = TimeMultiplicator.valueOf(config.datetimeUnit);
            } catch (NullPointerException | IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                return;
            }

            if (config.datetimeRound) {
                ZonedDateTime nowDT = now.atZone(ZoneId.systemDefault());
                switch (multiplicator) {
                    case WEEK:
                        nowDT = nowDT.with(ChronoField.DAY_OF_WEEK, 1);
                    case DAY:
                        nowDT = nowDT.with(ChronoField.HOUR_OF_DAY, 0);
                    case HOUR:
                        nowDT = nowDT.with(ChronoField.MINUTE_OF_HOUR, 0);
                    case MINUTE:
                        nowDT = nowDT.with(ChronoField.SECOND_OF_MINUTE, 0);
                }
                now = nowDT.toInstant();
            }
            Instant begin = Instant.EPOCH;
            Instant end = Instant.ofEpochMilli(Long.MAX_VALUE);

            if (config.datetimeStart != null) {
                begin = now.plusSeconds(config.datetimeStart.longValue() * multiplicator.secondsPerUnit);
            }
            if (config.datetimeEnd != null) {
                end = now.plusSeconds(config.datetimeEnd.longValue() * multiplicator.secondsPerUnit);
            }

            List<@NonNull Event> results = cal.getFilteredEventsBetween(begin, end, filter,
                    config.maxEvents.intValue());
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

        ScheduledFuture<?> currentUpdateFuture = updates;
        if (currentUpdateFuture != null) {
            currentUpdateFuture.cancel(true);
        }
        updates = scheduler.scheduleWithFixedDelay(() -> {
            updateStates();
        }, 5, 5, TimeUnit.MINUTES);
    }

    private void updateChannelSet(EventFilterConfiguration config) {
        @Nullable
        final ThingHandlerCallback handlerCallback = getCallback();
        if (handlerCallback == null) {
            return;
        }

        final List<@NonNull Channel> currentChannels = getThing().getChannels();
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

    private void generateExpectedChannelList(int resultCount) {
        if (resultChannels.size() == resultCount) {
            return;
        }
        resultChannels.clear();
        for (int position = 0; position < resultCount; position++) {
            ResultChannelSet current = new ResultChannelSet();
            current.resultGroup = new ChannelGroupUID(getThing().getUID(), RESULT_GROUP_ID_PREFIX + position);
            current.beginChannel = new ChannelUID(current.resultGroup, RESULT_BEGIN_ID);
            current.endChannel = new ChannelUID(current.resultGroup, RESULT_END_ID);
            current.titleChannel = new ChannelUID(current.resultGroup, RESULT_TITLE_ID);
            resultChannels.add(current);
        }
    }
}
