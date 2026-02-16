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
package org.openhab.binding.vesync.internal.api;

import static org.openhab.binding.vesync.internal.dto.requests.VeSyncProtocolConstants.*;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncAuthLoginWithAuthorizeCodeVeSync;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncAuthLoginWithAuthorizeCodeVeSyncRegionChange;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncAuthTokenRequest;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncAuthenticatedRequest;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestManagedDeviceBypassV2;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestManagedDevicesPage;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncAuthLoginWithAuthorizeCodeVeSyncResponse;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncAuthTokenResponse;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncLoginResponse;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncManagedDeviceBase;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncManagedDevicesPage;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncResponse;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncUserSession;
import org.openhab.binding.vesync.internal.exceptions.AuthenticationException;
import org.openhab.binding.vesync.internal.exceptions.DeviceUnknownException;
import org.openhab.binding.vesync.internal.handlers.VeSyncBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VeSyncV2ApiHelper {

    private final Logger logger = LoggerFactory.getLogger(VeSyncV2ApiHelper.class);

    private static final int RESPONSE_TIMEOUT_SEC = 5;

    private volatile @Nullable VeSyncUserSession loggedInSession;

    private final @Nullable HttpClient httpClient;

    private Map<String, @NotNull VeSyncManagedDeviceBase> macLookup;

    public VeSyncV2ApiHelper(final HttpClient httpClient) {
        this.httpClient = httpClient;
        macLookup = new HashMap<>();
    }

    public Map<String, @NotNull VeSyncManagedDeviceBase> getMacLookupMap() {
        return macLookup;
    }

    public void dispose() {
        loggedInSession = null;
        macLookup = new HashMap<>();
    }

    public static @NotNull String calculateMd5(final @Nullable String password) {
        if (password == null) {
            return "";
        }
        MessageDigest md5;
        StringBuilder md5Result = new StringBuilder();
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
        byte[] handshakeHash = md5.digest(password.getBytes(StandardCharsets.UTF_8));
        for (byte handshakeByte : handshakeHash) {
            md5Result.append(String.format("%02x", handshakeByte));
        }
        return md5Result.toString();
    }

    public void discoverDevices() throws AuthenticationException {
        try {
            VeSyncRequestManagedDevicesPage reqDevPage = new VeSyncRequestManagedDevicesPage(loggedInSession);
            boolean finished = false;
            int pageNo = 1;
            HashMap<String, VeSyncManagedDeviceBase> generatedMacLookup = new HashMap<>();
            while (!finished) {
                reqDevPage.pageNo = String.valueOf(pageNo);
                reqDevPage.pageSize = String.valueOf(100);
                final String result = reqV1Authorized(V1_MANAGED_DEVICES_ENDPOINT, reqDevPage);

                VeSyncManagedDevicesPage resultsPage = VeSyncConstants.GSON.fromJson(result,
                        VeSyncManagedDevicesPage.class);
                if (resultsPage == null || !resultsPage.outcome.getTotal().equals(resultsPage.outcome.getPageSize())) {
                    finished = true;
                } else {
                    ++pageNo;
                }

                if (resultsPage != null) {
                    for (VeSyncManagedDeviceBase device : resultsPage.outcome.list) {
                        logger.debug(
                                "Found device : {}, type: {}, deviceType: {}, connectionState: {}, deviceStatus: {}, deviceRegion: {}, cid: {}, configModule: {}, macID: {}, uuid: {}",
                                device.getDeviceName(), device.getType(), device.getDeviceType(),
                                device.getConnectionStatus(), device.getDeviceStatus(), device.getDeviceRegion(),
                                device.getCid(), device.getConfigModule(), device.getMacId(), device.getUuid());

                        // Update the mac address -> device table
                        generatedMacLookup.put(device.getMacId(), device);
                    }
                }
            }
            macLookup = Collections.unmodifiableMap(generatedMacLookup);
        } catch (final AuthenticationException ae) {
            logger.warn("Failed background device scan : {}", ae.getMessage());
            throw ae;
        }
    }

    public String reqV2Authorized(String url, final String macId, final VeSyncAuthenticatedRequest requestData)
            throws AuthenticationException, DeviceUnknownException {
        if (loggedInSession == null) {
            throw new AuthenticationException("User is not logged in");
        }

        @Nullable
        VeSyncUserSession session = loggedInSession;
        if (session != null) {
            url = session.serverUrl + url;
        } else {
            url = PROTOCOL + US_SERVER_ADDRESS + url; // Fallback
        }

        // Apply current session authentication data
        requestData.applyAuthentication(loggedInSession);

        // Apply specific addressing parameters
        if (requestData instanceof VeSyncRequestManagedDeviceBypassV2 veSyncRequestManagedDeviceBypassV2) {
            final VeSyncManagedDeviceBase deviceData = macLookup.get(macId);
            if (deviceData == null) {
                throw new DeviceUnknownException(String.format("Device not discovered with mac id: %s", macId));
            }
            veSyncRequestManagedDeviceBypassV2.cid = deviceData.cid;
            veSyncRequestManagedDeviceBypassV2.configModule = deviceData.configModule;
            veSyncRequestManagedDeviceBypassV2.configModel = deviceData.configModule;
            veSyncRequestManagedDeviceBypassV2.deviceRegion = deviceData.deviceRegion;
        }
        return reqV1Authorized(url, requestData);
    }

    public String reqV1Authorized(final String url, final VeSyncAuthenticatedRequest requestData)
            throws AuthenticationException {
        return directReqV1Authorized(url, requestData);
    }

    private String directReqV1Authorized(String url, final VeSyncAuthenticatedRequest requestData)
            throws AuthenticationException {
        try {
            final HttpClient client = httpClient;
            if (client == null) {
                throw new AuthenticationException("No HTTP Client");
            }

            @Nullable
            VeSyncUserSession session = loggedInSession;
            if (session != null && !url.startsWith(session.getServerUrl())) {
                url = session.getServerUrl() + url;
            }

            Request request = client.newRequest(url).method(requestData.httpMethod).timeout(RESPONSE_TIMEOUT_SEC,
                    TimeUnit.SECONDS);

            // No headers for login
            request.content(new StringContentProvider(VeSyncConstants.GSON.toJson(requestData)));

            logger.debug("{} @ {} with content\r\n{}", requestData.httpMethod, url,
                    VeSyncConstants.GSON.toJson(requestData));

            request.header(HttpHeader.CONTENT_TYPE, "application/json; utf-8");

            ContentResponse response = request.send();
            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                VeSyncResponse commResponse = VeSyncConstants.GSON.fromJson(response.getContentAsString(),
                        VeSyncResponse.class);

                if (commResponse != null && (commResponse.isMsgSuccess() || commResponse.isMsgDeviceOffline())) {
                    logger.debug("Got OK response {}", response.getContentAsString());
                    return response.getContentAsString();
                } else {
                    logger.debug("Got FAILED response {}", response.getContentAsString());
                    throw new AuthenticationException("Invalid JSON response from login");
                }
            } else {
                logger.debug("HTTP Response Code: {}", response.getStatus());
                logger.debug("HTTP Response Msg: {}", response.getReason());
                throw new AuthenticationException(
                        "HTTP response " + response.getStatus() + " - " + response.getReason());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new AuthenticationException(e);
        }
    }

    public synchronized void login(final @Nullable String username, final @Nullable String password,
            final @Nullable String timezone) throws AuthenticationException {
        if (username == null || password == null || timezone == null) {
            loggedInSession = null;
            return;
        }
        try {
            loggedInSession = processLogin(username, password, timezone).getUserSession();
        } catch (final AuthenticationException ae) {
            loggedInSession = null;
            throw ae;
        }
    }

    public void updateBridgeData(final VeSyncBridgeHandler bridge) {
        bridge.handleNewUserSession(loggedInSession);
    }

    private class LoginHelper {

        private HttpClient client;
        private String authorizeCode;
        private String bizToken;
        private String regionalServerUrl;
        private String userCurrentRegion;
        private String userCountryCode;

        private String token;
        private String accountId;
        private String acceptLanguage;
        private String registerTime;

        private LoginHelper(HttpClient client) {
            this.client = client;
            this.authorizeCode = "";
            this.bizToken = "";
            this.regionalServerUrl = "smartapi.vesync.com";
            this.userCurrentRegion = "US";
            this.userCountryCode = "";
            this.token = "";
            this.accountId = "";
            this.acceptLanguage = "";
            this.registerTime = "";
        }

        private boolean requestAuthToken(final String username, final String password)
                throws ExecutionException, InterruptedException, TimeoutException, AuthenticationException {
            final Request request = client
                    .newRequest("https://smartapi.vesync.com/globalPlatform/api/accountAuth/v1/authByPWDOrOTM")
                    .method(HttpMethod.POST).timeout(RESPONSE_TIMEOUT_SEC, TimeUnit.SECONDS);

            request.header(HttpHeader.CONTENT_TYPE, "application/json");

            request.content(new StringContentProvider(
                    VeSyncConstants.GSON.toJson(new VeSyncAuthTokenRequest(username, password, ""))));

            final ContentResponse response = request.send();

            // The 200 - OK confirms the server processed the request irrelevant of whether the credentials are any
            // good.
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                return false;
            }

            VeSyncAuthTokenResponse resp = VeSyncConstants.GSON.fromJson(response.getContentAsString(),
                    VeSyncAuthTokenResponse.class);

            if (resp != null && resp.isMsgSuccess()) {
                // Check for account lockout scenario
                if (resp.result.accountLockTimeInSec != null && resp.result.accountLockTimeInSec > 0) {
                    logger.warn("Account locked out for {} seconds", resp.result.accountLockTimeInSec);
                    throw new AuthenticationException(
                            "Account locked out for " + resp.result.accountLockTimeInSec + " seconds");
                }

                this.authorizeCode = resp.result.authorizeCode;
                this.registerTime = resp.result.registerTime;
            } else {
                return false;
            }
            return true;
        }

        private boolean loginByAuthorizeCode()
                throws ExecutionException, InterruptedException, TimeoutException, AuthenticationException {
            final Request request = client
                    .newRequest("https://smartapi.vesync.com/user/api/accountManage/v1/loginByAuthorizeCode4Vesync")
                    .method(HttpMethod.POST).timeout(RESPONSE_TIMEOUT_SEC, TimeUnit.SECONDS);

            request.header(HttpHeader.CONTENT_TYPE, "application/json");

            request.content(new StringContentProvider(
                    VeSyncConstants.GSON.toJson(new VeSyncAuthLoginWithAuthorizeCodeVeSync(this.authorizeCode, ""))));

            final ContentResponse response = request.send();
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                return false;
            }

            final VeSyncAuthLoginWithAuthorizeCodeVeSyncResponse result = VeSyncConstants.GSON
                    .fromJson(response.getContentAsString(), VeSyncAuthLoginWithAuthorizeCodeVeSyncResponse.class);

            if (result == null) {
                return false;
            }

            this.userCurrentRegion = result.result.currentRegion;
            this.userCountryCode = result.result.countryCode;
            this.acceptLanguage = result.result.acceptLanguage;

            if (result.isMsgSuccess()) {
                this.token = result.result.token;
                this.accountId = result.result.accountID;
                return true;
            }

            // It is very likely now there is a redirect to an alternative data center suitable for the users region
            if (result.msg.contains("cross region error")) {
                // We need to determine the correct URL that goes to the data center serving that region
                String hostingUrl = "smartapi.vesync.com";
                switch (result.result.currentRegion) {
                    case "EU":
                        hostingUrl = EU_SERVER_ADDRESS;
                        break;
                    case "US":
                    default:
                        hostingUrl = US_SERVER_ADDRESS;
                        break;
                }
                this.regionalServerUrl = hostingUrl;

                // We will need the biz token for the transfer to a new region
                this.bizToken = result.result.bizToken;
                return loginRegionalRedirectByAuthorizeCode();
            }
            return false;
        }

        private boolean loginRegionalRedirectByAuthorizeCode()
                throws ExecutionException, InterruptedException, TimeoutException, AuthenticationException {
            final String redirectedRegion = PROTOCOL + this.regionalServerUrl
                    + "/user/api/accountManage/v1/loginByAuthorizeCode4Vesync";

            final Request request = client.newRequest(redirectedRegion).method(HttpMethod.POST)
                    .timeout(RESPONSE_TIMEOUT_SEC, TimeUnit.SECONDS);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");

            VeSyncAuthLoginWithAuthorizeCodeVeSyncRegionChange regionChange = new VeSyncAuthLoginWithAuthorizeCodeVeSyncRegionChange(
                    this.authorizeCode, this.userCountryCode, this.bizToken);

            request.content(new StringContentProvider(VeSyncConstants.GSON.toJson(regionChange)));

            final ContentResponse response = request.send();

            final VeSyncAuthLoginWithAuthorizeCodeVeSyncResponse result = VeSyncConstants.GSON
                    .fromJson(response.getContentAsString(), VeSyncAuthLoginWithAuthorizeCodeVeSyncResponse.class);

            if (result == null) {
                return false;
            }

            if (result.isMsgSuccess()) {
                this.token = result.result.token;
                this.accountId = result.result.accountID;
                return true;
            }
            if (result.msg.contains("cross region error")) {
                throw new AuthenticationException("Code update required - region not supported - " + userCurrentRegion);
            }
            return false;
        }
    }

    private VeSyncLoginResponse processLogin(String username, String password, String timezone)
            throws AuthenticationException {
        try {
            final HttpClient client = httpClient;
            if (client == null) {
                throw new AuthenticationException("No HTTP Client");
            }

            final LoginHelper loginHelper = new LoginHelper(client);

            if (!loginHelper.requestAuthToken(username, password)) {
                // We cant continue if we don't have the authorizeCode parameter
                throw new AuthenticationException("Invalid username or password");
            }

            if (!loginHelper.loginByAuthorizeCode()) {
                throw new AuthenticationException("Invalid username or password");
            }
            VeSyncLoginResponse response = new VeSyncLoginResponse();
            response.result = new VeSyncUserSession();
            response.result.token = loginHelper.token;
            response.result.accountId = loginHelper.accountId;
            response.result.acceptLanguage = loginHelper.acceptLanguage;
            response.result.countryCode = loginHelper.userCountryCode;
            response.result.registerTime = loginHelper.registerTime;
            response.result.serverUrl = PROTOCOL + loginHelper.regionalServerUrl;

            return response;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new AuthenticationException(e);
        }
    }
}
