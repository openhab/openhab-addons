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
package org.openhab.binding.onebusaway.internal.handler;

import static org.openhab.binding.onebusaway.internal.OneBusAwayBindingConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openhab.binding.onebusaway.internal.config.ChannelConfig;
import org.openhab.binding.onebusaway.internal.config.RouteConfiguration;
import org.openhab.binding.onebusaway.internal.handler.ObaStopArrivalResponse.ArrivalAndDeparture;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RouteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Shawn Wilsher - Initial contribution
 */
public class RouteHandler extends BaseThingHandler implements RouteDataListener {

    public static final ThingTypeUID SUPPORTED_THING_TYPE = THING_TYPE_ROUTE;

    private final Logger logger = LoggerFactory.getLogger(RouteHandler.class);

    private RouteConfiguration config;
    private List<ScheduledFuture<?>> scheduledFutures = new CopyOnWriteArrayList<>();

    public RouteHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing {}...", channelUID);
            switch (channelUID.getId()) {
                case CHANNEL_ID_ARRIVAL:
                case CHANNEL_ID_DEPARTURE:
                case CHANNEL_ID_UPDATE:
                    StopHandler stopHandler = getStopHandler();
                    if (stopHandler != null) {
                        stopHandler.forceUpdate();
                    }
                    break;
                default:
                    logger.warn("Unnknown channel UID {} with comamnd {}", channelUID.getId(), command);
            }
        } else {
            logger.debug("The OneBusAway route is read-only and can not handle commands.");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OneBusAway route stop...");

        config = loadAndCheckConfiguration();
        if (config == null) {
            logger.debug("Initialization of OneBusAway route stop failed!");
            return;
        }
        StopHandler stopHandler = getStopHandler();
        if (stopHandler != null) {
            // We will be marked as ONLINE when we get data in our callback, which should be immediately because the
            // StopHandler, our bridge, won't be marked as online until it has data itself.
            stopHandler.registerRouteDataListener(this);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge unavailable");
        }
    }

    @Override
    public void dispose() {
        cancelAllScheduledFutures();
        StopHandler stopHandler = getStopHandler();
        if (stopHandler != null) {
            stopHandler.unregisterRouteDataListener(this);
        }
    }

    @Override
    public String getRouteId() {
        return config.getRouteId();
    }

    @Override
    public void onNewRouteData(long lastUpdateTime, List<ArrivalAndDeparture> data) {
        if (data.isEmpty()) {
            return;
        }
        // Publish to all of our linked channels.
        Calendar now = Calendar.getInstance();
        for (Channel channel : getThing().getChannels()) {
            if (channel.getKind() == ChannelKind.TRIGGER) {
                scheduleTriggerEvents(channel.getUID(), now, data);
            } else {
                publishChannel(channel.getUID(), now, lastUpdateTime, data);
            }
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private StopHandler getStopHandler() {
        return (StopHandler) getBridge().getHandler();
    }

    private RouteConfiguration loadAndCheckConfiguration() {
        RouteConfiguration config = getConfigAs(RouteConfiguration.class);
        if (config.getRouteId() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "routeId is not set");
            return null;
        }
        return config;
    }

    private void cancelAllScheduledFutures() {
        for (ScheduledFuture<?> future : scheduledFutures) {
            if (!future.isDone() || !future.isCancelled()) {
                future.cancel(true);
            }
        }
        scheduledFutures = new CopyOnWriteArrayList<>();
    }

    private void updatePropertiesFromArrivalAndDeparture(ArrivalAndDeparture data) {
        Map<String, String> props = editProperties();
        props.put(ROUTE_PROPERTY_HEADSIGN, data.tripHeadsign);
        props.put(ROUTE_PROPERTY_LONG_NAME, data.routeLongName);
        props.put(ROUTE_PROPERTY_SHORT_NAME, data.routeShortName);
        updateProperties(props);
    }

    /**
     * Publishes the channel with data and possibly schedules work to update it again when the next event has passed.
     */
    private void publishChannel(ChannelUID channelUID, Calendar now, long lastUpdateTime,
            List<ArrivalAndDeparture> arrivalAndDepartures) {
        if (channelUID.getId().equals(CHANNEL_ID_UPDATE)) {
            updateState(channelUID, new DateTimeType(
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastUpdateTime), ZoneId.systemDefault())));
            return;
        }

        ChannelConfig channelConfig = getThing().getChannel(channelUID.getId()).getConfiguration()
                .as(ChannelConfig.class);
        long offsetMs = TimeUnit.SECONDS.toMillis(channelConfig.getOffset());
        for (int i = 0; i < arrivalAndDepartures.size(); i++) {
            ArrivalAndDeparture data = arrivalAndDepartures.get(i);
            Calendar time;
            switch (channelUID.getId()) {
                case CHANNEL_ID_ARRIVAL:
                    time = (new Calendar.Builder())
                            .setInstant(
                                    (data.predicted ? data.predictedArrivalTime : data.scheduledArrivalTime) - offsetMs)
                            .build();
                    break;
                case CHANNEL_ID_DEPARTURE:
                    time = (new Calendar.Builder()).setInstant(
                            (data.predicted ? data.predictedDepartureTime : data.scheduledDepartureTime) - offsetMs)
                            .build();
                    break;
                default:
                    logger.warn("No code to handle publishing to {}", channelUID.getId());
                    return;
            }

            // Do not publish this if it's already passed.
            if (time.before(now)) {
                logger.debug("Not notifying {} because it is in the past.", channelUID.getId());
                continue;
            }
            updateState(channelUID,
                    new DateTimeType(ZonedDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault())));

            // Update properties only when we update arrival information. This is not perfect.
            if (channelUID.getId().equals(CHANNEL_ID_ARRIVAL)) {
                updatePropertiesFromArrivalAndDeparture(data);
            }

            // Schedule updates in the future. These may be canceled if we are notified about new data in the future.
            List<ArrivalAndDeparture> remaining = arrivalAndDepartures.subList(i + 1, arrivalAndDepartures.size());
            if (remaining.isEmpty()) {
                return;
            }
            scheduledFutures.add(scheduler.schedule(() -> {
                publishChannel(channelUID, Calendar.getInstance(), lastUpdateTime, remaining);
            }, time.getTimeInMillis() - now.getTimeInMillis(), TimeUnit.MILLISECONDS));
            return;
        }
    }

    private void scheduleTriggerEvents(ChannelUID channelUID, Calendar now,
            List<ArrivalAndDeparture> arrivalAndDepartures) {
        scheduleTriggerEvents(channelUID, now, arrivalAndDepartures,
                (ArrivalAndDeparture data) -> data.predicted ? data.predictedArrivalTime : data.scheduledArrivalTime,
                EVENT_ARRIVAL);
        scheduleTriggerEvents(channelUID, now, arrivalAndDepartures, new Function<ArrivalAndDeparture, Long>() {
            @Override
            public Long apply(ArrivalAndDeparture data) {
                return data.predicted ? data.predictedDepartureTime : data.scheduledDepartureTime;
            }
        }, EVENT_DEPARTURE);
    }

    private void scheduleTriggerEvents(ChannelUID channelUID, Calendar now,
            List<ArrivalAndDeparture> arrivalAndDepartures, Function<ArrivalAndDeparture, Long> dataPiece,
            String event) {
        ChannelConfig channelConfig = getThing().getChannel(channelUID.getId()).getConfiguration()
                .as(ChannelConfig.class);
        long offsetMs = TimeUnit.SECONDS.toMillis(channelConfig.getOffset());
        for (ArrivalAndDeparture data : arrivalAndDepartures) {
            long time = dataPiece.apply(data);
            // Do not schedule this if it's already passed.
            Calendar cal = (new Calendar.Builder()).setInstant(time - offsetMs).build();
            if (cal.before(now)) {
                continue;
            }

            // Schedule this trigger
            scheduledFutures.add(scheduler.schedule(() -> {
                triggerChannel(channelUID, event);
            }, cal.getTimeInMillis() - now.getTimeInMillis(), TimeUnit.MILLISECONDS));
        }
    }
}
