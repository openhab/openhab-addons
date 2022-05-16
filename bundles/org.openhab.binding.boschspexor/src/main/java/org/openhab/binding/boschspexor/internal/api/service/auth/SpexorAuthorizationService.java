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
package org.openhab.binding.boschspexor.internal.api.service.auth;

import static org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.boschspexor.internal.api.service.BoschSpexorBridgeConfig;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.common.contenttype.ContentType;
import com.nimbusds.oauth2.sdk.AbstractOptionallyIdentifiedRequest;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.device.DeviceAuthorizationRequest;
import com.nimbusds.oauth2.sdk.device.DeviceAuthorizationResponse;
import com.nimbusds.oauth2.sdk.device.DeviceCode;
import com.nimbusds.oauth2.sdk.device.DeviceCodeGrant;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * {@link SpexorAuthorizationService} is handling the authorization of the openHAB service.
 * Therefore the state is tracked with the {@link SpexorAuthGrantState}s.
 * The Service is polling against the OAuth2.0 device flow authorization service of Bosch spexor.
 *
 * @author Marc Fischer - Initial contribution *
 */
@NonNullByDefault
public class SpexorAuthorizationService {

    private final Logger logger = LoggerFactory.getLogger(SpexorAuthorizationService.class);

    /**
     * State machine of the authorization process
     *
     * @author Marc Fischer - Initial contribution
     *
     */
    public enum SpexorAuthGrantState {
        /**
         * openHAB bosch spexor bridge thing not initalized
         */
        BRIDGE_NOT_CONFIGURED,
        /**
         * openHAB authorization against Bosch spexor backend is not known
         */
        UNINITIALIZED,
        /**
         * openHAB authorization failed and needs another request
         */
        CODE_REQUEST_FAILED,
        /**
         * openHAB is authorized and could be used
         */
        AUTHORIZED,
        /**
         * openHAB has requested an unique identifier by the Bosch spexor backend to get access
         */
        CODE_REQUESTED,
        /**
         * openHAB is awaiting user acceptance - user needs to enter code or scan QR-Code
         */
        AWAITING_USER_ACCEPTANCE
    }

    private final HttpClient httpClient;
    private Optional<BoschSpexorBridgeConfig> bridgeConfig = Optional.empty();
    private Storage<String> storage;
    private AuthProcessingStatus processingStatus = new AuthProcessingStatus();
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private Optional<OAuthToken> token;
    private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private SpexorAuthorizationProcessListener authListener;

    public SpexorAuthorizationService(HttpClient httpClient, final @Reference StorageService storageService,
            SpexorAuthorizationProcessListener authListener) {
        this.httpClient = httpClient;
        this.authListener = authListener;
        this.storage = storageService.getStorage(BINDING_ID, String.class.getClassLoader());
        this.token = OAuthToken.load(storage);
    }

    public Optional<Request> newRequest(String... parameters) {
        try {
            authorize();
            if (SpexorAuthGrantState.AUTHORIZED.equals(processingStatus.getState()) && bridgeConfig.isPresent()
                    && token.isPresent()) {
                Request request = httpClient
                        .newRequest(String.join("", bridgeConfig.get().getHost(), String.join("", parameters)));

                request.header("Authorization", String.join(" ", "Bearer", token.get().getAccessToken().get()));
                return Optional.of(request);
            }
        } catch (Exception e) {
            logger.error("failed to load access token", e);
            processingStatus.error("Authorization information could not be loaded. Pleaes check logs.", authListener);
        }

        return Optional.empty();
    }

    public boolean isRegistered() {
        boolean result = token.isPresent();
        if (result) {
            try {
                result = token.isPresent() && token.get().getRefreshToken().isPresent()
                        && isNotEmpty(token.get().getRefreshToken().get());
            } catch (Exception e) {
                logger.error("failed to load access token", e);
                result = false;
            }
        }
        return result;
    }

    public boolean isRequestPending() {
        String deviceCode = getConstantBinding(DEVICE_CODE);
        boolean result = !isEmpty(storage.get(deviceCode));
        if (result) {
            // device was registered but not completed by user flow - timeout needs to be checked
            LocalDateTime deviceCodeRequestTime = getDeviceCodeRequestTime();
            String requestLifetime = storage.get(getConstantBinding(DEVICE_CODE_REQUEST_TIME_LIFETIME));
            long lifeTime = 10L;

            if (requestLifetime != null && isNumeric(requestLifetime)) {
                lifeTime = Long.valueOf(requestLifetime);
            }
            if (!isExpired(deviceCodeRequestTime, lifeTime)) {
                loadAccessToken();
            } else {
                result = false;
            }
        }
        return result;
    }

    /**
     * requests the state of the device (openHAB) - dependent on the state the authorization is polling for access token
     * or is already synced
     */
    public void authorize() {
        if (bridgeConfig.isEmpty()) {
            throw new IllegalStateException("configuration was not loaded");
        }

        String deviceCode = getConstantBinding(DEVICE_CODE);
        if (token.isEmpty()) {
            if (isEmpty(storage.get(deviceCode))) {
                registerAsDevice();
            } else {
                // device was registered but not completed by user flow - timeout needs to be checked
                LocalDateTime deviceCodeRequestTime = getDeviceCodeRequestTime();
                String requestLifetime = storage.get(getConstantBinding(DEVICE_CODE_REQUEST_TIME_LIFETIME));
                long lifeTime = 10L;

                if (requestLifetime != null && isNumeric(requestLifetime)) {
                    lifeTime = Long.valueOf(requestLifetime);
                }
                if (isExpired(deviceCodeRequestTime, lifeTime)) {
                    registerAsDevice();
                } else {
                    loadAccessToken();
                }
            }
        } else if (token.get().isAccessTokenExpired()) {
            refreshAccessToken();
        } else {
            // token is fine and doesn't need to be updated
            processingStatus.valid(authListener);
        }
    }

    public void reset() {
        storage.remove(getConstantBinding(DEVICE_CODE));
        storage.remove(getConstantBinding(DEVICE_CODE_REQUEST_TIME));
        storage.remove(getConstantBinding(DEVICE_CODE_REQUEST_TIME_LIFETIME));
        storage.remove(getConstantBinding(DEVICE_CODE_REQUEST_INTERVAL));
        if (token.isPresent()) {
            token.get().reset(storage);
            token = OAuthToken.load(storage);
        }
        processingStatus.uninitialized(authListener);
    }

    private void registerAsDevice() {
        try {
            DeviceAuthorizationRequest request = new DeviceAuthorizationRequest(
                    new URI(bridgeConfig.get().buildAuthorizationUrl()), new ClientID(bridgeConfig.get().getClientId()),
                    new Scope(bridgeConfig.get().getScope()));
            JSONObject response = requestSpexorBackend(request, "authorization");
            if (response != null) {
                DeviceAuthorizationResponse resp = DeviceAuthorizationResponse.parse(response);
                if (resp.indicatesSuccess()) {
                    storage.put(getConstantBinding(DEVICE_CODE), resp.toSuccessResponse().getDeviceCode().getValue());
                    storage.put(getConstantBinding(USER_CODE), resp.toSuccessResponse().getUserCode().getValue());
                    storage.put(getConstantBinding(DEVICE_CODE_REQUEST_TIME), LocalDateTime.now().toString());
                    storage.put(getConstantBinding(DEVICE_CODE_REQUEST_TIME_LIFETIME),
                            String.valueOf(resp.toSuccessResponse().getLifetime()));
                    storage.put(getConstantBinding(DEVICE_CODE_REQUEST_INTERVAL),
                            String.valueOf(resp.toSuccessResponse().getInterval()));
                    processingStatus.awaitingUserAcceptance(resp.toSuccessResponse().getDeviceCode().getValue(),
                            resp.toSuccessResponse().getUserCode().getValue(), authListener);
                    pollStatus();
                } else {
                    logger.error("error reported with code '{}'", resp.toErrorResponse().getErrorObject().getCode());
                    if (OAUTH_FLOW_INVALID_GRANT_CODE
                            .equalsIgnoreCase(resp.toErrorResponse().getErrorObject().getCode())) {
                        storage.remove(getConstantBinding(DEVICE_CODE));
                        storage.remove(getConstantBinding(USER_CODE));
                        storage.remove(getConstantBinding(DEVICE_CODE_REQUEST_TIME));
                        storage.remove(getConstantBinding(DEVICE_CODE_REQUEST_TIME_LIFETIME));
                        storage.remove(getConstantBinding(DEVICE_CODE_REQUEST_INTERVAL));
                        if (token.isPresent()) {
                            token.get().reset(storage);
                        }
                        processingStatus.uninitialized(authListener);
                    }
                    processingStatus.error(MessageFormat.format("Server responded with {0} \"{1}\"",
                            resp.toErrorResponse().getErrorObject().getCode(),
                            resp.toErrorResponse().getErrorObject().getDescription()), authListener);
                }
            }
        } catch (ParseException | URISyntaxException e) {
            String message = ("response did not comply to excected response:" + e.getLocalizedMessage());
            logger.error(message, e);
            processingStatus.error(message, authListener);
        }
    }

    private void pollStatus() {
        long interval = 5; // by default 5 seconds
        String requestInterval = storage.get(getConstantBinding(DEVICE_CODE_REQUEST_INTERVAL));
        if (requestInterval != null && isNumeric(requestInterval)) {
            interval = Long.valueOf(requestInterval);
        }
        final LocalDateTime requestedPolling = LocalDateTime.now();
        executor.schedule(() -> {
            SpexorAuthorizationService.this.authorize();
            logger.info("scheduled on {} and ended on {} delta {} ms", requestedPolling, LocalDateTime.now(),
                    ChronoUnit.MILLIS.between(requestedPolling, LocalDateTime.now()));
        }, interval, TimeUnit.SECONDS);
    }

    private void loadAccessToken() {
        String deviceCode = storage.get(getConstantBinding(DEVICE_CODE));
        String userCode = storage.get(getConstantBinding(USER_CODE));
        if (processingStatus.getState() == SpexorAuthGrantState.AWAITING_USER_ACCEPTANCE) {
            AuthorizationGrant authorizationGrant = new DeviceCodeGrant(new DeviceCode(deviceCode));
            processingStatus.setDeviceCode(deviceCode);
            String tokenUrl = bridgeConfig.get().buildTokenUrl();
            requestAuthorizationGrant(authorizationGrant, tokenUrl, "token");
        } else if (processingStatus.getState() == SpexorAuthGrantState.UNINITIALIZED && deviceCode != null
                && userCode != null) {
            processingStatus.awaitingUserAcceptance(deviceCode, userCode, authListener);
        } else {
            processingStatus.expiredDeviceToken(authListener);
            processingStatus.setDeviceCode(deviceCode);
        }
    }

    public Optional<OAuthToken> getToken() {
        return token;
    }

    private void requestAuthorizationGrant(AuthorizationGrant authorizationGrant, String tokenUrl, String purpose) {
        try {
            TokenRequest request = new TokenRequest(new URI(tokenUrl), new ClientID(bridgeConfig.get().getClientId()),
                    authorizationGrant, new Scope(bridgeConfig.get().getScope()));
            JSONObject response = requestSpexorBackend(request, purpose);
            if (response != null) {
                TokenResponse resp = TokenResponse.parse(response);
                if (resp.indicatesSuccess()) {
                    AccessTokenResponse successResponse = resp.toSuccessResponse();
                    OAuthToken token = OAuthToken.of(successResponse.getTokens());
                    logger.debug("new token is {}", token);
                    token.save(this.storage);
                    logger.debug("token was stored");
                    this.token = Optional.of(token);
                    processingStatus.valid(authListener);
                } else {
                    // handle error states (not all)
                    String error = resp.toErrorResponse().getErrorObject().getCode();
                    logger.error("error reported with code '{}'", resp.toErrorResponse().getErrorObject().getCode());
                    if (OAUTH_FLOW_AUTHORIZATION_PENDING.equalsIgnoreCase(error)) {
                        // authorization_pending
                        // -- The user hasn't finished authenticating, but hasn't canceled the flow. Repeat the request
                        // -- after at least interval seconds.
                        //
                        pollStatus();
                    } else if (OAUTH_FLOW_AUTHORIZATION_DECLINED.equalsIgnoreCase(error)
                            || OAUTH_FLOW_EXPIRED_TOKEN.equalsIgnoreCase(error)) {
                        logger.debug("authorization request returned error {}", error);
                        // authorization_declined
                        // -- The end user denied the authorization request. Stop polling, and revert to an
                        // -- unauthenticated state.
                        // expired_token
                        // -- At least expires_in seconds have passed, and authentication is no longer possible with
                        // -- this device_code. Stop polling and revert to an unauthenticated state
                        processingStatus.expiredDeviceToken(authListener);
                    } else if (OAUTH_FLOW_BAD_VERIFICATION_CODE.equalsIgnoreCase(error)) {
                        // bad_verification_code
                        // -- The device_code sent to the /token endpoint wasn't recognized. Verify that the client
                        // -- is sending the correct device_code in the request.
                        //
                        processingStatus.error(error, authListener);
                    } else if (OAUTH_FLOW_INVALID_GRANT_CODE.equalsIgnoreCase(error)) {
                        // invalid_grant
                        // -- The request is not permitted so it needs a reset
                        storage.remove(getConstantBinding(DEVICE_CODE));
                        storage.remove(getConstantBinding(USER_CODE));
                        storage.remove(getConstantBinding(DEVICE_CODE_REQUEST_TIME));
                        storage.remove(getConstantBinding(DEVICE_CODE_REQUEST_TIME_LIFETIME));
                        storage.remove(getConstantBinding(DEVICE_CODE_REQUEST_INTERVAL));
                        if (token.isPresent()) {
                            token.get().reset(storage);
                        }
                        processingStatus.uninitialized(authListener);
                    } else {
                        processingStatus.error("Unknown state '" + error + "'", authListener);
                    }
                }
            }
        } catch (Exception e) {
            String message = MessageFormat.format("invalid message {0}", e.getLocalizedMessage());
            logger.error(message, e);
            processingStatus.error(message, authListener);
            return;
        }
    }

    private void refreshAccessToken() {

        if (processingStatus.getState() == SpexorAuthGrantState.AUTHORIZED
                || processingStatus.getState() == SpexorAuthGrantState.UNINITIALIZED) {
            if (token.isPresent() && token.get().getRefreshToken().isPresent()) {
                AuthorizationGrant authorizationGrant = new RefreshTokenGrant(
                        new RefreshToken(token.get().getRefreshToken().get()));
                String refreshUrl = bridgeConfig.get().buildRefreshUrl();
                requestAuthorizationGrant(authorizationGrant, refreshUrl, "refresh");
            } else {
                processingStatus.error("could not access refresh token - its empty", authListener);
            }
        } else {
            processingStatus.expiredDeviceToken(authListener);
        }
    }

    private boolean isExpired(LocalDateTime time, long timeoutInSeconds) {
        return time.plusSeconds(timeoutInSeconds).isBefore(LocalDateTime.now());
    }

    private LocalDateTime getDeviceCodeRequestTime() {
        LocalDateTime result = LocalDateTime.MIN;
        String key = getConstantBinding(DEVICE_CODE_REQUEST_TIME);
        String value = storage.get(key);
        if (isNotEmpty(value)) {
            try {
                result = LocalDateTime.parse(value.subSequence(0, value.length() - 1));
            } catch (DateTimeException e) {
                logger.error("failed to parse the DeviceCode Request time stamp", e);
            }
        }
        return result;
    }

    @Nullable
    private JSONObject requestSpexorBackend(AbstractOptionallyIdentifiedRequest req, String contextName) {
        JSONObject result = null;
        HTTPRequest httpRequest = req.toHTTPRequest();
        Request request = httpClient.newRequest((httpRequest.getURI()));
        request.method(httpRequest.getMethod().name());
        if (httpRequest.getAccept() != null) {
            request.accept(httpRequest.getAccept());
        }
        if (httpRequest.getAuthorization() != null && !"".contentEquals(httpRequest.getAuthorization())) {
            request.header("Authorization", httpRequest.getAuthorization());
        }
        httpRequest.getHeaderMap().keySet().stream().forEach((c) -> request.header(c, httpRequest.getHeaderValue(c)));
        if (ContentType.APPLICATION_URLENCODED.getType().equals(request.getHeaders().get("Content-Type"))) {
            Fields formFields = new Fields();
            request.content(new FormContentProvider(formFields));
            httpRequest.getQueryParameters().keySet().stream()
                    .forEach((p) -> formFields.add(p, String.valueOf(httpRequest.getQueryParameters().get(p))));
        } else {
            request.content(new StringContentProvider(httpRequest.getQuery()));
        }

        ContentResponse response;
        try {
            response = request.send();
            logger.debug("send message {} headers[{}] with result {} content[{}]", request, request.getHeaders(),
                    response, response.getContentAsString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            String message = ("code couldn't be requested:" + e.getLocalizedMessage());
            logger.error(message);
            processingStatus.error(message, authListener);
            return result;
        }
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        try {
            Object parsedResult = parser.parse(response.getContent());
            if (parsedResult instanceof JSONObject) {
                result = (JSONObject) parsedResult;
                logger.debug("parsed json : {}", result);
            } else {
                String message = ("response did not comply to excected resonse. Excepted: JSON received:"
                        + (parsedResult == null ? "null" : parsedResult.getClass()));
                logger.error(message);
                processingStatus.error(message, authListener);
            }
        } catch (net.minidev.json.parser.ParseException e) {
            String message = ("response did not comply to excected response:" + e.getLocalizedMessage());
            logger.error(message, e);
            processingStatus.error(message, authListener);
        }
        return result;
    }

    public BoschSpexorBridgeConfig getConfig() {
        return bridgeConfig.get();
    }

    public void setConfig(BoschSpexorBridgeConfig bridgeConfig) {
        logger.info("spexor config was assigned");
        this.bridgeConfig = Optional.of(bridgeConfig);
    }

    public AuthProcessingStatus getStatus() {
        return processingStatus;
    }

    public ScheduledThreadPoolExecutor getThreadPoolExecutor() {
        return this.executor;
    }

    public boolean isNumeric(@Nullable String candidate) {
        if (candidate == null) {
            return false;
        }
        return pattern.matcher(candidate).matches();
    }

    public boolean isEmpty(@Nullable String candidate) {
        if (candidate == null) {
            return true;
        }
        return "".equalsIgnoreCase(candidate.trim());
    }

    public boolean isNotEmpty(@Nullable String candidate) {
        if (candidate == null) {
            return false;
        }
        return !"".equalsIgnoreCase(candidate.trim());
    }
}
