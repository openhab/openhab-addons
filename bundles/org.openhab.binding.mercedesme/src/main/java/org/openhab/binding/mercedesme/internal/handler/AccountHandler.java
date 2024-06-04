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
package org.openhab.binding.mercedesme.internal.handler;

import java.net.SocketException;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.server.CallbackServer;
import org.openhab.binding.mercedesme.internal.server.Utils;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
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
    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private Optional<CallbackServer> server = Optional.empty();

    Optional<AccountConfiguration> config = Optional.empty();

    public AccountHandler(Bridge bridge, HttpClient hc, OAuthFactory oaf) {
        super(bridge);
        httpClient = hc;
        oAuthFactory = oaf;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands available
    }

    @Override
    public void initialize() {
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        autodetectCallback();
        String configValidReason = configValid();
        if (!configValidReason.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configValidReason);
        } else {
            String callbackUrl = Utils.getCallbackAddress(config.get().callbackIP, config.get().callbackPort);
            thing.setProperty("callbackUrl", callbackUrl);
            server = Optional.of(new CallbackServer(this, httpClient, oAuthFactory, config.get(), callbackUrl));
            if (!server.get().start()) {
                String textKey = Constants.STATUS_TEXT_PREFIX + thing.getThingTypeUID().getId()
                        + Constants.STATUS_SERVER_RESTART;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, textKey);
            } else {
                // get fresh token
                this.getToken();
            }
        }
    }

    private void autodetectCallback() {
        // if Callback IP and Callback Port are not set => autodetect these values
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        Configuration updateConfig = super.editConfiguration();
        if (!updateConfig.containsKey("callbackPort")) {
            updateConfig.put("callbackPort", Utils.getFreePort());
        } else {
            Utils.addPort(config.get().callbackPort);
        }
        if (!updateConfig.containsKey("callbackIP")) {
            String ip;
            try {
                ip = Utils.getCallbackIP();
                updateConfig.put("callbackIP", ip);
            } catch (SocketException e) {
                logger.info("Cannot detect IP address {}", e.getMessage());
            }
        }
        super.updateConfiguration(updateConfig);
        // get new config after update
        config = Optional.of(getConfigAs(AccountConfiguration.class));
    }

    private String configValid() {
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        String textKey = Constants.STATUS_TEXT_PREFIX + thing.getThingTypeUID().getId();
        if (config.get().callbackIP.equals(Constants.NOT_SET)) {
            return textKey + Constants.STATUS_IP_MISSING;
        } else if (config.get().callbackPort == -1) {
            return textKey + Constants.STATUS_PORT_MISSING;
        } else if (config.get().clientId.equals(Constants.NOT_SET)) {
            return textKey + Constants.STATUS_CLIENT_ID_MISSING;
        } else if (config.get().clientSecret.equals(Constants.NOT_SET)) {
            return textKey + Constants.STATUS_CLIENT_SECRET_MISSING;
        } else {
            return Constants.EMPTY;
        }
    }

    @Override
    public void dispose() {
        if (server.isPresent()) {
            CallbackServer serv = server.get();
            serv.stop();
            serv.dispose();
            server = Optional.empty();
            Utils.removePort(config.get().callbackPort);
        }
    }

    @Override
    public void handleRemoval() {
        server.ifPresent(s -> s.deleteOAuthServiceAndAccessToken());
        super.handleRemoval();
    }

    /**
     * https://next.openhab.org/javadoc/latest/org/openhab/core/auth/client/oauth2/package-summary.html
     */
    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
        if (!tokenResponse.getAccessToken().isEmpty()) {
            // token not empty - fine
            updateStatus(ThingStatus.ONLINE);
        } else if (server.isEmpty()) {
            // server not running - fix first
            String textKey = Constants.STATUS_TEXT_PREFIX + thing.getThingTypeUID().getId()
                    + Constants.STATUS_SERVER_RESTART;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, textKey);
        } else {
            // all failed - start manual authorization
            String textKey = Constants.STATUS_TEXT_PREFIX + thing.getThingTypeUID().getId()
                    + Constants.STATUS_AUTH_NEEDED;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    textKey + " [\"" + thing.getProperties().get("callbackUrl") + "\"]");
        }
    }

    public String getToken() {
        return server.get().getToken();
    }

    public String getImageApiKey() {
        return config.get().imageApiKey;
    }

    @Override
    public String toString() {
        return Integer.toString(config.get().callbackPort);
    }
}
