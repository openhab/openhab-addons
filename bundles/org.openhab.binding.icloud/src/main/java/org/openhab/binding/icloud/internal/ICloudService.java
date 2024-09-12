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
package org.openhab.binding.icloud.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.icloud.internal.utilities.JsonUtils;
import org.openhab.binding.icloud.internal.utilities.ListUtil;
import org.openhab.binding.icloud.internal.utilities.Pair;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class to access Apple iCloud API.
 *
 * The implementation of this class is inspired by https://github.com/picklepete/pyicloud.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public class ICloudService {

    /**
     *
     */
    private static final String ICLOUD_CLIENT_ID = "d39ba9916b7251055b22c7f910e2ea796ee65e98b2ddecea8f5dde8d9d1a815d";

    private final Logger logger = LoggerFactory.getLogger(ICloudService.class);

    private static final String AUTH_ENDPOINT = "https://idmsa.apple.com/appleauth/auth";

    private static final String HOME_ENDPOINT = "https://www.icloud.com";

    private static final String SETUP_ENDPOINT = "https://setup.icloud.com/setup/ws/1";

    private String appleId;

    private String password;

    private String clientId;

    private Map<String, Object> data = new HashMap<>();

    private ICloudSession session;

    /**
     *
     * The constructor.
     *
     * @param appleId Apple id (e-mail address) for authentication
     * @param password Password used for authentication
     * @param stateStorage Storage to save authentication state
     */
    public ICloudService(String appleId, String password, Storage<String> stateStorage) {
        this.appleId = appleId;
        this.password = password;
        this.clientId = "auth-" + UUID.randomUUID().toString().toLowerCase();

        this.session = new ICloudSession(stateStorage);
        this.session.setDefaultHeaders(Pair.of("Origin", HOME_ENDPOINT), Pair.of("Referer", HOME_ENDPOINT + "/"));
    }

    /**
     * Initiate authentication
     *
     * @param forceRefresh Force a new authentication
     * @return {@code true} if authentication was successful
     * @throws IOException if I/O error occurred
     * @throws InterruptedException if request was interrupted
     */
    public boolean authenticate(boolean forceRefresh) throws IOException, InterruptedException {
        boolean loginSuccessful = false;
        if (this.session.getSessionToken() != null && !forceRefresh) {
            try {
                this.data = validateToken();
                logger.debug("Token is valid.");
                loginSuccessful = true;
            } catch (ICloudApiResponseException ex) {
                logger.debug("Token is not valid. Attemping new login.", ex);
            }
        }

        if (!loginSuccessful) {
            logger.debug("Authenticating as {}...", this.appleId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("accountName", this.appleId);
            requestBody.put("password", this.password);
            requestBody.put("rememberMe", true);
            if (session.hasToken()) {
                requestBody.put("trustTokens", new String[] { this.session.getTrustToken() });
            } else {
                requestBody.put("trustTokens", new String[0]);
            }

            List<Pair<String, String>> headers = getAuthHeaders();

            try {
                this.session.post(AUTH_ENDPOINT + "/signin?isRememberMeEnabled=true", JsonUtils.toJson(requestBody),
                        headers);
            } catch (ICloudApiResponseException ex) {
                return false;
            }
        }
        return authenticateWithToken();
    }

    /**
     * Try authentication with stored session token. Returns {@code true} if authentication was successful.
     *
     * @return {@code true} if authentication was successful
     *
     * @throws IOException if I/O error occurred
     * @throws InterruptedException if this request was interrupted
     *
     */
    public boolean authenticateWithToken() throws IOException, InterruptedException {
        Map<String, Object> requestBody = new HashMap<>();

        String accountCountry = session.getAccountCountry();
        if (accountCountry != null) {
            requestBody.put("accountCountryCode", accountCountry);
        }

        String sessionToken = session.getSessionToken();
        if (sessionToken != null) {
            requestBody.put("dsWebAuthToken", sessionToken);
        }

        requestBody.put("extended_login", true);

        if (session.hasToken()) {
            String token = session.getTrustToken();
            if (token != null) {
                requestBody.put("trustToken", token);
            }
        } else {
            requestBody.put("trustToken", "");
        }

        try {
            @Nullable
            Map<String, Object> localSessionData = JsonUtils
                    .toMap(session.post(SETUP_ENDPOINT + "/accountLogin", JsonUtils.toJson(requestBody), null));
            if (localSessionData != null) {
                data = localSessionData;
            }
        } catch (ICloudApiResponseException ex) {
            logger.debug("Invalid authentication.");
            return false;
        }
        return true;
    }

    /**
     * @param pair
     * @return
     */
    private List<Pair<String, String>> getAuthHeaders() {
        return new ArrayList<>(List.of(Pair.of("Accept", "*/*"), Pair.of("Content-Type", "application/json"),
                Pair.of("X-Apple-OAuth-Client-Id", ICLOUD_CLIENT_ID),
                Pair.of("X-Apple-OAuth-Client-Type", "firstPartyAuth"),
                Pair.of("X-Apple-OAuth-Redirect-URI", HOME_ENDPOINT),
                Pair.of("X-Apple-OAuth-Require-Grant-Code", "true"),
                Pair.of("X-Apple-OAuth-Response-Mode", "web_message"), Pair.of("X-Apple-OAuth-Response-Type", "code"),
                Pair.of("X-Apple-OAuth-State", this.clientId), Pair.of("X-Apple-Widget-Key", ICLOUD_CLIENT_ID)));
    }

    private Map<String, Object> validateToken() throws IOException, InterruptedException, ICloudApiResponseException {
        logger.debug("Checking session token validity");
        String result = session.post(SETUP_ENDPOINT + "/validate", null, null);
        logger.debug("Session token is still valid");

        @Nullable
        Map<String, Object> localSessionData = JsonUtils.toMap(result);
        if (localSessionData == null) {
            throw new IOException("Unable to create data object from json response");
        }
        return localSessionData;
    }

    /**
     * Checks if 2-FA authentication is required.
     *
     * @return {@code true} if 2-FA authentication ({@link #validate2faCode(String)}) is required.
     */
    public boolean requires2fa() {
        if (this.data.containsKey("dsInfo")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dsInfo = (@Nullable Map<String, Object>) this.data.get("dsInfo");
            if (dsInfo != null && ((Double) dsInfo.getOrDefault("hsaVersion", "0")) == 2.0) {
                return (this.data.containsKey("hsaChallengeRequired")
                        && ((Boolean) this.data.getOrDefault("hsaChallengeRequired", Boolean.FALSE)
                                || !isTrustedSession()));
            }
        }
        return false;
    }

    /**
     * Checks if session is trusted.
     *
     * @return {@code true} if session is trusted. Call {@link #trustSession()} if not.
     */
    public boolean isTrustedSession() {
        return (Boolean) this.data.getOrDefault("hsaTrustedBrowser", Boolean.FALSE);
    }

    /**
     * Provides 2-FA code to establish trusted session.
     *
     * @param code Code given by user for 2-FA authentication.
     * @return {@code true} if code was accepted
     * @throws IOException if I/O error occurred
     * @throws InterruptedException if this request was interrupted
     * @throws ICloudApiResponseException if the request failed (e.g. not OK HTTP return code)
     */
    public boolean validate2faCode(String code) throws IOException, InterruptedException, ICloudApiResponseException {
        Map<String, Object> requestBody = Map.of("securityCode", Map.of("code", code));

        List<Pair<String, String>> headers = ListUtil.replaceEntries(getAuthHeaders(),
                List.of(Pair.of("Accept", "application/json")));

        addSessionHeaders(headers);

        try {
            this.session.post(AUTH_ENDPOINT + "/verify/trusteddevice/securitycode", JsonUtils.toJson(requestBody),
                    headers);
        } catch (ICloudApiResponseException ex) {
            logger.trace("Exception on code verification with HTTP status {}. Verification might still be successful.",
                    ex.getStatusCode(), ex);
            // iCloud API returns different 4xx error codes even if validation is successful
            // currently 400 seems to show that verification "really" failed.
            if (ex.getStatusCode() == 400 || ex.getStatusCode() >= 500) {
                this.logger.debug("Verification failed with HTTP status {}.", ex.getStatusCode());
                return false;
            }
        }

        logger.debug("Code verification successful.");

        trustSession();
        return true;
    }

    private void addSessionHeaders(List<Pair<String, String>> headers) {
        String scnt = session.getScnt();
        if (scnt != null && !scnt.isEmpty()) {
            headers.add(Pair.of("scnt", scnt));
        }

        String sessionId = session.getSessionId();
        if (sessionId != null && !sessionId.isEmpty()) {
            headers.add(Pair.of("X-Apple-ID-Session-Id", sessionId));
        }
    }

    private @Nullable String getWebserviceUrl(String wsKey) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> webservices = (@Nullable Map<String, Object>) data.get("webservices");
            if (webservices == null) {
                return null;
            }
            if (webservices.get(wsKey) instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, ?> wsMap = (@Nullable Map<String, ?>) webservices.get(wsKey);
                if (wsMap == null) {
                    logger.error("Webservices result map has not expected format.");
                    return null;
                }
                return (String) wsMap.get("url");
            } else {
                logger.error("Webservices result map has not expected format.");
                return null;
            }
        } catch (ClassCastException e) {
            logger.error("ClassCastException, map has not expected format.", e);
            return null;
        }
    }

    /**
     * Establish trust for current session.
     *
     * @return {@code true} if successful.
     *
     * @throws IOException if I/O error occurred
     * @throws InterruptedException if this request was interrupted
     * @throws ICloudApiResponseException if the request failed (e.g. not OK HTTP return code)
     *
     */
    public boolean trustSession() throws IOException, InterruptedException, ICloudApiResponseException {
        List<Pair<String, String>> headers = getAuthHeaders();

        addSessionHeaders(headers);
        this.session.get(AUTH_ENDPOINT + "/2sv/trust", headers);
        return authenticateWithToken();
    }

    /**
     * Get access to find my iPhone service.
     *
     * @return Instance of {@link FindMyIPhoneServiceManager} for this session.
     * @throws IOException if I/O error occurred
     * @throws InterruptedException if this request was interrupted
     */
    public FindMyIPhoneServiceManager getDevices() throws IOException, InterruptedException {
        String webserviceUrl = getWebserviceUrl("findme");
        if (webserviceUrl != null) {
            return new FindMyIPhoneServiceManager(this.session, webserviceUrl);
        } else {
            throw new IllegalStateException("Webservice URLs not set. Need to authenticate first.");
        }
    }
}
