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

import org.openhab.binding.icloud.internal.utilities.Pair;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.nio.sctp.InvalidStreamException;

/**
 *
 * Class to access Apple iCloud API.
 *
 * The implementation of this class is inspired by https://github.com/picklepete/pyicloud.
 *
 * @author Simon Spielmann Initial contribution
 */
public class ICloudService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ICloudService.class);

    private final static String AUTH_ENDPOINT = "https://idmsa.apple.com/appleauth/auth";

    private final static String HOME_ENDPOINT = "https://www.icloud.com";

    private final static String SETUP_ENDPOINT = "https://setup.icloud.com/setup/ws/1";

    private final Gson gson = new GsonBuilder().create();

    private String appleId;

    private String password;

    private String clientId;

    private boolean withFamily = true;

    private Map<String, Object> data = new HashMap<>();

    private ICloudSession session;

    public ICloudService(String appleId, String password, Storage<String> stateStorage)
            throws IOException, InterruptedException {

        this.appleId = appleId;
        this.password = password;
        this.clientId = "auth-" + UUID.randomUUID().toString().toLowerCase();

        this.session = new ICloudSession(stateStorage);
        this.session.setDefaultHeaders(Pair.of("Accept", "*/*"), Pair.of("Origin", HOME_ENDPOINT),
                Pair.of("Referer", HOME_ENDPOINT + "/"));
    }

    /**
     * Initiate authentication
     *
     * @param forceRefresh Force a new authentication
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean authenticate(boolean forceRefresh) throws IOException, InterruptedException {

        boolean loginSuccessful = false;
        // pyicloud 286
        if (this.session.getSessionToken() != null && !forceRefresh) {
            try {
                this.data = validateToken();
                LOGGER.info("Token is valid.");
                loginSuccessful = true;
            } catch (ICloudAPIResponseException ex) {
                LOGGER.debug("Token is not valid. Attemping new login.");
            }
        }

        if (!loginSuccessful) {
            LOGGER.debug("Authenticating as {}...", this.appleId);

            // TODO use TO here?
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
     * @throws InterruptedException
     * @throws IOException
     *
     */
    public boolean authenticateWithToken() throws IOException, InterruptedException {

        // TODO use TO here?
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
            LOGGER.debug("Invalid authentication.");
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

    /**
     * @throws InterruptedException
     * @throws IOException
     *
     */
    private Map<String, Object> validateToken() throws IOException, InterruptedException {

        LOGGER.debug("Checking session token validity");
        String result = this.session.post(SETUP_ENDPOINT + "/validate", null, null);
        LOGGER.debug("Session token is still valid");
        return this.gson.fromJson(result, Map.class);
    }

    public boolean requires2fa() {

        if (this.data.containsKey("dsInfo")) {
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
     * @return
     */
    public boolean isTrustedSession() {

        return (Boolean) this.data.getOrDefault("hsaTrustedBrowser", Boolean.FALSE);
    }

    /**
     * @param code
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean validate2faCode(String code) throws IOException, InterruptedException {

        HashMap localdata = new HashMap();
        localdata.put("securityCode", Map.of("code", code));

        List<Pair<String, String>> headers = getAuthHeaders();

        addSessionHeaders(headers);

        try {
            this.session.post(AUTH_ENDPOINT + "/verify/trusteddevice/securitycode", this.gson.toJson(localdata),
                    headers);
        } catch (ICloudAPIResponseException ex) {
            // TODO
            // if error.code == -21669:
            // # Wrong verification code
            // LOGGER.error("Code verification failed.")
            return false;
        }

        LOGGER.debug("Code verification successful.");

        trustSession();
        return true;
        // return not self.requires_2sa
    }

    /**
     * @param headers
     */
    private void addSessionHeaders(List<Pair<String, String>> headers) {

        if (this.session.getScnt() != null && !this.session.getScnt().isEmpty()) {
            headers.add(Pair.of("scnt", this.session.getScnt()));
        }
        if (this.session.getSessionId() != null && !this.session.getSessionId().isEmpty()) {
            headers.add(Pair.of("X-Apple-ID-Session-Id", this.session.getSessionId()));
        }
    }

    private String getWebserviceUrl(String wsKey) {

        Map<String, Object> webservices = (Map<String, Object>) this.data.get("webservices");
        if (webservices == null) {
            return null;
        }
        return (String) ((Map) webservices.get(wsKey)).get("url");
    }

    /**
     * @throws InterruptedException
     * @throws IOException
     *
     */
    public void trustSession() throws IOException, InterruptedException {

        List<Pair<String, String>> headers = getAuthHeaders();

        addSessionHeaders(headers);
        this.session.get(AUTH_ENDPOINT + "/2sv/trust", headers);
        authenticateWithToken();
    }

    public FindMyIPhoneServiceManager getDevices() throws IOException, InterruptedException {

        if (getWebserviceUrl("findme") != null) {
            return new FindMyIPhoneServiceManager(this.session, getWebserviceUrl("findme"), this.withFamily);
        } else {
            throw new InvalidStreamException("Webservice URLs not set. Need to authenticate first.");
        }
    }
}
