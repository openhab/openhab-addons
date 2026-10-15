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
import org.openhab.binding.tedee.internal.api.TedeeCloudApi;
import org.openhab.binding.tedee.internal.api.TedeeLock;
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
 * Handles the Tedee Cloud bridge, including API communication, connection
 * monitoring, and access to Tedee lock operations for child Things.
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

    /**
     * Creates a handler for the Tedee Cloud bridge.
     *
     * @param bridge the Tedee Cloud bridge Thing
     * @param client the HTTP client used for API communication
     */
    public TedeeCloudBridgeHandler(Bridge bridge, HttpClient client) {
        super(bridge);
        this.client = client;
    }

    /**
     * Returns the handler services provided by this bridge.
     *
     * @return the collection of supported handler service classes
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(TedeeCloudDiscoveryService.class);
    }

    /**
     * Initializes the cloud bridge, validates its configuration, creates the
     * API client, and schedules periodic connectivity checks.
     */
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

    private TedeeCloudApi apiOrThrow() {
        TedeeCloudApi currentApi = api;
        if (currentApi == null) {
            throw new IllegalStateException("Tedee Cloud API is not initialized");
        }
        return currentApi;
    }

    /**
     * Retrieves information about a Tedee lock through the cloud API.
     *
     * @param id the identifier of the lock
     * @return the lock information
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public TedeeLock getLock(int id) throws TedeeApiException {
        return apiOrThrow().getLock(id);
    }

    /**
     * Retrieves the Tedee locks accessible through the cloud account.
     *
     * @return the list of accessible locks
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public List<TedeeLock> getLocks() throws TedeeApiException {
        return apiOrThrow().getLocks();
    }

    /**
     * Sends a command to lock the specified Tedee lock.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public void lock(int id) throws TedeeApiException {
        apiOrThrow().lock(id);
    }

    /**
     * Sends a command to unlock the specified Tedee lock.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public void unlock(int id) throws TedeeApiException {
        apiOrThrow().unlock(id);
    }

    /**
     * Sends a command to unlock the specified Tedee lock without pulling the
     * latch.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public void unlockWithoutPull(int id) throws TedeeApiException {
        apiOrThrow().unlockWithoutPull(id);
    }

    /**
     * Sends a command to pull the latch of the specified Tedee lock.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public void pull(int id) throws TedeeApiException {
        apiOrThrow().pull(id);
    }

    /**
     * Sends a command to unlock the specified Tedee lock or pull the latch,
     * according to the supported device operation.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public void unlockOrPull(int id) throws TedeeApiException {
        apiOrThrow().unlockOrPull(id);
    }

    /**
     * Indicates whether the cloud bridge is initialized and online.
     *
     * @return {@code true} if the API client is initialized and the bridge is online;
     *         {@code false} otherwise
     */
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

    /**
     * Handles commands received by the cloud bridge.
     *
     * <p>The bridge does not currently expose command channels, so commands
     * received here are not processed.
     *
     * @param channel the channel on which the command was received
     * @param command the command to handle
     */
    @Override
    public void handleCommand(ChannelUID channel, Command command) {
    }

    /**
     * Cancels the scheduled connectivity check and releases the API client
     * reference when the bridge handler is disposed.
     */
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