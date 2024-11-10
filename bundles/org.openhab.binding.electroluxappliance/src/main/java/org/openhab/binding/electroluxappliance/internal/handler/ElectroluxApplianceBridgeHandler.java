/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.electroluxappliance.internal.handler;

import static org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceBindingConstants.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceBridgeConfiguration;
import org.openhab.binding.electroluxappliance.internal.api.ElectroluxGroupAPI;
import org.openhab.binding.electroluxappliance.internal.discovery.ElectroluxApplianceDiscoveryService;
import org.openhab.binding.electroluxappliance.internal.dto.ApplianceDTO;
import org.openhab.binding.electroluxappliance.internal.listener.TokenUpdateListener;
import org.openhab.core.config.core.Configuration;
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
 * The {@link ElectroluxApplianceBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxApplianceBridgeHandler extends BaseBridgeHandler implements TokenUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(ElectroluxApplianceBridgeHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private int refreshTimeInSeconds = 300;
    private boolean isCommunicationError = false;

    private final Gson gson;
    private final HttpClient httpClient;
    private final Map<String, ApplianceDTO> electroluxApplianceThings = new ConcurrentHashMap<>();

    private @Nullable ElectroluxGroupAPI api;
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> instantUpdate;

    public ElectroluxApplianceBridgeHandler(Bridge bridge, HttpClient httpClient, Gson gson) {
        super(bridge);
        this.httpClient = httpClient;
        this.gson = gson;
    }

    @Override
    public void initialize() {
        ElectroluxApplianceBridgeConfiguration config = getConfigAs(ElectroluxApplianceBridgeConfiguration.class);

        refreshTimeInSeconds = config.refresh;

        if (config.apiKey.isBlank() || config.refreshToken.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration of API key, access and refresh token is mandatory");
        } else if (refreshTimeInSeconds < 10) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Refresh time cannot be less than 10!");
        } else {
            try {
                this.api = new ElectroluxGroupAPI(config, gson, httpClient, this);
                scheduler.execute(() -> {
                    updateStatus(ThingStatus.UNKNOWN);
                    startAutomaticRefresh();
                });
            } catch (RuntimeException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public void onTokenUpdated(@Nullable String newRefreshToken) {
        // Create a new configuration object with the updated tokens
        Configuration configuration = editConfiguration();
        configuration.put("refreshToken", newRefreshToken);
        // Update the configuration
        updateConfiguration(configuration);
    }

    public Map<String, ApplianceDTO> getElectroluxApplianceThings() {
        return electroluxApplianceThings;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(ElectroluxApplianceDiscoveryService.class);
    }

    @Override
    public void dispose() {
        stopAutomaticRefresh();
    }

    public @Nullable ElectroluxGroupAPI getElectroluxDeltaAPI() {
        return api;
    }

    private boolean refreshAndUpdateStatus() {
        if (api != null) {
            if (api.refresh(electroluxApplianceThings, isCommunicationError)) {
                getThing().getThings().stream().forEach(thing -> {
                    ElectroluxApplianceHandler handler = (ElectroluxApplianceHandler) thing.getHandler();
                    if (handler != null) {
                        handler.update();
                    }
                });
                updateStatus(ThingStatus.ONLINE);
                isCommunicationError = false;
                return true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                isCommunicationError = true;
            }
        }
        return false;
    }

    private void startAutomaticRefresh() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob == null || refreshJob.isCancelled()) {
            this.refreshJob = scheduler.scheduleWithFixedDelay(this::refreshAndUpdateStatus, 0, refreshTimeInSeconds,
                    TimeUnit.SECONDS);
        }
    }

    private void stopAutomaticRefresh() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
        refreshJob = this.instantUpdate;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }

    private synchronized void updateNow() {
        Future<?> localRef = instantUpdate;
        if (localRef == null || localRef.isDone()) {
            instantUpdate = scheduler.schedule(this::refreshAndUpdateStatus, 0, TimeUnit.SECONDS);
        } else {
            logger.debug("Already waiting for scheduled refresh");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received: {} on channelID: {}", command, channelUID);
        if (CHANNEL_STATUS.equals(channelUID.getId()) || command instanceof RefreshType) {
            updateNow();
        }
    }
}
