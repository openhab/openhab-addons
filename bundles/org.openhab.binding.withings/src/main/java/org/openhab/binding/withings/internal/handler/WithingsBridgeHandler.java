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
package org.openhab.binding.withings.internal.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.withings.internal.WithingsBindingConstants;
import org.openhab.binding.withings.internal.api.WithingsDataModel;
import org.openhab.binding.withings.internal.api.auth.AuthHandler;
import org.openhab.binding.withings.internal.api.auth.WithingsAccessTokenResponse;
import org.openhab.binding.withings.internal.api.device.DevicesHandler;
import org.openhab.binding.withings.internal.api.device.DevicesResponse;
import org.openhab.binding.withings.internal.config.WithingsBridgeConfiguration;
import org.openhab.binding.withings.internal.service.AccessTokenInitializableService;
import org.openhab.binding.withings.internal.service.person.Person;
import org.openhab.binding.withings.internal.service.person.PersonHandler;
import org.openhab.core.auth.client.oauth2.*;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class WithingsBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(WithingsBridgeHandler.class);

    private final HttpClient httpClient;
    private final AccessTokenInitializableService accessTokenService;
    private final List<WithingsThingHandler> thingHandlers;
    public WithingsBridgeConfiguration configuration = new WithingsBridgeConfiguration();
    private @Nullable ScheduledFuture<?> tokenRefreshJob;
    private @Nullable ScheduledFuture<?> refreshJob;
    private Optional<WithingsDataModel> currentModel;

    public WithingsBridgeHandler(Bridge bridge, AccessTokenInitializableService accessTokenService,
            HttpClient httpClient) {
        super(bridge);
        this.accessTokenService = accessTokenService;
        this.httpClient = httpClient;
        this.thingHandlers = new ArrayList<>();
        this.currentModel = Optional.empty();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Withings API bridge handler.");

        configuration = getConfigAs(WithingsBridgeConfiguration.class);

        scheduleTokenInitAndRefresh();
    }

    public void registerThingHandler(WithingsThingHandler thingHandler) {
        thingHandlers.add(thingHandler);

        notifyThingHandler(thingHandler);
    }

    private void connectionSucceed() {
        updateData();
        scheduleRefreshJob();

        updateStatus(ThingStatus.ONLINE);
    }

    private void scheduleTokenInitAndRefresh() {
        tokenRefreshJob = scheduler.scheduleWithFixedDelay(() -> {
            logger.debug("Initializing API Connection and scheduling token refresh every {}s",
                    WithingsBindingConstants.TOKEN_REFRESH_SECONDS);
            try {
                accessTokenService.init(thing.getUID().getAsString(), configuration);

                boolean isAuthCodeRedeemed = processAuthCodeIfRequired();

                if (!isAuthCodeRedeemed) {
                    Optional<String> refreshToken = accessTokenService.getRefreshToken();
                    refreshToken.ifPresent(this::refreshAccessToken);
                }

                if (accessTokenService.getAccessToken().isPresent()) {
                    connectionSucceed();
                } else {
                    logger.warn("There is no valid access token! Please configure a new auth-code.");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "Please configure a new auth-code");
                }
            } catch (Exception e) {
                logger.warn("Unable to connect Withings API : {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Withings API access failed, will retry in " + WithingsBindingConstants.TOKEN_REFRESH_SECONDS
                                + " seconds.");
            }
        }, 2, WithingsBindingConstants.TOKEN_REFRESH_SECONDS, TimeUnit.SECONDS);
    }

    private void scheduleRefreshJob() {
        refreshJob = scheduler.schedule(() -> {
            updateData();
            if (refreshJob != null && !refreshJob.isCancelled()) {
                refreshJob.cancel(false);
                refreshJob = null;
            }
            scheduleRefreshJob();
        }, WithingsBindingConstants.REFRESH_SECONDS, TimeUnit.SECONDS);
    }

    private void updateData() {
        List<DevicesResponse.Device> devices = new DevicesHandler(accessTokenService, httpClient).loadDevices();
        Optional<Person> person = new PersonHandler(accessTokenService, httpClient).loadPerson();

        currentModel = Optional.of(new WithingsDataModel(devices, person));
        notifyThingHandlers();
    }

    public void notifyThingHandler(WithingsThingHandler thingHandler) {
        currentModel.ifPresent(thingHandler::updateData);
    }

    private void notifyThingHandlers() {
        if (currentModel.isPresent()) {
            WithingsDataModel model = currentModel.get();
            for (WithingsThingHandler thingHandler : thingHandlers) {
                thingHandler.updateData(model);
            }
        }
    }

    private boolean processAuthCodeIfRequired() {
        if (!configuration.authCode.trim().isEmpty()) {
            logger.debug("Redeem auth-code...");

            AuthHandler authHandler = new AuthHandler(accessTokenService, httpClient);
            Optional<WithingsAccessTokenResponse> tokenResponse = authHandler.redeemAuthCode(configuration.clientId,
                    configuration.clientSecret, configuration.authCode);

            try {
                if (tokenResponse.isPresent()) {
                    AccessTokenResponse accessTokenResponse = tokenResponse.get().createAccessTokenResponse();
                    accessTokenService.importAccessToken(accessTokenResponse);
                    return true;
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Error on fetching refresh token! Please set an auth-code within the Withings API thing!");
                }
            } catch (OAuthException e) {
                logger.error("Error on importing refresh token! Please configure a new auth-code. Message: {}",
                        e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Can't connect to Withings API. Please set an auth-code within the Withings API thing!");
            } finally {
                clearAuthCode();
            }
        }
        return false;
    }

    private void refreshAccessToken(@Nullable String refreshToken) {
        if (refreshToken == null) {
            logger.warn("There is no valid refresh token! Please configure a new auth-code.");
            return;
        }

        logger.debug("Refreshing the access token...");
        try {

            AuthHandler authHandler = new AuthHandler(accessTokenService, httpClient);
            Optional<WithingsAccessTokenResponse> tokenResponse = authHandler.refreshAccessToken(configuration.clientId,
                    configuration.clientSecret, refreshToken);

            if (tokenResponse.isPresent()) {
                AccessTokenResponse accessTokenResponse = tokenResponse.get().createAccessTokenResponse();
                accessTokenService.importAccessToken(accessTokenResponse);
            } else {
                logger.error("Invalid access token response at refresh!");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Error on refreshing access token! Please set a new auth-code within the Withings API thing!");
            }
        } catch (OAuthException e) {
            logger.warn("Error refreshing access token! Detail: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can't connect to Withings API. Refreshing access token failed!");
        }
    }

    private void clearAuthCode() {
        final Configuration configuration = editConfiguration();
        configuration.put(WithingsBindingConstants.CONFIG_AUTH_CODE, "");
        updateConfiguration(configuration);

        this.configuration = getConfigAs(WithingsBridgeConfiguration.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");

        @Nullable
        ScheduledFuture<?> job = tokenRefreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            tokenRefreshJob = null;
        }

        job = refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            tokenRefreshJob = null;
        }
    }
}
