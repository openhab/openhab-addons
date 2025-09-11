/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.airparif.internal.handler;

import static org.openhab.binding.airparif.internal.AirParifBindingConstants.*;

import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airparif.internal.api.AirParifApi.Pollen;
import org.openhab.binding.airparif.internal.api.AirParifDto.Concentration;
import org.openhab.binding.airparif.internal.api.AirParifDto.PollensResponse;
import org.openhab.binding.airparif.internal.api.AirParifDto.Route;
import org.openhab.binding.airparif.internal.api.PollenAlertLevel;
import org.openhab.binding.airparif.internal.api.Pollutant;
import org.openhab.binding.airparif.internal.config.LocationConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LocationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LocationHandler extends BaseThingHandler implements HandlerUtils {
    private static final String AQ_JOB = "Local Air Quality";

    private final Logger logger = LoggerFactory.getLogger(LocationHandler.class);
    private final Map<String, ScheduledFuture<?>> jobs = new HashMap<>();

    private Map<Pollen, PollenAlertLevel> myPollens = Map.of();
    private @Nullable LocationConfiguration config;

    public LocationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(LocationConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        schedule(AQ_JOB, this::getConcentrations, Duration.ofSeconds(2));
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the AirParif bridge handler.");
        cleanJobs();
    }

    public void setPollens(PollensResponse pollens) {
        LocationConfiguration local = config;
        if (local != null) {
            updatePollenChannels(pollens.getDepartment(local.department));
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void updatePollenChannels(Map<Pollen, PollenAlertLevel> pollens) {
        ChannelGroupUID pollensUID = new ChannelGroupUID(thing.getUID(), GROUP_POLLENS);
        myPollens = pollens;
        pollens.forEach((pollen, level) -> updateState(new ChannelUID(pollensUID, pollen.name().toLowerCase()),
                new DecimalType(level.ordinal())));
    }

    private void getConcentrations() {
        AirParifBridgeHandler apiHandler = getBridgeHandler(AirParifBridgeHandler.class);
        LocationConfiguration local = config;
        long delay = 3600;
        if (apiHandler != null && local != null) {
            if (myPollens.isEmpty()) {
                updatePollenChannels(apiHandler.requestPollens(local.department));
            }

            Route route = apiHandler.getConcentrations(local.location);
            if (route != null) {
                int maxAlert = route.concentrations().stream().filter(conc -> conc.pollutant().hasUnit())
                        .map(Concentration::getAlertLevel).max(Comparator.comparing(Integer::valueOf)).get();

                route.concentrations().stream().forEach(concentration -> {
                    Pollutant pollutant = concentration.pollutant();
                    ChannelGroupUID groupUID = new ChannelGroupUID(thing.getUID(), pollutant.name().toLowerCase());
                    updateState(new ChannelUID(groupUID, CHANNEL_MESSAGE), concentration.getMessage());
                    if (!pollutant.hasUnit()) {
                        updateState(new ChannelUID(groupUID, CHANNEL_TIMESTAMP), concentration.getDate());
                        updateState(new ChannelUID(groupUID, CHANNEL_ALERT), new DecimalType(maxAlert));
                    } else {
                        updateState(new ChannelUID(groupUID, CHANNEL_VALUE), concentration.getQuantity());
                        updateState(new ChannelUID(groupUID, CHANNEL_ALERT),
                                new DecimalType(concentration.getAlertLevel()));
                    }
                });
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            delay = 10;
        }
        schedule(AQ_JOB, this::getConcentrations, Duration.ofSeconds(delay));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("This thing does not handle commands");
    }

    @Override
    public @Nullable Bridge getBridge() {
        return super.getBridge();
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Map<String, ScheduledFuture<?>> getJobs() {
        return jobs;
    }
}
