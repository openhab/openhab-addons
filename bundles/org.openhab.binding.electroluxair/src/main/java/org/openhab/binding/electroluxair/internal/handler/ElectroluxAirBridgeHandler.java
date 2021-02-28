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
package org.openhab.binding.electroluxair.internal.handler;

import static org.openhab.binding.electroluxair.internal.ElectroluxAirBindingConstants.THING_TYPE_BRIDGE;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.electroluxair.internal.ElectroluxAirBridgeConfiguration;
import org.openhab.binding.electroluxair.internal.api.ElectroluxDeltaAPI;
import org.openhab.binding.electroluxair.internal.discovery.ElectroluxAirDiscoveryService;
import org.openhab.binding.electroluxair.internal.dto.ElectroluxPureA9DTO;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ElectroluxAirBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxAirBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ElectroluxAirBridgeHandler.class);

    private final Gson gson;
    private final HttpClient httpClient;
    private @Nullable ElectroluxDeltaAPI api;

    private static int REFRESH_SEC = 300;
    private final Map<String, ElectroluxPureA9DTO> electroluxAirThings = new ConcurrentHashMap<>();

    private @Nullable ScheduledFuture<?> refreshJob;
    /**
     * Use cache for refresh command to not update again when call is made within 10 seconds of previous call.
     */
    private final ExpiringCache<Boolean> refreshCache = new ExpiringCache<>(Duration.ofSeconds(10),
            this::refreshAndUpdateStatus);

    public ElectroluxAirBridgeHandler(Bridge bridge, HttpClient httpClient, Gson gson) {
        super(bridge);
        this.httpClient = httpClient;
        this.gson = gson;
    }

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    @Override
    public void initialize() {
        ElectroluxAirBridgeConfiguration config = getConfigAs(ElectroluxAirBridgeConfiguration.class);

        try {
            ElectroluxDeltaAPI electroluxDeltaAPI = new ElectroluxDeltaAPI(config, gson, httpClient);

            if (config.username == null || config.password == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Configuration of username, password and client secret is mandatory");
            } else if (REFRESH_SEC < 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Refresh time cannot negative!");
            } else {
                try {
                    this.api = electroluxDeltaAPI;
                    scheduler.execute(() -> {

                        updateStatus(ThingStatus.UNKNOWN);
                        startAutomaticRefresh();

                    });
                } catch (RuntimeException e) {
                    logger.warn("Failed to initialize: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.warn("Exception caught. {}", e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received: {}", command);
        if (command instanceof RefreshType) {
            refreshCache.getValue();
        }
    }

    public Map<String, ElectroluxPureA9DTO> getElectroluxAirThings() {
        return electroluxAirThings;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(ElectroluxAirDiscoveryService.class);
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        stopAutomaticRefresh();
    }

    public @Nullable ElectroluxDeltaAPI getElectroluxDeltaAPI() {
        return api;
    }

    private boolean refreshAndUpdateStatus() {
        logger.debug("Refresh and update status!");

        if (api != null) {
            if (api.refresh(electroluxAirThings)) {
                logger.debug("Number of entries: {}", electroluxAirThings.size());
                getThing().getThings().stream().forEach(thing -> {
                    ElectroluxAirHandler handler = (ElectroluxAirHandler) thing.getHandler();
                    if (handler != null) {
                        handler.update();
                    }
                });
                updateStatus(ThingStatus.ONLINE);
                return true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
        return false;
    }

    private void startAutomaticRefresh() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        logger.debug("Start automatic refresh {}", refreshJob);
        if (refreshJob == null || refreshJob.isCancelled()) {
            try {
                this.refreshJob = scheduler.scheduleWithFixedDelay(this::refreshAndUpdateStatus, 0, REFRESH_SEC,
                        TimeUnit.SECONDS);
                logger.debug("Scheduling at fixed delay refreshjob {}", this.refreshJob);
            } catch (RejectedExecutionException e) {
                logger.warn("Automatic refresh job cannot be started!");
            }
        }
    }

    private void stopAutomaticRefresh() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        logger.debug("Stop automatic refresh for job {}", refreshJob);
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }
}
