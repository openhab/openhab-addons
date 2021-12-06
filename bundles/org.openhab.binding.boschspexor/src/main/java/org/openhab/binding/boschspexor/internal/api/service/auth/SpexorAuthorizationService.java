package org.openhab.binding.boschspexor.internal.api.service.auth;

import static org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.boschspexor.internal.api.service.BoschSpexorBridgeConfig;
import org.openhab.core.auth.oauth2client.internal.OAuthStoreHandler;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.nimbusds.oauth2.sdk.token.Tokens;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * {@link SpexorAuthorizationService} is handling the authorization of the openHAB service.
 * Therefore the state is tracked with the {@link SpexorAuthGrantState}s.
 * The Service is polling against the OAuth2.0 device flow authorization service of Bosch spexor.
 *
 * @author Marc
 *
 */
public class SpexorAuthorizationService {

    private final Logger logger = LoggerFactory.getLogger(SpexorAuthorizationService.class);

    /**
     * State machine of the authorization process
     *
     * @author Marc
     *
     */
    public enum SpexorAuthGrantState {
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

    private final OAuthStoreHandler oAuthStoreHandler;
    private final HttpClient httpClient;
    private BoschSpexorBridgeConfig bridgeConfig = null;
    private Storage<String> storage;
    private AuthProcessingStatus processingStatus = new AuthProcessingStatus();
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private String handle = null;

    public SpexorAuthorizationService(@NonNull OAuthStoreHandler oAuthStoreHandler, @NonNull HttpClient httpClient,
            final @Reference StorageService storageService) {
        this.httpClient = httpClient;
        this.oAuthStoreHandler = oAuthStoreHandler;
        this.storage = storageService.getStorage(BINDING_ID, String.class.getClassLoader());
        processingStatus.uninitialized();
    }

    public Request newRequest(String... parameters) {
        try {
            authorize();
            if (SpexorAuthGrantState.AUTHORIZED.equals(processingStatus.getState())) {
                String handle = getConstantBinding(bridgeConfig.getScope());
                Request request = httpClient
                        .newRequest(String.join("", bridgeConfig.getHost(), String.join("", parameters)));
                org.openhab.core.auth.client.oauth2.AccessTokenResponse token = oAuthStoreHandler
                        .loadAccessTokenResponse(handle);
                request.header("Authorization", String.join(" ", "Bearer", token.getAccessToken()));
                return request;
            }
        } catch (GeneralSecurityException e) {
            logger.error("failed to load access token", e);
            processingStatus.error("Authorization information could not be loaded. Pleaes check logs.");
        }

        return null;
    }

    public boolean isRegistered() {
        boolean result = bridgeConfig != null;
        if (result) {
            String handle = getConstantBinding(bridgeConfig.getScope());
            try {
                org.openhab.core.auth.client.oauth2.AccessTokenResponse tokenResp = oAuthStoreHandler
                        .loadAccessTokenResponse(handle);
                result = StringUtils.isNotEmpty(tokenResp.getRefreshToken());
            } catch (Exception e) {
                logger.error("failed to load access token", e);
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
        if (bridgeConfig == null) {
            throw new IllegalStateException("configuration was not loaded");
        }

        String deviceCode = getConstantBinding(DEVICE_CODE);
        try {
            org.openhab.core.auth.client.oauth2.AccessTokenResponse tokenResp = oAuthStoreHandler
                    .loadAccessTokenResponse(handle);
            if (tokenResp == null) {
                if (StringUtils.isEmpty(storage.get(deviceCode))) {
                    registerAsDevice();
                } else {
                    // device was registered but not completed by user flow - timeout needs to be checked
                    LocalDateTime deviceCodeRequestTime = getDeviceCodeRequestTime();
                    String requestLifetime = storage.get(getConstantBinding(DEVICE_CODE_REQUEST_TIME_LIFETIME));
                    long lifeTime = 10L;
                    if (requestLifetime != null && StringUtils.isNumeric(requestLifetime)) {
                        lifeTime = Long.valueOf(requestLifetime);
                    }
                    if (isExpired(deviceCodeRequestTime, lifeTime)) {
                        registerAsDevice();
                    } else {
                        loadAccessToken();
                    }
                }
            } else if (tokenResp.isExpired(LocalDateTime.now(), OAUTH_EXPIRE_BUFFER)) {
                refreshAccessToken();
            } else {
                // token is fine and doesn't need to be updated
                processingStatus.valid();
            }
        } catch (GeneralSecurityException e) {
            logger.error("failed to load access token", e);
            processingStatus.error("Authorization information could not be loaded. Pleaes check logs.");
        }
    }

    private void registerAsDevice() {
        try {
            DeviceAuthorizationRequest request = new DeviceAuthorizationRequest(
                    new URI(bridgeConfig.buildAuthorizationUrl()), new ClientID(bridgeConfig.getClientId()),
                    new Scope(bridgeConfig.getScope()));
            JSONObject response = requestSpexorBackend(request, "authorization");
            if (response != null) {
                DeviceAuthorizationResponse resp = DeviceAuthorizationResponse.parse(response);
                if (resp.indicatesSuccess()) {
                    storage.put(getConstantBinding(DEVICE_CODE), resp.toSuccessResponse().getDeviceCode().getValue());
                    storage.put(getConstantBinding(DEVICE_CODE_REQUEST_TIME), LocalDateTime.now().toString());
                    storage.put(getConstantBinding(DEVICE_CODE_REQUEST_TIME_LIFETIME),
                            String.valueOf(resp.toSuccessResponse().getLifetime()));
                    storage.put(getConstantBinding(DEVICE_CODE_REQUEST_INTERVAL),
                            String.valueOf(resp.toSuccessResponse().getInterval()));
                    processingStatus.awaitingUserAcceptance(resp.toSuccessResponse().getUserCode().getValue());
                    pollStatus();
                } else {
                    logger.error("error reported with code '{}'", resp.toErrorResponse().getErrorObject().getCode());
                    processingStatus.error(MessageFormat.format("Server responded with {0} \"{1}\"",
                            resp.toErrorResponse().getErrorObject().getCode(),
                            resp.toErrorResponse().getErrorObject().getDescription()));
                }
            }
        } catch (ParseException | URISyntaxException e) {
            String message = ("response did not comply to excected response:" + e.getLocalizedMessage());
            logger.error(message, e);
            processingStatus.error(message);
        }
    }

    private void pollStatus() {
        long interval = 5; // by default 5 seconds
        String requestInterval = storage.get(getConstantBinding(DEVICE_CODE_REQUEST_INTERVAL));
        if (requestInterval != null && StringUtils.isNumeric(requestInterval)) {
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
        if (processingStatus.getState() == SpexorAuthGrantState.AWAITING_USER_ACCEPTANCE) {
            AuthorizationGrant authorizationGrant = new DeviceCodeGrant(
                    new DeviceCode(storage.get(getConstantBinding(DEVICE_CODE))));
            String tokenUrl = bridgeConfig.buildTokenUrl();
            requestAuthorizationGrant(authorizationGrant, tokenUrl, "token");
        } else {
            processingStatus.expiredDeviceToken();
        }
    }

    private void requestAuthorizationGrant(AuthorizationGrant authorizationGrant, String tokenUrl, String purpose) {
        try {
            TokenRequest request = new TokenRequest(new URI(tokenUrl), new ClientID(bridgeConfig.getClientId()),
                    authorizationGrant, new Scope(bridgeConfig.getScope()));
            JSONObject response = requestSpexorBackend(request, purpose);
            if (response != null) {
                TokenResponse resp = TokenResponse.parse(response);
                if (resp.indicatesSuccess()) {
                    AccessTokenResponse successResponse = resp.toSuccessResponse();
                    String handle = getConstantBinding(bridgeConfig.getScope());
                    org.openhab.core.auth.client.oauth2.AccessTokenResponse accessTokenResponse = new org.openhab.core.auth.client.oauth2.AccessTokenResponse();
                    Tokens tokens = successResponse.getTokens();
                    accessTokenResponse.setAccessToken(tokens.getAccessToken().getValue());
                    accessTokenResponse.setCreatedOn(LocalDateTime.now());
                    accessTokenResponse.setRefreshToken(tokens.getRefreshToken().getValue());
                    accessTokenResponse.setExpiresIn(tokens.getAccessToken().getLifetime());
                    accessTokenResponse.setScope(String.valueOf(tokens.getAccessToken().getScope()));
                    accessTokenResponse.setTokenType(tokens.getAccessToken().getType().getValue());
                    oAuthStoreHandler.saveAccessTokenResponse(handle, accessTokenResponse);
                    processingStatus.valid();
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
                        // authorization_declined
                        // -- The end user denied the authorization request. Stop polling, and revert to an
                        // -- unauthenticated state.
                        // expired_token
                        // -- At least expires_in seconds have passed, and authentication is no longer possible with
                        // -- this device_code. Stop polling and revert to an unauthenticated state
                        processingStatus.expiredDeviceToken();
                    } else if (OAUTH_FLOW_BAD_VERIFICATION_CODE.equalsIgnoreCase(error)) {
                        // bad_verification_code
                        // -- The device_code sent to the /token endpoint wasn't recognized. Verify that the client
                        // is
                        // -- sending the correct device_code in the request.
                        //
                        processingStatus.error(error);
                    } else {
                        processingStatus.error("Unknown state '" + error + "'");
                    }
                }
            }
        } catch (URISyntaxException | ParseException e) {
            String message = MessageFormat.format("invalid message {0}", e.getLocalizedMessage());
            logger.error(message, e);
            processingStatus.error(message);
            return;
        }
    }

    private void refreshAccessToken() {

        if (processingStatus.getState() == SpexorAuthGrantState.AUTHORIZED) {
            String handle = getConstantBinding(bridgeConfig.getScope());
            try {
                org.openhab.core.auth.client.oauth2.AccessTokenResponse tokenResp = oAuthStoreHandler
                        .loadAccessTokenResponse(handle);
                AuthorizationGrant authorizationGrant = new RefreshTokenGrant(
                        new RefreshToken(tokenResp.getRefreshToken()));
                String refreshUrl = bridgeConfig.buildRefreshUrl();
                requestAuthorizationGrant(authorizationGrant, refreshUrl, "refresh");
            } catch (GeneralSecurityException e) {
                logger.error("could not load stored refresh token", e);
                processingStatus.error("could not access refresh token - '" + e.getLocalizedMessage() + "'");
            }
        } else {
            processingStatus.expiredDeviceToken();
        }
    }

    private boolean isExpired(LocalDateTime time, long timeoutInSeconds) {
        return time.plusSeconds(timeoutInSeconds).isBefore(LocalDateTime.now());
    }

    private LocalDateTime getDeviceCodeRequestTime() {
        LocalDateTime result = LocalDateTime.MIN;
        String key = getConstantBinding(DEVICE_CODE_REQUEST_TIME);
        String value = storage.get(key);
        if (!StringUtils.isEmpty(value)) {
            try {
                result = LocalDateTime.parse(value.subSequence(0, value.length() - 1));
            } catch (DateTimeException e) {
                logger.error("failed to parse the DeviceCode Request time stamp", e);
            }
        }
        return result;
    }

    private JSONObject requestSpexorBackend(AbstractOptionallyIdentifiedRequest req, String contextName) {
        JSONObject result = null;
        HTTPRequest httpRequest = req.toHTTPRequest();
        Request request = httpClient.newRequest((httpRequest.getURI()));
        request.method(httpRequest.getMethod().name());
        request.accept(httpRequest.getAccept());
        request.header("Authorization", httpRequest.getAuthorization());
        httpRequest.getHeaderMap().keySet().stream().forEach((c) -> request.header(c, httpRequest.getHeaderValue(c)));
        httpRequest.getQueryParameters().keySet().stream()
                .forEach((p) -> request.param(p, String.valueOf(httpRequest.getQueryParameters().get(p))));
        ContentResponse response;
        try {
            response = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            String message = ("code couldn't be requested:" + e.getLocalizedMessage());
            logger.error(message);
            processingStatus.error(message);
            return result;
        }
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        try {
            Object parsedResult = parser.parse(response.getContent());
            if (parsedResult instanceof JSONObject) {
                result = (JSONObject) parsedResult;
            } else {
                String message = ("response did not comply to excected resonse. Excepted: JSON received:"
                        + (parsedResult == null ? "null" : parsedResult.getClass()));
                logger.error(message);
                processingStatus.error(message);
            }
        } catch (net.minidev.json.parser.ParseException e) {
            String message = ("response did not comply to excected response:" + e.getLocalizedMessage());
            logger.error(message, e);
            processingStatus.error(message);
        }
        return result;
    }

    public BoschSpexorBridgeConfig getConfig() {
        return bridgeConfig;
    }

    public void setConfig(BoschSpexorBridgeConfig bridgeConfig) {
        this.bridgeConfig = bridgeConfig;
        handle = getConstantBinding(bridgeConfig.getScope());
    }

    public AuthProcessingStatus getStatus() {
        return processingStatus;
    }

    public ScheduledThreadPoolExecutor getThreadPoolExecutor() {
        return this.executor;
    }
}
