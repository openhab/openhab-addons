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
package org.openhab.binding.miio.internal.cloud;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link MiCloudQRConnector} class is login to the Xiaomi cloud using QR code authentication.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiCloudQRConnector extends MiCloudConnector {
    private static final int REQUEST_TIMEOUT_SECONDS = 120;
    private static final int DEFAULT_SESSION_TIMEOUT_SECONDS = 300;
    private final Logger logger = LoggerFactory.getLogger(MiCloudQRConnector.class);

    public MiCloudQRConnector(String username, String password, HttpClient httpClient) throws MiCloudException {
        super(username, password, httpClient);
    }

    public MiCloudQRConnector(@Nullable String username, @Nullable String password, HttpClient httpClient,
            @Nullable String clientId, @Nullable String userId, @Nullable String serviceToken,
            @Nullable String ssecurity) throws MiCloudException {
        super(username, password, httpClient, clientId, userId, serviceToken, ssecurity);
    }

    /**
     * Data class for holding login session data (QR code login)
     */
    public static class LoginSessionData {
        public final String imageUrl;
        public final String loginUrl;
        public final String longPollingUrl;
        public final long expiresAt;
        public final long timeout;

        public LoginSessionData(String imageUrl, String loginUrl, String longPollingUrl, long expiresAt, long timeout) {
            this.imageUrl = imageUrl;
            this.longPollingUrl = longPollingUrl;
            this.expiresAt = expiresAt;
            this.timeout = timeout;
            this.loginUrl = loginUrl;
        }

        @Override
        public String toString() {
            return "LoginSessionData{" + "imageUrl='" + imageUrl + '\'' + ", loginUrl='" + loginUrl + '\''
                    + ", longPollingUrl='" + longPollingUrl + '\'' + ", expiresAt=" + expiresAt + '}';
        }
    }

    /**
     * QR code login never requires username or password — authentication is done entirely
     * via the QR scan. Always returns true so the base constructor does not reject empty credentials.
     */
    @Override
    protected boolean checkCredentials() {
        return true;
    }

    @Override
    public boolean login(String ignored) {
        return login();
    }

    @Override
    public boolean login() {
        logger.debug("Cloud login using QR code");
        try {
            startClient();
            LoginSessionData sessionData = startLoginSession();
            if (sessionData == null) {
                logger.warn("Failed to start QR code login session");
                return false;
            }

            getQRImage(sessionData.imageUrl);
            logger.debug("Xiaomi QR code login session started; waiting for QR code scan: {}", sessionData.loginUrl);
            updateLoginState(CloudLoginState.AWAITING_QRLOGIN);

            String location = checkSession(sessionData, (sessionData.timeout + REQUEST_TIMEOUT_SECONDS) * 1000L);
            if (location == null || location.isEmpty()) {
                logger.warn("Failed to fetch service token, location is empty");
                return false;
            }

            this.sign = location;
            ContentResponse res = loginStep3(location);
            logger.debug("login step 3 response: {}: {}", res, res.getContentAsString());

            if (res.getStatus() == 200) {
                return true;
            } else {
                logger.warn("Failed to login to Xiaomi cloud, status code: {}", res.getStatus());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Xiaomi QR code login interrupted");
        } catch (MiCloudException | TimeoutException | ExecutionException | MalformedURLException e) {
            logger.warn("Error during Xiaomi QR code login", e);
        }
        return false;
    }

    /**
     * Fetches the QR code image from the given URL.
     *
     * @param url The URL of the QR code image
     * @return The image bytes, or empty array if failed
     */
    public byte[] getQRImage(String url) {
        try {
            return fetchAndInformImage(url, REQUEST_TIMEOUT_SECONDS, "miio-qr-");
        } catch (MiCloudException e) {
            logger.warn("Error getting QR image: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Starts a QR code login session (step 1 of Xiaomi login)
     *
     * @return LoginSessionData with QR code and session info, or null if failed
     */
    public @Nullable LoginSessionData startLoginSession() {
        logger.debug("Xiaomi login step 1 Starting QR code login session");
        try {
            String url = "https://account.xiaomi.com/longPolling/loginUrl";
            long now = System.currentTimeMillis();

            ContentResponse response = httpClient.newRequest(url).param("_qrsize", "240")
                    .param("qs", "%3Fsid%3Dxiaomiio%26_json%3Dtrue").param("callback", "https://sts.api.io.mi.com/sts")
                    .param("_hasLogo", "false").param("sid", "xiaomiio").param("serviceParam", "")
                    .param("_locale", locale.toString()).param("_dc", String.valueOf(now)).method("GET")
                    .agent(USERAGENT).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();

            String text = response.getContentAsString();
            logger.debug("Xiaomi login step 1 login session request response code: {}", response.getStatus());

            String json = CloudUtil.parseJson(text);
            logger.debug("Xiaomi login step 1 login session request response: {}", json);

            JsonObject responseData = GSON.fromJson(json, JsonObject.class);
            if (responseData == null) {
                logger.info("Xiaomi login step 1: Invalid JSON response");
                return null;
            }
            String qr = CloudUtil.getJsonString(responseData, "qr", "");
            String loginUrl = CloudUtil.getJsonString(responseData, "loginUrl", "");
            String lp = CloudUtil.getJsonString(responseData, "lp", "");
            int timeout = CloudUtil.getJsonInt(responseData, "timeout", DEFAULT_SESSION_TIMEOUT_SECONDS);

            // Validate required fields
            if (qr.isEmpty() || loginUrl.isEmpty() || lp.isEmpty()) {
                logger.info("Xiaomi login step 1: Missing required fields in response - qr: {}, loginUrl: {}, lp: {}",
                        !qr.isEmpty(), !loginUrl.isEmpty(), !lp.isEmpty());
                return null;
            }

            long expiresAt = System.currentTimeMillis() + timeout * 1000L;
            LoginSessionData sessionData = new LoginSessionData(qr, loginUrl, lp, expiresAt, timeout);
            logger.debug("Xiaomi login step 1 login: {}", sessionData);
            return sessionData;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("QR login session start interrupted");
            return null;
        } catch (TimeoutException | ExecutionException e) {
            logger.warn("Failed to start Xiaomi QR code login session", e);
            return null;
        }
    }

    /**
     * Checks the QR login session (long polling) and fetches login tokens if available.
     *
     * @param loginSessionData The session data from startLoginSession
     * @param pollTimeoutMillis Optional poll timeout in milliseconds (null for default)
     * @return The location URL if login is successful, null if still pending
     * @throws MiCloudException if polling times out or fails
     */
    public @Nullable String checkSession(LoginSessionData loginSessionData, @Nullable Long pollTimeoutMillis)
            throws MiCloudException {
        logger.debug("Xiaomi login step 2 checking session");
        if (loginSessionData.expiresAt < System.currentTimeMillis()) {
            throw new MiCloudException("Long polling timed out");
        }
        try {
            String pollUrl = loginSessionData.longPollingUrl;
            Request request = httpClient.newRequest(pollUrl).method("GET").timeout(
                    pollTimeoutMillis != null ? pollTimeoutMillis : 30 * REQUEST_TIMEOUT_SECONDS * 1000,
                    TimeUnit.MILLISECONDS);

            ContentResponse response = request.send();
            if (response.getStatus() != 200) {
                logger.debug("Xiaomi login step 2: Non-200 response: {}", response.getStatus());
                return null;
            }

            String json = response.getContentAsString().replace("&&&START&&&", "");
            JsonObject responseJson = GSON.fromJson(json, JsonObject.class);
            if (responseJson == null) {
                throw new MiCloudException("Invalid response during QR code login session check");
            }

            this.userId = CloudUtil.getJsonString(responseJson, "userId", "");
            this.ssecurity = CloudUtil.getJsonString(responseJson, "ssecurity", "");
            String cuserId = CloudUtil.getJsonString(responseJson, "cUserId", "");
            String passToken = CloudUtil.getJsonString(responseJson, "passToken", "");
            String location = CloudUtil.getJsonString(responseJson, "location", "");
            String code = CloudUtil.getJsonString(responseJson, "code", "");
            if (logger.isTraceEnabled()) {
                logger.trace("Xiaomi login ssecurity: {}", this.ssecurity);
                logger.trace("Xiaomi login userId: {}", this.userId);
                logger.trace("Xiaomi login cUserId: {}", cuserId);
                logger.trace("Xiaomi login passToken: {}", passToken);
                logger.trace("Xiaomi login location: {}", location);
                logger.trace("Xiaomi login code: {}", code);
            }
            if (location.isEmpty()) {
                throw new MiCloudException("Error getting login location URL. Return code: " + code);
            }
            return location;
        } catch (TimeoutException e) {
            logger.debug("Xiaomi login step 2 Long polling timed out");
            return null;
        } catch (MiCloudException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Xiaomi login step 2 interrupted");
            return null;
        } catch (ExecutionException e) {
            logger.debug("Xiaomi login step 2 Long polling requests failed: {}", e.getMessage());
            return null;
        }
    }
}
