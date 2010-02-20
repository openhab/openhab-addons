/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ojelectronics.internal.config.OJElectronicsBridgeConfiguration;
import org.openhab.binding.ojelectronics.internal.models.groups.GroupContentResponseModel;
import org.openhab.binding.ojelectronics.internal.services.RefreshGroupContentService;
import org.openhab.binding.ojelectronics.internal.services.RefreshService;
import org.openhab.binding.ojelectronics.internal.services.SignInService;
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
    private @Nullable SignInService signInService;
    private OJElectronicsBridgeConfiguration configuration;
    private @Nullable ScheduledFuture<?> signTask;

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
            new RefreshGroupContentService(groupContentResponse.groupContents, getThing().getThings()).handle();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    (errorMessage == null) ? "Wrong or no result model; Refreshing stoppped" : errorMessage);
            final RefreshService refreshService = this.refreshService;
            if (refreshService != null) {
                refreshService.stop();
            }
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
    }

    private void handleUnauthorized() {
        final RefreshService refreshService = this.refreshService;
        if (refreshService != null) {
            refreshService.stop();
        }
        restartRefreshServiceAsync(1);
    }

    private void handleUnauthorizedWhileSignIn() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "Could not sign in. Check user name and password.");
        final RefreshService refreshService = this.refreshService;
        if (refreshService != null) {
            refreshService.stop();
        }
    }

    private void handleConnectionLost() {
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
}
