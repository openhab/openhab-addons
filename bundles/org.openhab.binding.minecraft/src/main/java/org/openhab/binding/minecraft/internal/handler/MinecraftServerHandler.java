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
package org.openhab.binding.minecraft.internal.handler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.minecraft.internal.MinecraftBindingConstants;
import org.openhab.binding.minecraft.internal.config.ServerConfig;
import org.openhab.binding.minecraft.internal.message.OHMessage;
import org.openhab.binding.minecraft.internal.message.data.PlayerData;
import org.openhab.binding.minecraft.internal.message.data.ServerData;
import org.openhab.binding.minecraft.internal.message.data.SignData;
import org.openhab.binding.minecraft.internal.server.ServerConnection;
import org.openhab.binding.minecraft.internal.util.RetryWithDelay;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * The {@link MinecraftServerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class MinecraftServerHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(MinecraftServerHandler.class);

    private ServerConfig config;

    private Observable<ServerConnection> serverConnectionRX;
    private ServerConnection connection;
    private CompositeSubscription subscription;

    public MinecraftServerHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        subscription = new CompositeSubscription();
        config = getConfigAs(ServerConfig.class);
        logger.info("Initializing MinecraftHandler");
        connectToServer();
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Get server configuration
     *
     * @return
     */
    public ServerConfig getServerConfig() {
        return config;
    }

    /**
     * Directly connect to server.
     * Reconnects when connection is lost
     */
    private void connectToServer() {
        String host = config.getHostname();
        int port = config.getPort();

        serverConnectionRX = ServerConnection.create(getThing().getUID(), host, port)
                .doOnNext(item -> updateOnlineState(true)).doOnError(e -> updateOnlineState(false))
                .retryWhen(new RetryWithDelay(1, TimeUnit.MINUTES)).repeat().replay(1).refCount();

        Subscription serverUpdateSubscription = serverConnectionRX
                .flatMap(connection -> connection.getSocketHandler().getServerRx())
                .subscribe(serverData -> updateServerState(serverData));

        Subscription serverConnectionSubscription = serverConnectionRX.subscribe(connection -> {
            this.connection = connection;
        });

        subscription.add(serverUpdateSubscription);
        subscription.add(serverConnectionSubscription);
    }

    public Observable<List<SignData>> getSignsRx() {
        return serverConnectionRX.switchMap(connection -> connection.getSocketHandler().getSignsRx());
    }

    public Observable<List<PlayerData>> getPlayerRx() {
        return serverConnectionRX.switchMap(connection -> connection.getSocketHandler().getPlayersRx());
    }

    /**
     * Update online state of server
     *
     * @param isOnline true if server is online
     */
    private void updateOnlineState(boolean isOnline) {
        State onlineState = isOnline ? OnOffType.ON : OnOffType.OFF;
        updateState(MinecraftBindingConstants.CHANNEL_ONLINE, onlineState);
    }

    /**
     * Update state of minecraft server
     *
     * @param serverData
     */
    private void updateServerState(ServerData serverData) {
        State playersState = new DecimalType(serverData.getPlayers());
        State maxPlayersState = new DecimalType(serverData.getMaxPlayers());

        updateState(MinecraftBindingConstants.CHANNEL_PLAYERS, playersState);
        updateState(MinecraftBindingConstants.CHANNEL_MAX_PLAYERS, maxPlayersState);
    }

    /**
     * Send message to server.
     * Does nothing if no connection is established.
     *
     * @param message the message to send
     * @return true if message was sent.
     */
    public boolean sendMessage(OHMessage message) {
        if (connection != null) {
            connection.sendMessage(message);
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing minecraft server thing");

        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
