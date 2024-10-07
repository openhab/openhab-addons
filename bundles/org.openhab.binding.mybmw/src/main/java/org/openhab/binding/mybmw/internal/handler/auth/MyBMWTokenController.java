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
package org.openhab.binding.mybmw.internal.handler.auth;

import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.API_OAUTH_CONFIG;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.APP_VERSIONS;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.AUTHORIZATION_CODE;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.AUTH_PROVIDER;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.BRAND_BMW;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.CHINA_LOGIN;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.CHINA_PUBLIC_KEY;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.EADRAX_SERVER_MAP;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.LOGIN_NONCE;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.OAUTH_ENDPOINT;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.OCP_APIM_KEYS;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.REGION_CHINA;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.REGION_NORTH_AMERICA;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.REGION_ROW;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.USER_AGENT;
import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.X_USER_AGENT;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.AUTHORIZATION;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CLIENT_ID;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CODE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CODE_CHALLENGE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CODE_CHALLENGE_METHOD;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CODE_VERIFIER;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.CONTENT_TYPE_URL_ENCODED;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.GRANT_TYPE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.HEADER_ACP_SUBSCRIPTION_KEY;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.HEADER_BMW_CORRELATION_ID;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.HEADER_X_CORRELATION_ID;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.HEADER_X_IDENTITY_PROVIDER;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.HEADER_X_USER_AGENT;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.NONCE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.PASSWORD;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.REDIRECT_URI;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.RESPONSE_TYPE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.SCOPE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.STATE;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.USERNAME;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Cipher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * requests the tokens for MyBMW API authorization
 *
 * thanks to bimmer_connected https://github.com/bimmerconnected/bimmer_connected
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
     * update the Thing status accordingly.
     *
     * @return token
     */
    public Token getToken() {
        if (!token.isValid()) {
            boolean tokenUpdateSuccess = false;
            switch (configuration.region) {
                case REGION_CHINA:
                    tokenUpdateSuccess = updateTokenChina();
                    break;
                case REGION_NORTH_AMERICA:
                case REGION_ROW:
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
     * Everything is caught by surrounding try catch
     * - HTTP Exceptions
     * - JSONSyntax Exceptions
     * - potential NullPointer Exceptions
     *
     * @return true if the token was successfully updated
     */
    private synchronized boolean updateToken() {
        try {
            /*
             * Step 1) Get basic values for further queries
             */
            String uuidString = UUID.randomUUID().toString();

            String authValuesUrl = "https://" + EADRAX_SERVER_MAP.get(configuration.region) + API_OAUTH_CONFIG;
            Request authValuesRequest = httpClient.newRequest(authValuesUrl);
            authValuesRequest.header(HEADER_ACP_SUBSCRIPTION_KEY, OCP_APIM_KEYS.get(configuration.region));
            authValuesRequest.header(HEADER_X_USER_AGENT, String.format(X_USER_AGENT, BRAND_BMW,
                    APP_VERSIONS.get(configuration.region), configuration.region));
            authValuesRequest.header(HEADER_X_IDENTITY_PROVIDER, AUTH_PROVIDER);
            authValuesRequest.header(HEADER_X_CORRELATION_ID, uuidString);
            authValuesRequest.header(HEADER_BMW_CORRELATION_ID, uuidString);

            ContentResponse authValuesResponse = authValuesRequest.send();
            if (authValuesResponse.getStatus() != 200) {
                throw new HttpResponseException("URL: " + authValuesRequest.getURI() + ", Error: "
                        + authValuesResponse.getStatus() + ", Message: " + authValuesResponse.getContentAsString(),
                        authValuesResponse);
            }
            AuthQueryResponse aqr = JsonStringDeserializer.deserializeString(authValuesResponse.getContentAsString(),
                    AuthQueryResponse.class);

            logger.trace("authQueryResponse: {}", aqr);

            /*
             * Step 2) Calculate values for oauth base parameters
             */
            String codeVerifier = generateCodeVerifier();
            String codeChallenge = generateCodeChallenge(codeVerifier);
            String state = generateState();

            MultiMap<@Nullable String> baseParams = new MultiMap<>();
            baseParams.put(CLIENT_ID, aqr.clientId);
            baseParams.put(RESPONSE_TYPE, CODE);
            baseParams.put(REDIRECT_URI, aqr.returnUrl);
            baseParams.put(STATE, state);
            baseParams.put(NONCE, LOGIN_NONCE);
            baseParams.put(SCOPE, String.join(Constants.SPACE, aqr.scopes));
            baseParams.put(CODE_CHALLENGE, codeChallenge);
            baseParams.put(CODE_CHALLENGE_METHOD, "S256");

            /**
             * Step 3) Authorization with username and password
             */
            String loginUrl = aqr.gcdmBaseUrl + OAUTH_ENDPOINT;
            Request loginRequest = httpClient.POST(loginUrl);

            loginRequest.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);

            MultiMap<@Nullable String> loginParams = new MultiMap<>(baseParams);
            loginParams.put(GRANT_TYPE, AUTHORIZATION_CODE);
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
            MultiMap<@Nullable String> authParams = new MultiMap<>(baseParams);
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

            MultiMap<@Nullable String> codeParams = new MultiMap<>();
            codeParams.put(CODE, code);
            codeParams.put(CODE_VERIFIER, codeVerifier);
            codeParams.put(REDIRECT_URI, aqr.returnUrl);
            codeParams.put(GRANT_TYPE, AUTHORIZATION_CODE);
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

    private String generateState() {
        String stateBytes = StringUtils.getRandomAlphabetic(64).toLowerCase();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes.getBytes());
    }

    private String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private String generateCodeVerifier() {
        String verfifierBytes = StringUtils.getRandomAlphabetic(64).toLowerCase();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(verfifierBytes.getBytes());
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
        final MultiMap<@Nullable String> tokenMap = new MultiMap<>();
        UrlEncoded.decodeTo(encodedUrl, tokenMap, StandardCharsets.US_ASCII);
        final StringBuilder codeFound = new StringBuilder();
        tokenMap.forEach((key, value) -> {
            if (!value.isEmpty()) {
                String val = value.get(0);
                if (key.endsWith(CODE) && (val != null)) {
                    codeFound.append(val.toString());
                }
            }
        });
        return codeFound.toString();
    }

    private synchronized boolean updateTokenChina() {
        try {
            /**
             * Step 1) get public key
             */
            String publicKeyUrl = "https://" + EADRAX_SERVER_MAP.get(REGION_CHINA) + CHINA_PUBLIC_KEY;
            Request oauthQueryRequest = httpClient.newRequest(publicKeyUrl);
            oauthQueryRequest.header(HttpHeader.USER_AGENT, USER_AGENT);
            oauthQueryRequest.header(HEADER_X_USER_AGENT, String.format(X_USER_AGENT, BRAND_BMW,
                    APP_VERSIONS.get(configuration.region), configuration.region));
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
            String tokenUrl = "https://" + EADRAX_SERVER_MAP.get(REGION_CHINA) + CHINA_LOGIN;
            Request loginRequest = httpClient.POST(tokenUrl);
            loginRequest.header(HEADER_X_USER_AGENT, String.format(X_USER_AGENT, BRAND_BMW,
                    APP_VERSIONS.get(configuration.region), configuration.region));
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
