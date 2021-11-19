/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.sncf.internal.handler;

import static org.openhab.binding.sncf.internal.SncfBindingConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sncf.internal.SncfException;
import org.openhab.binding.sncf.internal.dto.Passage;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link StationHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class StationHandler extends BaseThingHandler {
    private static final DateTimeFormatter NAVITIA_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssZ");

    private final Logger logger = LoggerFactory.getLogger(StationHandler.class);
    private final LocationProvider locationProvider;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) String stationId;
    private @NonNullByDefault({}) String zoneOffset;

    public StationHandler(Thing thing, LocationProvider locationProvider) {
        super(thing);
        this.locationProvider = locationProvider;
    }

    @Override
    public void initialize() {
        logger.trace("Initializing the Station handler for {}", getThing().getUID());

        stationId = (String) getConfig().get("stopPointId");

        if (thing.getProperties().isEmpty()) {
            discoverAttributes(stationId);
        }
        zoneOffset = ZoneId.of(thing.getProperties().get(TIMEZONE)).getRules().getOffset(Instant.now()).getId()
                .replace(":", "");

        updateStatus(ThingStatus.ONLINE);
        scheduleRefresh(ZonedDateTime.now().plusSeconds(2));
    }

    private void discoverAttributes(String localStation) {
        SncfBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            Map<String, String> properties = new HashMap<>();
            try {
                bridgeHandler.stopPointDetail(localStation).ifPresent(stopPoint -> {
                    String stationLoc = String.format("%s,%s", stopPoint.coord.lat, stopPoint.coord.lon);
                    properties.put(LOCATION, stationLoc);
                    properties.put(TIMEZONE, stopPoint.stopArea.timezone);
                    PointType serverLoc = locationProvider.getLocation();
                    if (serverLoc != null) {
                        PointType stationLocation = new PointType(stationLoc);
                        double distance = serverLoc.distanceFrom(stationLocation).doubleValue();
                        properties.put(DISTANCE, new QuantityType<>(distance, SIUnits.METRE).toString());
                    }
                });
                ThingBuilder thingBuilder = editThing();
                thingBuilder.withProperties(properties);
                updateThing(thingBuilder.build());
            } catch (SncfException e) {
                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private void scheduleRefresh(ZonedDateTime when) {
        long wishedDelay = ZonedDateTime.now().until(when, ChronoUnit.SECONDS);
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            long existingDelay = job.getDelay(TimeUnit.SECONDS);
            if (existingDelay < wishedDelay) {
                return;
            }
            freeRefreshJob();
        }
        refreshJob = scheduler.schedule(this::queryApiAndUpdateChannels, wishedDelay, TimeUnit.SECONDS);
    }

    private void queryApiAndUpdateChannels() {
        SncfBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            try {
                bridgeHandler.getNextPassage(stationId, GROUP_DEPARTURE).ifPresent(departure -> {
                    getThing().getChannels().stream().map(Channel::getUID).filter(
                            channelUID -> isLinked(channelUID) && GROUP_DEPARTURE.equals(channelUID.getGroupId()))
                            .forEach(channelUID -> {
                                State state = getValue(channelUID.getIdWithoutGroup(), departure, GROUP_DEPARTURE);
                                updateState(channelUID, state);
                            });
                    scheduleRefresh(fromDTO(departure.stopDateTime.departureDateTime));
                });
                bridgeHandler.getNextPassage(stationId, GROUP_ARRIVAL).ifPresent(arrival -> {
                    getThing().getChannels().stream().map(Channel::getUID)
                            .filter(channelUID -> isLinked(channelUID) && GROUP_ARRIVAL.equals(channelUID.getGroupId()))
                            .forEach(channelUID -> {
                                State state = getValue(channelUID.getIdWithoutGroup(), arrival, GROUP_ARRIVAL);
                                updateState(channelUID, state);
                            });
                    scheduleRefresh(fromDTO(arrival.stopDateTime.arrivalDateTime));
                });
                updateStatus(ThingStatus.ONLINE);
            } catch (SncfException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                freeRefreshJob();
            }
        }
    }

    private State getValue(String channelId, Passage passage, String groupDeparture) {
        switch (channelId) {
            case DIRECTION:
                return new StringType(passage.route.direction.name);
            case CODE:
                return new StringType(passage.displayInformations.code);
            case COMMERCIAL_MODE:
                return new StringType(passage.displayInformations.commercialMode);
            case NAME:
                return new StringType(passage.displayInformations.name);
            case NETWORK:
                return new StringType(passage.displayInformations.network);
            case TIMESTAMP:
                return new DateTimeType(
                        fromDTO(groupDeparture.equals(GROUP_ARRIVAL) ? passage.stopDateTime.arrivalDateTime
                                : passage.stopDateTime.departureDateTime));
        }
        return UnDefType.NULL;
    }

    private ZonedDateTime fromDTO(String dateTime) {
        return ZonedDateTime.parse(dateTime + zoneOffset, NAVITIA_DATE_FORMAT);
    }

    private void freeRefreshJob() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
            this.refreshJob = null;
        }
    }

    @Override
    public void dispose() {
        freeRefreshJob();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            queryApiAndUpdateChannels();
        }
    }

    private @Nullable SncfBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler.getThing().getStatus() == ThingStatus.ONLINE) {
                    return (SncfBridgeHandler) handler;
                }
            }
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        return null;
    }
}
