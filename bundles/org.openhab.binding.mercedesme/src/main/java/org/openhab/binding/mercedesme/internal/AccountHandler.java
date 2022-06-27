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
import org.openhab.core.storage.Storage;
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
    private final Storage<String> storage;
    private final String tokenStorageKey;

    public AccountHandler(Bridge bridge, HttpClientFactory hcf, OAuthFactory oaf, Storage<String> storage) {
        super(bridge);

        logger.info("Storage {}", storage.getClass().getName());
        tokenStorageKey = bridge.getUID() + ":token";
        httpClientFactory = hcf;
        oAuthFactory = oaf;
        this.storage = storage;
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
            String callbackUrl = Utils.getCallbackAddress(config.get().callbackIP, config.get().callbackPort);
            thing.setProperty("callbackUrl", callbackUrl);

            server = Optional.of(new CallbackServer(this, httpClientFactory.getCommonHttpClient(), oAuthFactory,
                    config.get(), callbackUrl));

            if (storage.containsKey(tokenStorageKey)) {
                String tokenSerial = storage.get(tokenStorageKey);
                if (tokenSerial != null) {
                    AccessTokenResponse atr = (AccessTokenResponse) Utils.fromString(tokenSerial);
                    server.get().setToken(atr);
                } else {
                    logger.info("Token serial null in storage");
                }
                logger.info("Token restored from storage {}", tokenStorageKey);
            } else {
                logger.info("Token not found in storage");
            }

            server.get().start();
            String token = server.get().getToken();
            if (!token.equals(Constants.EMPTY)) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "Manual Authorization needed at " + callbackUrl);
            }
        }
    }

    private void handleConfig() {
        // Handle not initialized Thing with "best guess values"
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        logger.info("Config delivered {}", config.get().toString());
        Configuration updateConfig = super.editConfiguration();
        logger.info("Config to edit {}", updateConfig);
        if (!updateConfig.containsKey("callbackPort")) {
            updateConfig.put("callbackPort", Utils.getFreePort());
        } else {
            Utils.addPort(config.get().callbackPort);
        }
        if (!updateConfig.containsKey("callbackIP")) {
            updateConfig.put("callbackIP", Utils.getCallbackIP());
        }
        super.updateConfiguration(updateConfig);
    }

    private boolean isConfigValid() {
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        if (!config.isEmpty()) {
            if (!config.get().callbackIP.equals(Constants.NOT_SET) && config.get().callbackPort != -1
                    && !config.get().clientId.equals(Constants.NOT_SET)
                    && !config.get().clientSecret.equals(Constants.NOT_SET)) {
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
        logger.info("{} received new Access Token {}", config.get().callbackPort, tokenResponse.toString());
        if (!tokenResponse.isExpired(LocalDateTime.now(), 10)) {
            updateStatus(ThingStatus.ONLINE);
        }
        if (tokenResponse.getRefreshToken() != null) {
            logger.info("{} store token in {}", config.get().callbackPort, tokenStorageKey);
            String tokenSerial = Utils.toString(tokenResponse);
            storage.put(tokenStorageKey, tokenSerial);
        }
    }

    public String getToken() {
        if (server.isEmpty()) {
            return Constants.EMPTY;
        } else {
            return server.get().getToken();
        }
    }

    public String getImageApiKey() {
        return config.get().imageApiKey;
    }

    @Override
    public String toString() {
        return Integer.toString(config.get().callbackPort);
    }
}
