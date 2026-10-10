/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.transitapp.internal.handler;

import static org.openhab.binding.transitapp.internal.TransitAppBindingConstants.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transitapp.internal.action.TransitActions;
import org.openhab.binding.transitapp.internal.config.TransitAppStopConfiguration;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult.MergedItinerary;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult.RouteDeparture;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult.ScheduleItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TransitAppStopHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppStopHandler.class);

    private final Map<String, String> latestLineDepartures = new ConcurrentHashMap<>();
    private TransitAppStopConfiguration config = new TransitAppStopConfiguration();
    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitAppStopHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(TransitAppStopConfiguration.class);
        if (config.globalStopId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-stop-id");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            startPolling();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN);
            startPolling();
        } else {
            stopPolling();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private void startPolling() {
        ScheduledFuture<?> job = refreshJob;
        if (job == null || job.isCancelled()) {
            long refreshInterval = Math.max(30L, config.refreshInterval);
            refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void stopPolling() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::pollTransitApi);
        }
    }

    private synchronized void pollTransitApi() {
        Bridge localBridge = getBridge();
        if (localBridge == null || localBridge.getStatus() != ThingStatus.ONLINE) {
            return;
        }

        try {
            TransitAppBridgeHandler bridgeHandler = getTransitBridgeHandler();
            if (bridgeHandler == null) {
                return;
            }

            StopDeparturesResult result = bridgeHandler.getStopDepartures(config.globalStopId);
            updateStatus(ThingStatus.ONLINE);

            long nowSeconds = System.currentTimeMillis() / 1000;
            latestLineDepartures.clear();

            int groupIdx = processDepartures(result.getRouteDepartures(), nowSeconds, bridgeHandler.getMaxDepartures());
            clearRemainingDepartures(groupIdx);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            latestLineDepartures.clear();
            String errorMessage = e.getMessage();
            logger.debug("Communication issue: {}", errorMessage);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            clearRemainingDepartures(1);
        }
    }

    private int processDepartures(List<RouteDeparture> routeDepartures, long currentTimeSeconds, int maxDepartures) {
        if (routeDepartures.isEmpty()) {
            return 1;
        }

        List<FlattenedDeparture> allDepartures = new ArrayList<>();

        for (RouteDeparture routeDep : routeDepartures) {
            String sName = routeDep.getRouteShortName();
            final String shortName = sName != null ? sName : "";

            String lName = routeDep.getRouteLongName();
            final String longName = lName != null ? lName : "";

            for (MergedItinerary mergedItinerary : routeDep.getMergedItineraries()) {
                for (ScheduleItem schedule : mergedItinerary.getScheduleItems()) {
                    Instant depTime = schedule.getDepartureTime();
                    if (depTime == null || depTime.getEpochSecond() <= currentTimeSeconds) {
                        continue;
                    }

                    allDepartures.add(new FlattenedDeparture(depTime, shortName, longName, schedule));
                }
            }
        }

        allDepartures.sort(Comparator.comparing(a -> a.departureTime));

        int groupIdx = 1;
        for (FlattenedDeparture dep : allDepartures) {
            if (groupIdx > maxDepartures) {
                break;
            }

            long minutesUntilDeparture = (dep.departureTime.getEpochSecond() - currentTimeSeconds) / 60;
            updateDepartureState(groupIdx, dep.routeShortName, dep.routeLongName, dep.departureTime,
                    minutesUntilDeparture, dep.schedule);
            groupIdx++;
        }

        return groupIdx;
    }

    private static class FlattenedDeparture {
        final Instant departureTime;
        final String routeShortName;
        final String routeLongName;
        final ScheduleItem schedule;

        FlattenedDeparture(Instant departureTime, String routeShortName, String routeLongName, ScheduleItem schedule) {
            this.departureTime = departureTime;
            this.routeShortName = routeShortName;
            this.routeLongName = routeLongName;
            this.schedule = schedule;
        }
    }

    private void updateDepartureState(int groupIdx, String shortName, String longName, Instant depTime,
            long minutesUntilDeparture, ScheduleItem schedule) {
        String group = CHANNEL_GROUP_DEPARTURE_PREFIX + groupIdx;

        updateGroupState(group, CHANNEL_ROUTE_SHORT_NAME,
                shortName.isEmpty() ? UnDefType.UNDEF : new StringType(shortName));
        updateGroupState(group, CHANNEL_ROUTE_LONG_NAME,
                longName.isEmpty() ? UnDefType.UNDEF : new StringType(longName));
        updateGroupState(group, CHANNEL_MINUTES_UNTIL_DEPARTURE,
                new QuantityType<>(minutesUntilDeparture, Units.MINUTE));
        updateGroupState(group, CHANNEL_DEPARTURE_TIME,
                new DateTimeType(ZonedDateTime.ofInstant(depTime, ZoneId.systemDefault())));

        if (!shortName.isEmpty() && !latestLineDepartures.containsKey(shortName)) {
            latestLineDepartures.put(shortName, LocalTime.ofInstant(depTime, ZoneId.systemDefault()).toString());
        }

        Instant scheduledDepTime = schedule.getScheduledDepartureTime();
        long delayMinutes = 0;
        if (scheduledDepTime != null) {
            long delaySeconds = depTime.getEpochSecond() - scheduledDepTime.getEpochSecond();
            delayMinutes = delaySeconds / 60;
        }

        updateGroupState(group, CHANNEL_DELAY_MINUTES, new QuantityType<>(delayMinutes, Units.MINUTE));

        Integer wheelchair = schedule.getWheelchairAccessible();
        if (wheelchair != null && wheelchair == 1) {
            updateGroupState(group, CHANNEL_WHEELCHAIR_ACCESSIBLE, OnOffType.ON);
        } else if (wheelchair != null && wheelchair == 2) {
            updateGroupState(group, CHANNEL_WHEELCHAIR_ACCESSIBLE, OnOffType.OFF);
        } else {
            updateGroupState(group, CHANNEL_WHEELCHAIR_ACCESSIBLE, UnDefType.UNDEF);
        }

        Boolean cancelled = schedule.getIsCancelled();
        if (cancelled == null) {
            updateGroupState(group, CHANNEL_IS_CANCELLED, UnDefType.UNDEF);
        } else {
            updateGroupState(group, CHANNEL_IS_CANCELLED, OnOffType.from(cancelled));
        }
    }

    private void clearRemainingDepartures(int startIdx) {
        for (int i = startIdx; i <= MAX_CHANNEL_GROUPS; i++) {
            String group = CHANNEL_GROUP_DEPARTURE_PREFIX + i;
            updateGroupState(group, CHANNEL_ROUTE_SHORT_NAME, UnDefType.UNDEF);
            updateGroupState(group, CHANNEL_ROUTE_LONG_NAME, UnDefType.UNDEF);
            updateGroupState(group, CHANNEL_DEPARTURE_TIME, UnDefType.UNDEF);
            updateGroupState(group, CHANNEL_MINUTES_UNTIL_DEPARTURE, UnDefType.UNDEF);
            updateGroupState(group, CHANNEL_DELAY_MINUTES, UnDefType.UNDEF);
            updateGroupState(group, CHANNEL_WHEELCHAIR_ACCESSIBLE, UnDefType.UNDEF);
            updateGroupState(group, CHANNEL_IS_CANCELLED, UnDefType.UNDEF);
        }
    }

    private void updateGroupState(String group, String channel, State state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), state);
    }

    public String findNextDepartureByLine(String lineName) {
        return latestLineDepartures.getOrDefault(lineName, "N/A");
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.<Class<? extends ThingHandlerService>> singleton(TransitActions.class);
    }

    public @Nullable TransitAppBridgeHandler getTransitBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof TransitAppBridgeHandler bridgeHandler) {
            return bridgeHandler;
        }
        return null;
    }

    @Override
    public void dispose() {
        stopPolling();
        super.dispose();
    }
}
