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

import java.util.Map;
import java.util.concurrent.Executors;
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
public class OJElectronicsBridgeHandler extends BaseBridgeHandler implements BridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(OJElectronicsBridgeHandler.class);
    private final HttpClient httpClient;

    private @Nullable RefreshService refreshService;
    private @Nullable SignInService signInService;

    public OJElectronicsBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        logger.trace("OJElectronicsBridgeHandler.OJElectronicsBridgeHandler({})", bridge);
    }

    /**
     * Initializes the binding.
     */
    @Override
    public void initialize() {
        try {
            httpClient.start();
            ensureSignIn();

        } catch (Exception e) {
            logger.error("error initializing OJElectronicsBridgeHandler", e);
        }
    }

    /**
     * Disposes the binding.
     */
    @Override
    public void dispose() {
        super.dispose();
        if (refreshService != null) {
            refreshService.stop();
        }
        updateStatus(ThingStatus.OFFLINE);
    }

    /**
     * Handles commands
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nothing to do here
    }

    private void ensureSignIn() {
        if (signInService == null) {
            signInService = new SignInService(getConfiguration(), httpClient);
        }
        if (signInService != null) {
            signInService.SignIn(sessionId -> handleSignInDone(sessionId), () -> handleConnectionLost(),
                    () -> handleUnauthorizedWhileSignIn());
        }
    }

    private void handleRefreshDone(@Nullable GroupContentResponseModel groupContentResponse) {
        logger.trace("OJElectronicsBridgeHandler.handleRefreshDone({})", groupContentResponse);

        if (groupContentResponse != null && groupContentResponse.errorCode == 0) {
            new RefreshGroupContentService(groupContentResponse.groupContents, getThing().getThings()).Handle();
        } else {
            logger.error("Wrong or no result model; Refreshing stoppped");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            if (refreshService != null) {
                refreshService.stop();
            }
        }
    }

    private void handleSignInDone(String sessionId) {
        logger.trace("OJElectronicsBridgeHandler.handleSignInDone({})", sessionId);
        if (refreshService == null) {
            refreshService = new RefreshService(getConfiguration(), httpClient);
        }
        if (refreshService != null) {
            refreshService.start(sessionId, content -> handleRefreshDone(content), () -> handleConnectionLost(),
                    () -> handleUnauthorized());

            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void handleUnauthorized() {
        logger.trace("OJElectronicsBridgeHandler.handleUnauthorized()");
        if (refreshService != null) {
            refreshService.stop();
        }
        restartRefreshServiceAsync(1);
    }

    private void handleUnauthorizedWhileSignIn() {
        logger.trace("OJElectronicsBridgeHandler.handleUnauthorizedWhileSignIn()");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        if (refreshService != null) {
            refreshService.stop();
        }
    }

    private void handleConnectionLost() {
        logger.trace("OJElectronicsBridgeHandler.handleConnectionLost()");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        if (refreshService != null) {
            refreshService.stop();
        }
        restartRefreshServiceAsync(getConfiguration().refreshDelayInSeconds);
    }

    private void restartRefreshServiceAsync(long delayInSeconds) {
        logger.trace("OJElectronicsBridgeHandler.restartRefreshServiceAsync({})", delayInSeconds);
        Executors.newScheduledThreadPool(1).schedule(new Runnable() {
            @Override
            public void run() {
                ensureSignIn();
            }
        }, delayInSeconds, TimeUnit.SECONDS);
    }

    private OJElectronicsBridgeConfiguration getConfiguration() {
        return getConfigAs(OJElectronicsBridgeConfiguration.class);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.trace("OJElectronicsBridgeHandler.handleConfigurationUpdate({})", configurationParameters);
        if (refreshService != null) {
            refreshService.stop();
            refreshService = null;
        }
        signInService = null;
        super.handleConfigurationUpdate(configurationParameters);
    }
}
