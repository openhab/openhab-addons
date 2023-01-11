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
package org.openhab.binding.mybmw.internal.handler.auth;

import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.ACP_SUBSCRIPTION_KEY;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.AUTHORIZATION;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CLIENT_ID;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CODE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CODE_CHALLENGE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CODE_CHALLENGE_METHOD;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CODE_VERIFIER;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CONTENT_TYPE_URL_ENCODED;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.GRANT_TYPE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.NONCE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.PASSWORD;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.REDIRECT_URI;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.RESPONSE_TYPE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.SCOPE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.STATE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.USERNAME;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.X_USER_AGENT;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;
import org.openhab.binding.mybmw.internal.dto.auth.AuthQueryResponse;
import org.openhab.binding.mybmw.internal.dto.auth.AuthResponse;
import org.openhab.binding.mybmw.internal.dto.auth.ChinaPublicKeyResponse;
import org.openhab.binding.mybmw.internal.dto.auth.ChinaTokenExpiration;
import org.openhab.binding.mybmw.internal.dto.auth.ChinaTokenResponse;
import org.openhab.binding.mybmw.internal.handler.backend.JsonStringDeserializer;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * requests the tokens for MyBMW API authorization
 * 
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - extracted from myBmwProxy
 */
@NonNullByDefault
public class MyBMWTokenController {

    private final Logger logger = LoggerFactory.getLogger(MyBMWTokenController.class);

    private Token token = new Token();
    private MyBMWBridgeConfiguration configuration;
    private HttpClient httpClient;

    public MyBMWTokenController(MyBMWBridgeConfiguration configuration, HttpClient httpClient) {
        this.configuration = configuration;
        this.httpClient = httpClient;
    }

    /**
     * Gets new token if old one is expired or invalid. In case of error the token
     * remains.
     * So if token refresh fails the corresponding requests will also fail and
     * update the
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
                logger.warn("Authorization failed!");
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
    private synchronized boolean updateToken() {
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
            AuthQueryResponse aqr = JsonStringDeserializer.deserializeString(authValuesResponse.getContentAsString(),
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
            String code = codeFromUrl(authResponse.getHeaders().get(HttpHeader.LOCATION));

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
            AuthResponse ar = JsonStringDeserializer.deserializeString(codeResponse.getContentAsString(),
                    AuthResponse.class);
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

    private String codeFromUrl(String encodedUrl) {
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
    private synchronized boolean updateTokenChina() {
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
            ChinaPublicKeyResponse pkr = JsonStringDeserializer
                    .deserializeString(publicKeyResponse.getContentAsString(), ChinaPublicKeyResponse.class);

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
            ChinaTokenResponse cat = JsonStringDeserializer.deserializeString(authCode, ChinaTokenResponse.class);
            String token = cat.data.accessToken;
            // https://www.baeldung.com/java-jwt-token-decode
            String[] chunks = token.split("\\.");
            String tokenJwtDecodeStr = new String(Base64.getUrlDecoder().decode(chunks[1]));
            ChinaTokenExpiration cte = JsonStringDeserializer.deserializeString(tokenJwtDecodeStr,
                    ChinaTokenExpiration.class);
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
