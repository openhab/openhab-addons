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
package org.openhab.binding.electroluxair.internal.handler;

import static org.openhab.binding.electroluxair.internal.ElectroluxAirBindingConstants.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.electroluxair.internal.ElectroluxAirBridgeConfiguration;
import org.openhab.binding.electroluxair.internal.api.ElectroluxDeltaAPI;
import org.openhab.binding.electroluxair.internal.discovery.ElectroluxAirDiscoveryService;
import org.openhab.binding.electroluxair.internal.dto.ElectroluxPureA9DTO;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import com.google.gson.Gson;

/**
 * The {@link ElectroluxAirBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxAirBridgeHandler extends BaseBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private int refreshTimeInSeconds = 300;

    private final Gson gson;
    private final HttpClient httpClient;
    private final Map<String, ElectroluxPureA9DTO> electroluxAirThings = new ConcurrentHashMap<>();

    private @Nullable ElectroluxDeltaAPI api;
    private @Nullable ScheduledFuture<?> refreshJob;

    public ElectroluxAirBridgeHandler(Bridge bridge, HttpClient httpClient, Gson gson) {
        super(bridge);
        this.httpClient = httpClient;
        this.gson = gson;
    }

    @Override
    public void initialize() {
        ElectroluxAirBridgeConfiguration config = getConfigAs(ElectroluxAirBridgeConfiguration.class);

        ElectroluxDeltaAPI electroluxDeltaAPI = new ElectroluxDeltaAPI(config, gson, httpClient);
        refreshTimeInSeconds = config.refresh;

        if (config.username == null || config.password == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration of username, password is mandatory");
        } else if (refreshTimeInSeconds < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Refresh time cannot be negative!");
        } else {
            try {
                this.api = electroluxDeltaAPI;
                scheduler.execute(() -> {
                    updateStatus(ThingStatus.UNKNOWN);
                    startAutomaticRefresh();

                });
            } catch (RuntimeException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    public Map<String, ElectroluxPureA9DTO> getElectroluxAirThings() {
        return electroluxAirThings;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(ElectroluxAirDiscoveryService.class);
    }

    @Override
    public void dispose() {
        stopAutomaticRefresh();
    }

    public @Nullable ElectroluxDeltaAPI getElectroluxDeltaAPI() {
        return api;
    }

    private boolean refreshAndUpdateStatus() {
        if (api != null) {
            if (api.refresh(electroluxAirThings)) {
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
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_STATUS.equals(channelUID.getId()) && command instanceof RefreshType) {
            scheduler.schedule(this::refreshAndUpdateStatus, 1, TimeUnit.SECONDS);
        }
    }
}
