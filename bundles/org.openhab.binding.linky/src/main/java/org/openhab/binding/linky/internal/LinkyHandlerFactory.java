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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.linky.internal.api.EnedisHttpApi;
import org.openhab.binding.linky.internal.handler.LinkyHandler;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

/**
 * The {@link LinkyHandlerFactory} is responsible for creating things handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.linky")
public class LinkyHandlerFactory extends BaseThingHandlerFactory implements LinkyAccountHandler {
    private static final DateTimeFormatter LINKY_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");
    private static final DateTimeFormatter LINKY_LOCALDATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    private static final DateTimeFormatter LINKY_LOCALDATETIME_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss");
    private static final int REQUEST_BUFFER_SIZE = 8000;

    private final String clientId = "e551937c-5250-48bc-b4a6-2323af68db92";
    private final String clientSecret = "";

    private final Logger logger = LoggerFactory.getLogger(LinkyHandlerFactory.class);
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class,
                    (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                            .parse(json.getAsJsonPrimitive().getAsString(), LINKY_FORMATTER))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext) -> LocalDate
                            .parse(json.getAsJsonPrimitive().getAsString(), LINKY_LOCALDATE_FORMATTER))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> {
                        try {
                            return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(),
                                    LINKY_LOCALDATETIME_FORMATTER);
                        } catch (Exception ex) {
                            return LocalDate.parse(json.getAsJsonPrimitive().getAsString(), LINKY_LOCALDATE_FORMATTER)
                                    .atStartOfDay();
                        }
                    })

            .create();

    private final LocaleProvider localeProvider;
    private final HttpClient httpClient;
    private final LinkyAuthService authService;
    private final boolean oAuthSupport = false;
    private @Nullable OAuthClientService oAuthService;
    private final ThingRegistry thingRegistry;

    @Activate
    public LinkyHandlerFactory(final @Reference LocaleProvider localeProvider,
            final @Reference HttpClientFactory httpClientFactory, final @Reference LinkyAuthService authService,
            final @Reference OAuthFactory oAuthFactory, final @Reference ThingRegistry thingRegistry) {
        this.localeProvider = localeProvider;
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
        this.thingRegistry = thingRegistry;
        this.httpClient = httpClientFactory.createHttpClient(LinkyBindingConstants.BINDING_ID, sslContextFactory);
        httpClient.setFollowRedirects(false);
        httpClient.setRequestBufferSize(REQUEST_BUFFER_SIZE);
        this.authService = authService;

        this.oAuthService = oAuthFactory.createOAuthClientService("Linky", LinkyBindingConstants.LINKY_API_TOKEN_URL,
                LinkyBindingConstants.LINKY_AUTHORIZE_URL, clientId, clientSecret, LinkyBindingConstants.LINKY_SCOPES,
                true);
        this.authService.setLinkyAccountHandler(this);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        try {
            httpClient.start();
        } catch (Exception e) {
            logger.warn("Unable to start Jetty HttpClient {}", e.getMessage());
        }
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.warn("Unable to stop Jetty HttpClient {}", e.getMessage());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return LinkyBindingConstants.THING_TYPE_LINKY.equals(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {

        if (supportsThingType(thing.getThingTypeUID())) {
            LinkyHandler handler = new LinkyHandler(thing, localeProvider, gson, httpClient);
            return handler;
        }

        return null;
    }

    // ===========================================================================

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
        // Will work only in case of direct oAuth2 authentification to enedis
        // this is not the case in v1 as we go trough MyElectricalData

        if (oAuthSupport) {
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
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                throw new RuntimeException(e.getMessage(), e);
            } catch (final OAuthResponseException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        // Fallback for MyElectricalData
        else {
            String token = EnedisHttpApi.getToken(httpClient, clientId, reqCode);

            logger.debug("token: {}", token);

            Collection<Thing> col = this.thingRegistry.getAll();
            for (Thing thing : col) {
                if (LinkyBindingConstants.THING_TYPE_LINKY.equals(thing.getThingTypeUID())) {

                    Configuration config = thing.getConfiguration();
                    String prmId = (String) config.get("prmId");

                    if (!prmId.equals(reqCode)) {
                        continue;
                    }

                    config.put("token", token);
                    LinkyHandler handler = (LinkyHandler) thing.getHandler();
                    if (handler != null) {
                        handler.saveConfiguration(config);
                    }
                }
            }

            return token;
        }
    }

    private String updateProperties(AccessTokenResponse credentials) {
        return "";
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
        // Fallback for MyElectricalData
        else {
            String uri = LinkyBindingConstants.ENEDIS_AUTHORIZE_URL;
            uri = uri + "?";
            uri = uri + "&client_id=" + clientId;
            uri = uri + "&duration=" + "P36M";
            uri = uri + "&response_type=" + "code";
            return uri;
        }
    }

    @Override
    public String[] getAllPrmId() {
        Collection<Thing> col = this.thingRegistry.getAll();
        List<String> result = new ArrayList<String>();
        for (Thing thing : col) {
            if (LinkyBindingConstants.THING_TYPE_LINKY.equals(thing.getThingTypeUID())) {

                Configuration config = thing.getConfiguration();

                String prmId = (String) config.get("prmId");
                result.add(prmId);

            }
        }

        return result.toArray(new String[0]);
    }
}
