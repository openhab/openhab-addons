/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.openhab.binding.bmwconnecteddrive.internal.utils.HTTPConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.VehicleConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.dto.NetworkError;
import org.openhab.binding.bmwconnecteddrive.internal.dto.auth.AuthResponse;
import org.openhab.binding.bmwconnecteddrive.internal.handler.simulation.Injector;
import org.openhab.binding.bmwconnecteddrive.internal.utils.BimmerConstants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ImageProperties;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link ConnectedDriveProxy} This class holds the important constants for the BMW Connected Drive Authorization.
 * They
 * are taken from the Bimmercode from github {@link https://github.com/bimmerconnected/bimmer_connected}
 * File defining these constants
 * {@link https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/account.py}
 * https://customer.bmwgroup.com/one/app/oauth.js
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit & send of charge profile
 */
@NonNullByDefault
public class ConnectedDriveProxy {
    private final Logger logger = LoggerFactory.getLogger(ConnectedDriveProxy.class);
    private final Token token = new Token();
    private final HttpClient httpClient;
    private final HttpClient authHttpClient;
    private final String legacyAuthUri;
    private final ConnectedDriveConfiguration configuration;

    /**
     * URLs taken from https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/const.py
     */
    final String baseUrl;
    final String vehicleUrl;
    final String legacyUrl;
    final String vehicleStatusAPI = "/status";
    final String lastTripAPI = "/statistics/lastTrip";
    final String allTripsAPI = "/statistics/allTrips";
    final String chargeAPI = "/chargingprofile";
    final String destinationAPI = "/destinations";
    final String imageAPI = "/image";
    final String rangeMapAPI = "/rangemap";
    final String serviceExecutionAPI = "/executeService";
    final String serviceExecutionStateAPI = "/serviceExecutionStatus";

    public ConnectedDriveProxy(HttpClientFactory httpClientFactory, ConnectedDriveConfiguration config) {
        httpClient = httpClientFactory.getCommonHttpClient();
        authHttpClient = httpClientFactory.createHttpClient(AUTH_HTTP_CLIENT_NAME);
        authHttpClient.setFollowRedirects(false);
        configuration = config;

        final StringBuilder legacyAuth = new StringBuilder();
        legacyAuth.append("https://");
        legacyAuth.append(BimmerConstants.AUTH_SERVER_MAP.get(configuration.region));
        legacyAuth.append(BimmerConstants.OAUTH_ENDPOINT);
        legacyAuthUri = legacyAuth.toString();
        vehicleUrl = "https://" + getRegionServer() + "/webapi/v1/user/vehicles";
        baseUrl = vehicleUrl + "/";
        legacyUrl = "https://" + getRegionServer() + "/api/vehicle/dynamic/v1/";
    }

    private synchronized void call(final String url, final boolean post, final @Nullable MultiMap<String> params,
            final ResponseCallback callback) {
        // only executed in "simulation mode"
        // SimulationTest.testSimulationOff() assures Injector is off when releasing
        if (Injector.isActive()) {
            if (url.equals(baseUrl)) {
                ((StringResponseCallback) callback).onResponse(Injector.getDiscovery());
            } else if (url.endsWith(vehicleStatusAPI)) {
                ((StringResponseCallback) callback).onResponse(Injector.getStatus());
            } else {
                logger.debug("Simulation of {} not supported", url);
            }
            return;
        }
        final Request req;
        final String encoded = params == null || params.isEmpty() ? null
                : UrlEncoded.encode(params, StandardCharsets.UTF_8, false);
        final String completeUrl;

        if (post) {
            completeUrl = url;
            req = httpClient.POST(url);
            if (encoded != null) {
                req.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED, encoded, StandardCharsets.UTF_8));
            }
        } else {
            completeUrl = encoded == null ? url : url + Constants.QUESTION + encoded;
            req = httpClient.newRequest(completeUrl);
        }
        req.header(HttpHeader.AUTHORIZATION, getToken().getBearerToken());
        req.header(HttpHeader.REFERER, BimmerConstants.REFERER_URL);

        req.timeout(HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(Result result) {
                if (result.getResponse().getStatus() != 200) {
                    NetworkError error = new NetworkError();
                    error.url = completeUrl;
                    error.status = result.getResponse().getStatus();
                    if (result.getResponse().getReason() != null) {
                        error.reason = result.getResponse().getReason();
                    } else {
                        error.reason = result.getFailure().getMessage();
                    }
                    error.params = result.getRequest().getParams().toString();
                    logger.debug("HTTP Error {}", error.toString());
                    callback.onError(error);
                } else {
                    if (callback instanceof StringResponseCallback) {
                        ((StringResponseCallback) callback).onResponse(getContentAsString());
                    } else if (callback instanceof ByteResponseCallback) {
                        ((ByteResponseCallback) callback).onResponse(getContent());
                    } else {
                        logger.error("unexpected reponse type {}", callback.getClass().getName());
                    }
                }
            }
        });
    }

    public void get(String url, @Nullable MultiMap<String> params, ResponseCallback callback) {
        call(url, false, params, callback);
    }

    public void post(String url, @Nullable MultiMap<String> params, ResponseCallback callback) {
        call(url, true, params, callback);
    }

    public void requestVehicles(StringResponseCallback callback) {
        get(vehicleUrl, null, callback);
    }

    public void requestVehcileStatus(VehicleConfiguration config, StringResponseCallback callback) {
        get(baseUrl + config.vin + vehicleStatusAPI, null, callback);
    }

    public void requestLegacyVehcileStatus(VehicleConfiguration config, StringResponseCallback callback) {
        // see https://github.com/jupe76/bmwcdapi/search?q=dynamic%2Fv1
        get(legacyUrl + config.vin + "?offset=-60", null, callback);
    }

    public void requestLastTrip(VehicleConfiguration config, StringResponseCallback callback) {
        get(baseUrl + config.vin + lastTripAPI, null, callback);
    }

    public void requestAllTrips(VehicleConfiguration config, StringResponseCallback callback) {
        get(baseUrl + config.vin + allTripsAPI, null, callback);
    }

    public void requestChargingProfile(VehicleConfiguration config, StringResponseCallback callback) {
        get(baseUrl + config.vin + chargeAPI, null, callback);
    }

    public void requestDestinations(VehicleConfiguration config, StringResponseCallback callback) {
        get(baseUrl + config.vin + destinationAPI, null, callback);
    }

    public void requestRangeMap(VehicleConfiguration config, @Nullable MultiMap<String> params,
            StringResponseCallback callback) {
        get(baseUrl + config.vin + rangeMapAPI, params, callback);
    }

    public void requestImage(VehicleConfiguration config, ImageProperties props, ByteResponseCallback callback) {
        final String localImageUrl = baseUrl + config.vin + imageAPI;
        final MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add("width", Integer.toString(props.size));
        dataMap.add("height", Integer.toString(props.size));
        dataMap.add("view", props.viewport);
        get(localImageUrl, dataMap, callback);
    }

    private String getRegionServer() {
        final String retVal = BimmerConstants.SERVER_MAP.get(configuration.region);
        return retVal == null ? Constants.INVALID : retVal;
    }

    private String getAuthorizationValue() {
        final String retVal = BimmerConstants.AUTHORIZATION_VALUE_MAP.get(configuration.region);
        return retVal == null ? Constants.INVALID : retVal;
    }

    RemoteServiceHandler getRemoteServiceHandler(VehicleHandler vehicleHandler) {
        return new RemoteServiceHandler(vehicleHandler, this);
    }

    // Token handling

    /**
     * Gets new token if old one is expired or invalid. In case of error the token remains.
     * So if token refresh fails the corresponding requests will also fail and update the
     * Thing status accordingly.
     *
     * @return token
     */
    public Token getToken() {
        if (token.isExpired() || !token.isValid()) {
            updateToken();
        }
        return token;
    }

    /**
     * Authorize at BMW Connected Drive Portal and get Token
     *
     * @return
     */
    private synchronized void updateToken() {
        if (!authHttpClient.isStarted()) {
            try {
                authHttpClient.start();
            } catch (Exception e) {
                logger.warn("Auth Http Client cannot be started {}", e.getMessage());
                return;
            }
        }

        final Request req = authHttpClient.POST(legacyAuthUri);
        req.header(HttpHeader.CONNECTION, KEEP_ALIVE);
        req.header(HttpHeader.HOST, getRegionServer());
        req.header(HttpHeader.AUTHORIZATION, getAuthorizationValue());
        req.header(CREDENTIALS, BimmerConstants.CREDENTIAL_VALUES);
        req.header(HttpHeader.REFERER, BimmerConstants.REFERER_URL);

        final MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add("grant_type", "password");
        dataMap.add(SCOPE, BimmerConstants.SCOPE_VALUES);
        dataMap.add(USERNAME, configuration.userName);
        dataMap.add(PASSWORD, configuration.password);
        req.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED,
                UrlEncoded.encode(dataMap, StandardCharsets.UTF_8, false), StandardCharsets.UTF_8));
        try {
            ContentResponse contentResponse = req.timeout(HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send();
            // Status needs to be 302 - Response is stored in Header
            if (contentResponse.getStatus() == 302) {
                final HttpFields fields = contentResponse.getHeaders();
                final HttpField field = fields.getField(HttpHeader.LOCATION);
                tokenFromUrl(field.getValue());
            } else if (contentResponse.getStatus() == 200) {
                final String stringContent = contentResponse.getContentAsString();
                if (stringContent != null && !stringContent.isEmpty()) {
                    try {
                        final AuthResponse authResponse = Converter.getGson().fromJson(stringContent,
                                AuthResponse.class);
                        if (authResponse != null) {
                            token.setToken(authResponse.accessToken);
                            token.setType(authResponse.tokenType);
                            token.setExpiration(authResponse.expiresIn);
                        } else {
                            logger.debug("not an Authorization response: {}", stringContent);
                        }
                    } catch (JsonSyntaxException jse) {
                        logger.debug("Authorization response unparsable: {}", stringContent);
                    }
                } else {
                    logger.debug("Authorization response has no content");
                }
            } else {
                logger.debug("Authorization status {} reason {}", contentResponse.getStatus(),
                        contentResponse.getReason());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Authorization exception: {}", e.getMessage());
        }
    }

    void tokenFromUrl(String encodedUrl) {
        final MultiMap<String> tokenMap = new MultiMap<String>();
        UrlEncoded.decodeTo(encodedUrl, tokenMap, StandardCharsets.US_ASCII);
        tokenMap.forEach((key, value) -> {
            if (value.size() > 0) {
                String val = value.get(0);
                if (key.endsWith(ACCESS_TOKEN)) {
                    token.setToken(val.toString());
                } else if (key.equals(EXPIRES_IN)) {
                    token.setExpiration(Integer.parseInt(val.toString()));
                } else if (key.equals(TOKEN_TYPE)) {
                    token.setType(val.toString());
                }
            }
        });
    }
}
