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
import java.util.concurrent.ScheduledExecutorService;
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
    protected ScheduledExecutorService executorService = this.scheduler;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @NonNullByDefault({}) TeslascopeWebTargets webTargets;
    private String apiKey = "";

    private final Gson gson = new Gson();

    public TeslascopeAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        config = getConfigAs(TeslascopeAccountConfiguration.class);
        webTargets = new TeslascopeWebTargets(httpClient);
    }

    public String getApiKey() {
        return apiKey;
    }

    public ThingUID getUID() {
        logger.info("thing.getUID() = {}", thing.getUID());
        return thing.getUID();
    }

    public String getVehicleList() {
        try {
            return webTargets.getVehicleList(apiKey);
        } catch (TeslascopeAuthenticationException e) {
            logger.debug("Unexpected authentication error connecting to Teslascope API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return "";
        } catch (TeslascopeCommunicationException e) {
            logger.debug("Unexpected error connecting to Teslascope API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return "";
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing apiKey configuration");
            return;
        }
        apiKey = config.apiKey;
        updateStatus(ThingStatus.UNKNOWN);
        pollingJob = executorService.scheduleWithFixedDelay(this::pollingCode, 0, config.refreshInterval,
                TimeUnit.SECONDS);
    }

    protected void pollData() {

        String responseVehicleList = getVehicleList();
        JsonArray jsonArrayVehicleList = JsonParser.parseString(responseVehicleList).getAsJsonArray();
        VehicleList vehicleList = gson.fromJson(jsonArrayVehicleList.get(0), VehicleList.class);
        if (vehicleList == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to retrieve Vehicle List");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * The actual polling loop
     */
    protected void pollingCode() {
        pollData();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(TeslascopeVehicleDiscoveryService.class);
    }
}
