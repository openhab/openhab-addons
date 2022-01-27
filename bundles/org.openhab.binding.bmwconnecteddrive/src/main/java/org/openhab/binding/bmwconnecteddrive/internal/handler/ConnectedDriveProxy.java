/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
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
    private Optional<RemoteServiceHandler> remoteServiceHandler = Optional.empty();
    private final Token token = new Token();
    private final HttpClient httpClient;
    private final HttpClient authHttpClient;
    private final ConnectedDriveConfiguration configuration;

    /**
     * URLs taken from https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/const.py
     */
    final String baseUrl;
    final String vehicleUrl;
    final String legacyUrl;
    final String remoteCommandUrl;
    final String remoteStatusUrl;
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
    public static final String REMOTE_SERVICE_EADRAX_BASE_URL = "/eadrax-vrccs/v2/presentation/remote-commands/"; // '/{vin}/{service_type}'
    final String remoteServiceEADRXstatusUrl = REMOTE_SERVICE_EADRAX_BASE_URL + "eventStatus?eventId={event_id}";
    final String vehicleEADRXPoiUrl = "/eadrax-dcs/v1/send-to-car/send-to-car";

    public ConnectedDriveProxy(HttpClientFactory httpClientFactory, ConnectedDriveConfiguration config) {
        httpClient = httpClientFactory.getCommonHttpClient();
        authHttpClient = httpClientFactory.createHttpClient(AUTH_HTTP_CLIENT_NAME);
        configuration = config;

        vehicleUrl = "https://" + BimmerConstants.API_SERVER_MAP.get(configuration.region) + "/webapi/v1/user/vehicles";
        baseUrl = vehicleUrl + "/";
        legacyUrl = "https://" + BimmerConstants.API_SERVER_MAP.get(configuration.region) + "/api/vehicle/dynamic/v1/";
        navigationAPIUrl = "https://" + BimmerConstants.API_SERVER_MAP.get(configuration.region)
                + "/api/vehicle/navigation/v1/";
        remoteCommandUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(configuration.region)
                + REMOTE_SERVICE_EADRAX_BASE_URL;
        remoteStatusUrl = remoteCommandUrl + "eventStatus";
    }

    public synchronized void call(final String url, final boolean post, final @Nullable String encoding,
            final @Nullable String params, final ResponseCallback callback) {
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
        final String completeUrl;

        if (post) {
            completeUrl = url;
            req = httpClient.POST(url);
            if (encoding != null) {
                if (CONTENT_TYPE_URL_ENCODED.equals(encoding)) {
                    req.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED, params, StandardCharsets.UTF_8));
                } else if (CONTENT_TYPE_JSON_ENCODED.equals(encoding)) {
                    req.header(HttpHeader.CONTENT_TYPE, encoding);
                    req.content(new StringContentProvider(CONTENT_TYPE_JSON_ENCODED, params, StandardCharsets.UTF_8));
                }
            }
        } else {
            completeUrl = params == null ? url : url + Constants.QUESTION + params;
            req = httpClient.newRequest(completeUrl);
        }
        req.header(HttpHeader.AUTHORIZATION, getToken().getBearerToken());
        req.header(HttpHeader.REFERER, BimmerConstants.LEGACY_REFERER_URL);

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

    public void get(String url, @Nullable String coding, @Nullable String params, ResponseCallback callback) {
        call(url, false, coding, params, callback);
    }

    public void post(String url, @Nullable String coding, @Nullable String params, ResponseCallback callback) {
        call(url, true, coding, params, callback);
    }

    public void requestVehicles(StringResponseCallback callback) {
        get(vehicleUrl, null, null, callback);
    }

    public void requestVehcileStatus(VehicleConfiguration config, StringResponseCallback callback) {
        get(baseUrl + config.vin + vehicleStatusAPI, null, null, callback);
    }

    public void requestLegacyVehcileStatus(VehicleConfiguration config, StringResponseCallback callback) {
        // see https://github.com/jupe76/bmwcdapi/search?q=dynamic%2Fv1
        get(legacyUrl + config.vin + "?offset=-60", null, null, callback);
    }

    public void requestLNavigation(VehicleConfiguration config, StringResponseCallback callback) {
        // see https://github.com/jupe76/bmwcdapi/search?q=dynamic%2Fv1
        get(navigationAPIUrl + config.vin, null, null, callback);
    }

    public void requestLastTrip(VehicleConfiguration config, StringResponseCallback callback) {
        get(baseUrl + config.vin + lastTripAPI, null, null, callback);
    }

    public void requestAllTrips(VehicleConfiguration config, StringResponseCallback callback) {
        get(baseUrl + config.vin + allTripsAPI, null, null, callback);
    }

    public void requestChargingProfile(VehicleConfiguration config, StringResponseCallback callback) {
        get(baseUrl + config.vin + chargeAPI, null, null, callback);
    }

    public void requestDestinations(VehicleConfiguration config, StringResponseCallback callback) {
        get(baseUrl + config.vin + destinationAPI, null, null, callback);
    }

    public void requestRangeMap(VehicleConfiguration config, @Nullable MultiMap<String> params,
            StringResponseCallback callback) {
        get(baseUrl + config.vin + rangeMapAPI, CONTENT_TYPE_URL_ENCODED,
                UrlEncoded.encode(params, StandardCharsets.UTF_8, false), callback);
    }

    public void requestImage(VehicleConfiguration config, ImageProperties props, ByteResponseCallback callback) {
        final String localImageUrl = baseUrl + config.vin + imageAPI;
        final MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add("width", Integer.toString(props.size));
        dataMap.add("height", Integer.toString(props.size));
        dataMap.add("view", props.viewport);

        get(localImageUrl, CONTENT_TYPE_URL_ENCODED, UrlEncoded.encode(dataMap, StandardCharsets.UTF_8, false),
                callback);
    }

    RemoteServiceHandler getRemoteServiceHandler(VehicleHandler vehicleHandler) {
        remoteServiceHandler = Optional.of(new RemoteServiceHandler(vehicleHandler, this));
        return remoteServiceHandler.get();
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
        if (!token.isValid()) {
            if (configuration.preferMyBmw) {
                if (!updateToken()) {
                    if (!updateLegacyToken()) {
                        logger.debug("Authorization failed!");
                    }
                }
            } else {
                if (!updateLegacyToken()) {
                    if (!updateToken()) {
                        logger.debug("Authorization failed!");
                    }
                }
            }
        }
        remoteServiceHandler.ifPresent(serviceHandler -> {
            serviceHandler.setMyBmwApiUsage(token.isMyBmwApiUsage());
        });
        return token;
    }

    public synchronized boolean updateToken() {
        if (BimmerConstants.REGION_CHINA.equals(configuration.region)) {
            // region China currently not supported for MyBMW API
            logger.debug("Region {} not supported yet for MyBMW Login", BimmerConstants.REGION_CHINA);
            return false;
        }
        if (!startAuthClient()) {
            return false;
        } // else continue
        String authUri = "https://" + BimmerConstants.AUTH_SERVER_MAP.get(configuration.region)
                + BimmerConstants.OAUTH_ENDPOINT;

        Request authRequest = authHttpClient.POST(authUri);
        authRequest.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);

        MultiMap<String> authChallenge = getTokenBaseValues();
        authChallenge.addAllValues(getTokenAuthValues());
        String authEncoded = UrlEncoded.encode(authChallenge, Charset.defaultCharset(), false);
        authRequest.content(new StringContentProvider(authEncoded));
        try {
            ContentResponse authResponse = authRequest.timeout(HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send();
            String authResponseString = URLDecoder.decode(authResponse.getContentAsString(), Charset.defaultCharset());
            String authCode = getAuthCode(authResponseString);
            if (!Constants.EMPTY.equals(authCode)) {
                MultiMap<String> codeChallenge = getTokenBaseValues();
                codeChallenge.put(AUTHORIZATION, authCode);

                Request codeRequest = authHttpClient.POST(authUri).followRedirects(false);
                codeRequest.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
                String codeEncoded = UrlEncoded.encode(codeChallenge, Charset.defaultCharset(), false);
                codeRequest.content(new StringContentProvider(codeEncoded));
                ContentResponse codeResponse = codeRequest.timeout(HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send();
                String code = ConnectedDriveProxy.codeFromUrl(codeResponse.getHeaders().get(HttpHeader.LOCATION));

                // Get Token
                String tokenUrl = "https://" + BimmerConstants.AUTH_SERVER_MAP.get(configuration.region)
                        + BimmerConstants.TOKEN_ENDPOINT;

                Request tokenRequest = authHttpClient.POST(tokenUrl).followRedirects(false);
                tokenRequest.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
                tokenRequest.header(HttpHeader.AUTHORIZATION,
                        BimmerConstants.AUTHORIZATION_VALUE_MAP.get(configuration.region));
                String tokenEncoded = UrlEncoded.encode(getTokenValues(code), Charset.defaultCharset(), false);
                tokenRequest.content(new StringContentProvider(tokenEncoded));
                ContentResponse tokenResponse = tokenRequest.timeout(HTTP_TIMEOUT_SEC, TimeUnit.SECONDS).send();
                AuthResponse authResponseJson = Converter.getGson().fromJson(tokenResponse.getContentAsString(),
                        AuthResponse.class);
                token.setToken(authResponseJson.accessToken);
                token.setType(authResponseJson.tokenType);
                token.setExpiration(authResponseJson.expiresIn);
                token.setMyBmwApiUsage(true);
                return true;
            }
        } catch (InterruptedException | ExecutionException |

                TimeoutException e) {
            logger.debug("Authorization exception: {}", e.getMessage());
        }
        return false;
    }

    private boolean startAuthClient() {
        if (!authHttpClient.isStarted()) {
            try {
                authHttpClient.start();
            } catch (Exception e) {
                logger.error("Auth HttpClient start failed!");
                return false;
            }
        }
        return true;
    }

    private MultiMap<String> getTokenBaseValues() {
        MultiMap<String> baseValues = new MultiMap<String>();
        baseValues.add(CLIENT_ID, Constants.EMPTY + BimmerConstants.CLIENT_ID.get(configuration.region));
        baseValues.add(RESPONSE_TYPE, CODE);
        baseValues.add(REDIRECT_URI, BimmerConstants.REDIRECT_URI_VALUE);
        baseValues.add("state", Constants.EMPTY + BimmerConstants.STATE.get(configuration.region));
        baseValues.add("nonce", "login_nonce");
        baseValues.add(SCOPE, BimmerConstants.SCOPE_VALUES);
        return baseValues;
    }

    private MultiMap<String> getTokenAuthValues() {
        MultiMap<String> authValues = new MultiMap<String>();
        authValues.add(GRANT_TYPE, "authorization_code");
        authValues.add(USERNAME, configuration.userName);
        authValues.add(PASSWORD, configuration.password);
        return authValues;
    }

    private MultiMap<String> getTokenValues(String code) {
        MultiMap<String> tokenValues = new MultiMap<String>();
        tokenValues.put(CODE, code);
        tokenValues.put("code_verifier", Constants.EMPTY + BimmerConstants.CODE_VERIFIER.get(configuration.region));
        tokenValues.put(REDIRECT_URI, BimmerConstants.REDIRECT_URI_VALUE);
        tokenValues.put(GRANT_TYPE, "authorization_code");
        return tokenValues;
    }

    private String getAuthCode(String response) {
        String[] keys = response.split("&");
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].startsWith(AUTHORIZATION)) {
                String authCode = keys[i].split("=")[1];
                authCode = authCode.split("\"")[0];
                return authCode;
            }
        }
        return Constants.EMPTY;
    }

    public synchronized boolean updateLegacyToken() {
        logger.debug("updateLegacyToken");
        try {
            /**
             * The authorization with Jetty HttpClient doens't work anymore
             * When calling Jetty with same headers and content a ConcurrentExcpetion is thrown
             * So fallback legacy authorization will stay on java.net handling
             */
            String authUri = "https://" + BimmerConstants.AUTH_SERVER_MAP.get(configuration.region)
                    + BimmerConstants.OAUTH_ENDPOINT;
            URL url = new URL(authUri);
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty(HttpHeader.CONTENT_TYPE.toString(), CONTENT_TYPE_URL_ENCODED);
            con.setRequestProperty(HttpHeader.CONNECTION.toString(), KEEP_ALIVE);
            con.setRequestProperty(HttpHeader.HOST.toString(),
                    BimmerConstants.API_SERVER_MAP.get(configuration.region));
            con.setRequestProperty(HttpHeader.AUTHORIZATION.toString(),
                    BimmerConstants.LEGACY_AUTHORIZATION_VALUE_MAP.get(configuration.region));
            con.setRequestProperty(CREDENTIALS, BimmerConstants.LEGACY_CREDENTIAL_VALUES);
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            byte[] input = getAuthEncodedData().getBytes("utf-8");
            os.write(input, 0, input.length);

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            token.setMyBmwApiUsage(false);
            return tokenFromUrl(con.getHeaderField(HttpHeader.LOCATION.toString()));
        } catch (IOException e) {
            logger.warn("{}", e.getMessage());
        }
        return false;
    }

    public boolean tokenFromUrl(String encodedUrl) {
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
        logger.info("Token valid? {}", token.isValid());
        return token.isValid();
    }

    public static String codeFromUrl(String encodedUrl) {
        final MultiMap<String> tokenMap = new MultiMap<String>();
        UrlEncoded.decodeTo(encodedUrl, tokenMap, StandardCharsets.US_ASCII);
        final StringBuilder codeFound = new StringBuilder();
        tokenMap.forEach((key, value) -> {
            if (value.size() > 0) {
                String val = value.get(0);
                if (key.endsWith(CODE)) {
                    codeFound.append(val.toString());
                }
            }
        });
        return codeFound.toString();
    }

    private String getAuthEncodedData() {
        MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add(CLIENT_ID, BimmerConstants.LEGACY_CLIENT_ID);
        dataMap.add(RESPONSE_TYPE, TOKEN);
        dataMap.add(REDIRECT_URI, BimmerConstants.LEGACY_REDIRECT_URI_VALUE);
        dataMap.add(SCOPE, BimmerConstants.LEGACY_SCOPE_VALUES);
        dataMap.add(USERNAME, configuration.userName);
        dataMap.add(PASSWORD, configuration.password);
        return UrlEncoded.encode(dataMap, Charset.defaultCharset(), false);
    }
}
