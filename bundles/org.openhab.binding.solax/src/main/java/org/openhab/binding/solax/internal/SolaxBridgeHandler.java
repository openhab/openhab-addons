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
package org.openhab.binding.solax.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.connectivity.LocalHttpConnector;
import org.openhab.binding.solax.internal.connectivity.rawdata.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.InverterData;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolaxBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SolaxBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SolaxBridgeHandler.class);
    private static final int INITIAL_SCHEDULE_DELAY_SECONDS = 5;

    private SolaxConfiguration config;

    private LocalHttpConnector localHttpConnector;
    private Set<ScheduledFuture<?>> schedules = new HashSet<>();

    public SolaxBridgeHandler(Bridge bridge) {
        super(bridge);

        config = getConfigAs(SolaxConfiguration.class);
        localHttpConnector = new LocalHttpConnector(config.password, config.hostname);
    }

    @Override
    public void initialize() {
        logger.debug("Start initialize()...");
        updateStatus(ThingStatus.UNKNOWN);

        int refreshInterval = config.refresh;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        logger.debug("Scheduling regular interval retrival on every {} {}", refreshInterval, timeUnit);
        ScheduledFuture<?> fixedDelaySchedule = scheduler.scheduleWithFixedDelay(this::retrieveData,
                INITIAL_SCHEDULE_DELAY_SECONDS, refreshInterval, timeUnit);
        schedules.add(fixedDelaySchedule);
    }

    private void retrieveData() {
        try {
            String rawJsonData = localHttpConnector.retrieveData();
            logger.debug("Raw data retrieved = {}", rawJsonData);

            updateInverterData(rawJsonData);

            Bridge bridge = getThing();
            if (bridge.getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            logger.warn("Exception received while attempting to retrieve data via HTTP", e);
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void updateInverterData(@Nullable String rawJsonData) {
        LocalConnectRawDataBean inverterParsedData = LocalConnectRawDataBean.fromJson(rawJsonData);
        Bridge bridge = getThing();
        List<Thing> things = bridge.getThings();
        if (inverterParsedData != null) {
            for (Thing thing : things) {
                ThingHandler handler = thing.getHandler();
                if (handler instanceof InverterDataUpdateListener listener) {
                    listener.updateListener(inverterParsedData);
                }
            }
        } else {
            logger.warn("Parsed bean from the raw JSON data is null. Rawdata={}", rawJsonData);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        schedules.forEach(schedule -> {
            boolean success = schedule.cancel(true);
            String cancelingSuccessful = success ? "successful" : "failed";
            logger.debug("Canceling schedule of {} is {}", schedule, cancelingSuccessful);
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do now
    }

    public @Nullable InverterData scanForInverter() throws IOException {
        String rawJsonData = localHttpConnector.retrieveData();
        logger.debug("Raw data retrieved = {}", rawJsonData);
        return LocalConnectRawDataBean.fromJson(rawJsonData);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(SolaxDiscoveryService.class);
    }
}
