/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

/**
 *
 * TODO
 *
 * @author Simon Spielmann
 */
public class ICloudService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ICloudService.class);

    private final static String AUTH_ENDPOINT = "https://idmsa.apple.com/appleauth/auth";

    private final static String HOME_ENDPOINT = "https://www.icloud.com";

    private final static String SETUP_ENDPOINT = "https://setup.icloud.com/setup/ws/1";

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    private String appleId;

    private String password;

    private String clientId;

    private boolean withFamily = true;

    private Map<String, Object> data = new HashMap<>();

    // TODO why this pyicloud
    private Object params;

    private ICloudSession session;

    public ICloudService(String appleId, String password, Storage<String> stateStorage)
            throws IOException, InterruptedException {

        this(appleId, password, "auth-" + UUID.randomUUID().toString().toLowerCase(), stateStorage);
    }

    public ICloudService(String appleId, String password, String clientId, Storage<String> stateStorage)
            throws IOException, InterruptedException {

        this.appleId = appleId;
        this.password = password;
        this.clientId = clientId;

        this.session = new ICloudSession(stateStorage);
        this.session.updateHeaders(Pair.of("Accept", "*/*"), Pair.of("Origin", HOME_ENDPOINT),
                Pair.of("Referer", HOME_ENDPOINT + "/"));

        // loadCookies();
        // set ClientId from stored session base.py L 228-253;

        // FIXME refactor do not do it in constructor
        authenticate(false, null);
    }

    private void authenticate(boolean forceRefresh, String service) throws IOException, InterruptedException {

        boolean loginSuccessful = false;
        // pyicloud 286
        if (this.session.getSessionToken() != null && !forceRefresh) {
            this.data = validateToken();
            LOGGER.info("Token is valid.");
            loginSuccessful = true;
        }

        if (!loginSuccessful && service != null) {
            // TODO work with maps?
            Map<String, Object> app = (Map<String, Object>) ((Map<String, Object>) this.data.get("apps")).get(service);
            if (app.containsKey("canLaunchWithOneFactor") && app.get("canLaunchWithOneFactor").equals(Boolean.TRUE)) {
                try {
                    authenticateWithCredentialsService(service);
                    loginSuccessful = true;
                } catch (Exception ex) {
                    LOGGER.debug("Cannot log into service. Attemping new login.");
                }
            }
        }

        if (!loginSuccessful) {
            LOGGER.debug("Authenticating as {}...", this.appleId);

            // TODO use TO here?
            HashMap localdata = new HashMap();
            localdata.put("accountName", this.appleId);
            localdata.put("password", this.password);
            localdata.put("rememberMe", true);
            if (this.session.hasToken()) {
                localdata.put("trustTokens", new String[] { this.session.getTrustToken() });
            } else {
                localdata.put("trustTokens", new String[0]);
            }

            // TODO why this pycloud 318?

            List<Pair<String, String>> headers = getAuthHeaders(null);

            try {
                this.session.post(AUTH_ENDPOINT + "/signin?isRememberMeEnabled=true", this.gson.toJson(localdata),
                        headers);
            } catch (ICloudAPIResponseException ex) {
                throw new RuntimeException("Invalid username/password.");
            }

        }
        authenticateWithToken();
    }

    /**
     * @throws InterruptedException
     * @throws IOException
     *
     */
    public void authenticateWithToken() throws IOException, InterruptedException {

        // TODO use TO here?
        HashMap localdata = new HashMap();
        localdata.put("accountCountryCode", this.session.getAccountCountry());
        localdata.put("dsWebAuthToken", this.session.getSessionToken());
        localdata.put("extended_login", true);
        if (this.session.hasToken()) {
            localdata.put("trustToken", this.session.getTrustToken());
        } else {
            localdata.put("trustToken", "");
        }

        try {
            this.data = this.gson.fromJson(
                    this.session.post(SETUP_ENDPOINT + "/accountLogin", this.gson.toJson(localdata), null), Map.class);
        } catch (ICloudAPIResponseException ex) {
            throw new RuntimeException("Invalid authentication");
        }
    }

    /**
     * @param pair
     * @return
     */
    private List<Pair<String, String>> getAuthHeaders(Pair<String, String>... replacement) {

        List<Pair<String, String>> result = new ArrayList(List.of(Pair.of("Accept", "*/*"),
                Pair.of("Content-Type", "application/json"),
                Pair.of("X-Apple-OAuth-Client-Id", "d39ba9916b7251055b22c7f910e2ea796ee65e98b2ddecea8f5dde8d9d1a815d"),
                Pair.of("X-Apple-OAuth-Client-Type", "firstPartyAuth"),
                Pair.of("X-Apple-OAuth-Redirect-URI", "https://www.icloud.com"),
                Pair.of("X-Apple-OAuth-Require-Grant-Code", "true"),
                Pair.of("X-Apple-OAuth-Response-Mode", "web_message"), Pair.of("X-Apple-OAuth-Response-Type", "code"),
                Pair.of("X-Apple-OAuth-State", this.clientId),
                Pair.of("X-Apple-Widget-Key", "d39ba9916b7251055b22c7f910e2ea796ee65e98b2ddecea8f5dde8d9d1a815d")));

        if (replacement != null) {
            ICloudSession.updateList(result, replacement);
        }
        return result;
    }

    /**
     * @param service
     */
    private void authenticateWithCredentialsService(String service) {

        throw new RuntimeException("Not implemented!");
    }

    /**
     * @throws InterruptedException
     * @throws IOException
     *
     */
    private Map<String, Object> validateToken() throws IOException, InterruptedException {

        LOGGER.debug("Checking session token validity");
        try {
            String result = this.session.post(SETUP_ENDPOINT + "/validate", null, null);
            LOGGER.debug("Session token is still valid");
            return this.gson.fromJson(result, Map.class);
        } catch (ICloudAPIResponseException ex) {
            return null;
        }
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

        List<Pair<String, String>> headers = getAuthHeaders(Pair.of("Accept", "application/json"));

        if (this.session.getScnt() != null && !this.session.getScnt().isEmpty()) {
            headers.add(Pair.of("scnt", this.session.getScnt()));
        }
        if (this.session.getSessionId() != null && !this.session.getSessionId().isEmpty()) {
            headers.add(Pair.of("X-Apple-ID-Session-Id", this.session.getSessionId()));
        }

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

        if (this.session.getScnt() != null && !this.session.getScnt().isEmpty()) {
            headers.add(Pair.of("scnt", this.session.getScnt()));
        }
        if (this.session.getSessionId() != null && !this.session.getSessionId().isEmpty()) {
            headers.add(Pair.of("X-Apple-ID-Session-Id", this.session.getSessionId()));
        }
        this.session.get(AUTH_ENDPOINT + "/2sv/trust", null, headers);
        authenticateWithToken();
    }

    public FindMyIPhoneServiceManager getDevices() throws IOException, InterruptedException {
        if (getWebserviceUrl("findme") != null) {
            return new FindMyIPhoneServiceManager(this.session, getWebserviceUrl("findme"), this.withFamily);
        } else {
            throw new IOException("Webservice URLs not set. Need to authenticate first.");
        }
    }
}
