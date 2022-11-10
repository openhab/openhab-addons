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
package org.openhab.binding.icloud.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.icloud.internal.utilities.ListUtil;
import org.openhab.binding.icloud.internal.utilities.Pair;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * Class to access Apple iCloud API.
 *
 * The implementation of this class is inspired by https://github.com/picklepete/pyicloud.
 *
 * @author Simon Spielmann Initial contribution
 */
public class ICloudService {

    private final Logger logger = LoggerFactory.getLogger(ICloudService.class);

    private final static String AUTH_ENDPOINT = "https://idmsa.apple.com/appleauth/auth";

    private final static String HOME_ENDPOINT = "https://www.icloud.com";

    private final static String SETUP_ENDPOINT = "https://setup.icloud.com/setup/ws/1";

    private final Gson gson = new GsonBuilder().create();

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
        // pyicloud 286
        if (this.session.getSessionToken() != null && !forceRefresh) {
            try {
                this.data = validateToken();
                logger.debug("Token is valid.");
                loginSuccessful = true;
            } catch (ICloudAPIResponseException ex) {
                logger.debug("Token is not valid. Attemping new login.", ex);
            }
        }

        if (!loginSuccessful) {
            logger.debug("Authenticating as {}...", this.appleId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("accountName", this.appleId);
            requestBody.put("password", this.password);
            requestBody.put("rememberMe", true);
            if (this.session.hasToken()) {
                requestBody.put("trustTokens", new String[] { this.session.getTrustToken() });
            } else {
                requestBody.put("trustTokens", new String[0]);
            }

            List<Pair<String, String>> headers = getAuthHeaders();

            try {
                this.session.post(AUTH_ENDPOINT + "/signin?isRememberMeEnabled=true", this.gson.toJson(requestBody),
                        headers);
            } catch (ICloudAPIResponseException ex) {
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
        requestBody.put("accountCountryCode", this.session.getAccountCountry());
        requestBody.put("dsWebAuthToken", this.session.getSessionToken());
        requestBody.put("extended_login", true);
        if (this.session.hasToken()) {
            requestBody.put("trustToken", this.session.getTrustToken());
        } else {
            requestBody.put("trustToken", "");
        }

        try {
            this.data = this.gson.fromJson(
                    this.session.post(SETUP_ENDPOINT + "/accountLogin", this.gson.toJson(requestBody), null),
                    Map.class);
        } catch (ICloudAPIResponseException ex) {
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
                Pair.of("X-Apple-OAuth-Client-Id", "d39ba9916b7251055b22c7f910e2ea796ee65e98b2ddecea8f5dde8d9d1a815d"),
                Pair.of("X-Apple-OAuth-Client-Type", "firstPartyAuth"),
                Pair.of("X-Apple-OAuth-Redirect-URI", "https://www.icloud.com"),
                Pair.of("X-Apple-OAuth-Require-Grant-Code", "true"),
                Pair.of("X-Apple-OAuth-Response-Mode", "web_message"), Pair.of("X-Apple-OAuth-Response-Type", "code"),
                Pair.of("X-Apple-OAuth-State", this.clientId),
                Pair.of("X-Apple-Widget-Key", "d39ba9916b7251055b22c7f910e2ea796ee65e98b2ddecea8f5dde8d9d1a815d")));
    }

    private Map<String, Object> validateToken() throws IOException, InterruptedException {

        logger.debug("Checking session token validity");
        String result = this.session.post(SETUP_ENDPOINT + "/validate", null, null);
        logger.debug("Session token is still valid");
        return this.gson.fromJson(result, Map.class);
    }

    /**
     * Checks if 2-FA authentication is required.
     *
     * @return {@code true} if 2-FA authentication ({@link #validate2faCode(String)}) is required.
     */
    public boolean requires2fa() {

        if (this.data.containsKey("dsInfo")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dsInfo = (Map<String, Object>) this.data.get("dsInfo");
            if (((Double) dsInfo.getOrDefault("hsaVersion", "0")) == 2.0) {
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
     */
    public boolean validate2faCode(String code) throws IOException, InterruptedException {

        Map<String, Object> requestBody = Map.of("securityCode", Map.of("code", code));

        List<Pair<String, String>> headers = ListUtil.replaceEntries(getAuthHeaders(),
                List.of(Pair.of("Accept", "application/json")));

        addSessionHeaders(headers);

        try {
            this.session.post(AUTH_ENDPOINT + "/verify/trusteddevice/securitycode", this.gson.toJson(requestBody),
                    headers);
        } catch (ICloudAPIResponseException ex) {
            logger.debug("Code verification failed.", ex);
            return false;
        }

        logger.debug("Code verification successful.");

        trustSession();
        return true;
        // return not self.requires_2sa
    }

    private void addSessionHeaders(List<Pair<String, String>> headers) {

        if (this.session.getScnt() != null && !this.session.getScnt().isEmpty()) {
            headers.add(Pair.of("scnt", this.session.getScnt()));
        }
        if (this.session.getSessionId() != null && !this.session.getSessionId().isEmpty()) {
            headers.add(Pair.of("X-Apple-ID-Session-Id", this.session.getSessionId()));
        }
    }

    private String getWebserviceUrl(String wsKey) {
        @SuppressWarnings("unchecked")
        Map<String, Object> webservices = (Map<String, Object>) this.data.get("webservices");
        if (webservices == null) {
            return null;
        }
        return (String) ((Map<?, ?>) webservices.get(wsKey)).get("url");
    }

    /**
     * Establish trust for current session.
     *
     * @return {@code true} if successful.
     *
     * @throws IOException if I/O error occurred
     * @throws InterruptedException if this request was interrupted
     *
     */
    public boolean trustSession() throws IOException, InterruptedException {

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

        if (getWebserviceUrl("findme") != null) {
            return new FindMyIPhoneServiceManager(this.session, getWebserviceUrl("findme"));
        } else {
            throw new IllegalStateException("Webservice URLs not set. Need to authenticate first.");
        }
    }
}
