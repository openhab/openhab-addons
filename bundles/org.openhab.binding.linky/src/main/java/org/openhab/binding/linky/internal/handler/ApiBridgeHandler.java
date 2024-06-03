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
package org.openhab.binding.linky.internal.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.linky.internal.LinkyAuthServlet;
import org.openhab.binding.linky.internal.LinkyBindingConstants;
import org.openhab.binding.linky.internal.LinkyException;
import org.openhab.binding.linky.internal.api.EnedisHttpApi;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link ApiBridgeHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class ApiBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(ApiBridgeHandler.class);

    private static final int REQUEST_BUFFER_SIZE = 8000;

    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) BundleContext bundleContext;

    private final HttpClient httpClient;
    private @Nullable EnedisHttpApi enedisApi;
    private final Gson gson;
    private OAuthClientService oAuthService;
    private final ThingRegistry thingRegistry;

    private static @Nullable HttpServlet servlet;

    private static final String TEMPLATE_PATH = "templates/";
    private static final String TEMPLATE_INDEX = TEMPLATE_PATH + "index.html";

    public ApiBridgeHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            final @Reference ThingRegistry thingRegistry, ComponentContext componentContext, Gson gson) {
        super(bridge);

        SslContextFactory sslContextFactory = new SslContextFactory.Client();
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { TrustAllTrustManager.getInstance() }, null);
            sslContextFactory.setSslContext(sslContext);

        } catch (NoSuchAlgorithmException e) {
            logger.warn("An exception occurred while requesting the SSL encryption algorithm : '{}'", e.getMessage(),
                    e);
        } catch (KeyManagementException e) {
            logger.warn("An exception occurred while initialising the SSL context : '{}'", e.getMessage(), e);
        }

        this.gson = gson;
        this.httpService = httpService;
        this.thingRegistry = thingRegistry;
        this.bundleContext = componentContext.getBundleContext();

        this.httpClient = httpClientFactory.createHttpClient(LinkyBindingConstants.BINDING_ID, sslContextFactory);
        httpClient.setFollowRedirects(false);
        httpClient.setRequestBufferSize(REQUEST_BUFFER_SIZE);

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.warn("Unable to start Jetty HttpClient {}", e.getMessage());
        }

        this.oAuthService = oAuthFactory.createOAuthClientService(LinkyBindingConstants.BINDING_ID,
                LinkyBindingConstants.ENEDIS_API_TOKEN_URL_PREPROD, LinkyBindingConstants.ENEDIS_AUTHORIZE_URL_PREPROD,
                LinkyBindingConstants.clientId, LinkyBindingConstants.clientSecret, LinkyBindingConstants.LINKY_SCOPES,
                true);

        registerServlet();

        updateStatus(ThingStatus.UNKNOWN);
    }

    public @Nullable EnedisHttpApi getEnedisApi() {
        return enedisApi;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Netatmo API bridge handler.");

        updateStatus(ThingStatus.UNKNOWN);

        EnedisHttpApi api = new EnedisHttpApi(this, gson, this.httpClient);

        this.enedisApi = api;

        scheduler.submit(() -> {
            try {
                api.initialize();
                updateStatus(ThingStatus.ONLINE);
            } catch (LinkyException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    /*
     * @Override
     * protected void deactivate(ComponentContext componentContext) {
     * super.deactivate(componentContext);
     * try {
     * httpClient.stop();
     * } catch (Exception e) {
     * logger.warn("Unable to stop Jetty HttpClient {}", e.getMessage());
     * }
     * }
     */

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down Netatmo API bridge handler.");

        httpService.unregister(LinkyBindingConstants.LINKY_ALIAS);
        httpService.unregister(LinkyBindingConstants.LINKY_ALIAS + LinkyBindingConstants.LINKY_IMG_ALIAS);

        super.dispose();
    }

    private void registerServlet() {
        try {
            if (servlet == null) {
                servlet = createServlet();

                httpService.registerServlet(LinkyBindingConstants.LINKY_ALIAS, servlet, new Hashtable<>(),
                        httpService.createDefaultHttpContext());
                httpService.registerResources(LinkyBindingConstants.LINKY_ALIAS + LinkyBindingConstants.LINKY_IMG_ALIAS,
                        "web", null);
            }

        } catch (NamespaceException | ServletException | IOException e) {
            logger.warn("Error during linky servlet startup", e);
        }
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

    public String authorize(String redirectUri, String reqState, String reqCode) throws LinkyException {
        // Will work only in case of direct oAuth2 authentification to enedis
        // this is not the case in v1 as we go trough MyElectricalData

        try {

            logger.debug("Make call to Enedis to get access token.");
            final AccessTokenResponse credentials = oAuthService
                    .getAccessTokenByClientCredentials(LinkyBindingConstants.LINKY_SCOPES);

            String accessToken = credentials.getAccessToken();

            logger.debug("Acces token: {}", accessToken);
            return accessToken;
        } catch (RuntimeException | OAuthException | IOException e) {
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new LinkyException("Error during oAuth authorize :" + e.getMessage(), e);
        } catch (final OAuthResponseException e) {
            throw new LinkyException("\"Error during oAuth authorize :" + e.getMessage(), e);
        }

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
    }

    public boolean isAuthorized() {
        final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

        return accessTokenResponse != null && accessTokenResponse.getAccessToken() != null
                && accessTokenResponse.getRefreshToken() != null;
    }

    public String getToken() throws LinkyException {

        AccessTokenResponse accesToken = getAccessTokenResponse();
        if (accesToken == null) {
            accesToken = getAccessTokenByClientCredentials();
        }

        if (accesToken == null) {
            throw new LinkyException("no token");
        }

        return accesToken.getAccessToken();
    }

    private @Nullable AccessTokenResponse getAccessTokenByClientCredentials() {
        try {
            return oAuthService.getAccessTokenByClientCredentials(LinkyBindingConstants.LINKY_SCOPES);
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    private @Nullable AccessTokenResponse getAccessTokenResponse() {
        try {
            return oAuthService.getAccessTokenResponse();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    public String formatAuthorizationUrl(String redirectUri) {
        // Will work only in case of direct oAuth2 authentification to enedis
        // this is not the case in v1 as we go trough MyElectricalData
        try {
            String uri = this.oAuthService.getAuthorizationUrl(redirectUri, LinkyBindingConstants.LINKY_SCOPES,
                    LinkyBindingConstants.BINDING_ID);
            return uri;
        } catch (final OAuthException e) {
            logger.debug("Error constructing AuthorizationUrl: ", e);
            return "";
        }
    }

    public List<String> getAllPrmId() {
        List<String> result = new ArrayList<String>();

        Collection<Thing> col = this.thingRegistry.getAll();

        for (Thing thing : col) {
            if (LinkyBindingConstants.THING_TYPE_LINKY.equals(thing.getThingTypeUID())) {
                Configuration config = thing.getConfiguration();

                String prmId = (String) config.get("prmId");
                result.add(prmId);
            }
        }

        return result;
    }
}
