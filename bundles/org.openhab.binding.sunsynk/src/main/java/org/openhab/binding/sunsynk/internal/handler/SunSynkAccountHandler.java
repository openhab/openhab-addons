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
package org.openhab.binding.sunsynk.internal.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sunsynk.internal.api.dto.APIdata;
import org.openhab.binding.sunsynk.internal.api.dto.Client;
import org.openhab.binding.sunsynk.internal.api.dto.Details;
import org.openhab.binding.sunsynk.internal.classes.Inverter;
import org.openhab.binding.sunsynk.internal.config.SunSynkAccountConfig;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link SunSynkAccountHandler} is responsible for handling the SunSynk Account Bridge
 *
 *
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SunSynkAccountHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SunSynkAccountHandler.class);
    private @Nullable Client sunAccount = new Client();
    private @Nullable ScheduledFuture<?> discoverApiKeyJob;

    public SunSynkAccountHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("SunSynk Handler Intialised attempting to retrieve configuration");
        startDiscoverApiKeyJob();
    }

    public void setBridgeOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    public List<Inverter> getInvertersFromSunSynk() {
        logger.debug("Attempting to find inverters tied to account");
        Details sunAccountDetails = getDetails(APIdata.static_access_token);
        ArrayList<Inverter> inverters = sunAccountDetails.getInverters(APIdata.static_access_token);
        if (!inverters.isEmpty() | inverters != null) {
            return inverters;
        }
        return new ArrayList<>();
    }

    private void startDiscoverApiKeyJob() {
        if (discoverApiKeyJob == null || discoverApiKeyJob.isCancelled()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    logger.debug("Starting account discovery job");
                    configAccount();
                    logger.debug("Doneaccount discoveryjob");
                }
            };
            discoverApiKeyJob = scheduler.schedule(runnable, 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing sunsynk bridge handler.");

        if (discoverApiKeyJob != null && !discoverApiKeyJob.isCancelled()) {
            discoverApiKeyJob.cancel(true);
            discoverApiKeyJob = null;
        }
    }

    private @Nullable Details getDetails(String access_token) {
        try {
            Gson gson = new Gson();
            Properties headers = new Properties();
            String response = "";
            String httpsURL = makeLoginURL(
                    "api/v1/inverters?page=1&limit=10&total=0&status=-1&sn=&plantId=&type=-2&softVer=&hmiVer=&agentCompanyId=-1&gsn=");
            headers.setProperty("Accept", "application/json");
            headers.setProperty("Authorization", "Bearer " + access_token);
            response = HttpUtil.executeUrl("GET", httpsURL, headers, null, "application/json", 2000);
            @Nullable
            Details output = gson.fromJson(response, Details.class);
            return output;
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Error attempting to find inverters registered to account", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to find inverters registered to account");
            return new Details();
        }
    }

    public void configAccount() {
        SunSynkAccountConfig accountConfig = getConfigAs(SunSynkAccountConfig.class);
        this.sunAccount = authenticate(accountConfig.getEmail(), accountConfig.getPassword());
        String newToken = this.sunAccount.getData().getAccessToken();
        if (newToken.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE);
            logger.debug("Account fialed to authenticate, likely a certificate path or password problem.");
            if (this.sunAccount.getCode() == 102) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Check e-mail and password!");
                logger.debug("Looks like its your password or email.");
            }
            return;
        }
        APIdata.static_access_token = newToken;
        updateStatus(ThingStatus.ONLINE);
        logger.debug("Account configuration updated : {}", this.sunAccount.getData().toString());
    }

    public void refreshAccount() {
        Long expires_in = this.sunAccount.getExpiresIn();
        Long issued_at = this.sunAccount.getIssuedAt();
        if ((issued_at + expires_in) - Instant.now().getEpochSecond() > 30) { // 30 seconds
            logger.debug("Account configuration token not expired.");
            return;
        }
        logger.debug("Account configuration token expired : {}", this.sunAccount.getData().toString());
        SunSynkAccountConfig accountConfig = getConfigAs(SunSynkAccountConfig.class);
        String refreshtToken = this.sunAccount.getRefreshTokenString();
        this.sunAccount = refresh(accountConfig.getEmail(), refreshtToken);
        String newToken = this.sunAccount.getData().getAccessToken();
        if (newToken.isEmpty()) {
            logger.debug("Account failed to refresh, likely a certificate path or password problem.");
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        APIdata.static_access_token = newToken;
        logger.debug("Account configuration refreshed : {}", this.sunAccount.getData().toString());
    }

    private @Nullable Client authenticate(String username, String password) {
        Gson gson = new Gson();
        String response = "";
        String httpsURL = makeLoginURL("oauth/token");
        String payload = makeLoginBody(username, password);
        Properties headers = new Properties();
        try {
            headers.setProperty("Accept", "application/json");
            headers.setProperty("Requester", "www.openhab.org"); // optional
            InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
            response = HttpUtil.executeUrl("POST", httpsURL, headers, stream, "application/json", 2000);
            @Nullable
            Client output = gson.fromJson(response, Client.class);
            return output;
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Error attempting to autheticate account", e.getCause());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to authenticate account");
            return new Client();
        }
    }

    private @Nullable Client refresh(String username, String refreshToken) {
        Gson gson = new Gson();
        String response = "";
        String httpsURL = makeLoginURL("oauth/token");
        String payload = makeRefreshBody(username, refreshToken);
        Properties headers = new Properties();
        try {
            headers.setProperty("Accept", "application/json");
            headers.setProperty("Requester", "www.openhab.org"); // optional
            InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
            response = HttpUtil.executeUrl("POST", httpsURL, headers, stream, "application/json", 2000);
            @Nullable
            Client output = gson.fromJson(response, Client.class);
            return output;
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Error attempting to autheticate account", e.getCause());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to authenticate account");
            return new Client();
        }
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
}
