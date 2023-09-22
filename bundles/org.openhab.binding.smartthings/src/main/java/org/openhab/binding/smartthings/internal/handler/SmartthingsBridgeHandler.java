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
package org.openhab.binding.smartthings.internal.handler;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsAccountHandler;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartthingsHandlerFactory;
import org.openhab.binding.smartthings.internal.SmartthingsServlet;
import org.openhab.binding.smartthings.internal.api.SmartthingsApi;
import org.openhab.binding.smartthings.internal.api.SmartthingsNetworkConnector;
import org.openhab.binding.smartthings.internal.discovery.SmartthingsDiscoveryService;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartthingsBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public abstract class SmartthingsBridgeHandler extends ConfigStatusBridgeHandler
        implements SmartthingsAccountHandler, AccessTokenRefreshListener {
    private final Logger logger = LoggerFactory.getLogger(SmartthingsBridgeHandler.class);

    protected SmartthingsBridgeConfig config;

    protected SmartthingsHandlerFactory smartthingsHandlerFactory;
    protected BundleContext bundleContext;
    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    private @NonNullByDefault({}) SmartthingsApi smartthingsApi;

    private @Nullable SmartthingsServlet servlet;
    private @Nullable OAuthClientService oAuthService;
    private @NonNullByDefault({}) SmartthingsNetworkConnector networkConnector;
    private final OAuthFactory oAuthFactory;

    public SmartthingsBridgeHandler(Bridge bridge, SmartthingsHandlerFactory smartthingsHandlerFactory,
            BundleContext bundleContext, HttpService httpService, OAuthFactory oAuthFactory,
            HttpClientFactory httpClientFactory) {
        super(bridge);
        this.smartthingsHandlerFactory = smartthingsHandlerFactory;
        this.bundleContext = bundleContext;
        this.httpService = httpService;
        this.oAuthFactory = oAuthFactory;
        this.httpClientFactory = httpClientFactory;
        config = getThing().getConfiguration().as(SmartthingsBridgeConfig.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Commands are handled by the "Things"
    }

    @Reference
    protected void setSmartthingsDiscoveryService(SmartthingsDiscoveryService disco) {
        logger.info("disco");
    }

    @Override
    public void initialize() {
        // Validate the config
        if (!validateConfig(this.config)) {
            return;
        }

        if (servlet == null) {
            servlet = new SmartthingsServlet(httpService);
            servlet.activate();
        }

        OAuthClientService oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(),
                SmartthingsBindingConstants.SMARTTHINGS_API_TOKEN_URL,
                SmartthingsBindingConstants.SMARTTHINGS_AUTHORIZE_URL, config.clientId, config.clientSecret,
                SmartthingsBindingConstants.SMARTTHINGS_SCOPES, true);

        this.oAuthService = oAuthService;
        oAuthService.addAccessTokenRefreshListener(SmartthingsBridgeHandler.this);

        smartthingsApi = new SmartthingsApi(httpClientFactory, oAuthService);

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {

    }

    public SmartthingsApi getSmartthingsApi() {
        return smartthingsApi;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    protected boolean validateConfig(SmartthingsBridgeConfig config) {

        return true;
    }

    public SmartthingsHandlerFactory getSmartthingsHandlerFactory() {
        return smartthingsHandlerFactory;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages = new LinkedList<>();

        return configStatusMessages;
    }

    @Override
    public boolean isAuthorized() {
        final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

        return accessTokenResponse != null && accessTokenResponse.getAccessToken() != null
                && accessTokenResponse.getRefreshToken() != null;

    }

    private @Nullable AccessTokenResponse getAccessTokenResponse() {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            return oAuthService == null ? null : oAuthService.getAccessTokenResponse();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    @Override
    public String authorize(String redirectUri, String reqCode) {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            if (oAuthService == null) {
                throw new OAuthException("OAuth service is not initialized");
            }
            logger.debug("Make call to Smartthings to get access token.");
            final AccessTokenResponse credentials = oAuthService.getAccessTokenResponseByAuthorizationCode(reqCode,
                    redirectUri);
            final String user = updateProperties(credentials);
            logger.debug("Authorized for user: {}", user);
            return user;
        } catch (RuntimeException | OAuthException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        } catch (final OAuthResponseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String updateProperties(AccessTokenResponse credentials) {
        /*
         * if (spotifyApi != null) {
         *
         * final Me me = spotifyApi.getMe();
         * final String user = me.getDisplayName() == null ? me.getId() : me.getDisplayName();
         * final Map<String, String> props = editProperties();
         *
         * props.put(PROPERTY_SPOTIFY_USER, user);
         * updateProperties(props);
         * return user;
         * }
         */
        return "";
    }

    @Override
    public String formatAuthorizationUrl(String redirectUri) {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            if (oAuthService == null) {
                throw new OAuthException("OAuth service is not initialized");
            }

            return oAuthService.getAuthorizationUrl(redirectUri, null, thing.getUID().getAsString());
        } catch (final OAuthException e) {
            logger.debug("Error constructing AuthorizationUrl: ", e);
            return "";
        }
    }
}
