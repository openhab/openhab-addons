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
package org.openhab.binding.teslascope.internal;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.teslascope.internal.api.VehicleList;
import org.openhab.binding.teslascope.internal.discovery.TeslascopeVehicleDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

/**
 * The {@link TeslascopeAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author paul@smedley.id.au - Initial contribution
 */
@NonNullByDefault
public class TeslascopeAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TeslascopeAccountHandler.class);

    private @Nullable TeslascopeAccountConfiguration config;
    private @Nullable ScheduledFuture<?> pollFuture;

    private final TeslascopeWebTargets webTargets;

    private final Gson gson = new Gson();

    public TeslascopeAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        webTargets = new TeslascopeWebTargets(httpClient);
    }

    public ThingUID getUID() {
        return thing.getUID();
    }

    public String getVehicleList() {
        try {
            return webTargets.getVehicleList(config.apiKey);
        } catch (TeslascopeAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Authentication problem: " + e.getMessage());
            return "";
        } catch (TeslascopeCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication problem: " + e.getMessage());
            return "";
        }
    }

    public String getDetailedInformation(String publicID) {
        try {
            return webTargets.getDetailedInformation(publicID, config.apiKey);
        } catch (TeslascopeAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Authentication problem: " + e.getMessage());
            return "";
        } catch (TeslascopeCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication problem: " + e.getMessage());
            return "";
        }
    }

    public void sendCommand(String publicID, String command) {
        try {
            webTargets.sendCommand(publicID, config.apiKey, command);
        } catch (TeslascopeAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Authentication problem: " + e.getMessage());
        } catch (TeslascopeCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication problem: " + e.getMessage());
        }
    }

    public void sendCommand(String publicID, String command, String params) {
        try {
            webTargets.sendCommand(publicID, config.apiKey, command, params);
        } catch (TeslascopeAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Authentication problem: " + e.getMessage());
        } catch (TeslascopeCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication problem: " + e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // we do not have any channels -> nothing to do here
    }

    @Override
    public void initialize() {
        config = getConfigAs(TeslascopeAccountConfiguration.class);
        if (config.apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-api-key");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);

        this.pollFuture = scheduler.scheduleWithFixedDelay(this::pollStatus, 0, 300, TimeUnit.SECONDS);

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    private void pollStatus() {
        String responseVehicleList = getVehicleList();
        JsonArray jsonArrayVehicleList = JsonParser.parseString(responseVehicleList).getAsJsonArray();
        if (jsonArrayVehicleList.size() > 0) {
            VehicleList vehicleList = gson.fromJson(jsonArrayVehicleList.get(0), VehicleList.class);
            if (vehicleList == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error.no-vehicles");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.no-vehicles");
        }
    }

    private void stopPoll() {
        final Future<?> future = pollFuture;
        if (future != null) {
            future.cancel(true);
            pollFuture = null;
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(TeslascopeVehicleDiscoveryService.class);
    }
}
