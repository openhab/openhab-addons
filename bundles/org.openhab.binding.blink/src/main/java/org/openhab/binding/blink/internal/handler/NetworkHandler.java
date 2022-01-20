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
package org.openhab.binding.blink.internal.handler;

import static org.openhab.binding.blink.internal.BlinkBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.blink.internal.config.NetworkConfiguration;
import org.openhab.binding.blink.internal.service.NetworkService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
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

/**
 * The {@link NetworkHandler} is responsible for initializing network things and handling commands, which are
 * sent to one of the network's channels.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
public class NetworkHandler extends BaseThingHandler implements EventListener {

    private final Logger logger = LoggerFactory.getLogger(NetworkHandler.class);

    @NonNullByDefault({})
    NetworkConfiguration config;
    @NonNullByDefault({})
    AccountHandler accountHandler;
    NetworkService networkService;

    public NetworkHandler(Thing thing, HttpClientFactory httpClientFactory, Gson gson) {
        super(thing);
        networkService = new NetworkService(httpClientFactory.getCommonHttpClient(), gson);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (CHANNEL_NETWORK_ARMED.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(CHANNEL_NETWORK_ARMED,
                            accountHandler.getNetworkArmed(Long.toString(config.networkId), false));
                } else if (command instanceof OnOffType) {
                    OnOffType cmd = (OnOffType) command;
                    boolean enable = (cmd == OnOffType.ON);
                    Long cmdId = networkService.arm(accountHandler.getBlinkAccount(), Long.toString(config.networkId),
                            enable);
                    networkService.watchCommandStatus(scheduler, accountHandler.getBlinkAccount(), config.networkId,
                            cmdId, this::asyncCommandFinished);
                }
            }
        } catch (IOException e) {
            accountHandler.setOffline(e);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(NetworkConfiguration.class);

        @Nullable
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null) {
            logger.warn("Cannot handle commands of blink things without a bridge: {}", thing.getUID().getAsString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge");
            return;
        }
        accountHandler = (AccountHandler) bridge.getHandler();

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleHomescreenUpdate() {
        try {
            updateState(CHANNEL_NETWORK_ARMED, accountHandler.getNetworkArmed(String.valueOf(config.networkId), false));
        } catch (IOException e) {
            accountHandler.setOffline(e);
        }
    }

    @Override
    public void dispose() {
        networkService.dispose();
        super.dispose();
    }

    private void asyncCommandFinished(boolean success) {
        if (success) {
            accountHandler.getDevices(true); // trigger refresh of homescreen
        }
    }
}
