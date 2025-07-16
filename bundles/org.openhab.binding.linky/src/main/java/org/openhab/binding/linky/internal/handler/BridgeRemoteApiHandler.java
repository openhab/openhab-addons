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
package org.openhab.binding.linky.internal.handler;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linky.internal.config.LinkyBridgeApiConfiguration;
import org.openhab.binding.linky.internal.constants.LinkyBindingConstants;
import org.openhab.binding.linky.internal.helpers.LinkyAuthServlet;
import org.openhab.binding.linky.internal.types.LinkyException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link BridgeRemoteApiHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public abstract class BridgeRemoteApiHandler extends BridgeRemoteBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(BridgeRemoteApiHandler.class);

    private final OAuthFactory oAuthFactory;

    private @Nullable OAuthClientService oAuthService;

    private static @Nullable HttpServlet servlet;

    protected String tokenUrl = "";
    protected String authorizeUrl = "";

    public BridgeRemoteApiHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            ComponentContext componentContext, Gson gson) {
        super(bridge, httpClientFactory, oAuthFactory, httpService, componentContext, gson);

        this.oAuthFactory = oAuthFactory;

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void initialize() {
        super.initialize();

        config = getConfigAs(LinkyBridgeApiConfiguration.class);

        if (Objects.requireNonNull(config).seemsValid()) {
            this.oAuthService = oAuthFactory.createOAuthClientService(LinkyBindingConstants.BINDING_ID, tokenUrl,
                    authorizeUrl, getClientId(), getClientSecret(), LinkyBindingConstants.LINKY_SCOPES, true);

            registerServlet();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-mandatory-settings");
        }
    }

    public abstract String getClientId();

    public abstract String getClientSecret();

    public abstract boolean getIsSandbox();

    private void registerServlet() {
        try {
            if (servlet == null) {
                servlet = createServlet();

                httpService.registerServlet(LinkyBindingConstants.LINKY_ALIAS, servlet, new Hashtable<>(),
                        httpService.createDefaultHttpContext());
                httpService.registerResources(LinkyBindingConstants.LINKY_ALIAS + LinkyBindingConstants.LINKY_IMG_ALIAS,
                        "web", null);
            }
        } catch (NamespaceException | ServletException | LinkyException e) {
            logger.warn("Error during linky servlet startup", e);
        }
    }

    @Override
    public void dispose() {
        httpService.unregister(LinkyBindingConstants.LINKY_ALIAS);
        httpService.unregister(LinkyBindingConstants.LINKY_ALIAS + LinkyBindingConstants.LINKY_IMG_ALIAS);

        super.dispose();
    }

    /**
     * Creates a new {@link LinkyAuthServlet}.
     *
     * @return the newly created servlet
     * @throws IOException thrown when an HTML template could not be read
     */
    private HttpServlet createServlet() throws LinkyException {
        return new LinkyAuthServlet(this);
    }

    public String authorize(String redirectUri, String reqState, String reqCode) throws LinkyException {
        // Will work only in case of direct oAuth2 authentification to enedis
        // this is not the case in v1 as we go trough MyElectricalData

        try {
            logger.debug("Make call to Enedis to get access token.");
            OAuthClientService lcOAuthService = this.oAuthService;
            if (lcOAuthService == null) {
                return "";
            }

            final AccessTokenResponse credentials = lcOAuthService
                    .getAccessTokenByClientCredentials(LinkyBindingConstants.LINKY_SCOPES);

            String accessToken = credentials.getAccessToken();

            logger.debug("Acces token: {}", accessToken);
            return accessToken;
        } catch (RuntimeException | OAuthException | IOException e) {
            throw new LinkyException("Error during oAuth authorize :" + e.getMessage(), e);
        } catch (final OAuthResponseException e) {
            throw new LinkyException("Error during oAuth authorize :" + e.getMessage(), e);
        }
    }

    public boolean isAuthorized() {
        final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

        return accessTokenResponse != null && accessTokenResponse.getAccessToken() != null
                && accessTokenResponse.getRefreshToken() != null;
    }

    protected @Nullable AccessTokenResponse getAccessTokenByClientCredentials() {
        try {
            OAuthClientService lcOAuthService = this.oAuthService;
            if (lcOAuthService == null) {
                return null;
            }

            return lcOAuthService.getAccessTokenByClientCredentials(LinkyBindingConstants.LINKY_SCOPES);
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    protected @Nullable AccessTokenResponse getAccessTokenResponse() {
        try {
            OAuthClientService lcOAuthService = this.oAuthService;
            if (lcOAuthService == null) {
                return null;
            }

            return lcOAuthService.getAccessTokenResponse();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    public String formatAuthorizationUrl(String redirectUri) {
        try {
            OAuthClientService lcOAuthService = this.oAuthService;
            if (lcOAuthService == null) {
                return "";
            }

            String uri = lcOAuthService.getAuthorizationUrl(redirectUri, LinkyBindingConstants.LINKY_SCOPES,
                    LinkyBindingConstants.BINDING_ID);
            return uri;
        } catch (final OAuthException e) {
            logger.debug("Error constructing AuthorizationUrl: ", e);
            return "";
        }
    }

    @Override
    public boolean supportNewApiFormat() {
        return true;
    }
}
