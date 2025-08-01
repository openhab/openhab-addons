/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.io.IOException;
import java.nio.file.Paths;
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
 * The {@link MiCloudQRConnector} class is logon to the Xiaomi cloud using QR code authentication.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiCloudQRConnector extends MiCloudConnector {
    private static final int REQUEST_TIMEOUT_SECONDS = 120;
    private final Logger logger = LoggerFactory.getLogger(MiCloudQRConnector.class);

    public MiCloudQRConnector(String username, String password, HttpClient httpClient) throws MiCloudException {
        super(username, password, httpClient);
    }

    public MiCloudQRConnector(@Nullable String username, @Nullable String password, HttpClient httpClient,
            @Nullable String clientId, @Nullable String userId, @Nullable String serviceToken,
            @Nullable String ssecurity) throws MiCloudException {
        super(username, password, httpClient, clientId, userId, serviceToken, ssecurity);
    }

    // Data class for holding login session data (QR code login)
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

    @Override
    public synchronized boolean login(String ignored) {
        return login();
    }

    @Override
    public synchronized boolean login() {
        logger.debug("Cloud login using QR code");
        try {
            LoginSessionData sessionData = startLoginSession();
            if (sessionData != null) {
                /// updateState(CHANNEL_QR_CODE, new RawType(sessionData.imageContent, "image/png"));

                getQRImage(sessionData.imageUrl);
                logger.info("Xiaomi QR code login: {}", sessionData.loginUrl);

                // miIoScheduler.scheduleWithFixedDelay(() -> {
                // try {
                String location = checkSession(sessionData, null);
                if (location != null && !location.isEmpty()) {
                    this.sign = location;
                    ContentResponse res = loginStep3(location);
                    logger.debug("login step 3 response: {}: {}", res, res.getContentAsString());
                    if (res.getStatus() == 200) {
                        logger.info("Xiaomi QR code login successful");
                        return true;
                    } else {
                        logger.warn("Failed to login to Xiaomi cloud, status code: {}", res.getStatus());
                    }

                } else {
                    logger.warn("Failed to fetch service token, status code: {}", location);
                }
            }

        } catch (Exception e) {
            logger.warn("Error during Xiaomi QR code login", e);
        }
        return false;
    }

    public byte[] getQRImage(String url) {
        ContentResponse response;
        try {
            response = httpClient.newRequest(url).header("User-Agent", USERAGENT).method("GET").send();
            final byte[] content = response.getContent();

            String fileDest = "capcha.jpg";
            try {

                fileDest = Paths.get(fileDest).toAbsolutePath().toString();
                CloudUtil.writeBytesToFileNio(content, fileDest);
                logger.info("Saved to {} -> {} bytes", fileDest, content.length);
            } catch (IOException e) {
                logger.warn("Error writing {}.\r\n{}", fileDest, e.getMessage(), e);
            }
            informImageListeners(content);

            return content;

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error getting image: {}", e.getMessage(), e);
        }
        return new byte[0];
    }

    /**
     * Starts a QR code login session (step 1 of Xiaomi login)
     *
     * @return LoginSessionData with QR code and session info
     */
    public @Nullable LoginSessionData startLoginSession() {
        logger.debug("Xiaomi login step 1 Starting QR code login session");
        try {
            String url = "https://account.xiaomi.com/longPolling/loginUrl";
            long now = System.currentTimeMillis();

            Request request = httpClient.newRequest("https://google.com").timeout(REQUEST_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
            logger.debug("Initial request to google.com: {}", request.send().getStatus());
            ContentResponse response = httpClient.newRequest(url).param("_qrsize", "240")
                    .param("qs", "%3Fsid%3Dxiaomiio%26_json%3Dtrue").param("callback", "https://sts.api.io.mi.com/sts")
                    .param("_hasLogo", "false").param("sid", "xiaomiio").param("serviceParam", "")
                    .param("_locale", locale.toString()).param("_dc", String.valueOf(now)).method("GET")
                    .agent(USERAGENT).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            String text = response.getContentAsString();
            logger.debug("Xiaomi login step 1 login session request response code: {}", response.getStatus());
            String json = CloudUtil.parseJson(text);

            logger.debug("Xiaomi login step 1 login session request response: {}", json);
            JsonObject responseData = GSON.fromJson(json, com.google.gson.JsonObject.class);
            String qr = responseData.get("qr").getAsString();
            String loginUrl = responseData.get("loginUrl").getAsString();
            String lp = responseData.get("lp").getAsString();
            int timeout = responseData.get("timeout").getAsInt();
            long expiresAt = System.currentTimeMillis() + timeout * 1000L;
            LoginSessionData sessionData = new LoginSessionData(qr, loginUrl, lp, expiresAt, timeout);
            logger.debug("Xiaomi login step 1 login: {}", sessionData);
            return sessionData;
        } catch (Exception e) {
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
                return null;
            }
            String json = response.getContentAsString().replace("&&&START&&&", "");
            JsonObject responseJson = GSON.fromJson(json, JsonObject.class);
            this.userId = responseJson.get("userId").getAsString();
            this.ssecurity = responseJson.get("ssecurity").getAsString();
            String cuserId = responseJson.get("cUserId").getAsString();
            String passToken = responseJson.get("passToken").getAsString();
            String location = responseJson.get("location").getAsString();
            String code = responseJson.get("code").getAsString();
            logger.debug("Xiaomi login ssecurity: {}", this.ssecurity);
            logger.debug("Xiaomi login userId: {}", this.userId);
            logger.debug("Xiaomi login cUserId: {}", cuserId);
            logger.debug("Xiaomi login passToken: {}", passToken);
            logger.debug("Xiaomi login location: {}", location);
            logger.debug("Xiaomi login code: {}", code);
            if (location == null || location.isEmpty()) {
                throw new MiCloudException("Error getting logon location URL. Return code: " + code);
            }
            return location;
        } catch (TimeoutException e) {
            logger.debug("Xiaomi login step 2 Long polling timed out");
            return null;
        } catch (Exception e) {
            logger.debug("Xiaomi login step 2 Long polling requests failed: {}", e.getMessage());
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
    /*
     * public @Nullable String checkSession(LoginSessionData loginSessionData, @Nullable Long pollTimeoutMillis) throws
     * MiCloudException {
     * logger.debug("Xiaomi login step 2 checking session");
     * if (loginSessionData.expiresAt < System.currentTimeMillis()) {
     * throw new MiCloudException("Long polling timed out");
     * }
     * try {
     * String pollUrl = loginSessionData.longPollingUrl;
     * Request request = httpClient.newRequest(pollUrl)
     * .method("GET")
     * .timeout(pollTimeoutMillis != null ? pollTimeoutMillis : REQUEST_TIMEOUT_SECONDS * 1000, TimeUnit.MILLISECONDS);
     * ContentResponse response = request.send();
     * if (response.getStatus() != 200) {
     * return null;
     * }
     * String json = response.getContentAsString().replace("&&&START&&&", "");
     * JsonObject responseJson = GSON.fromJson(json, JsonObject.class);
     * this.userId = responseJson.get("userId").getAsString();
     * this.ssecurity = responseJson.get("ssecurity").getAsString();
     * String cuserId = responseJson.get("cUserId").getAsString();
     * String passToken = responseJson.get("passToken").getAsString();
     * String location = responseJson.get("location").getAsString();
     * String code = responseJson.get("code").getAsString();
     * logger.debug("Xiaomi login ssecurity: {}", this.ssecurity);
     * logger.debug("Xiaomi login userId: {}", this.userId);
     * logger.debug("Xiaomi login cUserId: {}", cuserId);
     * logger.debug("Xiaomi login passToken: {}", passToken);
     * logger.debug("Xiaomi login location: {}", location);
     * logger.debug("Xiaomi login code: {}", code);
     * if (location == null || location.isEmpty()) {
     * throw new MiCloudException("Error getting logon location URL. Return code: " + code);
     * }
     * return location;
     * } catch (TimeoutException e) {
     * logger.debug("Xiaomi login step 2 Long polling timed out");
     * return null;
     * } catch (Exception e) {
     * logger.debug("Xiaomi login step 2 Long polling requests failed: {}", e.getMessage());
     * return null;
     * }
     * }
     */
    /**
     * Fetches the service token by visiting the location URL (step 3 of QR login).
     *
     * @param location The URL to visit
     * @return The ContentResponse from the request
     * @throws MiCloudException if the request fails
     */
    /*
     * public ContentResponse fetchServiceToken(String location) throws MiCloudException {
     * logger.debug("Xiaomi login step 3 @ {}", location);
     * try {
     * Request request = httpClient.newRequest(location)
     * .method("GET")
     * .header("content-type", "application/x-www-form-urlencoded")
     * .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
     * ContentResponse response = request.send();
     * logger.debug("Xiaomi login step 3 content: {}", response.getContentAsString());
     * logger.debug("Xiaomi login step 3 status code: {}", response.getStatus());
     * // Try to extract serviceToken from cookies
     * List<HttpCookie> cookies = response.getCookies();
     * for (HttpCookie cookie : cookies) {
     * if ("serviceToken".equals(cookie.getName())) {
     * this.serviceToken = cookie.getValue();
     * break;
     * }
     * }
     * return response;
     * } catch (Exception e) {
     * throw new MiCloudException("Failed to fetch service token: " + e.getMessage(), e);
     * }
     * }
     */
}
