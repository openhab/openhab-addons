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
package org.openhab.binding.linky.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */
@NonNullByDefault
public class LinkyAuthService implements LinkyAccountHandler {

    private static final String TEMPLATE_PATH = "templates/";
    private static final String TEMPLATE_INDEX = TEMPLATE_PATH + "index.html";

    private final Logger logger = LoggerFactory.getLogger(LinkyAuthService.class);

    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) BundleContext bundleContext;

    private final boolean oAuthSupport = true;
    private @Nullable OAuthClientService oAuthService;

    public LinkyAuthService(final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            final @Reference ComponentContext componentContext) {

        this.httpService = httpService;

        this.oAuthService = oAuthFactory.createOAuthClientService(LinkyBindingConstants.BINDING_ID,
                LinkyBindingConstants.ENEDIS_API_TOKEN_URL, LinkyBindingConstants.ENEDIS_AUTH_AUTHORIZE_URL,
                LinkyBindingConstants.clientId, LinkyBindingConstants.clientSecret, LinkyBindingConstants.LINKY_SCOPES,
                true);

        try {
            bundleContext = componentContext.getBundleContext();

            httpService.registerServlet(LinkyBindingConstants.LINKY_ALIAS, createServlet(), new Hashtable<>(),
                    httpService.createDefaultHttpContext());
            httpService.registerResources(LinkyBindingConstants.LINKY_ALIAS + LinkyBindingConstants.LINKY_IMG_ALIAS,
                    "web", null);

        } catch (NamespaceException | ServletException | IOException e) {
            logger.warn("Error during linky servlet startup", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(LinkyBindingConstants.LINKY_ALIAS);
        httpService.unregister(LinkyBindingConstants.LINKY_ALIAS + LinkyBindingConstants.LINKY_IMG_ALIAS);
    }

    /**
     * Creates a new {@link LinkyAuthServlet}.
     *
     * @return the newly created servlet
     * @throws IOException thrown when an HTML template could not be read
     */
    private HttpServlet createServlet() throws IOException {
        return new LinkyAuthServlet(this, readTemplate(TEMPLATE_INDEX));
    }

    /**
     * Reads a template from file and returns the content as String.
     *
     * @param templateName name of the template file to read
     * @return The content of the template file
     * @throws IOException thrown when an HTML template could not be read
     */
    private String readTemplate(String templateName) throws IOException {
        final URL index = bundleContext.getBundle().getEntry(templateName);

        if (index == null) {
            throw new FileNotFoundException(
                    String.format("Cannot find '{}' - failed to initialize Linky servlet", templateName));
        } else {
            try (InputStream inputStream = index.openStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    @Override
    public String authorize(String redirectUri, String reqState, String reqCode) throws LinkyException {
        // Will work only in case of direct oAuth2 authentification to enedis
        // this is not the case in v1 as we go trough MyElectricalData

        if (oAuthSupport) {
            try {
                OAuthClientService oAuthService = this.oAuthService;
                if (oAuthService == null) {
                    throw new OAuthException("OAuth service is not initialized");
                }
                logger.debug("Make call to Enedis to get access token.");
                final AccessTokenResponse credentials = oAuthService
                        .getAccessTokenByClientCredentials(LinkyBindingConstants.LINKY_SCOPES);

                final String user = updateProperties(credentials);
                logger.debug("Authorized for user: {}", user);
                return user;
            } catch (RuntimeException | OAuthException | IOException e) {
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                throw new LinkyException("Error during oAuth authorize :" + e.getMessage(), e);
            } catch (final OAuthResponseException e) {
                throw new LinkyException("\"Error during oAuth authorize :" + e.getMessage(), e);
            }
        }
        // Fallback for MyElectricalData
        else {
            /*
             * String token = EnedisHttpApi.getToken(httpClient, LinkyBindingConstants.clientId, reqCode);
             *
             * logger.debug("token: {}", token);
             *
             * Collection<Thing> col = this.thingRegistry.getAll();
             * for (Thing thing : col) {
             * if (LinkyBindingConstants.THING_TYPE_LINKY.equals(thing.getThingTypeUID())) {
             * Configuration config = thing.getConfiguration();
             * String prmId = (String) config.get("prmId");
             *
             * if (!prmId.equals(reqCode)) {
             * continue;
             * }
             *
             * config.put("token", token);
             * LinkyHandler handler = (LinkyHandler) thing.getHandler();
             * if (handler != null) {
             * handler.saveConfiguration(config);
             * }
             * }
             * }
             *
             * return token;
             */

            return "";
        }
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

    private String updateProperties(AccessTokenResponse credentials) {
        return credentials.getAccessToken();
    }

    @Override
    public String formatAuthorizationUrl(String redirectUri) {
        // Will work only in case of direct oAuth2 authentification to enedis
        // this is not the case in v1 as we go trough MyElectricalData
        if (oAuthSupport) {
            try {
                OAuthClientService oAuthService = this.oAuthService;
                if (oAuthService == null) {
                    throw new OAuthException("OAuth service is not initialized");
                }

                String uri = oAuthService.getAuthorizationUrl(redirectUri, null, "Linky");
                return uri;
            } catch (final OAuthException e) {
                logger.debug("Error constructing AuthorizationUrl: ", e);
                return "";
            }
        }
        return "";
    }

    @Override
    public List<String> getAllPrmId() {
        List<String> result = new ArrayList<String>();
        /*
         * Collection<Thing> col = this.thingRegistry.getAll();
         *
         * for (Thing thing : col) {
         * if (LinkyBindingConstants.THING_TYPE_LINKY.equals(thing.getThingTypeUID())) {
         * Configuration config = thing.getConfiguration();
         *
         * String prmId = (String) config.get("prmId");
         * result.add(prmId);
         * }
         * }
         */

        return result;
    }

}
