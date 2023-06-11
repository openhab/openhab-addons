/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.handler;

import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.*;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.mybmw.internal.MyBMWConfiguration;
import org.openhab.binding.mybmw.internal.VehicleConfiguration;
import org.openhab.binding.mybmw.internal.dto.auth.AuthQueryResponse;
import org.openhab.binding.mybmw.internal.dto.auth.AuthResponse;
import org.openhab.binding.mybmw.internal.dto.auth.ChinaPublicKeyResponse;
import org.openhab.binding.mybmw.internal.dto.auth.ChinaTokenExpiration;
import org.openhab.binding.mybmw.internal.dto.auth.ChinaTokenResponse;
import org.openhab.binding.mybmw.internal.dto.network.NetworkError;
import org.openhab.binding.mybmw.internal.handler.simulation.Injector;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.HTTPConstants;
import org.openhab.binding.mybmw.internal.utils.ImageProperties;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyBMWProxy} This class holds the important constants for the BMW Connected Drive Authorization.
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
public class MyBMWProxy {
    private final Logger logger = LoggerFactory.getLogger(MyBMWProxy.class);
    private Optional<RemoteServiceHandler> remoteServiceHandler = Optional.empty();
    private final Token token = new Token();
    private final HttpClient httpClient;
    private final MyBMWConfiguration configuration;

    /**
     * URLs taken from https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/const.py
     */
    final String vehicleUrl;
    final String remoteCommandUrl;
    final String remoteStatusUrl;
    final String serviceExecutionAPI = "/executeService";
    final String serviceExecutionStateAPI = "/serviceExecutionStatus";
    final String remoteServiceEADRXstatusUrl = BimmerConstants.API_REMOTE_SERVICE_BASE_URL
            + "eventStatus?eventId={event_id}";

    public MyBMWProxy(HttpClientFactory httpClientFactory, MyBMWConfiguration config) {
        httpClient = httpClientFactory.getCommonHttpClient();
        configuration = config;

        vehicleUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(configuration.region)
                + BimmerConstants.API_VEHICLES;

        remoteCommandUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(configuration.region)
                + BimmerConstants.API_REMOTE_SERVICE_BASE_URL;
        remoteStatusUrl = remoteCommandUrl + "eventStatus";
    }

    public synchronized void call(final String url, final boolean post, final @Nullable String encoding,
            final @Nullable String params, final String brand, final ResponseCallback callback) {
        // only executed in "simulation mode"
        // SimulationTest.testSimulationOff() assures Injector is off when releasing
        if (Injector.isActive()) {
            if (url.equals(vehicleUrl)) {
                ((StringResponseCallback) callback).onResponse(Injector.getDiscovery());
            } else if (url.endsWith(vehicleUrl)) {
                ((StringResponseCallback) callback).onResponse(Injector.getStatus());
            } else {
                logger.debug("Simulation of {} not supported", url);
            }
            return;
        }

        // return in case of unknown brand
        if (!BimmerConstants.ALL_BRANDS.contains(brand.toLowerCase())) {
            logger.warn("Unknown Brand {}", brand);
            return;
        }

        final Request req;
        final String completeUrl;

        if (post) {
            completeUrl = url;
            req = httpClient.POST(url);
            if (encoding != null) {
                req.header(HttpHeader.CONTENT_TYPE, encoding);
                if (CONTENT_TYPE_URL_ENCODED.equals(encoding)) {
                    req.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED, params, StandardCharsets.UTF_8));
                } else if (CONTENT_TYPE_JSON_ENCODED.equals(encoding)) {
                    req.content(new StringContentProvider(CONTENT_TYPE_JSON_ENCODED, params, StandardCharsets.UTF_8));
                }
            }
        } else {
            completeUrl = params == null ? url : url + Constants.QUESTION + params;
            req = httpClient.newRequest(completeUrl);
        }
        req.header(HttpHeader.AUTHORIZATION, getToken().getBearerToken());
        req.header(HTTPConstants.X_USER_AGENT,
                String.format(BimmerConstants.X_USER_AGENT, brand, configuration.region));
        req.header(HttpHeader.ACCEPT_LANGUAGE, configuration.language);
        if (callback instanceof ByteResponseCallback) {
            req.header(HttpHeader.ACCEPT, "image/png");
        } else {
            req.header(HttpHeader.ACCEPT, CONTENT_TYPE_JSON_ENCODED);
        }

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

    public void get(String url, @Nullable String coding, @Nullable String params, final String brand,
            ResponseCallback callback) {
        call(url, false, coding, params, brand, callback);
    }

    public void post(String url, @Nullable String coding, @Nullable String params, final String brand,
            ResponseCallback callback) {
        call(url, true, coding, params, brand, callback);
    }

    /**
     * request all vehicles for one specific brand
     *
     * @param brand
     * @param callback
     */
    public void requestVehicles(String brand, StringResponseCallback callback) {
        // calculate necessary parameters for query
        MultiMap<String> vehicleParams = new MultiMap<String>();
        vehicleParams.put(BimmerConstants.TIRE_GUARD_MODE, Constants.ENABLED);
        vehicleParams.put(BimmerConstants.APP_DATE_TIME, Long.toString(System.currentTimeMillis()));
        vehicleParams.put(BimmerConstants.APP_TIMEZONE, Integer.toString(Converter.getOffsetMinutes()));
        String params = UrlEncoded.encode(vehicleParams, StandardCharsets.UTF_8, false);
        get(vehicleUrl + "?" + params, null, null, brand, callback);
    }

    /**
     * request vehicles for all possible brands
     *
     * @param callback
     */
    public void requestVehicles(StringResponseCallback callback) {
        BimmerConstants.ALL_BRANDS.forEach(brand -> {
            requestVehicles(brand, callback);
        });
    }

    public void requestImage(VehicleConfiguration config, ImageProperties props, ByteResponseCallback callback) {
        final String localImageUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(configuration.region)
                + "/eadrax-ics/v3/presentation/vehicles/" + config.vin + "/images?carView=" + props.viewport;
        get(localImageUrl, null, null, config.vehicleBrand, callback);
    }

    /**
     * request charge statistics for electric vehicles
     *
     * @param callback
     */
    public void requestChargeStatistics(VehicleConfiguration config, StringResponseCallback callback) {
        MultiMap<String> chargeStatisticsParams = new MultiMap<String>();
        chargeStatisticsParams.put("vin", config.vin);
        chargeStatisticsParams.put("currentDate", Converter.getCurrentISOTime());
        String params = UrlEncoded.encode(chargeStatisticsParams, StandardCharsets.UTF_8, false);
        String chargeStatisticsUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(configuration.region)
                + "/eadrax-chs/v1/charging-statistics?" + params;
        get(chargeStatisticsUrl, null, null, config.vehicleBrand, callback);
    }

    /**
     * request charge statistics for electric vehicles
     *
     * @param callback
     */
    public void requestChargeSessions(VehicleConfiguration config, StringResponseCallback callback) {
        MultiMap<String> chargeSessionsParams = new MultiMap<String>();
        chargeSessionsParams.put("vin", "WBY1Z81040V905639");
        chargeSessionsParams.put("maxResults", "40");
        chargeSessionsParams.put("include_date_picker", "true");
        String params = UrlEncoded.encode(chargeSessionsParams, StandardCharsets.UTF_8, false);
        String chargeSessionsUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(configuration.region)
                + "/eadrax-chs/v1/charging-sessions?" + params;

        get(chargeSessionsUrl, null, null, config.vehicleBrand, callback);
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
            boolean tokenUpdateSuccess = false;
            switch (configuration.region) {
                case BimmerConstants.REGION_CHINA:
                    tokenUpdateSuccess = updateTokenChina();
                    break;
                case BimmerConstants.REGION_NORTH_AMERICA:
                    tokenUpdateSuccess = updateToken();
                    break;
                case BimmerConstants.REGION_ROW:
                    tokenUpdateSuccess = updateToken();
                    break;
                default:
                    logger.warn("Region {} not supported", configuration.region);
                    break;
            }
            if (!tokenUpdateSuccess) {
                logger.debug("Authorization failed!");
            }
        }
        return token;
    }

    /**
     * Everything is catched by surroundig try catch
     * - HTTP Exceptions
     * - JSONSyntax Exceptions
     * - potential NullPointer Exceptions
     *
     * @return
     */
    @SuppressWarnings("null")
    public synchronized boolean updateToken() {
        try {
            /*
             * Step 1) Get basic values for further queries
             */
            String authValuesUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(configuration.region)
                    + BimmerConstants.API_OAUTH_CONFIG;
            Request authValuesRequest = httpClient.newRequest(authValuesUrl);
            authValuesRequest.header(ACP_SUBSCRIPTION_KEY, BimmerConstants.OCP_APIM_KEYS.get(configuration.region));
            authValuesRequest.header(X_USER_AGENT,
                    String.format(BimmerConstants.X_USER_AGENT, BimmerConstants.BRAND_BMW, configuration.region));

            ContentResponse authValuesResponse = authValuesRequest.send();
            if (authValuesResponse.getStatus() != 200) {
                throw new HttpResponseException("URL: " + authValuesRequest.getURI() + ", Error: "
                        + authValuesResponse.getStatus() + ", Message: " + authValuesResponse.getContentAsString(),
                        authValuesResponse);
            }
            AuthQueryResponse aqr = Converter.getGson().fromJson(authValuesResponse.getContentAsString(),
                    AuthQueryResponse.class);

            /*
             * Step 2) Calculate values for base parameters
             */
            String verfifierBytes = Converter.getRandomString(64);
            String codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(verfifierBytes.getBytes());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            String codeChallange = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            String stateBytes = Converter.getRandomString(16);
            String state = Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes.getBytes());

            MultiMap<String> baseParams = new MultiMap<String>();
            baseParams.put(CLIENT_ID, aqr.clientId);
            baseParams.put(RESPONSE_TYPE, CODE);
            baseParams.put(REDIRECT_URI, aqr.returnUrl);
            baseParams.put(STATE, state);
            baseParams.put(NONCE, BimmerConstants.LOGIN_NONCE);
            baseParams.put(SCOPE, String.join(Constants.SPACE, aqr.scopes));
            baseParams.put(CODE_CHALLENGE, codeChallange);
            baseParams.put(CODE_CHALLENGE_METHOD, "S256");

            /**
             * Step 3) Authorization with username and password
             */
            String loginUrl = aqr.gcdmBaseUrl + BimmerConstants.OAUTH_ENDPOINT;
            Request loginRequest = httpClient.POST(loginUrl);
            loginRequest.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);

            MultiMap<String> loginParams = new MultiMap<String>(baseParams);
            loginParams.put(GRANT_TYPE, BimmerConstants.AUTHORIZATION_CODE);
            loginParams.put(USERNAME, configuration.userName);
            loginParams.put(PASSWORD, configuration.password);
            loginRequest.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED,
                    UrlEncoded.encode(loginParams, StandardCharsets.UTF_8, false), StandardCharsets.UTF_8));
            ContentResponse loginResponse = loginRequest.send();
            if (loginResponse.getStatus() != 200) {
                throw new HttpResponseException("URL: " + loginRequest.getURI() + ", Error: "
                        + loginResponse.getStatus() + ", Message: " + loginResponse.getContentAsString(),
                        loginResponse);
            }
            String authCode = getAuthCode(loginResponse.getContentAsString());

            /**
             * Step 4) Authorize with code
             */
            Request authRequest = httpClient.POST(loginUrl).followRedirects(false);
            MultiMap<String> authParams = new MultiMap<String>(baseParams);
            authParams.put(AUTHORIZATION, authCode);
            authRequest.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
            authRequest.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED,
                    UrlEncoded.encode(authParams, StandardCharsets.UTF_8, false), StandardCharsets.UTF_8));
            ContentResponse authResponse = authRequest.send();
            if (authResponse.getStatus() != 302) {
                throw new HttpResponseException("URL: " + authRequest.getURI() + ", Error: " + authResponse.getStatus()
                        + ", Message: " + authResponse.getContentAsString(), authResponse);
            }
            String code = MyBMWProxy.codeFromUrl(authResponse.getHeaders().get(HttpHeader.LOCATION));

            /**
             * Step 5) Request token
             */
            Request codeRequest = httpClient.POST(aqr.tokenEndpoint);
            String basicAuth = "Basic "
                    + Base64.getUrlEncoder().encodeToString((aqr.clientId + ":" + aqr.clientSecret).getBytes());
            codeRequest.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
            codeRequest.header(AUTHORIZATION, basicAuth);

            MultiMap<String> codeParams = new MultiMap<String>();
            codeParams.put(CODE, code);
            codeParams.put(CODE_VERIFIER, codeVerifier);
            codeParams.put(REDIRECT_URI, aqr.returnUrl);
            codeParams.put(GRANT_TYPE, BimmerConstants.AUTHORIZATION_CODE);
            codeRequest.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED,
                    UrlEncoded.encode(codeParams, StandardCharsets.UTF_8, false), StandardCharsets.UTF_8));
            ContentResponse codeResponse = codeRequest.send();
            if (codeResponse.getStatus() != 200) {
                throw new HttpResponseException("URL: " + codeRequest.getURI() + ", Error: " + codeResponse.getStatus()
                        + ", Message: " + codeResponse.getContentAsString(), codeResponse);
            }
            AuthResponse ar = Converter.getGson().fromJson(codeResponse.getContentAsString(), AuthResponse.class);
            token.setType(ar.tokenType);
            token.setToken(ar.accessToken);
            token.setExpiration(ar.expiresIn);
            return true;
        } catch (Exception e) {
            logger.warn("Authorization Exception: {}", e.getMessage());
        }
        return false;
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

    @SuppressWarnings("null")
    public synchronized boolean updateTokenChina() {
        try {
            /**
             * Step 1) get public key
             */
            String publicKeyUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(BimmerConstants.REGION_CHINA)
                    + BimmerConstants.CHINA_PUBLIC_KEY;
            Request oauthQueryRequest = httpClient.newRequest(publicKeyUrl);
            oauthQueryRequest.header(HttpHeader.USER_AGENT, BimmerConstants.USER_AGENT);
            oauthQueryRequest.header(X_USER_AGENT,
                    String.format(BimmerConstants.X_USER_AGENT, BimmerConstants.BRAND_BMW, configuration.region));
            ContentResponse publicKeyResponse = oauthQueryRequest.send();
            if (publicKeyResponse.getStatus() != 200) {
                throw new HttpResponseException("URL: " + oauthQueryRequest.getURI() + ", Error: "
                        + publicKeyResponse.getStatus() + ", Message: " + publicKeyResponse.getContentAsString(),
                        publicKeyResponse);
            }
            ChinaPublicKeyResponse pkr = Converter.getGson().fromJson(publicKeyResponse.getContentAsString(),
                    ChinaPublicKeyResponse.class);

            /**
             * Step 2) Encode password with public key
             */
            // https://www.baeldung.com/java-read-pem-file-keys
            String publicKeyStr = pkr.data.value;
            String publicKeyPEM = publicKeyStr.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll(System.lineSeparator(), "").replace("-----END PUBLIC KEY-----", "").replace("\\r", "")
                    .replace("\\n", "").trim();
            byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(spec);
            // https://www.thexcoders.net/java-ciphers-rsa/
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(configuration.password.getBytes());
            String encodedPassword = Base64.getEncoder().encodeToString(encryptedBytes);

            /**
             * Step 3) Send Auth with encoded password
             */
            String tokenUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(BimmerConstants.REGION_CHINA)
                    + BimmerConstants.CHINA_LOGIN;
            Request loginRequest = httpClient.POST(tokenUrl);
            loginRequest.header(X_USER_AGENT,
                    String.format(BimmerConstants.X_USER_AGENT, BimmerConstants.BRAND_BMW, configuration.region));
            String jsonContent = "{ \"mobile\":\"" + configuration.userName + "\", \"password\":\"" + encodedPassword
                    + "\"}";
            loginRequest.content(new StringContentProvider(jsonContent));
            ContentResponse tokenResponse = loginRequest.send();
            if (tokenResponse.getStatus() != 200) {
                throw new HttpResponseException("URL: " + loginRequest.getURI() + ", Error: "
                        + tokenResponse.getStatus() + ", Message: " + tokenResponse.getContentAsString(),
                        tokenResponse);
            }
            String authCode = getAuthCode(tokenResponse.getContentAsString());

            /**
             * Step 4) Decode access token
             */
            ChinaTokenResponse cat = Converter.getGson().fromJson(authCode, ChinaTokenResponse.class);
            String token = cat.data.accessToken;
            // https://www.baeldung.com/java-jwt-token-decode
            String[] chunks = token.split("\\.");
            String tokenJwtDecodeStr = new String(Base64.getUrlDecoder().decode(chunks[1]));
            ChinaTokenExpiration cte = Converter.getGson().fromJson(tokenJwtDecodeStr, ChinaTokenExpiration.class);
            Token t = new Token();
            t.setToken(token);
            t.setType(cat.data.tokenType);
            t.setExpirationTotal(cte.exp);
            return true;
        } catch (Exception e) {
            logger.warn("Authorization Exception: {}", e.getMessage());
        }
        return false;
    }
}
