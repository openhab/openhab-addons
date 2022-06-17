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
package org.openhab.binding.mercedesme.internal;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.server.CallbackServer;
import org.openhab.binding.mercedesme.internal.server.Utils;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccountHandler} takes care of the valid authorization for the user account
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler implements AccessTokenRefreshListener {
    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private HttpClientFactory httpClientFactory;
    private Optional<CallbackServer> server = Optional.empty();
    private Optional<AccountConfiguration> config = Optional.empty();

    private final OAuthFactory oAuthFactory;

    public AccountHandler(Bridge bridge, HttpClientFactory hcf, OAuthFactory oaf) {
        super(bridge);
        httpClientFactory = hcf;
        oAuthFactory = oaf;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands available
    }

    @Override
    public void initialize() {
        logger.info("Initialize");
        updateStatus(ThingStatus.UNKNOWN);
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        handleConfig();
        if (!isConfigValid()) {
            logger.info("Config not valid");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            logger.info("Config valid - start server and start auth");
            server = Optional
                    .of(new CallbackServer(this, httpClientFactory.getCommonHttpClient(), oAuthFactory, config.get()));
            super.updateProperty("authorizationUrl", server.get().getAuthorizationUrl());
            if (!config.get().token.equals(Constants.EMPTY)) {
                try {
                    AccessTokenResponse atr = (AccessTokenResponse) Utils.fromString(config.get().token);
                    server.get().setToken(atr);
                } catch (ClassNotFoundException | IOException e) {
                    logger.warn("Unable to deserialize token {}", config.get().token);
                }
            }
            server.get().start();
            String token = server.get().getToken();
            if (!token.equals(Constants.EMPTY)) {
                updateStatus(ThingStatus.ONLINE);
            } // else: status update done in authorization callback
        }
    }

    private void handleConfig() {
        // Handle not initialized Thing with "best guess values"
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        int port = config.get().callbackPort;
        String ip = config.get().callbackIP;
        String scope = config.get().scope;

        logger.info("Config delivered {}", config.get().toString());
        Configuration updateConfig = super.editConfiguration();
        logger.info("Config to edit {}", updateConfig);
        if (port == -1) {
            port = Utils.getFreePort();
            updateConfig.put("callbackPort", port);
        } else {
            Utils.addPort(port);
        }
        if (Constants.NOT_SET.equals(ip)) {
            ip = Utils.getCallbackIP();
            updateConfig.put("callbackIP", ip);
        }
        if (Constants.NOT_SET.equals(scope)) {
            scope = Constants.SCOPE_EV + Constants.SPACE + Constants.SCOPE_FUEL + Constants.SPACE + Constants.SCOPE_LOCK
                    + Constants.SPACE + Constants.SCOPE_ODO + Constants.SPACE + Constants.SCOPE_OFFLINE
                    + Constants.SPACE + Constants.SCOPE_STATUS;
            updateConfig.put("scope", scope);
        }
        updateConfig.put("callbackUrl", Utils.getCallbackAddress(ip, port));
        super.updateConfiguration(updateConfig);
    }

    private boolean isConfigValid() {
        if (!config.isEmpty()) {
            if (!config.get().callbackIP.equals(Constants.NOT_SET) && config.get().callbackPort != -1
                    && !config.get().clientId.equals(Constants.NOT_SET)
                    && !config.get().clientSecret.equals(Constants.NOT_SET)) {
                // Callback address with port and client data is set - config seems to be valid
                return true;
            }
        } else {
            logger.info("Config is empty");
        }
        return false;
    }

    @Override
    public void dispose() {
        if (!server.isEmpty()) {
            logger.info("Dispose - stop server");
            server.get().stop();
        } else {
            logger.info("Dispose - no server created");
        }
    }

    /**
     * https://next.openhab.org/javadoc/latest/org/openhab/core/auth/client/oauth2/package-summary.html
     */
    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
        if (!tokenResponse.isExpired(LocalDateTime.now(), 10)) {
            updateStatus(ThingStatus.ONLINE);
        }
        if (tokenResponse.getRefreshToken() != null) {
            try {
                String ser = Utils.toString(tokenResponse);
                Configuration c = super.editConfiguration();
                c.put("token", ser);
                super.updateConfiguration(c);
            } catch (IOException e) {
                logger.info("Error serializing token {}", e.getMessage());
            }
        }
    }

    public String getToken() {
        return server.get().getToken();
    }
}
