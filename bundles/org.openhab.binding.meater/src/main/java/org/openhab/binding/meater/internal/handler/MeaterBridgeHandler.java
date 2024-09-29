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
package org.openhab.binding.meater.internal.handler;

import static org.openhab.binding.meater.internal.MeaterBindingConstants.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.meater.internal.MeaterBridgeConfiguration;
import org.openhab.binding.meater.internal.api.MeaterRestAPI;
import org.openhab.binding.meater.internal.discovery.MeaterDiscoveryService;
import org.openhab.binding.meater.internal.dto.MeaterProbeDTO;
import org.openhab.binding.meater.internal.exceptions.MeaterException;
import org.openhab.core.i18n.LocaleProvider;
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
 * The {@link MeaterBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class MeaterBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(MeaterBridgeHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private final Gson gson;
    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;
    private final Map<String, MeaterProbeDTO.Device> meaterProbeThings = new ConcurrentHashMap<>();

    private int refreshTimeInSeconds = 300;
    private @Nullable MeaterRestAPI api;
    private @Nullable ScheduledFuture<?> refreshJob;

    public MeaterBridgeHandler(Bridge bridge, HttpClient httpClient, Gson gson, LocaleProvider localeProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.gson = gson;
        this.localeProvider = localeProvider;
    }

    @Override
    public void initialize() {
        MeaterBridgeConfiguration config = getConfigAs(MeaterBridgeConfiguration.class);

        api = new MeaterRestAPI(config, gson, httpClient, localeProvider);
        refreshTimeInSeconds = config.refresh;

        if (config.email.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/config.missing-username-password.description");
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            scheduler.execute(() -> {
                startAutomaticRefresh();
            });
        }
    }

    public Map<String, MeaterProbeDTO.Device> getMeaterThings() {
        return meaterProbeThings;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MeaterDiscoveryService.class);
    }

    @Override
    public void dispose() {
        stopAutomaticRefresh();
        meaterProbeThings.clear();
    }

    private void refreshAndUpdateStatus() {
        MeaterRestAPI localAPI = api;
        if (localAPI == null) {
            return;
        }

        try {
            localAPI.refresh(meaterProbeThings);
            updateStatus(ThingStatus.ONLINE);
            getThing().getThings().stream().forEach(thing -> {
                MeaterHandler handler = (MeaterHandler) thing.getHandler();
                if (handler != null) {
                    handler.update();
                }
            });
        } catch (MeaterException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
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
        logger.debug("Command received: {}", command);
        if (command instanceof RefreshType) {
            if (channelUID.getId().equals(CHANNEL_STATUS)) {
                logger.debug("Refresh command on status channel {} will trigger instant refresh", channelUID);
                refreshAndUpdateStatus();
            }
        }
    }
}
