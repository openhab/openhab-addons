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
package org.openhab.binding.ondilo.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ondilo.internal.dto.Pool;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OndiloBridgeHandler} Handler for the Ondilo Bridge (account-level, manages OAuth2 and device discovery)
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public class OndiloBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(OndiloBridgeHandler.class);
    private static final ConcurrentHashMap<String, OndiloBridgeHandler> OAUTH_SERVICE_REGISTRY = new ConcurrentHashMap<>();
    private final OAuthFactory oAuthFactory;
    public @Nullable OAuthClientService oAuthService;
    private @Nullable OndiloBridge bridge;
    private @Nullable String redirectURI;

    public OndiloBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
    }

    // Call this when generating the authorization URL
    private void registerOAuthService(String state, OndiloBridgeHandler service) {
        OAUTH_SERVICE_REGISTRY.put(state, service);
    }

    public static @Nullable OndiloBridgeHandler getOAuthServiceByState(String state) {
        return OAUTH_SERVICE_REGISTRY.get(state);
    }

    private void unregisterOAuthService(String state) {
        OAUTH_SERVICE_REGISTRY.remove(state);
    }

    @Override
    public void initialize() {
        logger.trace("Start initialization of Ondilo Bridge Handler");
        String clientSecret = thing.getUID().getAsString();

        OndiloBridgeConfiguration bridgeConfiguration = getConfigAs(OndiloBridgeConfiguration.class);
        final String openHABURL = bridgeConfiguration.getURL();
        if (!isValidUrl(openHABURL)) {
            logger.error("Invalid openHAB URL: {}", openHABURL);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid openHAB URL: " + openHABURL);
            return;
        }
        redirectURI = openHABURL + (openHABURL.endsWith("/") ? "" : "/") + "ondilo/oauth2callback";

        OAuthClientService oAuthService = oAuthFactory.getOAuthClientService(clientSecret);

        if (oAuthService == null) {
            oAuthService = oAuthFactory.createOAuthClientService(clientSecret,
                    "https://interop.ondilo.com/oauth2/token", // tokenEndpoint
                    "https://interop.ondilo.com/oauth2/authorize", // authorizationEndpoint
                    "customer_api", // clientId
                    clientSecret, // clientSecret
                    "api", // scope
                    null // no basic auth
            );
        }
        this.oAuthService = oAuthService;
        AccessTokenResponse accessTokenResponse = null;
        try {
            accessTokenResponse = oAuthService.getAccessTokenResponse();
        } catch (OAuthException | java.io.IOException | org.openhab.core.auth.client.oauth2.OAuthResponseException e) {
            logger.error("Failed to get OAuth2 access token: {}", e.getMessage(), e);
        }
        if (accessTokenResponse != null) {
            // Already authorized, proceed
            finalizeInitialize(accessTokenResponse, oAuthService);
        } else {
            // Not authorized, start OAuth2 flow (show authorization URL)
            startOAuth2Authorization(clientSecret, oAuthService);
        }
    }

    private void startOAuth2Authorization(String clientSecret, OAuthClientService oAuthService) {
        logger.trace("Start oAuth2 flow");

        registerOAuthService(clientSecret, this);
        try {
            String url = oAuthService.getAuthorizationUrl(redirectURI, "api", clientSecret);
            logger.error("Authorize bridge: {}", url);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Authorize bridge: " + url);
        } catch (OAuthException e) {
            logger.error("Failed to get OAuth2 authorization URL: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "OAuth2 error: " + e.getMessage());
        }
    }

    public void onOAuth2Authorized(String authorizationCode, OAuthClientService oAuthService) {
        logger.trace("Finalize oAuth2 flow");

        String clientSecret = thing.getUID().getAsString();
        AccessTokenResponse accessTokenResponse = null;
        try {
            accessTokenResponse = oAuthService.getAccessTokenResponseByAuthorizationCode(authorizationCode,
                    redirectURI);
        } catch (OAuthException | java.io.IOException | OAuthResponseException e) {
            logger.error("Failed to get access token by authorization code: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "OAuth2 error: " + e.getMessage());
            return;
        }
        logger.info("accessTokenResponse available");

        unregisterOAuthService(clientSecret);
        finalizeInitialize(accessTokenResponse, oAuthService);
    }

    private void finalizeInitialize(AccessTokenResponse accessTokenResponse, OAuthClientService oAuthService) {
        logger.trace("Finalize initialization of Ondilo Bridge Handler");

        if (bridge == null) {
            OndiloBridgeConfiguration bridgeConfiguration = getConfigAs(OndiloBridgeConfiguration.class);
            final int refreshInterval = bridgeConfiguration.getRefreshInterval();
            OndiloBridge currentBridge = new OndiloBridge(this, oAuthService, accessTokenResponse, refreshInterval,
                    scheduler);
            bridge = currentBridge;
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public Optional<List<Pool>> getPools() {
        List<Pool> currentPools = ((bridge != null) ? bridge.getPools() : null);
        if (currentPools == null) {
            logger.debug("No pools available, returning empty list");
            return Optional.empty();
        } else {
            return Optional.of(currentPools);
        }
    }

    @Override
    public void dispose() {
        OndiloBridge currentBridge = bridge;
        if (currentBridge != null) {
            currentBridge.stopOndiloBridgePolling();
            bridge = null;
        }
        if (oAuthService != null) {
            oAuthFactory.ungetOAuthService(thing.getUID().getAsString());
            oAuthService = null;
        }
        bridge = null;
        logger.debug("Ondilo Bridge disposed");
        super.dispose();
    }

    public @Nullable OndiloBridge getOndiloBridge() {
        return bridge;
    }

    public void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
        super.updateStatus(status, detail, description);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands for the bridge
    }

    public static boolean isValidUrl(String url) {
        try {
            URI.create(url).toURL();
            return true;
        } catch (IllegalArgumentException | MalformedURLException e) {
            return false;
        }
    }
}
