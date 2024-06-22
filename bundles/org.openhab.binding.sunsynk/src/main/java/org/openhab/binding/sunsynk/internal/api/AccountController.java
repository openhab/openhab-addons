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

package org.openhab.binding.sunsynk.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sunsynk.internal.api.dto.APIdata;
import org.openhab.binding.sunsynk.internal.api.dto.Client;
import org.openhab.binding.sunsynk.internal.api.dto.Details;
import org.openhab.binding.sunsynk.internal.api.dto.Inverter;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkAuthenticateException;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkInverterDiscoveryException;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkTokenException;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AccountController} is the internal class for a Sunsynk Connect
 * Account.
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class AccountController {
    private final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private @Nullable Client sunAccount = new Client();

    public AccountController() {
    }

    public void authenticate(String username, String password)
            throws SunSynkAuthenticateException, SunSynkTokenException {
        String payload = makeLoginBody(username, password);
        sendHttp(payload);
    }

    public void refreshAccount(String username) throws SunSynkAuthenticateException, SunSynkTokenException {
        if (sunAccount == null) {
            throw new SunSynkAuthenticateException(
                    "Sun Synk Account could not be authenticated: Try re-enabling account");
        } else if (!sunAccount.dataIsNotNull()) {
            throw new SunSynkAuthenticateException(
                    "Sun Synk Account could not be authenticated: Try re-enabling account");
        }
        Long expires_in = this.sunAccount.getExpiresIn();
        Long issued_at = this.sunAccount.getIssuedAt();
        if ((issued_at + expires_in) - Instant.now().getEpochSecond() > 30) { // > 30 seconds
            logger.debug("Account configuration token not expired.");
            return;
        }
        logger.debug("Account configuration token expired : {}", this.sunAccount.getData().toString());
        String payload = makeRefreshBody(username, this.sunAccount.getRefreshTokenString());
        sendHttp(payload);
    }

    private void sendHttp(String payload) throws SunSynkAuthenticateException, SunSynkTokenException {
        Gson gson = new Gson();
        String response = "";
        String httpsURL = makeLoginURL("oauth/token");
        Properties headers = new Properties();
        headers.setProperty("Accept", "application/json");
        headers.setProperty("Requester", "www.openhab.org"); // optional
        InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        try {
            response = HttpUtil.executeUrl("POST", httpsURL, headers, stream, "application/json", 2000);
            this.sunAccount = gson.fromJson(response, Client.class);
        } catch (IOException | JsonSyntaxException e) {
            throw new SunSynkAuthenticateException("Sun Synk Account could not be authenticated", e);
        }
        if (this.sunAccount == null) {
            throw new SunSynkAuthenticateException("Sun Synk Account failed to get a response.");
        }
        if (this.sunAccount.getCode() == 102) {
            logger.info("Sun Synk Account could not be authenticated: {}.", this.sunAccount.getMsg());
            throw new SunSynkAuthenticateException(
                    "Sun Synk Accountfailed to authenticate: Check your password or email.");
        }
        if (this.sunAccount.getStatus() == 404) {
            logger.info("Sun Synk Account could not be authenticated: 404 {} {}.", this.sunAccount.getError(),
                    this.sunAccount.getPath());
            throw new SunSynkAuthenticateException("Sun Synk Accountfailed to authenticate: 404 Not Found.");
        }
        getToken();
    }

    private void getToken() throws SunSynkTokenException {
        if (this.sunAccount.getData().getAccessToken().isEmpty()) {
            throw new SunSynkTokenException("Sun Synk Account failed to get token");
        }
        APIdata.static_access_token = this.sunAccount.getData().getAccessToken();
    }

    public ArrayList<Inverter> getDetails() throws SunSynkInverterDiscoveryException {
        @Nullable
        Details output = new Details();
        ArrayList<Inverter> inverters = new ArrayList<>();
        try {
            Gson gson = new Gson();
            Properties headers = new Properties();
            String response = "";
            String httpsURL = makeLoginURL(
                    "api/v1/inverters?page=1&limit=10&total=0&status=-1&sn=&plantId=&type=-2&softVer=&hmiVer=&agentCompanyId=-1&gsn=");
            headers.setProperty("Accept", "application/json");
            headers.setProperty("Authorization", "Bearer " + APIdata.static_access_token);
            headers.setProperty("Requester", "www.openhab.org"); // optional
            response = HttpUtil.executeUrl("GET", httpsURL, headers, null, "application/json", 2000);
            output = gson.fromJson(response, Details.class);
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Error attempting to find inverters registered to account: Msg = {}. Cause = {}.",
                    e.getMessage().toString(), e.getCause().toString());
            throw new SunSynkInverterDiscoveryException("Failed to discover Inverters", e);
        }
        inverters = output.getInverters(APIdata.static_access_token);
        if (!inverters.isEmpty() | inverters != null) {
            return inverters;
        }
        return inverters;
    }

    private static String makeLoginURL(String path) {
        return "https://api.sunsynk.net" + "/" + path;
    }

    private static String makeLoginBody(String username, String password) {
        return "{\"username\": \"" + username + "\", \"password\": \"" + password
                + "\", \"grant_type\": \"password\", \"client_id\": \"csp-web\"}";
    }

    private static String makeRefreshBody(String username, String refresh_token) {
        return "{\"grant_type\": \"refresh_token\", \"username\": \"" + username + "\", \"refresh_token\": \""
                + refresh_token + "\", \"client_id\": \"csp-web\"}";
    }

    @Override
    public String toString() {
        return this.sunAccount.getData().toString();
    }
}
