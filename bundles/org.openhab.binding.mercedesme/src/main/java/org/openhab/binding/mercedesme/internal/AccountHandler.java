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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mercedesme.internal.server.CallbackServer;
import org.openhab.binding.mercedesme.internal.server.Utils;
import org.openhab.binding.mercedesme.internal.utils.TokenWrapper;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.ItemFactory;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.PersistenceService;
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
    private TokenWrapper tw = new TokenWrapper();

    private final OAuthFactory oAuthFactory;
    private Optional<StringItem> authUrlItem = Optional.empty();
    private Optional<StringItem> tokenItem = Optional.empty();

    private final ItemFactory itemFactory;
    private final ItemRegistry itemRegistry;
    private Optional<PersistenceService> persistenceService = Optional.empty();

    public AccountHandler(Bridge bridge, HttpClientFactory hcf, OAuthFactory oaf, ItemRegistry itr, ItemFactory itf,
            @Nullable PersistenceService ps) {
        super(bridge);
        httpClientFactory = hcf;
        oAuthFactory = oaf;

        this.itemFactory = itf;
        this.itemRegistry = itr;
        if (ps != null) {
            persistenceService = Optional.of(ps);
        }
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

            // get items from Persistence
            restoreItems();

            // super.updateProperty("authorizationUrl", server.get().getAuthorizationUrl());
            authUrlItem.ifPresentOrElse(i -> {
                StringItem si = authUrlItem.get();
                if (si.getState().toString().equals(Constants.EMPTY)) {
                    si.setState(StringType.valueOf(server.get().getAuthorizationUrl()));
                    logger.info("Auth Item set Auth URL {}", authUrlItem.get().getState().toString());
                } else {
                    logger.info("Auth Item get Auth URL {}", authUrlItem.get().getState().toString());
                }
            }, () -> {
                logger.info("Auth URL not restored from item");
            });

            tokenItem.ifPresentOrElse(i -> {
                StringItem si = tokenItem.get();
                if (si.getState().toString().equals(Constants.EMPTY)) {
                    logger.info("Token item is empty");
                } else {
                    String tokenSerial = si.getState().toString();
                    logger.info("Token Item {}", tokenSerial);
                    AccessTokenResponse atr;
                    try {
                        atr = (AccessTokenResponse) Utils.fromString(tokenSerial);
                        server.get().setToken(atr);
                    } catch (ClassNotFoundException | IOException e) {
                        logger.warn("Unable to deserialize token {}", config.get().token);
                    }
                }
            }, () -> {
                logger.info("Token not restored from item");
            });

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
        tw.setToken(tokenResponse);
        if (!tokenResponse.isExpired(LocalDateTime.now(), 10)) {
            updateStatus(ThingStatus.ONLINE);
        }
        if (tokenResponse.getRefreshToken() != null) {
            try {
                String tokenSerial = Utils.toString(tokenResponse);
                Configuration c = super.editConfiguration();
                c.put("token", tokenSerial);
                super.updateConfiguration(c);

                // store in item
                tokenItem.ifPresentOrElse(i -> {
                    StringItem si = tokenItem.get();
                    si.setState(StringType.valueOf(tokenSerial));
                    storeItems();
                }, () -> {
                    logger.info("Storing Token not possible");
                });
            } catch (IOException e) {
                logger.info("Error serializing token {}", e.getMessage());
            }
        }
    }

    public String getToken() {
        return server.get().getToken();
    }

    private void restoreItems() {
        String authItemName = "MercedesMeAuthorizationURL_" + thing.getUID().getId();
        String tokenItemName = "MercedesMeTokenSerial_" + thing.getUID().getId();
        logger.info("Restore {} {}", tokenItemName, authItemName);
        StringItem si = (StringItem) itemRegistry.get(authItemName);
        if (si == null) {
            si = (StringItem) itemFactory.createItem("String", authItemName);
            if (si != null) {
                si.setState(StringType.valueOf(Constants.EMPTY));
                authUrlItem = Optional.of(si);
            } else {
                logger.info("Unable to create Item {}", authItemName);
            }
        } else {
            logger.info("AuthUrl Item found {}", si.getState().toString());
            authUrlItem = Optional.of(si);
        }

        si = (StringItem) itemRegistry.get(tokenItemName);
        if (si == null) {
            si = (StringItem) itemFactory.createItem("String", tokenItemName);
            if (si != null) {
                si.setState(StringType.valueOf(Constants.EMPTY));
                tokenItem = Optional.of(si);
            } else {
                logger.info("Unable to create Item {}", tokenItemName);
            }
        } else {
            logger.info("Token Item found {}", si.getState().toString());
            tokenItem = Optional.of(si);
        }
    }

    private void storeItems() {
        if (persistenceService.isPresent()) {
            if (authUrlItem.isPresent()) {
                persistenceService.get().store(authUrlItem.get());
                logger.info("Auth Item stored {}", authUrlItem.get().getState().toString());
            } else {
                logger.info("No Auth Url Item available");
            }
            if (tokenItem.isPresent()) {
                persistenceService.get().store(tokenItem.get());
                logger.info("Token Item stored {}", tokenItem.get().getState().toString());
            } else {
                logger.info("No Token Item available");
            }
        } else {
            logger.info("No persistence service available");
        }
    }
}
