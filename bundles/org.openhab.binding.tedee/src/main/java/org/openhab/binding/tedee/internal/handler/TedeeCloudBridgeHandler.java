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

package org.openhab.binding.tedee.internal.handler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tedee.internal.api.TedeeApiException;
import org.openhab.binding.tedee.internal.api.TedeeClient;
import org.openhab.binding.tedee.internal.api.TedeeCloudApi;
import org.openhab.binding.tedee.internal.configuration.TedeeCloudConfiguration;
import org.openhab.binding.tedee.internal.discovery.TedeeCloudDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Configuration for the Tedee Cloud Bridge.
 *
 * @author Alex Goll - Initial contribution
 */

@NonNullByDefault
public class TedeeCloudBridgeHandler extends BaseBridgeHandler implements TedeeTransportProvider {

    private final HttpClient client;
    private final Logger logger = LoggerFactory.getLogger(TedeeCloudBridgeHandler.class);

    private @Nullable TedeeCloudApi api;
    private @Nullable ScheduledFuture<?> job;
    private TedeeCloudConfiguration cfg = new TedeeCloudConfiguration();

    public TedeeCloudBridgeHandler(Bridge bridge, HttpClient client) {
        super(bridge);
        this.client = client;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(TedeeCloudDiscoveryService.class);
    }

    @Override
    public void initialize() {
        cfg = getConfigAs(TedeeCloudConfiguration.class);

        if (cfg.personalAccessKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Personal Access Key required");
            return;
        }

        api = new TedeeCloudApi(client, cfg.personalAccessKey);

        scheduler.execute(this::check);

        job = scheduler.scheduleWithFixedDelay(this::check, cfg.pollInterval, cfg.pollInterval, TimeUnit.SECONDS);
    }

    @Override
    public @Nullable TedeeClient getClient() {
        return api;
    }

    @Override
    public boolean ready() {
        return api != null && getThing().getStatus() == ThingStatus.ONLINE;
    }

    private void check() {
        TedeeCloudApi currentApi = api;

        if (currentApi == null) {
            return;
        }

        try {
            currentApi.getLocks();
            updateStatus(ThingStatus.ONLINE);
        } catch (TedeeApiException e) {
            logger.warn("Tedee Cloud check failed: {}", e.getMessage());

            ThingStatusDetail detail = e.getStatus() == 401 || e.getStatus() == 403
                    ? ThingStatusDetail.CONFIGURATION_ERROR
                    : ThingStatusDetail.COMMUNICATION_ERROR;

            updateStatus(ThingStatus.OFFLINE, detail, e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Unexpected error while checking Tedee Cloud", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channel, Command command) {
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> currentJob = job;

        if (currentJob != null) {
            currentJob.cancel(true);
        }

        api = null;
        super.dispose();
    }
}
