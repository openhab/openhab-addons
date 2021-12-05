/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.blink.internal.handler;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.blink.internal.config.NetworkConfiguration;
import org.openhab.binding.blink.internal.service.NetworkService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

import static org.openhab.binding.blink.internal.BlinkBindingConstants.*;

/**
 * The {@link NetworkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
public class NetworkHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(NetworkHandler.class);

    NetworkService networkService;
    NetworkConfiguration config = new NetworkConfiguration();
    @Nullable AccountHandler accountHandler;

    public NetworkHandler(Thing thing, HttpClientFactory httpClientFactory, Gson gson) throws IllegalArgumentException {
        super(thing);
        networkService = new NetworkService(httpClientFactory.getCommonHttpClient(), gson);
        if (getBridge() == null || getBridge().getHandler() == null) {
            throw new IllegalArgumentException("Cannot initialize blink network thing without a blink account bridge");
        }
        accountHandler = (AccountHandler) getBridge().getHandler();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (accountHandler == null) {
            // never happens, but it's impossible to set accountHandler in constructor as nonnull without the compiler
            // being unhappy. without this null-check, IntelliJ is unhappy. this if makes me unhappy, but no one cares.
            return;
        }
        try {
            if (CHANNEL_NETWORK_ARMED.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(CHANNEL_NETWORK_ARMED,
                            accountHandler.getNetworkArmed(Long.toString(config.networkId), false));
                } else if (command instanceof OnOffType) {
                    OnOffType cmd = (OnOffType) command;
                    boolean enable = (cmd == OnOffType.ON);
                    networkService.arm(accountHandler.getBlinkAccount(), Long.toString(config.networkId), enable);
                    // enable/disable is an async command in the api, changes might not be reflected in updateState
                    updateState(CHANNEL_NETWORK_ARMED, cmd);
                }
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(NetworkConfiguration.class);
        updateStatus(ThingStatus.ONLINE);
    }
}
