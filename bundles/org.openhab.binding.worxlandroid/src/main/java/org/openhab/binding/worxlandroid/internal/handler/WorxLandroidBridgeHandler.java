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
package org.openhab.binding.worxlandroid.internal.handler;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.worxlandroid.internal.api.WebApiException;
import org.openhab.binding.worxlandroid.internal.api.WorxApiHandler;
import org.openhab.binding.worxlandroid.internal.api.dto.ProductItemStatus;
import org.openhab.binding.worxlandroid.internal.api.dto.UsersMeResponse;
import org.openhab.binding.worxlandroid.internal.config.WebApiConfiguration;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WorxLandroidBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nils - Initial contribution
 * @author Gaël L'hopital - Refactored with oAuthFactory, removed AWSClient
 */
@NonNullByDefault
public class WorxLandroidBridgeHandler extends BaseBridgeHandler
        implements AccessTokenRefreshListener, ThingHandlerHelper {
    private static final String URL_OAUTH_TOKEN = "https://id.worx.com/oauth/token";
    private static final String CLIENT_ID = "013132A8-DB34-4101-B993-3C8348EA0EBC";

    private final Logger logger = LoggerFactory.getLogger(WorxLandroidBridgeHandler.class);
    private final WorxApiHandler apiHandler;
    private final OAuthFactory oAuthFactory;

    public final OAuthClientService oAuthClientService;

    private String accessToken = "";
    private int retryCount = 3;
    private int retryDelayS = 1;
    private Optional<ScheduledFuture<?>> tokenRefreshJob = Optional.empty();
    private Optional<ScheduledFuture<?>> connectionJob = Optional.empty();

    public WorxLandroidBridgeHandler(Bridge bridge, WorxApiHandler apiHandler, OAuthFactory oAuthFactory) {
        super(bridge);
        this.apiHandler = apiHandler;
        this.oAuthFactory = oAuthFactory;
        this.oAuthClientService = oAuthFactory.createOAuthClientService(getThing().getUID().getAsString(),
                URL_OAUTH_TOKEN, null, CLIENT_ID, null, "*", true);
        oAuthClientService.addAccessTokenRefreshListener(this);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Landroid API bridge handler.");
        WebApiConfiguration config = getConfigAs(WebApiConfiguration.class);

        if (config.username.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-username");
            return;
        }

        if (config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-password");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> initiateConnection(config.username, config.password));
    }

    private void initiateConnection(String username, String password) {
        stopConnectionJob();
        try {
            accessToken = oAuthClientService.getAccessTokenByResourceOwnerPasswordCredentials(username, password, "*")
                    .getAccessToken();

            UsersMeResponse user = apiHandler.retrieveMe(accessToken);
            updateProperties(apiHandler.getDeserializer().toMap(user));

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException | WebApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (OAuthResponseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/oauth-connection-error");
        } catch (OAuthException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                String message = cause.getMessage();
                if (message != null && message.contains("http code 403")) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/oauth-connection-delayed");
                    connectionJob = Optional
                            .of(scheduler.schedule(() -> initiateConnection(username, password), 1, TimeUnit.HOURS));
                    return;
                }
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/oauth-connection-error");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Landroid Bridge is read-only and does not handle commands");
    }

    @Override
    public void dispose() {
        stopConnectionJob();
        stopTokenRefreshJob();

        oAuthClientService.removeAccessTokenRefreshListener(this);
        oAuthFactory.ungetOAuthService(getThing().getUID().getAsString());
        super.dispose();
    }

    private void stopTokenRefreshJob() {
        tokenRefreshJob.ifPresent(job -> job.cancel(true));
        tokenRefreshJob = Optional.empty();
    }

    private void stopConnectionJob() {
        connectionJob.ifPresent(job -> job.cancel(true));
        connectionJob = Optional.empty();
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
        accessToken = tokenResponse.getAccessToken();
    }

    public void requestTokenRefresh() {
        if (tokenRefreshJob.isPresent()) {
            return;
        }

        try {
            oAuthClientService.refreshToken();
            retryCount = 3;
            stopTokenRefreshJob();
        } catch (IOException | OAuthResponseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (OAuthException e) {
            if (retryCount > 0) {
                tokenRefreshJob = Optional.of(scheduler.schedule(() -> {
                    retryCount--;
                    requestTokenRefresh();
                }, retryDelayS, TimeUnit.MINUTES));
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/oauth-refresh-error");
            }
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public @Nullable ProductItemStatus retrieveDeviceStatus(String serialNumber) throws WebApiException {
        return apiHandler.retrieveDeviceStatus(accessToken, serialNumber);
    }

    public List<ProductItemStatus> retrieveAllDevices() throws WebApiException {
        return apiHandler.retrieveDeviceStatus(accessToken);
    }

    public boolean resetBladeTime(String serialNumber) {
        return apiHandler.resetBladeTime(accessToken, serialNumber);
    }

    public boolean resetBatteryCycles(String serialNumber) {
        return apiHandler.resetBatteryCycles(accessToken, serialNumber);
    }

    @Override
    public boolean isLinked(ChannelUID channelUID) {
        return super.isLinked(channelUID);
    }

    @Override
    public void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }
}
