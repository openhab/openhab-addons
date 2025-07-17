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

import static org.openhab.binding.ondilo.internal.OndiloBindingConstants.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ondilo.internal.dto.Pool;
import org.openhab.binding.ondilo.internal.dto.UserInfo;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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
    private final int refreshInterval;
    private final String openHABURL;
    public @Nullable OAuthClientService oAuthService;
    private @Nullable OndiloBridge bridge;
    private @Nullable String redirectURI;

    public OndiloBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;

        OndiloBridgeConfiguration bridgeConfiguration = getConfigAs(OndiloBridgeConfiguration.class);
        refreshInterval = bridgeConfiguration.getRefreshInterval();
        openHABURL = bridgeConfiguration.getURL();
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

        if (!isValidUrl(openHABURL)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, I18N_URL_INVALID);
            return;
        }
        updateStatus(ThingStatus.UNKNOWN); // Set to UNKNOWN initially

        scheduler.execute(() -> getInitialAccessToken());
    }

    private void getInitialAccessToken() {
        String clientSecret = thing.getUID().getAsString();
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
        } catch (InterruptedIOException e) {
            logger.debug("OAuth2 access token retrieval interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (OAuthException | IOException | OAuthResponseException e) {
            logger.warn("Failed to get OAuth2 access token: {}", e.getMessage(), e);
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
        logger.trace("Start OAuth2 flow");

        registerOAuthService(clientSecret, this);
        try {
            String url = oAuthService.getAuthorizationUrl(redirectURI, "api", clientSecret);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    I18N_OAUTH2_PENDING + " [\"" + url + "\"]");
        } catch (OAuthException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    I18N_OAUTH2_ERROR + " [\"" + e.getMessage() + "\"]");
        }
    }

    public void onOAuth2Authorized(String authorizationCode, OAuthClientService oAuthService) {
        logger.trace("Finalize OAuth2 flow");

        String clientSecret = thing.getUID().getAsString();
        AccessTokenResponse accessTokenResponse = null;
        try {
            accessTokenResponse = oAuthService.getAccessTokenResponseByAuthorizationCode(authorizationCode,
                    redirectURI);
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, I18N_OAUTH2_INTERRUPTED);
            return;
        } catch (OAuthException | IOException | OAuthResponseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    I18N_OAUTH2_ERROR + " [\"" + e.getMessage() + "\"]");
            return;
        }
        logger.info("Ondilo Account successfully authorized with access token");

        unregisterOAuthService(clientSecret);
        finalizeInitialize(accessTokenResponse, oAuthService);
    }

    private void finalizeInitialize(AccessTokenResponse accessTokenResponse, OAuthClientService oAuthService) {
        logger.trace("Finalize initialization of Ondilo Bridge Handler");

        if (bridge == null) {
            bridge = new OndiloBridge(this, oAuthService, accessTokenResponse, refreshInterval, scheduler);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public Optional<List<Pool>> getPools() {
        OndiloBridge bridge = this.bridge;
        if (bridge == null) {
            logger.trace("Bridge is null, return empty list");
            return Optional.empty();
        } else {
            List<Pool> currentPools = bridge.getPools();
            if (currentPools == null || currentPools.isEmpty()) {
                logger.trace("No Ondilo ICOs available, return empty list");
                return Optional.empty();
            } else {
                return Optional.of(currentPools);
            }
        }
    }

    public void updateUserInfo(UserInfo userInfo) {
        Map<String, String> properties = editProperties();
        properties.put(PROPERTY_BRIDGE_USER_INFO, userInfo.getUserInfo());
        updateProperties(properties);
    }

    @Override
    public void dispose() {
        String clientSecret = thing.getUID().getAsString();
        OndiloBridge currentBridge = bridge;
        if (currentBridge != null) {
            currentBridge.dispose();
            bridge = null;
        }
        if (oAuthService != null) {
            oAuthFactory.ungetOAuthService(clientSecret);
            oAuthService = null;
        }

        OAUTH_SERVICE_REGISTRY.remove(clientSecret);
        redirectURI = null;
        bridge = null;
        logger.trace("Ondilo Bridge disposed");
        super.dispose();
    }

    public @Nullable OndiloBridge getOndiloBridge() {
        return bridge;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            // not implemented as it would causes >10 channel updates in a row during setup (exceeds given API quota)
            // If you want to update the values, use the poll channel instead
            return;
        } else {
            if (channelUID.getId().equals(CHANNEL_POLL_UPDATE)) {
                if (command instanceof OnOffType cmd) {
                    if (cmd == OnOffType.ON) {
                        OndiloBridge bridge = this.bridge;
                        if (bridge != null) {
                            bridge.pollOndiloICOs();
                        } else {
                            logger.warn("Bridge is null, cannot poll Ondilo ICOs");
                        }
                        // Reset the channel state to OFF after polling
                        updateState(CHANNEL_POLL_UPDATE, OnOffType.OFF);
                    }
                }
            }
        }
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
