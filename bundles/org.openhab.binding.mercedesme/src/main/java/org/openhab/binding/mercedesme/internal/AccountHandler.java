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

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.server.CallbackServer;
import org.openhab.binding.mercedesme.internal.server.Utils;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
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
public class AccountHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private HttpClientFactory httpClientFactory;
    private Optional<ScheduledFuture<?>> initializerJob = Optional.empty();
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
        updateStatus(ThingStatus.UNKNOWN);
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        if (!isConfigValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            server = Optional.of(new CallbackServer(oAuthFactory, config.get().callbackPort));
            server.get().start();
        }
    }

    private void handleConfig() {
        // Handle not initialized Thing with "best guess values"
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        if (config.get().callbackPort == -1) {
            config.get().callbackPort = Utils.getFreePort();
        } else {
            Utils.addPort(config.get().callbackPort);
        }
        if (config.get().callbackIP.equals(Constants.NOT_SET)) {
            config.get().callbackIP = Utils.getCallbackIP();
        }
        if (config.get().scope.equals(Constants.NOT_SET)) {
            // set all scopes for account
            config.get().scope = Constants.SCOPE_EV + Constants.SPACE + Constants.SCOPE_FUEL + Constants.SPACE
                    + Constants.SCOPE_LOCK + Constants.SPACE + Constants.SCOPE_ODO + Constants.SPACE
                    + Constants.SCOPE_OFFLINE + Constants.SPACE + Constants.SCOPE_STATUS;
        }
        config.get().callbackUrl = Utils.getCallbackAddress(config.get().callbackIP, config.get().callbackPort);
    }

    private boolean isConfigValid() {
        if (!config.isEmpty()) {
            if (!config.get().callbackIP.equals(Constants.NOT_SET) && config.get().callbackPort != -1
                    && config.get().clientID.equals(Constants.NOT_SET)
                    && config.get().clientSecret.equals(Constants.NOT_SET)) {
                // Callback address with port and client data is set - config seems to be valid
                return true;
            }
        }
        return false;
    }

    @Override
    public void dispose() {
        initializerJob.ifPresent(job -> job.cancel(true));
    }
}
