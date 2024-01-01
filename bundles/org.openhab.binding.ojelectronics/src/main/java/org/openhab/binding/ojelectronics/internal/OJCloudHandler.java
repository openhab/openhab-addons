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
package org.openhab.binding.ojelectronics.internal;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.ojelectronics.internal.config.OJElectronicsBridgeConfiguration;
import org.openhab.binding.ojelectronics.internal.models.SignalRResultModel;
import org.openhab.binding.ojelectronics.internal.models.groups.GroupContentResponseModel;
import org.openhab.binding.ojelectronics.internal.services.OJDiscoveryService;
import org.openhab.binding.ojelectronics.internal.services.RefreshGroupContentService;
import org.openhab.binding.ojelectronics.internal.services.RefreshService;
import org.openhab.binding.ojelectronics.internal.services.RefreshThermostatsService;
import org.openhab.binding.ojelectronics.internal.services.SignInService;
import org.openhab.binding.ojelectronics.internal.services.UpdateService;
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
 * Handles all traffic with OJ Electronics cloud
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
public class OJCloudHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(OJCloudHandler.class);
    private final HttpClient httpClient;

    private @Nullable RefreshService refreshService;
    private @Nullable UpdateService updateService;
    private @Nullable SignInService signInService;
    private OJElectronicsBridgeConfiguration configuration;
    private @Nullable ScheduledFuture<?> signTask;
    private @Nullable ScheduledFuture<?> updateTask;
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
        final RefreshService localRefreshService = this.refreshService;
        if (localRefreshService != null) {
            localRefreshService.stop();
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

    public synchronized void updateThinksChannelValuesToCloud() {
        final UpdateService localUpdateService = this.updateService;
        if (localUpdateService != null) {
            final ScheduledFuture<?> localUpdateTask = this.updateTask;
            if (localUpdateTask != null) {
                localUpdateTask.cancel(false);
            }
            this.updateTask = scheduler.schedule(() -> {
                localUpdateService.updateAllThermostats(getThing().getThings());
                this.updateTask = null;
            }, 2, TimeUnit.SECONDS);
        }
    }

    private void ensureSignIn() {
        if (signInService == null) {
            signInService = new SignInService(configuration, httpClient);
        }
        final SignInService localSignInService = this.signInService;
        if (localSignInService != null) {
            localSignInService.signIn(this::handleSignInDone, this::handleConnectionLost,
                    this::handleUnauthorizedWhileSignIn);
        }
    }

    private void initializationDone(@Nullable GroupContentResponseModel groupContentResponse,
            @Nullable String errorMessage) {
        logger.trace("OJElectronicsCloudHandler.initializationDone({})", groupContentResponse);
        if (groupContentResponse != null && groupContentResponse.errorCode == 0) {
            internalInitializationDone(groupContentResponse);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    (errorMessage == null) ? "Wrong or no result model; Refreshing stoppped" : errorMessage);
            final RefreshService localRefreshService = this.refreshService;
            if (localRefreshService != null) {
                localRefreshService.stop();
            }
        }
    }

    private void refreshDone(@Nullable SignalRResultModel resultModel, @Nullable String errorMessage) {
        logger.trace("OJElectronicsCloudHandler.refreshDone({})", resultModel);
        if (resultModel != null) {
            new RefreshThermostatsService(resultModel.getThermostats(), resultModel.getThermostatRealTimes(),
                    getThing().getThings()).handle();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    (errorMessage == null) ? "Wrong or no result model; Refreshing stoppped" : errorMessage);
            final RefreshService localRefreshService = this.refreshService;
            if (localRefreshService != null) {
                localRefreshService.stop();
            }
        }
    }

    private void internalInitializationDone(GroupContentResponseModel groupContentResponse) {
        new RefreshGroupContentService(groupContentResponse.groupContents, getThing().getThings()).handle();
        final OJDiscoveryService localDiscoveryService = this.discoveryService;
        if (localDiscoveryService != null) {
            localDiscoveryService.setScanResultForDiscovery(groupContentResponse.groupContents);
        }
    }

    private void handleSignInDone(String sessionId) {
        logger.trace("OJElectronicsCloudHandler.handleSignInDone({})", sessionId);
        if (refreshService == null) {
            refreshService = new RefreshService(configuration, httpClient);
        }
        final RefreshService localRefreshService = this.refreshService;
        if (localRefreshService != null) {
            localRefreshService.start(sessionId, this::initializationDone, this::refreshDone,
                    this::handleConnectionLost, this::handleUnauthorized);

            updateStatus(ThingStatus.ONLINE);
        }
        this.updateService = new UpdateService(configuration, httpClient, this::handleConnectionLost,
                this::handleUnauthorized);
    }

    private void handleUnauthorized() {
        logger.trace("OJElectronicsCloudHandler.handleUnauthorized()");
        final RefreshService localRefreshService = this.refreshService;
        if (localRefreshService != null) {
            localRefreshService.stop();
        }
        restartRefreshServiceAsync(1);
    }

    private void handleUnauthorizedWhileSignIn() {
        logger.trace("OJElectronicsCloudHandler.handleUnauthorizedWhileSignIn()");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "Could not sign in. Check user name and password.");
        final RefreshService localRefreshService = this.refreshService;
        if (localRefreshService != null) {
            localRefreshService.stop();
        }
    }

    public void reInitialize() {
        logger.trace("OJElectronicsCloudHandler.reInitialize()");
        final RefreshService localRefreshService = this.refreshService;
        if (localRefreshService != null) {
            localRefreshService.stop();
        }
        restartRefreshServiceAsync(1);
    }

    private void handleConnectionLost(@Nullable String message) {
        logger.trace("OJElectronicsCloudHandler.handleConnectionLost()");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        final RefreshService localRefreshService = this.refreshService;
        if (localRefreshService != null) {
            localRefreshService.stop();
        }
        restartRefreshServiceAsync(30);
    }

    private void restartRefreshServiceAsync(long delayInSeconds) {
        signTask = scheduler.schedule(this::ensureSignIn, delayInSeconds, TimeUnit.SECONDS);
    }

    public void setDiscoveryService(OJDiscoveryService ojDiscoveryService) {
        this.discoveryService = ojDiscoveryService;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(OJDiscoveryService.class);
    }
}
