/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
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
    private HttpClient httpClient;
    private HttpClient authHttpClient;
    private String legacyAuthUri;
    private ConnectedDriveConfiguration configuration;

    /**
     * URLs taken from https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/const.py
     */
    String baseUrl;
    String vehicleUrl;
    String legacyUrl;
    String vehicleStatusAPI = "/status";
    String lastTripAPI = "/statistics/lastTrip";
    String allTripsAPI = "/statistics/allTrips";
    String chargeAPI = "/chargingprofile";
    String destinationAPI = "/destinations";
    String imageAPI = "/image";
    String rangeMapAPI = "/rangemap";
    String serviceExecutionAPI = "/executeService";
    String serviceExecutionStateAPI = "/serviceExecutionStatus";

    public ConnectedDriveProxy(HttpClientFactory httpClientFactory, ConnectedDriveConfiguration config) {
        httpClient = httpClientFactory.getCommonHttpClient();
        authHttpClient = httpClientFactory.createHttpClient(AUTH_HTTP_CLIENT_NAME);
        authHttpClient.setFollowRedirects(false);
        try {
            authHttpClient.start();
        } catch (Exception e) {
            logger.warn("Auth Http Client cannot be started");
        }
        configuration = config;

        StringBuilder legacyAuth = new StringBuilder();
        legacyAuth.append("https://");
        legacyAuth.append(BimmerConstants.AUTH_SERVER_MAP.get(configuration.region));
        legacyAuth.append(BimmerConstants.OAUTH_ENDPOINT);
        legacyAuthUri = legacyAuth.toString();
        vehicleUrl = "https://" + getRegionServer() + "/webapi/v1/user/vehicles";
        baseUrl = vehicleUrl + "/";
        legacyUrl = "https://" + getRegionServer() + "/api/vehicle/dynamic/v1/";
    }

    private synchronized void call(String url, boolean post, Optional<MultiMap<String>> params,
            ResponseCallback callback) {
        // only executed in "simulation mode"
        // SimulationTest.testSimulationOff() assures Injector is off when releasing
        if (Injector.isActive()) {
            if (url.equals(baseUrl)) {
                ((StringResponseCallback) callback).onResponse(Injector.getDiscovery());
            } else if (url.endsWith(vehicleStatusAPI)) {
                ((StringResponseCallback) callback).onResponse(Injector.getStatus());
            } else {
                logger.info("Simulation of {} not supported", url);
            }
            return;
        }
        Request req;
        final StringBuilder completeUrl = new StringBuilder(url);
        if (post) {
            req = httpClient.POST(completeUrl.toString());
            if (params.isPresent()) {
                String urlEncodedParameter = UrlEncoded.encode(params.get(), Charset.defaultCharset(), false);
                req.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED, urlEncodedParameter,
                        Charset.defaultCharset()));
            }
        } else {
            if (params.isPresent()) {
                completeUrl.append(Constants.QUESTION)
                        .append(UrlEncoded.encode(params.get(), Charset.defaultCharset(), false)).toString();
            }
            req = httpClient.newRequest(completeUrl.toString());
        }
        req.header(HttpHeader.AUTHORIZATION, getToken().getBearerToken());
        req.header(HttpHeader.REFERER, BimmerConstants.REFERER_URL);

        req.timeout(HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(org.eclipse.jetty.client.api.Result result) {
                if (result.getResponse().getStatus() != 200) {
                    NetworkError error = new NetworkError();
                    error.url = completeUrl.toString();
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
                    } else {
                        ((ByteResponseCallback) callback).onResponse(getContent());
                    }
                }
            }
        });
    }

    public void get(String url, Optional<MultiMap<String>> params, ResponseCallback callback) {
        call(url, false, params, callback);
    }

    public void post(String url, Optional<MultiMap<String>> params, ResponseCallback callback) {
        call(url, true, params, callback);
    }

    public void requestVehicles(StringResponseCallback callback) {
        get(vehicleUrl, Optional.empty(), callback);
    }

    public void requestVehcileStatus(VehicleConfiguration config, StringResponseCallback callback) {
        get(new StringBuilder(baseUrl).append(config.vin).append(vehicleStatusAPI).toString(), Optional.empty(),
                callback);
    }

    public void requestLegacyVehcileStatus(VehicleConfiguration config, StringResponseCallback callback) {
        // see https://github.com/jupe76/bmwcdapi/search?q=dynamic%2Fv1
        get(new StringBuilder(legacyUrl).append(config.vin).append("?offset=-60").toString(), Optional.empty(),
                callback);
    }

    public void requestLastTrip(VehicleConfiguration config, StringResponseCallback callback) {
        get(new StringBuilder(baseUrl).append(config.vin).append(lastTripAPI).toString(), Optional.empty(), callback);
    }

    public void requestAllTrips(VehicleConfiguration config, StringResponseCallback callback) {
        get(new StringBuilder(baseUrl).append(config.vin).append(allTripsAPI).toString(), Optional.empty(), callback);
    }

    public void requestChargingProfile(VehicleConfiguration config, StringResponseCallback callback) {
        get(new StringBuilder(baseUrl).append(config.vin).append(chargeAPI).toString(), Optional.empty(), callback);
    }

    public void requestDestinations(VehicleConfiguration config, StringResponseCallback callback) {
        get(new StringBuilder(baseUrl).append(config.vin).append(destinationAPI).toString(), Optional.empty(),
                callback);
    }

    public void requestRangeMap(VehicleConfiguration config, Optional<MultiMap<String>> params,
            StringResponseCallback callback) {
        get(new StringBuilder(baseUrl).append(config.vin).append(rangeMapAPI).toString(), params, callback);
    }

    public void requestImage(VehicleConfiguration config, ImageProperties props, ByteResponseCallback callback) {
        String localImageUrl = new StringBuilder(baseUrl).append(config.vin).append(imageAPI).toString();
        MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add("width", Integer.toString(props.size));
        dataMap.add("height", Integer.toString(props.size));
        dataMap.add("view", props.viewport);
        get(localImageUrl, Optional.of(dataMap), callback);
    }

    private String getRegionServer() {
        String retVal = BimmerConstants.SERVER_MAP.get(configuration.region);
        if (retVal != null) {
            return retVal;
        } else {
            return Constants.INVALID;
        }
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
        Request req = authHttpClient.POST(legacyAuthUri);

        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
        req.header(HttpHeader.CONNECTION, KEEP_ALIVE);
        req.header(HttpHeader.HOST, BimmerConstants.SERVER_MAP.get(configuration.region));
        req.header(HttpHeader.AUTHORIZATION, BimmerConstants.AUTHORIZATION_VALUE_MAP.get(configuration.region));
        req.header(CREDENTIALS, BimmerConstants.CREDENTIAL_VALUES);
        req.header(HttpHeader.REFERER, BimmerConstants.REFERER_URL);

        MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add("grant_type", "password");
        dataMap.add(SCOPE, BimmerConstants.SCOPE_VALUES);
        dataMap.add(USERNAME, configuration.userName);
        dataMap.add(PASSWORD, configuration.password);
        String urlEncodedData = UrlEncoded.encode(dataMap, Charset.defaultCharset(), false);
        req.header(CONTENT_LENGTH, Integer.toString(urlEncodedData.length()));
        req.content(new StringContentProvider(urlEncodedData));
        try {
            ContentResponse contentResponse = req.timeout(HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send();
            // Status needs to be 302 - Response is stored in Header
            if (contentResponse.getStatus() == 302) {
                HttpFields fields = contentResponse.getHeaders();
                HttpField field = fields.getField(HttpHeader.LOCATION);
                tokenFromUrl(field.getValue());
            } else if (contentResponse.getStatus() == 200) {
                AuthResponse authResponse = Converter.getGson().fromJson(contentResponse.getContentAsString(),
                        AuthResponse.class);
                if (authResponse != null) {
                    token.setToken(authResponse.accessToken);
                    token.setType(authResponse.tokenType);
                    token.setExpiration(authResponse.expiresIn);
                }
            } else {
                logger.debug("Authorization status {} reason {}", contentResponse.getStatus(),
                        contentResponse.getReason());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Authorization exception: {}", e.getMessage(), e);
        }
    }

    void tokenFromUrl(String encodedUrl) {
        MultiMap<String> tokenMap = new MultiMap<String>();
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
