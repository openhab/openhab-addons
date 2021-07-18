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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private final String authUri;
    private final String legacyAuthUri;
    private final ConnectedDriveConfiguration configuration;
    private String clientId = "dbf0a542-ebd1-4ff0-a9a7-55172fbfce35";

    /**
     * URLs taken from https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/const.py
     */
    final String baseUrl;
    final String vehicleUrl;
    final String legacyUrl;
    final String navigationAPIUrl;
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
        legacyAuthUri = "https://" + BimmerConstants.LEGACY_AUTH_SERVER_MAP.get(configuration.region)
                + BimmerConstants.LEGACY_OAUTH_ENDPOINT;
        authUri = "https://" + BimmerConstants.AUTH_SERVER_MAP.get(configuration.region)
                + BimmerConstants.OAUTH_ENDPOINT;
        vehicleUrl = "https://" + BimmerConstants.SERVER_MAP.get(configuration.region) + "/webapi/v1/user/vehicles";
        baseUrl = vehicleUrl + "/";
        legacyUrl = "https://" + BimmerConstants.SERVER_MAP.get(configuration.region) + "/api/vehicle/dynamic/v1/";
        navigationAPIUrl = "https://" + BimmerConstants.SERVER_MAP.get(configuration.region)
                + "/api/vehicle/navigation/v1/";
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

    public void requestLNavigation(VehicleConfiguration config, StringResponseCallback callback) {
        // see https://github.com/jupe76/bmwcdapi/search?q=dynamic%2Fv1
        get(navigationAPIUrl + config.vin, null, callback);
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
    private synchronized void updateLegacyToken() {
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
        req.header(HttpHeader.HOST, BimmerConstants.SERVER_MAP.get(configuration.region));
        req.header(HttpHeader.AUTHORIZATION, BimmerConstants.AUTHORIZATION_VALUE_MAP.get(configuration.region));
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

    /**
     * Authorize at BMW Connected Drive Portal and get Token
     *
     * @return
     */
    private synchronized void jettyUpdateToken() {
        if (!authHttpClient.isStarted()) {
            try {
                authHttpClient.start();
            } catch (Exception e) {
                logger.warn("Auth Http Client cannot be started {}", e.getMessage());
                return;
            }
        }
        // POST("https://customer.bmwgroup.com/gcdm/oauth/authenticate");
        Request req = authHttpClient.POST(authUri);

        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
        req.header(HttpHeader.CONNECTION, KEEP_ALIVE);
        req.header(HttpHeader.HOST, BimmerConstants.AUTH_SERVER_MAP.get(configuration.region));
        req.header(HttpHeader.AUTHORIZATION, BimmerConstants.AUTHORIZATION_VALUE_MAP.get(configuration.region));
        req.header(CREDENTIALS, BimmerConstants.CREDENTIAL_VALUES);
        logger.info("Post Uri: {}, Headers adapted {}", req.getURI(), req.getHeaders().size());

        req.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED, getAuthEncodedData(), StandardCharsets.UTF_8));
        // String urlEncodedData = UrlEncoded.encode(dataMap, Charset.defaultCharset(), false);
        // req.header(CONTENT_LENGTH, Integer.toString(urlEncodedData.length()));
        // req.content(new StringContentProvider(urlEncodedData));
        // logger.info("Header: {}, URL: {}", req.getHeaders(), urlEncodedData);

        // req.timeout(HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send(new BufferingResponseListener() {
        // @NonNullByDefault({})
        // @Override
        // public void onComplete(Result result) {
        // if (result.getResponse().getStatus() == 302) {
        // HttpFields fields = result.getResponse().getHeaders();
        // HttpField field = fields.getField(HttpHeader.LOCATION);
        // tokenFromUrl(field.getValue());
        // logger.info("Jetty Auth succeeded!");
        // } else {
        // logger.debug("Authorization status {} reason {}", result.getResponse().getStatus(),
        // result.getResponse().getReason());
        // }
        // }
        // });

        try {
            ContentResponse contentResponse = req.timeout(HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send();
            // Status needs to be 302 - Response is stored in Header
            if (contentResponse.getStatus() == 302) {
                HttpFields fields = contentResponse.getHeaders();
                HttpField field = fields.getField(HttpHeader.LOCATION);
                tokenFromUrl(field.getValue());
            } else {
                logger.info("Authorization status {} reason {} content {}", contentResponse.getStatus(),
                        contentResponse.getHeaders(), new String(contentResponse.getContent(), StandardCharsets.UTF_8));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Authorization exception: {}", e.getMessage());
            StackTraceElement[] trace = e.getStackTrace();
            for (int i = 0; i < trace.length; i++) {
                logger.info("{}", trace[i]);
            }
        }
        logger.info("updateToken - finish");
    }

    public synchronized void updateToken() {
        try {
            URL url = new URL(authUri);
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty(HttpHeader.CONTENT_TYPE.toString(), CONTENT_TYPE_URL_ENCODED);
            con.setRequestProperty(HttpHeader.CONNECTION.toString(), KEEP_ALIVE);
            con.setRequestProperty(HttpHeader.HOST.toString(), BimmerConstants.SERVER_MAP.get(configuration.region));
            con.setRequestProperty(HttpHeader.AUTHORIZATION.toString(),
                    BimmerConstants.AUTHORIZATION_VALUE_MAP.get(configuration.region));
            con.setRequestProperty(CREDENTIALS, BimmerConstants.CREDENTIAL_VALUES);
            con.setDoOutput(true);

            // logger.info("Header: {}, URL: {}", con.getHeaderFields(), urlEncodedData);

            OutputStream os = con.getOutputStream();
            byte[] input = getAuthEncodedData().getBytes("utf-8");
            os.write(input, 0, input.length);

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            logger.info("Headers adapted {}", con.getHeaderFields());
            logger.info("Auth Response Code {} Message {} ", con.getResponseCode(), con.getResponseMessage());
            tokenFromUrl(con.getHeaderField(HttpHeader.LOCATION.toString()));
        } catch (IOException e) {
            logger.warn("{}", e.getMessage());
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

    private String getAuthEncodedData() {
        MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add(CLIENT_ID, clientId);
        dataMap.add(RESPONSE_TYPE, TOKEN);
        dataMap.add(REDIRECT_URI, BimmerConstants.REDIRECT_URI_VALUE);
        dataMap.add(SCOPE, BimmerConstants.SCOPE_VALUES);
        dataMap.add(USERNAME, configuration.userName);
        dataMap.add(PASSWORD, configuration.password);
        return UrlEncoded.encode(dataMap, StandardCharsets.UTF_8, false);
    }
}
