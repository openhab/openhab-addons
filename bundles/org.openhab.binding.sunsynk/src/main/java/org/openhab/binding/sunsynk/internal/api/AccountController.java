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
import java.util.Objects;
import java.util.Properties;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.sunsynk.internal.api.dto.APIdata;
import org.openhab.binding.sunsynk.internal.api.dto.Client;
import org.openhab.binding.sunsynk.internal.api.dto.Details;
import org.openhab.binding.sunsynk.internal.api.dto.Inverter;
import org.openhab.binding.sunsynk.internal.api.dto.SunSynkLogin;
import org.openhab.binding.sunsynk.internal.api.dto.TokenRefresh;
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
    private static final int TIMEOUT_IN_MS = 4000;
    private final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private static final String BEARER_TYPE = "Bearer ";
    private Client sunAccount = new Client();

    public AccountController() {
    }

    /**
     * Authenticates a Sunsynk Connect API account using a username and password.
     * 
     * @param username The username you use with Sunsynk Connect App
     * @param password The password you use with Sunsynk Connect App
     * @throws SunSynkAuthenticateException
     * @throws SunSynkTokenException
     */
    public void authenticate(String username, String password)
            throws SunSynkAuthenticateException, SunSynkTokenException {
        String payload = makeLoginBody(username, password);
        sendHttp(payload);
    }

    /**
     * Checks if a Sunsynk Connect account token is expired and gets a new one if required.
     * 
     * @param username
     * @throws SunSynkAuthenticateException
     * @throws SunSynkTokenException
     */
    public void refreshAccount(String username) throws SunSynkAuthenticateException, SunSynkTokenException {
        Long expiresIn = this.sunAccount.getExpiresIn();
        Long issuedAt = this.sunAccount.getIssuedAt();
        if ((issuedAt + expiresIn) - Instant.now().getEpochSecond() > 30) { // > 30 seconds
            logger.debug("Account configuration token not expired.");
            return;
        }
        logger.debug("Account configuration token expired : {}", this.sunAccount.getData().toString());
        String payload = makeRefreshBody(username, this.sunAccount.getRefreshTokenString());
        sendHttp(payload);
    }

    @SuppressWarnings("unused") // We need client to be nullable. Then we check for null. Without this compiler warns of
                                // unsed block under null check
    private void sendHttp(String payload) throws SunSynkAuthenticateException, SunSynkTokenException {
        Gson gson = new Gson();
        String response = "";
        String httpsURL = makeLoginURL("oauth/token");
        Properties headers = new Properties();
        headers.setProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        try {
            response = HttpUtil.executeUrl(HttpMethod.POST.asString(), httpsURL, headers, stream,
                    MediaType.APPLICATION_JSON, TIMEOUT_IN_MS);

            @Nullable
            Client client = gson.fromJson(response, Client.class);
            if (client == null) {
                throw new SunSynkAuthenticateException(
                        "Sun Synk Account could not be authenticated: Try re-enabling account");
            }
            this.sunAccount = client;
        } catch (IOException | JsonSyntaxException e) {
            throw new SunSynkAuthenticateException("Sun Synk Account could not be authenticated", e);
        }
        if (this.sunAccount.getCode() == 102) {
            logger.debug("Sun Synk Account could not be authenticated: {}.", this.sunAccount.getMsg());
            throw new SunSynkAuthenticateException(
                    "Sun Synk Accountfailed to authenticate: Check your password or email.");
        }
        if (this.sunAccount.getStatus() == 404) {
            logger.debug("Sun Synk Account could not be authenticated: 404 {} {}.", this.sunAccount.getError(),
                    this.sunAccount.getPath());
            throw new SunSynkAuthenticateException("Sun Synk Accountfailed to authenticate: 404 Not Found.");
        }
        getToken();
    }

    private void getToken() throws SunSynkAuthenticateException {
        APIdata data = this.sunAccount.getData();
        APIdata.staticAccessToken = data.getAccessToken();
    }

    /**
     * Discovers a list of all inverter tied to a Sunsynk Connect Account
     * 
     * @return List of connected inveters
     * @throws SunSynkInverterDiscoveryException
     */
    @SuppressWarnings("unused")
    public ArrayList<Inverter> getDetails() throws SunSynkInverterDiscoveryException {
        Details output = new Details();
        ArrayList<Inverter> inverters = new ArrayList<>();
        try {
            Gson gson = new Gson();
            Properties headers = new Properties();
            String response = "";

            String httpsURL = makeLoginURL(
                    "api/v1/inverters?page=1&limit=10&total=0&status=-1&sn=&plantId=&type=-2&softVer=&hmiVer=&agentCompanyId=-1&gsn=");
            headers.setProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            headers.setProperty(HttpHeaders.AUTHORIZATION, BEARER_TYPE + APIdata.staticAccessToken);
            response = HttpUtil.executeUrl(HttpMethod.GET.asString(), httpsURL, headers, null,
                    MediaType.APPLICATION_JSON, TIMEOUT_IN_MS);
            @Nullable
            Details maybeDeats = gson.fromJson(response, Details.class);
            if (maybeDeats == null) {
                throw new SunSynkInverterDiscoveryException("Failed to discover Inverters");
            }
            output = maybeDeats;
        } catch (IOException | JsonSyntaxException e) {
            if (logger.isDebugEnabled()) {
                String message = Objects.requireNonNullElse(e.getMessage(), "unkown error message");
                Throwable cause = e.getCause();
                String causeMessage = cause != null ? Objects.requireNonNullElse(cause.getMessage(), "unkown cause")
                        : "unkown cause";
                logger.debug("Error attempting to find inverters registered to account: Msg = {}. Cause = {}.", message,
                        causeMessage);
            }
            throw new SunSynkInverterDiscoveryException("Failed to discover Inverters", e);
        }
        inverters = output.getInverters(APIdata.staticAccessToken);
        return inverters;
    }

    private static String makeLoginURL(String path) {
        return "https://api.sunsynk.net" + "/" + path;
    }

    private static String makeLoginBody(String username, String password) {
        Gson gson = new Gson();
        SunSynkLogin login = new SunSynkLogin(username, password);
        return gson.toJson(login);
    }

    private static String makeRefreshBody(String username, String refreshToken) {
        Gson gson = new Gson();
        TokenRefresh refresh = new TokenRefresh(username, refreshToken);
        return gson.toJson(refresh);
    }

    @Override
    public String toString() {
        try {
            return this.sunAccount.getData().toString();
        } catch (SunSynkAuthenticateException e) {
            return "Tried to print client data, value is null.";
        }
    }
}
