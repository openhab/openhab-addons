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

import static org.openhab.binding.vesync.internal.VeSyncConstants.EMPTY_STRING;
import static org.openhab.binding.vesync.internal.dto.requests.VeSyncProtocolConstants.*;

import java.net.HttpURLConnection;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
import org.openhab.binding.vesync.internal.dto.responses.VeSyncAuthLoginWithAuthorizeCodeVeSyncResponse;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncAuthTokenResponse;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncLoginResponse;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncUserSession;
import org.openhab.binding.vesync.internal.exceptions.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
class LoginAuthV2Helper {

    private final Logger logger = LoggerFactory.getLogger(LoginAuthV2Helper.class);
    private final HttpClient client;
    private final VeSyncUserSession loginData = new VeSyncUserSession();

    private static final String US_TOKEN_REQ_URL = US_SERVER + "/globalPlatform/api/accountAuth/v1/authByPWDOrOTM";
    private static final String US_AUTH_BY_TOKEN = US_SERVER + "/user/api/accountManage/v1/loginByAuthorizeCode4Vesync";
    private static final String AUTH_CONTENT_TYPE = "application/json";

    private String authorizeCode;
    private String bizToken;
    private String userCurrentRegion;

    protected LoginAuthV2Helper(HttpClient client) {
        this.client = client;

        // These parameters are utilised during the sequence
        authorizeCode = EMPTY_STRING;
        bizToken = EMPTY_STRING;
        userCurrentRegion = "US";

        // To reduce any potential issues preset this to the global instance
        loginData.serverUrl = US_SERVER;
    }

    protected VeSyncLoginResponse getVeSyncLoginResponse() {
        final VeSyncLoginResponse simResponse = new VeSyncLoginResponse();
        simResponse.result = loginData;
        return simResponse;
    }

    protected boolean requestAuthToken(final String username, final String password)
            throws ExecutionException, InterruptedException, TimeoutException, AuthenticationException {
        final Request request = client.newRequest(US_TOKEN_REQ_URL).method(HttpMethod.POST)
                .timeout(VeSyncV2ApiHelper.RESPONSE_TIMEOUT_SEC, TimeUnit.SECONDS);

        request.header(HttpHeader.CONTENT_TYPE, AUTH_CONTENT_TYPE);

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

            authorizeCode = resp.result.authorizeCode;
            loginData.registerTime = resp.result.registerTime;
        } else if (resp != null && resp.code != null && resp.msg != null) {
            final String msg = resp.msg.toLowerCase(Locale.ENGLISH);
            if ("-11202129".equals(resp.code) || msg.contains("the account does not exist")) {
                throw new AuthenticationException("The account does not exist");
            } else if ("-11201129".equals(resp.code) || msg.contains("account or password incorrect")) {
                throw new AuthenticationException("Account or password incorrect");
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    protected boolean loginByAuthorizeCode()
            throws ExecutionException, InterruptedException, TimeoutException, AuthenticationException {
        final Request request = client.newRequest(US_AUTH_BY_TOKEN).method(HttpMethod.POST)
                .timeout(VeSyncV2ApiHelper.RESPONSE_TIMEOUT_SEC, TimeUnit.SECONDS);

        request.header(HttpHeader.CONTENT_TYPE, AUTH_CONTENT_TYPE);

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

        if (result.result != null) {
            userCurrentRegion = result.result.currentRegion;
            loginData.countryCode = result.result.countryCode;
            loginData.acceptLanguage = result.result.acceptLanguage;

            if (result.isMsgSuccess()) {
                loginData.token = result.result.token;
                loginData.accountId = result.result.accountID;
                return true;
            }

            // It is very likely now there is a redirect to an alternative data center suitable for the users region
            if (result.msg != null && result.msg.toLowerCase(Locale.ENGLISH).contains("cross region error")) {
                // We need to determine the correct URL that goes to the data center serving that region
                String hostingUrl;
                switch (result.result.currentRegion) {
                    case "EU":
                        hostingUrl = EU_SERVER_ADDRESS;
                        break;
                    case "US":
                    default:
                        hostingUrl = US_SERVER_ADDRESS;
                        break;
                }
                loginData.serverUrl = PROTOCOL + "://" + hostingUrl;

                // We will need the biz token for the transfer to a new region
                this.bizToken = result.result.bizToken;
                return loginRegionalRedirectByAuthorizeCode();
            }
        }
        return false;
    }

    private boolean loginRegionalRedirectByAuthorizeCode()
            throws ExecutionException, InterruptedException, TimeoutException, AuthenticationException {
        final String redirectedRegion = loginData.serverUrl + "/user/api/accountManage/v1/loginByAuthorizeCode4Vesync";

        final Request request = client.newRequest(redirectedRegion).method(HttpMethod.POST)
                .timeout(VeSyncV2ApiHelper.RESPONSE_TIMEOUT_SEC, TimeUnit.SECONDS);
        request.header(HttpHeader.CONTENT_TYPE, AUTH_CONTENT_TYPE);

        VeSyncAuthLoginWithAuthorizeCodeVeSyncRegionChange regionChange = new VeSyncAuthLoginWithAuthorizeCodeVeSyncRegionChange(
                this.authorizeCode, loginData.countryCode, this.bizToken);

        request.content(new StringContentProvider(VeSyncConstants.GSON.toJson(regionChange)));

        final ContentResponse response = request.send();

        final VeSyncAuthLoginWithAuthorizeCodeVeSyncResponse result = VeSyncConstants.GSON
                .fromJson(response.getContentAsString(), VeSyncAuthLoginWithAuthorizeCodeVeSyncResponse.class);

        if (result == null) {
            return false;
        }

        if (result.isMsgSuccess()) {
            loginData.token = result.result.token;
            loginData.accountId = result.result.accountID;
            return true;
        }
        if (result.msg != null && result.msg.toLowerCase(Locale.ENGLISH).contains("cross region error")) {
            throw new AuthenticationException("Code update required - region not supported - " + userCurrentRegion);
        }
        return false;
    }
}
