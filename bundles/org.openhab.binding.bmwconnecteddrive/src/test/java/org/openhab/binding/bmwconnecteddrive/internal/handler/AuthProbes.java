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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.dto.auth.AuthResponse;
import org.openhab.binding.bmwconnecteddrive.internal.utils.BimmerConstants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AuthProbes} This class holds the important constants for the BMW Connected Drive Authorization.
 * They
 * are taken from the Bimmercode from github {@link https://github.com/bimmerconnected/bimmer_connected}
 * File defining these constants
 * {@link https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/account.py}
 * https://customer.bmwgroup.com/one/app/oauth.js
 *
 * @author Bernd Weymann - Initial contribution
 *         https://github.com/weymann/openhab-addons/blob/80001e89d8b03ea633b2279dcff81f322a5ad6aa/bundles/org.openhab.binding.bmwconnecteddrive/src/main/java/org/openhab/binding/bmwconnecteddrive/internal/handler/ConnectedDriveProxy.java
 *
 */
@NonNullByDefault
public class AuthProbes {
    private final Logger logger = LoggerFactory.getLogger(AuthProbes.class);
    private final Token token = new Token();
    private HttpClient httpClient;
    private HttpClient authHttpClient;
    private String authUri;
    private String legacyAuthUri;
    private ConnectedDriveConfiguration configuration;
    private String clientId = "dbf0a542-ebd1-4ff0-a9a7-55172fbfce35";

    /**
     * URLs taken from https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/const.py
     *
     * """URLs for different services and error code mapping."""
     *
     * AUTH_URL = 'https://customer.bmwgroup.com/{gcdm_oauth_endpoint}/authenticate'
     * AUTH_URL_LEGACY = 'https://{server}/gcdm/oauth/token'
     * BASE_URL = 'https://{server}/webapi/v1'
     *
     * VEHICLES_URL = BASE_URL + '/user/vehicles'
     * VEHICLE_VIN_URL = VEHICLES_URL + '/{vin}'
     * VEHICLE_STATUS_URL = VEHICLE_VIN_URL + '/status'
     * REMOTE_SERVICE_STATUS_URL = VEHICLE_VIN_URL + '/serviceExecutionStatus?serviceType={service_type}'
     * REMOTE_SERVICE_URL = VEHICLE_VIN_URL + "/executeService"
     * VEHICLE_IMAGE_URL = VEHICLE_VIN_URL + "/image?width={width}&height={height}&view={view}"
     * VEHICLE_POI_URL = VEHICLE_VIN_URL + '/sendpoi'
     *
     * }
     */
    String baseUrl;
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

    public AuthProbes(HttpClientFactory httpClientFactory, ConnectedDriveConfiguration config) {
        httpClient = httpClientFactory.getCommonHttpClient();
        authHttpClient = httpClientFactory.createHttpClient(AUTH_HTTP_CLIENT_NAME);
        authHttpClient.setFollowRedirects(false);
        try {
            authHttpClient.start();
        } catch (Exception e) {
            logger.warn("Auth Http Client cannot be started");
        }
        configuration = config;
        // generate URI for Authorization
        // see https://customer.bmwgroup.com/one/app/oauth.js
        StringBuilder uri = new StringBuilder();
        uri.append("https://customer.bmwgroup.com");
        if (BimmerConstants.LEGACY_AUTH_SERVER_MAP.equals(configuration.region)) {
            uri.append("/gcdm/usa/oauth/authenticate");
        } else {
            uri.append("/gcdm/oauth/authenticate");
        }
        authUri = uri.toString();

        StringBuilder legacyAuth = new StringBuilder();
        legacyAuth.append("https://");
        legacyAuth.append(BimmerConstants.AUTH_SERVER_MAP.get(configuration.region));
        legacyAuth.append(BimmerConstants.OAUTH_ENDPOINT);
        legacyAuthUri = legacyAuth.toString();
        baseUrl = "https://" + getRegionServer() + "/webapi/v1/user/vehicles/";
        legacyUrl = "https://" + getRegionServer() + "/api/vehicle/dynamic/v1/";
    }

    private String getRegionServer() {
        String retVal = BimmerConstants.LEGACY_AUTH_SERVER_MAP.get(configuration.region);
        if (retVal != null) {
            return retVal;
        } else {
            return Constants.INVALID;
        }
    }

    /**
     * Gets new token if old one is expired or invalid. In case of error the token remains.
     * So if token refresh fails the corresponding requests will also fail and update the
     * Thing status accordingly.
     *
     * @return token
     */
    public Token getToken() {
        if (token.isExpired() || !token.isValid()) {
            legacyUpdateToken();
        }
        return token;
    }

    /**
     * Authorize at BMW Connected Drive Portal and get Token
     *
     * @return
     */
    private synchronized void jettyUpdateToken() {
        logger.info("updateToken - start");
        Request req = authHttpClient.POST(authUri);

        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
        req.header(HttpHeader.CONNECTION, KEEP_ALIVE);
        req.header(HttpHeader.HOST, BimmerConstants.LEGACY_AUTH_SERVER_MAP.get(configuration.region));
        req.header(HttpHeader.AUTHORIZATION, BimmerConstants.AUTHORIZATION_VALUE_MAP.get(configuration.region));
        req.header(CREDENTIALS, BimmerConstants.LEGACY_CREDENTIAL_VALUES);

        MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add(CLIENT_ID, clientId);
        dataMap.add(RESPONSE_TYPE, TOKEN);
        dataMap.add(REDIRECT_URI, BimmerConstants.REDIRECT_URI_VALUE);
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
            } else {
                logger.debug("Authorization status {} reason {}", contentResponse.getStatus(),
                        contentResponse.getReason());
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
            URL url = new URL("https://customer.bmwgroup.com/gcdm/oauth/authenticate");
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            con.setRequestProperty(HttpHeader.CONTENT_TYPE.toString(), CONTENT_TYPE_URL_ENCODED);
            con.setRequestProperty(HttpHeader.CONNECTION.toString(), KEEP_ALIVE);
            con.setRequestProperty(HttpHeader.HOST.toString(),
                    BimmerConstants.LEGACY_AUTH_SERVER_MAP.get(BimmerConstants.AUTH_SERVER_ROW));
            con.setRequestProperty(HttpHeader.AUTHORIZATION.toString(),
                    BimmerConstants.AUTHORIZATION_VALUE_MAP.get(configuration.region));
            con.setRequestProperty(CREDENTIALS, BimmerConstants.LEGACY_CREDENTIAL_VALUES);
            con.setDoOutput(true);

            MultiMap<String> dataMap = new MultiMap<String>();
            dataMap.add(CLIENT_ID, clientId);
            dataMap.add(RESPONSE_TYPE, TOKEN);
            dataMap.add(REDIRECT_URI, BimmerConstants.REDIRECT_URI_VALUE);
            dataMap.add(SCOPE, BimmerConstants.SCOPE_VALUES);
            dataMap.add(USERNAME, configuration.userName);
            dataMap.add(PASSWORD, configuration.password);
            String urlEncodedData = UrlEncoded.encode(dataMap, Charset.defaultCharset(), false);
            OutputStream os = con.getOutputStream();
            byte[] input = urlEncodedData.getBytes("utf-8");
            os.write(input, 0, input.length);
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            logger.info("Response Code {} Message {} ", con.getResponseCode(), con.getResponseMessage());
            tokenFromUrl(con.getHeaderField(HttpHeader.LOCATION.toString()));
        } catch (IOException e) {
            logger.warn("{}", e.getMessage());
        }
    }

    private synchronized void legacyUpdateToken() {
        try {
            logger.info("Auth {}", legacyAuthUri);
            URL url = new URL(legacyAuthUri);
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            con.setRequestProperty(HttpHeader.CONTENT_TYPE.toString(), CONTENT_TYPE_URL_ENCODED);
            con.setRequestProperty(HttpHeader.CONNECTION.toString(), KEEP_ALIVE);
            con.setRequestProperty(HttpHeader.HOST.toString(),
                    BimmerConstants.LEGACY_AUTH_SERVER_MAP.get(BimmerConstants.LEGACY_AUTH_SERVER_ROW));
            // con.setRequestProperty(HttpHeader.AUTHORIZATION.toString(), BimmerConstants.LEGACY_AUTHORIZATION_VALUE);
            con.setRequestProperty(CREDENTIALS, BimmerConstants.LEGACY_CREDENTIAL_VALUES);
            con.setDoOutput(true);

            MultiMap<String> dataMap = new MultiMap<String>();
            dataMap.add("grant_type", "password");
            dataMap.add(SCOPE, BimmerConstants.SCOPE_VALUES);
            dataMap.add(USERNAME, configuration.userName);
            dataMap.add(PASSWORD, configuration.password);
            String urlEncodedData = UrlEncoded.encode(dataMap, Charset.defaultCharset(), false);
            OutputStream os = con.getOutputStream();
            byte[] input = urlEncodedData.getBytes("utf-8");
            os.write(input, 0, input.length);
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            logger.info("Response Code {} Message {} ", con.getResponseCode(), con.getResponseMessage());
            // logger.info("Response {}", response.toString());
            AuthResponse authResponse = Converter.getGson().fromJson(response.toString(), AuthResponse.class);
            // token.setToken(authResponse.access_token);
            // token.setType(authResponse.token_type);
            // token.setExpiration(authResponse.expires_in);
        } catch (IOException e) {
            logger.warn("{}", e.getMessage());
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
