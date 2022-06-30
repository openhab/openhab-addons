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
    private Optional<String> tokenStorageKey = Optional.empty();
    private final OAuthFactory oAuthFactory;
    private final Storage<String> storage;

    Optional<AccountConfiguration> config = Optional.empty();

    public AccountHandler(Bridge bridge, HttpClientFactory hcf, OAuthFactory oaf, Storage<String> storage) {
        super(bridge);
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
        updateStatus(ThingStatus.UNKNOWN);
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        handleConfig();
        String configValidReason = configValid();
        if (!configValidReason.equals(Constants.EMPTY)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configValidReason);
        } else {
            String callbackUrl = Utils.getCallbackAddress(config.get().callbackIP, config.get().callbackPort);
            thing.setProperty("callbackUrl", callbackUrl);

            server = Optional.of(new CallbackServer(this, httpClientFactory.getCommonHttpClient(), oAuthFactory,
                    config.get(), callbackUrl));
            tokenStorageKey = Optional.of(config.get().clientId + ":token");
            if (storage.containsKey(tokenStorageKey.get())) {
                String tokenSerial = storage.get(tokenStorageKey.get());
                if (tokenSerial != null) {
                    AccessTokenResponse atr = (AccessTokenResponse) Utils.fromString(tokenSerial);
                    server.get().setToken(atr);
                } else {
                    logger.debug("Token cannot be restored from storage - manual authorization needed");
                }
            } else {
                if (!keyMigration()) {
                    logger.debug("Token not found in storage - manual authorization needed");
                }
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
        // if Callback IP and Callback Port are not set => autodetect these values
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        Configuration updateConfig = super.editConfiguration();
        if (!updateConfig.containsKey("callbackPort")) {
            updateConfig.put("callbackPort", Utils.getFreePort());
        } else {
            Utils.addPort(config.get().callbackPort);
        }
        if (!updateConfig.containsKey("callbackIP")) {
            updateConfig.put("callbackIP", Utils.getCallbackIP());
        }
        super.updateConfiguration(updateConfig);
        // get new config after update
        config = Optional.of(getConfigAs(AccountConfiguration.class));
    }

    private String configValid() {
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        if (!config.isEmpty()) {
            if (config.get().callbackIP.equals(Constants.NOT_SET)) {
                return "Callback IP " + Constants.NOT_SET;
            } else if (config.get().callbackPort == -1) {
                return "Callback Port " + Constants.NOT_SET;
            } else if (config.get().clientId.equals(Constants.NOT_SET)) {
                return "Client ID " + Constants.NOT_SET;
            } else if (config.get().clientSecret.equals(Constants.NOT_SET)) {
                return "Client Secret " + Constants.NOT_SET;
            } else {
                return Constants.EMPTY;
            }
        } else {
            logger.debug("Config is empty");
        }
        return Constants.EMPTY;
    }

    @Override
    public void dispose() {
        if (!server.isEmpty()) {
            server.get().stop();
            Utils.removePort(config.get().callbackPort);
        }
    }

    /**
     * https://next.openhab.org/javadoc/latest/org/openhab/core/auth/client/oauth2/package-summary.html
     */
    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
        logger.debug("{} received new Access Token", config.get().callbackPort);
        if (!tokenResponse.isExpired(LocalDateTime.now(), 10)) {
            updateStatus(ThingStatus.ONLINE);
        }
        if (tokenResponse.getRefreshToken() != null) {
            logger.debug("{} store token in {}", config.get().callbackPort, tokenStorageKey.get());
            String tokenSerial = Utils.toString(tokenResponse);
            storage.put(tokenStorageKey.get(), tokenSerial);
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

    /**
     * Intermediate function to correct storage of token.
     * Before token was stored with unique key bridge.getUUID now token is stored with key clientId.
     * With this change you're able to delete and create bridges in openHAB without loosing token data if the same
     * clientId is configured
     */
    private boolean keyMigration() {
        String oldTokenStorageKey = super.getThing().getUID() + ":token";
        if (storage.containsKey(oldTokenStorageKey)) {
            String tokenSerial = storage.get(oldTokenStorageKey);
            if (tokenSerial != null) {
                AccessTokenResponse atr = (AccessTokenResponse) Utils.fromString(tokenSerial);
                server.get().setToken(atr);
                logger.debug("Token migration successful");
                // put migration token with adjusted key
                storage.put(tokenStorageKey.get(), tokenSerial);
                // remove migration token from storage
                storage.remove(oldTokenStorageKey);
                return true;
            } else {
                logger.debug("Cannot restore Token for migration");
                return false;
            }
        } else {
            logger.debug("No Token for migration found");
            return false;
        }
    }
}
