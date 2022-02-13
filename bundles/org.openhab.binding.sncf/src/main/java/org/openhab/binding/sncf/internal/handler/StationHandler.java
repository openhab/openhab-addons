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
import org.openhab.core.thing.ThingStatusInfo;
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
        if (stationId == null || stationId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/null-or-empty-station-id");
            return;
        }

        if (thing.getProperties().isEmpty() && !discoverAttributes(stationId)) {
            return;
        }

        String timezone = thing.getProperties().get(TIMEZONE);
        if (timezone == null || timezone.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/null-or-empty-timezone");
            return;
        }

        zoneOffset = ZoneId.of(timezone).getRules().getOffset(Instant.now()).getId().replace(":", "");
        scheduleRefresh(ZonedDateTime.now().plusSeconds(2));
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (thing.getStatus() == ThingStatus.ONLINE) {
            initialize();
        }
    }

    private boolean discoverAttributes(String localStation) {
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
                return true;
            } catch (SncfException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        return false;
    }

    private void scheduleRefresh(@Nullable ZonedDateTime when) {
        // Ensure we'll try to refresh in one minute if no valid timestamp is provided
        long wishedDelay = ZonedDateTime.now().until(when != null ? when : ZonedDateTime.now().plusMinutes(1),
                ChronoUnit.SECONDS);
        wishedDelay = wishedDelay < 0 ? 60 : wishedDelay;
        logger.debug("wishedDelay is {} seconds", wishedDelay);
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            long existingDelay = job.getDelay(TimeUnit.SECONDS);
            logger.debug("existingDelay is {} seconds", existingDelay);
            if (existingDelay < wishedDelay && existingDelay > 0) {
                logger.debug("Do nothing, existingDelay earlier than wishedDelay");
                return;
            }
            freeRefreshJob();
        }
        logger.debug("Scheduling update in {} seconds.", wishedDelay);
        refreshJob = scheduler.schedule(() -> updateThing(), wishedDelay, TimeUnit.SECONDS);
    }

    private void updateThing() {
        SncfBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            scheduler.submit(() -> {
                updatePassage(bridgeHandler, GROUP_ARRIVAL);
                updatePassage(bridgeHandler, GROUP_DEPARTURE);
            });
        }
    }

    private void updatePassage(SncfBridgeHandler bridgeHandler, String direction) {
        try {
            bridgeHandler.getNextPassage(stationId, direction).ifPresentOrElse(passage -> {
                getThing().getChannels().stream().map(Channel::getUID)
                        .filter(channelUID -> isLinked(channelUID) && direction.equals(channelUID.getGroupId()))
                        .forEach(channelUID -> {
                            State state = getValue(channelUID.getIdWithoutGroup(), passage, direction);
                            updateState(channelUID, state);
                        });
                ZonedDateTime eventTime = getEventTimestamp(passage, direction);
                if (eventTime != null) {
                    scheduleRefresh(eventTime.plusSeconds(10));
                }
            }, () -> {
                logger.debug("No {} available", direction);
                scheduleRefresh(ZonedDateTime.now().plusMinutes(5));
            });
            updateStatus(ThingStatus.ONLINE);
        } catch (SncfException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            freeRefreshJob();
        }
    }

    private State getValue(String channelId, Passage passage, String direction) {
        switch (channelId) {
            case DIRECTION:
                return fromNullableString(passage.displayInformations.direction);
            case LINE_NAME:
                return fromNullableString(String.format("%s %s", passage.displayInformations.commercialMode,
                        passage.displayInformations.code));
            case NAME:
                return fromNullableString(passage.displayInformations.name);
            case NETWORK:
                return fromNullableString(passage.displayInformations.network);
            case TIMESTAMP:
                return fromNullableTime(passage, direction);
        }
        return UnDefType.NULL;
    }

    private State fromNullableString(@Nullable String aValue) {
        return aValue != null ? StringType.valueOf(aValue) : UnDefType.NULL;
    }

    private @Nullable ZonedDateTime getEventTimestamp(Passage passage, String direction) {
        String eventTime = direction.equals(GROUP_ARRIVAL) ? passage.stopDateTime.arrivalDateTime
                : passage.stopDateTime.departureDateTime;
        return eventTime != null ? ZonedDateTime.parse(eventTime + zoneOffset, NAVITIA_DATE_FORMAT) : null;
    }

    private State fromNullableTime(Passage passage, String direction) {
        ZonedDateTime timestamp = getEventTimestamp(passage, direction);
        return timestamp != null ? new DateTimeType(timestamp) : UnDefType.NULL;
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
            updateThing();
        }
    }

    private @Nullable SncfBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler.getThing().getStatus() == ThingStatus.ONLINE) {
                    return (SncfBridgeHandler) handler;
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                    return null;
                }
            }
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        return null;
    }
}
