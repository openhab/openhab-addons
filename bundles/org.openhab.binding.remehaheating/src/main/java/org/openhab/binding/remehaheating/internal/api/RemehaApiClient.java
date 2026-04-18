/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.remehaheating.internal.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link RemehaApiClient} handles OAuth2 PKCE authentication and API communication with Remeha Home services.
 *
 * This client implements a custom OAuth2 PKCE authentication flow required by the Remeha API.
 * The openHAB core OAuth2 client cannot be used because the Remeha API uses Azure B2C with a non-standard
 * authentication flow that requires:
 * - CSRF token extraction from authentication page cookies
 * - Custom state properties (TID) handling for Azure B2C
 * - Multi-step form submission with CSRF tokens
 * - Manual authorization code extraction from redirect responses
 *
 * The standard OAuth2 Resource Owner Password Credentials flow is not supported by this Azure B2C
 * configuration, and the authorization code flow requires programmatic interaction with the login form,
 * which is not possible with the standard OAuth2 client.
 *
 * @author Michael Fraedrich - Initial contribution
 */
@NonNullByDefault
public class RemehaApiClient {
    private final Logger logger = LoggerFactory.getLogger(RemehaApiClient.class);
    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private @Nullable String accessToken;
    private String codeVerifier = "";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String API_BASE_URL = "https://api.bdrthermea.net/Mobile/api";
    private static final String SUBSCRIPTION_KEY = "df605c5470d846fc91e848b1cc653ddf";
    private static final long REQUEST_TIMEOUT_MS = 30000;
    private static final Pattern CSRF_PATTERN = Pattern.compile("x-ms-cpim-csrf=([^;]+)");

    /**
     * Creates a new RemehaApiClient with the provided HttpClient.
     *
     * Note: This client requires custom buffer sizes (16384 bytes) to handle large OAuth2 responses
     * from Azure B2C authentication. The HttpClient should be created via HttpClientFactory.createHttpClient()
     * with buffer sizes configured in the factory, not using the common HTTP client.
     *
     * @param httpClient HttpClient instance with appropriate buffer sizes configured
     */
    public RemehaApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Authenticates with Remeha API using OAuth2 PKCE flow.
     *
     * This method performs the complete authentication sequence:
     * 1. Generates PKCE code verifier and challenge
     * 2. Initiates OAuth2 authorization request
     * 3. Extracts CSRF token from response cookies
     * 4. Submits user credentials
     * 5. Retrieves authorization code from redirect
     * 6. Exchanges authorization code for access token
     *
     * @param email Remeha Home account email
     * @param password Remeha Home account password
     * @return true if authentication successful, false otherwise
     */
    public boolean authenticate(String email, String password) {
        try {
            codeVerifier = generateRandomString();
            String codeChallenge = generateCodeChallenge(codeVerifier);
            String state = generateRandomString();

            String authUrl = buildAuthUrl(codeChallenge, state);
            Request authRequest = httpClient.newRequest(authUrl).method(HttpMethod.GET).timeout(REQUEST_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);

            ContentResponse response = authRequest.send();
            String requestId = response.getHeaders().get("x-request-id");
            String csrfToken = extractCsrfToken(response);

            if (csrfToken == null || requestId == null) {
                logger.debug("Failed to extract CSRF token or request ID");
                return false;
            }

            String stateProperties = createStateProperties(requestId);
            if (!submitCredentials(email, password, csrfToken, stateProperties)) {
                return false;
            }

            String authCode = getAuthorizationCode(csrfToken, stateProperties);
            if (authCode == null) {
                return false;
            }

            return exchangeCodeForToken(authCode);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Authentication interrupted", e);
            return false;
        } catch (Exception e) {
            logger.debug("Authentication failed", e);
            return false;
        }
    }

    /**
     * Retrieves the dashboard data containing all heating system information.
     *
     * The dashboard includes:
     * - Appliance information (boiler status, water pressure)
     * - Climate zones (room temperature, target temperature)
     * - Hot water zones (DHW temperature, mode, status)
     * - Outdoor temperature information
     *
     * @return Dashboard JSON object or null if request fails
     */
    public @Nullable JsonObject getDashboard() {
        if (accessToken == null) {
            return null;
        }
        try {
            ContentResponse response = httpClient.newRequest(API_BASE_URL + "/homes/dashboard").method(HttpMethod.GET)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).header("Authorization", "Bearer " + accessToken)
                    .header("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY).send();
            if (response.getStatus() == 401) {
                logger.debug("Received 401 Unauthorized, token expired");
                accessToken = null;
                return null;
            }
            return gson.fromJson(response.getContentAsString(), JsonObject.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Dashboard request interrupted", e);
            return null;
        } catch (Exception e) {
            logger.debug("Failed to get dashboard", e);
            return null;
        }
    }

    /**
     * Sets the target room temperature for a climate zone.
     *
     * @param climateZoneId Climate zone identifier from dashboard data
     * @param temperature Target temperature in Celsius
     * @return true if request successful, false otherwise
     */
    public boolean setTemperature(String climateZoneId, double temperature) {
        return apiRequest("/climate-zones/" + climateZoneId + "/modes/manual",
                "{\"roomTemperatureSetPoint\":" + temperature + "}");
    }

    /**
     * Sets the DHW (Domestic Hot Water) operating mode.
     *
     * @param hotWaterZoneId Hot water zone identifier from dashboard data
     * @param mode DHW mode: "anti-frost", "schedule", or "continuous-comfort"
     * @return true if request successful, false otherwise
     */
    public boolean setDhwMode(String hotWaterZoneId, String mode) {
        return apiRequest("/hot-water-zones/" + hotWaterZoneId + "/modes/" + mode, null);
    }

    private String generateCodeChallenge(String verifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private String generateRandomString() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildAuthUrl(String codeChallenge, String state) {
        return "https://remehalogin.bdrthermea.net/bdrb2cprod.onmicrosoft.com/oauth2/v2.0/authorize"
                + "?response_type=code" + "&client_id=6ce007c6-0628-419e-88f4-bee2e6418eec" + "&redirect_uri="
                + URLEncoder.encode("com.b2c.remehaapp://login-callback", StandardCharsets.UTF_8) + "&scope="
                + URLEncoder.encode(
                        "openid https://bdrb2cprod.onmicrosoft.com/iotdevice/user_impersonation offline_access",
                        StandardCharsets.UTF_8)
                + "&state=" + state + "&code_challenge=" + codeChallenge + "&code_challenge_method=S256"
                + "&p=B2C_1A_RPSignUpSignInNewRoomV3.1" + "&brand=remeha" + "&lang=en" + "&nonce=defaultNonce"
                + "&prompt=login" + "&signUp=False";
    }

    private @Nullable String extractCsrfToken(ContentResponse response) {
        HttpFields headers = response.getHeaders();
        logger.debug("Extracting CSRF token from cookies");
        for (String setCookieHeader : headers.getValuesList("Set-Cookie")) {
            if (setCookieHeader != null && setCookieHeader.contains("x-ms-cpim-csrf=")) {
                Matcher matcher = CSRF_PATTERN.matcher(setCookieHeader);
                if (matcher.find()) {
                    String token = matcher.group(1);
                    logger.debug("CSRF token extracted from cookies");
                    return token;
                }
            }
        }
        logger.debug("No CSRF token found in cookies");
        return null;
    }

    private String createStateProperties(String requestId) {
        String json = "{\"TID\":\"" + requestId + "\"}";
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private boolean submitCredentials(String email, String password, String csrfToken, String stateProperties) {
        try {
            String baseUrl = "https://remehalogin.bdrthermea.net/bdrb2cprod.onmicrosoft.com/B2C_1A_RPSignUpSignInNewRoomv3.1/SelfAsserted";

            String formData = "request_type=RESPONSE" + "&signInName="
                    + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&password="
                    + URLEncoder.encode(password, StandardCharsets.UTF_8);

            logger.debug("Submitting credentials with CSRF token");

            Request request = httpClient.newRequest(baseUrl).method(HttpMethod.POST)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .param("tx", "StateProperties=" + stateProperties).param("p", "B2C_1A_RPSignUpSignInNewRoomv3.1")
                    .header("x-csrf-token", csrfToken).header("Content-Type", "application/x-www-form-urlencoded")
                    .content(new StringContentProvider(formData));

            ContentResponse response = request.send();
            int status = response.getStatus();
            logger.debug("Submit credentials response: {}", status);
            if (status != 200) {
                logger.debug("Credential submission failed with status: {}", status);
            }
            return status == 200;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Credential submission interrupted", e);
            return false;
        } catch (Exception e) {
            logger.debug("Failed to submit credentials", e);
            return false;
        }
    }

    private @Nullable String getAuthorizationCode(String csrfToken, String stateProperties) {
        try {
            String baseUrl = "https://remehalogin.bdrthermea.net/bdrb2cprod.onmicrosoft.com/B2C_1A_RPSignUpSignInNewRoomv3.1/api/CombinedSigninAndSignup/confirmed";

            Request request = httpClient.newRequest(baseUrl).method(HttpMethod.GET)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).param("rememberMe", "false")
                    .param("csrf_token", csrfToken).param("tx", "StateProperties=" + stateProperties)
                    .param("p", "B2C_1A_RPSignUpSignInNewRoomv3.1").followRedirects(false);

            ContentResponse response = request.send();
            logger.debug("Authorization code response status: {}", response.getStatus());

            if (response.getStatus() == 302) {
                String location = response.getHeaders().get("Location");
                logger.debug("Redirect location: {}", location);
                if (location != null) {
                    Pattern pattern = Pattern.compile("code=([^&]+)");
                    Matcher matcher = pattern.matcher(location);
                    if (matcher.find()) {
                        String authCode = matcher.group(1);
                        logger.debug("Authorization code successfully extracted.");
                        return authCode;
                    }
                }
            } else {
                logger.debug("Expected 302 redirect, got {}", response.getStatus());
            }
        } catch (Exception e) {
            logger.debug("Failed to get authorization code: {}", e.getMessage());
        }
        return null;
    }

    private boolean exchangeCodeForToken(String authCode) {
        try {
            String url = "https://remehalogin.bdrthermea.net/bdrb2cprod.onmicrosoft.com/oauth2/v2.0/token?p=B2C_1A_RPSignUpSignInNewRoomV3.1";
            String formData = "grant_type=authorization_code&code=" + authCode + "&redirect_uri="
                    + URLEncoder.encode("com.b2c.remehaapp://login-callback", StandardCharsets.UTF_8)
                    + "&code_verifier=" + codeVerifier + "&client_id=6ce007c6-0628-419e-88f4-bee2e6418eec";

            Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .content(new StringContentProvider(formData));

            ContentResponse response = request.send();
            if (response.getStatus() == 200) {
                String json = response.getContentAsString();
                JsonObject tokenResponse = gson.fromJson(json, JsonObject.class);
                if (tokenResponse != null && tokenResponse.has("access_token")) {
                    accessToken = tokenResponse.get("access_token").getAsString();
                    logger.debug("Successfully obtained access token");
                    return true;
                } else {
                    logger.debug("Token response missing access_token field");
                }
            } else {
                logger.debug("Token exchange failed with status: {}", response.getStatus());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Token exchange interrupted", e);
        } catch (java.util.concurrent.TimeoutException e) {
            logger.debug("Token exchange timed out after {}ms", REQUEST_TIMEOUT_MS, e);
        } catch (Exception e) {
            logger.debug("Failed to exchange code for token: {}", e.getMessage());
        }
        return false;
    }

    private boolean apiRequest(String path, @Nullable String jsonData) {
        if (accessToken == null) {
            return false;
        }
        try {
            Request request = httpClient.newRequest(API_BASE_URL + path).method(HttpMethod.POST)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).header("Authorization", "Bearer " + accessToken)
                    .header("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY).header("Content-Type", "application/json");
            if (jsonData != null) {
                request.content(new StringContentProvider(jsonData));
            }
            int status = request.send().getStatus();
            if (status == 401) {
                logger.debug("Received 401 Unauthorized, token expired");
                accessToken = null;
                return false;
            }
            return status == 200;
        } catch (Exception e) {
            logger.debug("API request failed for {}: {}", path, e.getMessage());
            return false;
        }
    }
}
