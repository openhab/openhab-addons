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
package org.openhab.binding.ojelectronics.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.ojelectronics.internal.config.OJElectronicsBridgeConfiguration;
import org.openhab.binding.ojelectronics.internal.models.groups.GroupContentResponseModel;
import org.openhab.binding.ojelectronics.internal.services.OJDiscoveryService;
import org.openhab.binding.ojelectronics.internal.services.RefreshGroupContentService;
import org.openhab.binding.ojelectronics.internal.services.RefreshService;
import org.openhab.binding.ojelectronics.internal.services.SignInService;
import org.openhab.binding.ojelectronics.internal.services.UpdateService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles all traffic with OJ Electronics cloud
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
public class OJCloudHandler extends BaseBridgeHandler implements BridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(OJCloudHandler.class);
    private final HttpClient httpClient;

    private @Nullable RefreshService refreshService;
    private @Nullable UpdateService updateService;
    private @Nullable SignInService signInService;
    private OJElectronicsBridgeConfiguration configuration;
    private @Nullable ScheduledFuture<?> signTask;
    private @Nullable OJDiscoveryService discoveryService;

    /**
     * Creates a new instance of {@link OJCloudHandler}
     *
     * @param bridge {@link Bridge}
     * @param httpClient HttpClient
     */
    public OJCloudHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        this.configuration = new OJElectronicsBridgeConfiguration();
    }

    /**
     * Initializes the binding.
     */
    @Override
    public void initialize() {
        configuration = getConfigAs(OJElectronicsBridgeConfiguration.class);
        ensureSignIn();
    }

    /**
     * Disposes the binding.
     */
    @Override
    public void dispose() {
        final RefreshService refreshService = this.refreshService;
        if (refreshService != null) {
            refreshService.stop();
        }
        final ScheduledFuture<?> signTask = this.signTask;
        if (signTask != null) {
            signTask.cancel(true);
        }
        this.refreshService = null;
        signInService = null;
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private void ensureSignIn() {
        if (signInService == null) {
            signInService = new SignInService(configuration, httpClient);
        }
        final SignInService signInService = this.signInService;
        if (signInService != null) {
            signInService.signIn(this::handleSignInDone, this::handleConnectionLost,
                    this::handleUnauthorizedWhileSignIn);
        }
    }

    private void handleRefreshDone(@Nullable GroupContentResponseModel groupContentResponse,
            @Nullable String errorMessage) {
        logger.trace("OJElectronicsCloudHandler.handleRefreshDone({})", groupContentResponse);
        if (groupContentResponse != null && groupContentResponse.errorCode == 0) {
            internalRefreshDone(groupContentResponse);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    (errorMessage == null) ? "Wrong or no result model; Refreshing stoppped" : errorMessage);
            final RefreshService refreshService = this.refreshService;
            if (refreshService != null) {
                refreshService.stop();
            }
        }
    }

    private void internalRefreshDone(GroupContentResponseModel groupContentResponse) {
        new RefreshGroupContentService(groupContentResponse.groupContents, getThing().getThings()).handle();
        final OJDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.setScanResultForDiscovery(groupContentResponse.groupContents);
        }
        final UpdateService updateService = this.updateService;
        if (updateService != null) {
            updateService.updateAllThermostats(getThing().getThings());
        }
    }

    private void handleSignInDone(String sessionId) {
        logger.trace("OJElectronicsCloudHandler.handleSignInDone({})", sessionId);
        if (refreshService == null) {
            refreshService = new RefreshService(configuration, httpClient, scheduler);
        }
        final RefreshService refreshService = this.refreshService;
        if (refreshService != null) {
            refreshService.start(sessionId, this::handleRefreshDone, this::handleConnectionLost,
                    this::handleUnauthorized);

            updateStatus(ThingStatus.ONLINE);
        }
        this.updateService = new UpdateService(configuration, httpClient, sessionId);
    }

    private void handleUnauthorized() {
        logger.trace("OJElectronicsCloudHandler.handleUnauthorized()");
        final RefreshService refreshService = this.refreshService;
        if (refreshService != null) {
            refreshService.stop();
        }
        restartRefreshServiceAsync(1);
    }

    private void handleUnauthorizedWhileSignIn() {
        logger.trace("OJElectronicsCloudHandler.handleUnauthorizedWhileSignIn()");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "Could not sign in. Check user name and password.");
        final RefreshService refreshService = this.refreshService;
        if (refreshService != null) {
            refreshService.stop();
        }
    }

    private void handleConnectionLost() {
        logger.trace("OJElectronicsCloudHandler.handleConnectionLost()");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        final RefreshService refreshService = this.refreshService;
        if (refreshService != null) {
            refreshService.stop();
        }
        restartRefreshServiceAsync(configuration.refreshDelayInSeconds);
    }

    private void restartRefreshServiceAsync(long delayInSeconds) {
        signTask = scheduler.schedule(this::ensureSignIn, delayInSeconds, TimeUnit.SECONDS);
    }

    public void setDiscoveryService(OJDiscoveryService ojDiscoveryService) {
        this.discoveryService = ojDiscoveryService;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(OJDiscoveryService.class);
    }
}
