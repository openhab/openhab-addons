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
package org.openhab.binding.blink.internal.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.blink.internal.config.AccountConfiguration;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkEvents;
import org.openhab.binding.blink.internal.dto.BlinkHomescreen;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link AccountService} class handles all communication with account related blink apis.
 *
 * @author Matthias Oesterheld - Initial contribution
 * @author Robert T. Brown (-rb) - support Blink Authentication changes in 2025 (OAUTHv2)
 *
 */
@NonNullByDefault
public class AccountService extends BaseBlinkApiService {

    private static final String TIER_URL = "https://rest-prod.immedia-semi.com/api/v1/users/tier_info";
    private static final String OAUTH_BASE_URL = "https://api.oauth.blink.com";
    private static final String OAUTH_INITIAL_AUTH_URI = "/oauth/v2/authorize";
    private static final String OAUTH_SIGNIN_URI = "/oauth/v2/signin";
    private static final String OAUTH_MFA_VERIFY_URI = "/oauth/v2/2fa/verify";
    private static final String OAUTH_TOKEN_URI = "/oauth/token";
    private static final String OAUTH_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_7 like Mac OS X) "
            + "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.1 Mobile/15E148 Safari/604.1";
    private static final String OAUTH_TOKEN_USER_AGENT = "Blink/2511191620 CFNetwork/3860.200.71 Darwin/25.1.0";
    private static final String OAUTH_REDIRECT_URI = "immedia-blink://applinks.blink.com/signin/callback";
    private static final String OAUTH_V2_CLIENT_ID = "ios";

    private PkcePair pkce = new PkcePair();
    @Nullable
    private String csrfToken;
    Storage<String> storage;

    private final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(HttpClient httpClient, Storage<String> storage, Gson gson) {
        super(httpClient, gson);
        this.storage = storage;
    }

    /**
     * Authenticate to Blink using username and password, as stage 1 of OAUTH v2 authentication flow.
     * After this method returns, the user will receive an MFA code via SMS or email. They must type
     * it into the BlinkAccount configuration panel, and save it, which will initiate stage 2.
     *
     * @param config AccountConfiguration containing login data
     * @param hardwareId unique client hardware ID string
     * @throws IOException
     */
    public void loginStage1WithUsername(AccountConfiguration config, String hardwareId) throws IOException {
        // There is no MFA code in the configuration yet. Start the login flow at the beginning.
        Map<String, String> params = new HashMap<>();

        loginStage1SendHardwareId(hardwareId);
        csrfToken = loginStage1GetCsrfToken();
        if (csrfToken != null) {
            params.put("csrf-token", csrfToken);
        }
        params.put("username", config.email);
        params.put("password", config.password);
        loginStage1SendUsername(params);
        // User will gets an MFA code on their phone or via email, and must type it into the Blink Account Thing
    }

    /**
     * This is Stage 1, Step 1 OAUTH authentication: we send our unique hardware id and other identifying info.
     *
     * @param hardwareId a UUID uniquely identifying this client hardware
     * @throws IOException if there is a problem with the endpoint (maybe a network connectivity issue)
     */
    private void loginStage1SendHardwareId(String hardwareId) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        String url = OAUTH_BASE_URL + OAUTH_INITIAL_AUTH_URI;

        params.put("app_brand", "blink");
        params.put("app_version", "50.1");
        params.put("client_id", OAUTH_V2_CLIENT_ID);
        params.put("code_challenge", pkce.challenge);
        params.put("code_challenge_method", "S256");
        params.put("device_brand", "Apple");
        params.put("device_model", "iPhone16,1");
        params.put("device_os_version", "26.1");
        params.put("hardware_id", hardwareId);
        params.put("redirect_uri", OAUTH_REDIRECT_URI);
        params.put("response_type", "code");
        params.put("scope", "client");

        final Request request = httpClient.newRequest(url).method("GET");
        request.agent(OAUTH_USER_AGENT);
        request.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        request.header("Accept-Language", "en-US,en;q=0.9");
        params.forEach(request::param);

        logger.debug("Sending oauth initial authorize request: {}", url);
        ContentResponse contentResponse;
        try {
            contentResponse = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error calling Blink API ({}). Reason: {}", url, e.getMessage());
            throw new IOException(e);
        }
        if (contentResponse.getStatus() != 200) {
            throw new IOException("Blink OAUTH Initial Authorize failed <Status " + contentResponse.getStatus() + ">");
        }
    }

    /**
     * This is Stage 1 Step 2 of OAUTH authentication: GET the CSRF token so we can pass it to a later API endpoint
     *
     * @return the csrf-token value, used in subsequent OAUTH calls.
     * @throws IOException upon any sort of communication error
     */
    private @Nullable String loginStage1GetCsrfToken() throws IOException {
        String csrfToken = null;
        String url = OAUTH_BASE_URL + OAUTH_SIGNIN_URI;

        final Request request = httpClient.newRequest(url).method("GET");
        request.agent(OAUTH_USER_AGENT);
        request.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        request.header("Accept-Language", "en-US,en;q=0.9");

        logger.debug("fetching Blink Login Web Page: {}", url);
        ContentResponse contentResponse;
        try {
            contentResponse = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error fetching Blink Sign In page. Reason: {}", e.getMessage());
            throw new IOException(e);
        }
        if (contentResponse.getStatus() != 200) {
            throw new IOException(
                    "Blink OAUTH Sign In Page Request unsuccessful <Status " + contentResponse.getStatus() + ">");
        }
        // This is an HTML login page and one could use an HTML parser but I chose to just RegEx the csrf token field
        String contentString = contentResponse.getContentAsString();
        Pattern csrfPattern = Pattern.compile("csrf-token\":\"(.*?)\"");
        Matcher matches = csrfPattern.matcher(contentString);
        if (matches.find()) {
            csrfToken = matches.group(1);
            logger.debug("sign in page: CSRF token appears to be {}", partiallyRedactedString(csrfToken));
        } else {
            logger.warn("Unable to parse the Blink Login Web Page. They may have restructured the page at {}", url);
        }
        return csrfToken;
    }

    /**
     * This is Stage 1 Step 3 of OAUTH authentication: sending the username(email address) and password.
     *
     * @param params a Map of email address (username), password, and CSRF token, which are POSTed to Blink
     * @return a boolean indicating if the authentication was successful, and thus if the user will be sent an MFA code.
     * @throws IOException if any error occurs, including an incorrect password or email address.
     */
    private boolean loginStage1SendUsername(Map<String, String> params) throws IOException {
        String url = OAUTH_BASE_URL + OAUTH_SIGNIN_URI;

        final Request request = httpClient.newRequest(url).method("POST");
        request.agent(OAUTH_USER_AGENT);
        String data = getFormDataAsString(params);

        request.header("Accept", "*/*");
        request.header("Content-Type", "application/x-www-form-urlencoded");
        request.header("Origin", OAUTH_BASE_URL);
        request.header("Referer", url);
        request.content(new StringContentProvider(data));

        String csrf_token = params.get("csrf-token");
        if (csrf_token == null) {
            csrf_token = "MISSING";
        }
        logger.debug("Posting oauth signin credentials (user={}, pass=[REDACTED] csrf={}): {}", params.get("username"),
                partiallyRedactedString(csrf_token), url);
        ContentResponse contentResponse;
        try {
            contentResponse = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error performing Blink Sign In. Reason: {}", e.getMessage());
            throw new IOException(e);
        }
        if (contentResponse.getStatus() == 412) {
            logger.info("Blink Sign In credentials accepted, Blink is now sending the user an MFA code");
            logger.debug("MFA details returned by Blink: {}", contentResponse.getContentAsString());
            // FYI: returns JSON which includes the user_id. loginStage2SendMfaCode() also provides the user_id.
            return true;
        }
        if (contentResponse.getStatus() != 200) {
            throw new IOException("Blink OAUTH Sign In unsuccessful <Status " + contentResponse.getStatus() + ">");
        }
        // It is not clear whether this will ever occur--I believe Blink requires MFA on every account
        logger.debug("Blink Sign In credentials accepted (no additional MFA required). Login Successful!");
        logger.debug("Content body returned by Blink: {}", contentResponse.getContentAsString());
        return false;
    }

    /**
     * Authenticate to Blink using the MFA code, as stage 2 of OAUTH v2 authentication flow. After this
     * method returns a BlinkAccount DTO, we are considered fully authenticated, and can go ONLINE.
     *
     * @param config The BlinkAccount configuration, in particular, containing the MFA code.
     * @param hardwareId The unique client hardwareId value
     * @return BlinkAccount with account information and auth tokens.
     * @throws IOException upon any error talking to Blink
     */
    public BlinkAccount loginStage2WithMfa(@Nullable AccountConfiguration config, @Nullable String hardwareId)
            throws IOException {
        String userId = "";
        if (config == null || hardwareId == null || csrfToken == null) {
            throw new IllegalArgumentException("Cannot complete Blink OAUTH login without initial login values");
        }

        logger.debug("MFA code exists ({})! Initiating second half of login flow.... ", config.mfaCode);

        // Of course it's not null, I just checked this above, throwing an exception.
        if (csrfToken != null) {
            userId = loginStage2SendMfaCode(csrfToken, config.mfaCode);
        }

        String authCode = loginStage2GetAuthCode();
        BlinkAccount.Auth auth = loginStage2GetTokens(authCode, hardwareId);
        BlinkAccount blinkAccount = loginStage2GetAccountDetails(auth, hardwareId, userId);
        blinkAccount.lastTokenRefresh = Instant.now();
        return blinkAccount;
    }

    /**
     * This is Stage 2 Step 1 of OAUTH authentication flow: submit the MFA code which Blink sent to the user.
     *
     * @param csrfToken this was the CSRF token which was returned earlier in Stage 1.
     * @param mfaCode the MFA code which Blink sent to the user via SMS or email.
     * @return the userId (seems to be a number, but we return it in String form)
     * @throws IOException if anything went wrong, including an incorrect MFA code mistyped by the user.
     */
    private String loginStage2SendMfaCode(String csrfToken, String mfaCode) throws IOException {
        String url = OAUTH_BASE_URL + OAUTH_MFA_VERIFY_URI;
        String userId = "";

        final Request request = httpClient.newRequest(url).method("POST");
        request.agent(OAUTH_USER_AGENT);
        request.header("Accept", "*/*");
        request.header("Content-Type", "application/x-www-form-urlencoded");
        request.header("Origin", OAUTH_BASE_URL);
        request.header("Referer", OAUTH_BASE_URL + OAUTH_SIGNIN_URI);

        Map<String, String> params = new HashMap<>();
        params.put("2fa_code", mfaCode); // yes, blink uses an underscore here
        params.put("remember_me", "false"); // yes, blink uses an underscore here
        params.put("csrf-token", csrfToken); // yes, blink uses a dash here
        String data = getFormDataAsString(params);

        request.content(new StringContentProvider(data));

        logger.debug("Posting MFA Code (mfa={}, csrf={}): {}", mfaCode, partiallyRedactedString(csrfToken), url);
        ContentResponse contentResponse;
        try {
            contentResponse = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error sending Blink MFA Code to {}. Reason: {}", url, e.getMessage());
            logger.error("NOTE: Via testing, we determined that when the MFA Code is incorrect, Blink Servers respond"
                    + "with a non-compliant HTTP response (Missing WWW-Authenticate header), causing the openhab jetty"
                    + "client to throw an exception. Let's tell the user they likely MISTYPED their MFA Code.");
            throw new IOException("MFA Code " + mfaCode + " Failed. Check for errors, and try again.");
        }
        if (contentResponse.getStatus() == 201) {
            String json = contentResponse.getContentAsString();
            logger.debug("Blink MFA Code submitted, Blink responded with {}", json);
            JsonObject top = gson.fromJson(json, JsonObject.class);
            if (top == null) {
                logger.error("Blink MFA code submitted, but unable to parse response as JSON: {}", json);
                throw new IOException("Unable to parse Blink MFA response as JSON");
            }

            JsonElement statusElement = top.get("status");
            String status = (statusElement == null) ? "FAILED" : statusElement.getAsString();
            JsonElement userIdElement = top.get("user_id");
            userId = (userIdElement == null) ? "" : userIdElement.getAsString();

            if (status.equals("auth-completed")) {
                logger.info("Successfully submitted Blink MFA Code");
            } else {
                logger.info("Blink MFA Code was not accepted, check MFA code");
                throw new IOException("Blink says: Incorrect MFA code provided");
            }
        } else {
            logger.error("Unexpected status from MFA code submission. Status = {}, result = {}",
                    contentResponse.getStatus(), contentResponse.getContentAsString());
            throw new IOException("MFA code submission was not successful");
        }
        return userId;
    }

    /**
     * This is Stage 2 Step 2 of the OAUTH authentication flow: getting an "auth code" for Step 3.
     *
     * This seems sketchy but apparently we GET from the authorize API endpoint, and when it sends
     * back a 30x REDIRECT response, then we parse the redirect URL to pull out the "code" param.
     *
     * This originated from github.com/fronzbot/blinkpy in blinkpy/api.py oauth_get_authorization_code()
     *
     * @return the authorization code which we need to obtain a token.
     * @throws IOException
     */
    private String loginStage2GetAuthCode() throws IOException {
        String url = OAUTH_BASE_URL + OAUTH_INITIAL_AUTH_URI;
        final Request request = httpClient.newRequest(url).method("GET");
        request.agent(OAUTH_USER_AGENT);
        request.header("Accept", "*/*");
        request.header("Referer", OAUTH_BASE_URL + OAUTH_SIGNIN_URI);
        request.followRedirects(false);

        logger.debug("fetching auth code from authorize endpoint: {}", url);
        ContentResponse contentResponse;
        try {
            contentResponse = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error reading Authorization endpoint. Reason: {}", e.getMessage());
            throw new IOException(e);
        }
        int status = contentResponse.getStatus();
        if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
            String location = contentResponse.getHeaders().get("Location");
            logger.trace("authorization page says Redirect to {}", location);
            Pattern codePattern = Pattern.compile("\\?(|.*&)code=(.*?)(&|$)");
            Matcher matches = codePattern.matcher(location);
            if (matches.find()) {
                String codeValue = matches.group(2);
                logger.debug("authorization page: the code value is {}", partiallyRedactedString(codeValue));
                return codeValue;
            } else {
                logger.debug("ERROR: authorization page: the query parameter 'code' was not found in the Redirect url");
                throw new IOException("Authorization endpoint returned a REDIRECT location " + location
                        + ", but that URL is missing a 'code' value ");
            }
        }
        logger.debug("ERROR: authorization page was expected to return a Redirect url, but instead returned status "
                + status);
        throw new IOException("Authorization endpoint " + url
                + " expected to return a REDIRECT location, but instead returned " + status);
    }

    /**
     * This is Stage 2 Step 3 of OAUTH authentication flow: providing the auth code from Stage 2 Step 2, and receiving
     * an access token and a refresh token for use when the access token expires.
     *
     * @param authCode this is the auth code returned from Stage 2 Step 2
     * @param hardwareId this is our unique client hardware id, originally sent to Blink in Stage 1 Step 1.
     * @return the BlinkAccount.Auth information (tokens)
     * @throws IOException if anything went wrong
     */
    private BlinkAccount.Auth loginStage2GetTokens(String authCode, @Nullable String hardwareId) throws IOException {
        if (hardwareId == null) {
            throw new IllegalArgumentException("Cannot complete Blink OAUTH login without initial login values");
        }

        String url = OAUTH_BASE_URL + OAUTH_TOKEN_URI;
        final Request request = httpClient.newRequest(url).method("POST");

        request.agent(OAUTH_TOKEN_USER_AGENT);
        request.header("Accept", "*/*");
        request.header("Content-Type", "application/x-www-form-urlencoded");

        Map<String, String> params = new HashMap<>();
        params.put("app_brand", "blink");
        params.put("client_id", OAUTH_V2_CLIENT_ID);
        params.put("code", authCode);
        params.put("code_verifier", pkce.verifier);
        params.put("grant_type", "authorization_code");
        params.put("hardware_id", hardwareId);
        params.put("redirect_uri", OAUTH_REDIRECT_URI);
        params.put("scope", "client");
        String data = getFormDataAsString(params);

        request.content(new StringContentProvider(data));

        logger.debug("Posting Auth Code {}, hardwareId={}: {}", partiallyRedactedString(authCode), hardwareId, url);
        ContentResponse contentResponse;
        try {
            contentResponse = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            // it would seem that blink sends a 401, but doesn't include an WWW-Authentication header, which is
            // in violation of the OAUTH2 specification, and the jetty client in openhab is throwing an exception
            // due to blink's non-compliance. Anyway a 401 means unauthorized so something went wrong here.
            logger.error("Error retrieving oauth tokens from {}. Reason: {}", url, e.getMessage());
            throw new IOException(e);
        }
        if (contentResponse.getStatus() != 200) {
            logger.error("Error retrieving oauth tokens from {}. Status = {}", url, contentResponse.getStatus());
            throw new IOException("Unable to retrieve Blink Auth Tokens from Endpoint");
        }

        BlinkAccount.Auth auth = parseTokenInfo(contentResponse.getContentAsString());
        logger.info("Success! Blink Authorization Tokens Acquired");

        return auth;
    }

    /**
     * Private helper function to parse the token JSON retrieved in Stage 2 Step 3 (and also refreshToken() calls
     * when the access token expires and needs to be refreshed).
     *
     * @param json the JSON string returned by the Blink API
     * @return the BlinkAccount.Auth DTO object which reflects the JSON provided (and a calculated expiration time)
     * @throws IOException if a parsing error occurs (e.g. invalid JSON)
     */
    private BlinkAccount.Auth parseTokenInfo(String json) throws IOException {
        BlinkAccount.Auth auth = gson.fromJson(json, BlinkAccount.Auth.class);
        if (auth == null) {
            throw new IOException("Unable to parse JSON containing token info: " + json);
        }
        int REFERESH_BEFORE_EXPIRATION_SECS = 600; // refresh the token this many seconds before it actually expires.
        // Avoid race condition where we think the token is good, but then it expires in the middle of a set of calls.
        auth.tokenExpiresAt = Instant.now().plusSeconds(auth.expires_in - REFERESH_BEFORE_EXPIRATION_SECS);
        return auth;
    }

    /**
     * This call (only works once authenticated) returns some account information, e.g. account_id and tier.
     *
     * @param auth contains the authentication tokens
     * @param hardwareId our unique client hardware id which was provided to Blink in Stage 1 Step 1.
     * @param userId our user id associated with the Blink login credentials
     * @return a fully populated BlinkAccount DTO, specifically we fill in the account.tier and account.account_id
     *         fields
     * @throws IOException in the event that the call fails, e.g. we are not yet authenticated, or the network is down
     */
    private BlinkAccount loginStage2GetAccountDetails(BlinkAccount.Auth auth, String hardwareId, String userId)
            throws IOException {
        BlinkAccount acct = new BlinkAccount();
        String url = TIER_URL;

        acct.auth = auth;
        acct.account = new BlinkAccount.Account();
        acct.account.hardware_id = hardwareId;
        acct.account.user_id = userId;
        final Request request = httpClient.newRequest(url).method("GET");
        request.agent(OAUTH_USER_AGENT);
        request.header("Authorization", "Bearer " + acct.auth.access_token);
        request.header("Content-Type", "application/x-www-form-urlencoded");

        logger.trace("fetching tier info: {}", url);
        ContentResponse contentResponse;
        try {
            contentResponse = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error fetching Blink Account Tier Info page. Reason: {}", e.getMessage());
            throw new IOException(e);
        }
        if (contentResponse.getStatus() != 200) {
            throw new IOException(
                    "Error fetching Blink Account Tier Info page: <Status " + contentResponse.getStatus() + ">");
        }
        String json = contentResponse.getContentAsString();
        BlinkAccount.Account partialAcct = gson.fromJson(json, BlinkAccount.Account.class);
        if (partialAcct != null) {
            acct.account.tier = partialAcct.tier;
            acct.account.account_id = partialAcct.account_id;
        }
        return acct;
    }

    /**
     * this method asks the Blink API to refresh an existing authentication token, giving us a new access_token, a
     * new refresh_token, with a new expiration time.
     *
     * @param account the current account information, including the existing tokens in account.auth.
     * @return an updated BlinkAccount DTO, with new tokens in the BlinkAccount.auth object.
     * @throws IOException in the event that the token cannot be refreshed, e.g. network failure or bad token.
     */
    public BlinkAccount refreshToken(@Nullable BlinkAccount account) throws IOException {
        if (account == null || account.auth == null || account.account == null) {
            throw new IllegalArgumentException("This Blink Account is not authenticated yet");
        }

        String url = OAUTH_BASE_URL + OAUTH_TOKEN_URI;
        final Request request = httpClient.newRequest(url).method("POST");

        request.agent(OAUTH_USER_AGENT);
        request.header("Accept", "*/*");
        request.header("Content-Type", "application/x-www-form-urlencoded");
        request.header("hardware_id", account.account.hardware_id);

        Map<String, String> params = new HashMap<>();
        params.put("client_id", OAUTH_V2_CLIENT_ID);
        params.put("scope", "client");
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", account.auth.refresh_token);

        String data = getFormDataAsString(params);

        request.content(new StringContentProvider(data));

        logger.debug("Posting refresh_token request (hardwareId={}): {}", account.account.hardware_id, url);
        ContentResponse contentResponse;
        try {
            contentResponse = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error refreshing oauth token from {}. Reason: {}", url, e.getMessage());
            throw new IOException(e);
        }
        if (contentResponse.getStatus() != 200) {
            logger.error("Error retrieving oauth tokens from {}. Status = {}", url, contentResponse.getStatus());
            throw new IOException("Unable to refresh Blink OAuth Token, status code=" + contentResponse.getStatus());
        }
        BlinkAccount refreshedAccount = new BlinkAccount(account);
        refreshedAccount.auth = parseTokenInfo(contentResponse.getContentAsString());
        refreshedAccount.lastTokenRefresh = Instant.now();
        logger.info("Success! Blink Authorization Token Refreshed");
        return refreshedAccount;
    }

    /**
     * This method will make a brief API call to Blink to verify that the authentication tokens are valid.
     *
     * @param account the current BlinkAccount to be verified
     * @return true if the API call succeeds, otherwise false for any failure (e.g. account is null, network is down,
     *         or authentication token is expired)
     */
    public boolean verifyAuthentication(@Nullable BlinkAccount account) {
        BlinkAccount tmp;
        logger.debug("Attempting to verify our Blink connection using account: {}", account);
        if (account == null) {
            logger.debug("This Blink Account is not authenticated yet");
            return false;
        }
        try {
            tmp = loginStage2GetAccountDetails(account.auth, account.account.hardware_id, account.account.user_id);
        } catch (IOException e) {
            logger.warn("The BlinkAccount is apparently not authenticated properly (or fails to connect)");
            return false;
        }
        logger.debug("Successful connection to Blink, confirmed accountId {}", tmp.account.account_id);
        return true;
    }

    /**
     * Get the list of devices known to this account (e.g. cameras and networks)
     *
     * @param account the account information, including auth tokens
     * @return the "BlinkHomescreen" is a collection of all of the Blink devices
     * @throws IOException if the call fails due to network errors, or an invalid authentication token
     */
    public BlinkHomescreen getDevices(@Nullable BlinkAccount account) throws IOException {
        if (account == null || account.account == null) {
            throw new IllegalArgumentException("This Blink Account is not authenticated yet");
        }
        String uri = "/api/v3/accounts/" + account.account.account_id + "/homescreen";
        return apiRequest(account.account.tier, uri, HttpMethod.GET, account.auth.access_token, null,
                BlinkHomescreen.class);
    }

    /**
     * Get the list of events which have happened since the last time, typically new recordings from motion detections.
     *
     * @param account the account information, including auth tokens
     * @param since we are asking for events since this time (typically the timestamp of the previous call)
     * @return the BlinkEvents which have occurred
     * @throws IOException in the event the call fails, e.g. network error or invalid tokens
     */
    public BlinkEvents getEvents(@Nullable BlinkAccount account, OffsetDateTime since) throws IOException {
        if (account == null || account.account == null) {
            throw new IllegalArgumentException("This Blink Account is not authenticated yet");
        }
        String uri = "/api/v1/accounts/" + account.account.account_id + "/media/changed";
        Map<String, String> params = Map.of("since", since.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        return apiRequest(account.account.tier, uri, HttpMethod.GET, account.auth.access_token, params,
                BlinkEvents.class);
    }

    /**
     * Some strings should be partially redacted, so during debugging you can see a few characters to make sure the
     * value is what you expect, but not enough to expose any secrets. Typically this is for authentication token
     * values. Please don't use this for passwords--those should be fully redacted since they are relatively short.
     *
     * @param longStr a long string (e.g. token value) which we don't want to print in its entirety.
     * @return a partially redacted string such as: "1234____REDACTED____XYWZ", thus hiding the majority of longStr.
     */
    private String partiallyRedactedString(String longStr) {
        if (longStr.length() < 8) {
            return longStr;
        }
        String shortStr = longStr.substring(0, 4) + "____REDACTED____" + longStr.substring(longStr.length() - 4);
        return shortStr;
    }

    /**
     * A method to take a Map<> of key:value pairs, and convert them to a URL-Encoded string for sending to an API
     * endpoint
     *
     * @param formData a map of Key: Value pairs
     * @return a URL-Encoded string suitable for appending to an HTTP GET URL, such as: "key1=val1&key2=val2"
     */
    private static String getFormDataAsString(Map<String, String> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (formBodyBuilder.length() > 0) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }
        return formBodyBuilder.toString();
    }

    /** OAUTH v2 PKCE functionality **/

    public class PkcePair {
        final String verifier;
        final String challenge;

        public PkcePair() {
            String candidate;
            byte[] randomBytes = new byte[32];
            new SecureRandom().nextBytes(randomBytes);
            verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(verifier.getBytes("UTF-8"));
                candidate = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            } catch (Exception e) {
                logger.error("Blink login failure: Unable to set up OAUTH challenge due to error {}", e);
                candidate = "challenge declined";
            }
            challenge = candidate; // compiler incorrectly claims that try/catch may assign challenge twice
        }
    }
}
