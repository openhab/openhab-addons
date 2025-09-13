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
package org.openhab.binding.smartthings.internal.handler;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsAccountHandler;
import org.openhab.binding.smartthings.internal.SmartthingsAuthService;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartthingsHandlerFactory;
import org.openhab.binding.smartthings.internal.SmartthingsServlet;
import org.openhab.binding.smartthings.internal.api.SmartthingsApi;
import org.openhab.binding.smartthings.internal.api.SmartthingsNetworkConnector;
import org.openhab.binding.smartthings.internal.api.SmartthingsNetworkConnectorImpl;
import org.openhab.binding.smartthings.internal.discovery.SmartthingsDiscoveryService;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.openhab.binding.smartthings.internal.type.SmartthingsTypeRegistry;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
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
public abstract class SmartthingsBridgeHandler extends BaseBridgeHandler
        implements SmartthingsAccountHandler, AccessTokenRefreshListener {
    private final Logger logger = LoggerFactory.getLogger(SmartthingsBridgeHandler.class);

    protected SmartthingsBridgeConfig config;

    protected SmartthingsHandlerFactory smartthingsHandlerFactory;
    protected BundleContext bundleContext;
    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    private @NonNullByDefault({}) SmartthingsApi smartthingsApi;
    private @NonNullByDefault({}) SmartthingsAuthService authService;
    protected @NonNullByDefault({}) SmartthingsTypeRegistry typeRegistry;
    protected @NonNullByDefault({}) SmartthingsDiscoveryService discoService;

    private @Nullable SmartthingsServlet servlet;
    private @Nullable OAuthClientService oAuthService;
    private @NonNullByDefault({}) SmartthingsNetworkConnector networkConnector;
    private final OAuthFactory oAuthFactory;
    private String appId = "";

    public SmartthingsBridgeHandler(Bridge bridge, SmartthingsHandlerFactory smartthingsHandlerFactory,
            SmartthingsAuthService authService, BundleContext bundleContext, HttpService httpService,
            OAuthFactory oAuthFactory, HttpClientFactory httpClientFactory, SmartthingsTypeRegistry typeRegistry) {
        super(bridge);

        this.smartthingsHandlerFactory = smartthingsHandlerFactory;
        this.bundleContext = bundleContext;
        this.httpService = httpService;
        this.oAuthFactory = oAuthFactory;
        this.authService = authService;
        this.httpClientFactory = httpClientFactory;
        this.typeRegistry = typeRegistry;

        config = getThing().getConfiguration().as(SmartthingsBridgeConfig.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Commands are handled by the "Things"
    }

    @Reference
    public void registerDiscoveryListener(SmartthingsDiscoveryService disco) {
        this.discoService = disco;
        this.discoService.setSmartthingsTypeRegistry(typeRegistry);
    }

    @Override
    public void initialize() {
        // Validate the config
        if (!validateConfig(this.config)) {
            return;
        }

        OAuthClientService oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(),
                SmartthingsBindingConstants.SMARTTHINGS_API_TOKEN_URL,
                SmartthingsBindingConstants.SMARTTHINGS_AUTHORIZE_URL, config.clientId, config.clientSecret,
                SmartthingsBindingConstants.SMARTTHINGS_SCOPES, true);

        this.oAuthService = oAuthService;
        oAuthService.addAccessTokenRefreshListener(SmartthingsBridgeHandler.this);
        this.networkConnector = new SmartthingsNetworkConnectorImpl(httpClientFactory, oAuthService);

        authService.registerServlet();

        smartthingsApi = new SmartthingsApi(httpClientFactory, networkConnector, config.token);

        if (servlet == null) {
            servlet = new SmartthingsServlet(this, httpService, networkConnector, config.token);
            servlet.activate();
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public void updateConfig(String clientId, String clientSecret) {
        config.clientId = clientId;
        config.clientSecret = clientSecret;
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
    public String authorize(String redirectUri, String reqCode) throws SmartthingsException {
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
            throw new SmartthingsException("unable to authorize request", e);
        } catch (final OAuthResponseException e) {
            throw new SmartthingsException("unable to authorize request", e);
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

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return this.appId;
    }

    public SmartthingsNetworkConnector getNetworkConnector() {
        return this.networkConnector;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SmartthingsDiscoveryService.class);
    }

    public SmartthingsTypeRegistry getSmartthingsTypeRegistry() {
        return this.typeRegistry;
    }
}
