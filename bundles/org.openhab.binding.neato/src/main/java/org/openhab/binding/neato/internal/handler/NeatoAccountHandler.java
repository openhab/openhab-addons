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
package org.openhab.binding.neato.internal.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.neato.internal.classes.BeehiveAuthentication;
import org.openhab.binding.neato.internal.classes.NeatoAccountInformation;
import org.openhab.binding.neato.internal.classes.Robot;
import org.openhab.binding.neato.internal.config.NeatoAccountConfig;
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

/**
 * Bridge handler to manage Neato Cloud Account
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class NeatoAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NeatoAccountHandler.class);

    public NeatoAccountHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    private List<Robot> sendGetRobots(String accessToken) {
        Properties headers = new Properties();
        headers.setProperty("Accept", "application/vnd.neato.nucleo.v1");

        headers.setProperty("Authorization", "Token token=" + accessToken);

        try {
            String resultString = HttpUtil.executeUrl("GET", "https://beehive.neatocloud.com/dashboard", headers, null,
                    "application/json", 20000);

            Gson gson = new Gson();
            NeatoAccountInformation accountInformation = gson.fromJson(resultString, NeatoAccountInformation.class);

            logger.debug("Result from WS call to get Robots: {}", resultString);

            return accountInformation.getRobots();
        } catch (IOException e) {
            logger.debug("Error attempting to find robots registered to account", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to find robots registered to account");
            return new ArrayList<>();
        }
    }

    public @NonNull List<Robot> getRobotsFromNeato() {
        logger.debug("Attempting to find robots tied to account");
        NeatoAccountConfig accountConfig = getConfigAs(NeatoAccountConfig.class);
        String accessToken = authenticate(accountConfig.getEmail(), accountConfig.getPassword());

        if (accessToken != null) {
            return sendGetRobots(accessToken);
        }

        return new ArrayList<>();
    }

    private String authenticate(String username, String password) {
        Gson gson = new Gson();

        AuthRequest req = new AuthRequest(username, password, "ios",
                new BigInteger(130, new SecureRandom()).toString(64));

        String authenticationResponse = "";
        try {
            authenticationResponse = sendAuthRequestToNeato(gson.toJson(req));
        } catch (IOException e) {
            logger.debug("Error when sending Authentication request to Neato.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error when sending Authentication request to Neato.");
        }

        BeehiveAuthentication authenticationObject = gson.fromJson(authenticationResponse, BeehiveAuthentication.class);
        return authenticationObject.getAccessToken();
    }

    private String sendAuthRequestToNeato(String data) throws IOException {
        Properties headers = new Properties();
        headers.setProperty("Accept", "application/vnd.neato.nucleo.v1");

        InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        String resultString = HttpUtil.executeUrl("POST", "https://beehive.neatocloud.com/sessions", headers, stream,
                "application/json", 20000);

        logger.debug("Authentication Response: {}", resultString);

        return resultString;
    }

    private static class AuthRequest {
        private String email;
        private String password;
        private String os;
        private String token;

        public AuthRequest(String email, String password, String os, String token) {
            this.email = email;
            this.password = password;
            this.os = os;
            this.token = token;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public String getOs() {
            return os;
        }

        public String getToken() {
            return token;
        }
    }
}
