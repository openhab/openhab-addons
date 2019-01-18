/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.minecraft.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.minecraft.internal.MinecraftBindingConstants;
import org.openhab.binding.minecraft.internal.MinecraftHandlerFactory;
import org.openhab.binding.minecraft.internal.config.ServerConfig;
import org.openhab.binding.minecraft.internal.handler.MinecraftServerHandler;
import org.openhab.binding.minecraft.internal.message.data.PlayerData;
import org.openhab.binding.minecraft.internal.message.data.SignData;
import org.openhab.binding.minecraft.internal.server.ServerConnection;
import org.openhab.binding.minecraft.internal.util.Pair;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Handles discovery of Minecraft server, players and signs.
 *
 * @author Mattias Markehed - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.minecraft")
public class MinecraftDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(MinecraftDiscoveryService.class);

    private static final int DISCOVER_TIMEOUT_SECONDS = 60;

    private CompositeSubscription subscription;

    public MinecraftDiscoveryService() {
        super(Collections.singleton(MinecraftBindingConstants.THING_TYPE_SERVER), DISCOVER_TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Minecraft discovery scan");
        discoverServers();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stopping Minecraft discovery scan");
        stopDiscovery();
        super.stopScan();
    }

    /**
     * Start scanning for players and signs on servers.
     */
    private void discoverServers() {
        subscription = new CompositeSubscription();

        Observable<ServerConnection> serverRx = serversConnectRx().cache();

        Subscription playerSubscription = subscribePlayersRx(serverRx);
        Subscription signsSubscription = subscribeSignsRx(serverRx);

        subscription.add(playerSubscription);
        subscription.add(signsSubscription);
    }

    /**
     * Subscribe for sign updates
     *
     * @param serverRx server stream
     * @return subscription for listening to sign events.
     */
    private Subscription subscribeSignsRx(Observable<ServerConnection> serverRx) {
        return serverRx
                .flatMap(connection -> connection.getSocketHandler().getSignsRx().distinct(), (connection, signs) -> {
                    return new Pair<ServerConnection, List<SignData>>(connection, signs);
                }).subscribe(conectionSignPair -> {
                    for (SignData sign : conectionSignPair.second) {
                        submitSignDiscoveryResults(conectionSignPair.first.getThingUID(), sign);
                    }
                }, e -> logger.error("Error while scanning for signs", e));
    }

    /**
     * Subscribe to player updates
     *
     * @param serverRx server stream
     * @return subscription for listening to player events.
     */
    private Subscription subscribePlayersRx(Observable<ServerConnection> serverRx) {
        return serverRx
                .flatMap(socketHandler -> socketHandler.getSocketHandler().getPlayersRx().distinct(),
                        (connection, players) -> new Pair<ServerConnection, List<PlayerData>>(connection, players))
                .subscribeOn(Schedulers.newThread()).subscribe(conectionPlayerPair -> {
                    for (PlayerData player : conectionPlayerPair.second) {
                        submitPlayerDiscoveryResults(conectionPlayerPair.first.getThingUID(), player.getName());
                    }
                }, e -> logger.error("Error while scanning for players", e));
    }

    /**
     * Teardown subscribers and stop searching for players and signs.
     */
    private void stopDiscovery() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    /**
     * Get all servers that have been added as observable.
     *
     * @return observable emitting server objects.
     */
    private Observable<ServerConnection> serversConnectRx() {
        return Observable.from(MinecraftHandlerFactory.getMinecraftServers())
                .flatMap(new Func1<MinecraftServerHandler, Observable<ServerConnection>>() {
                    @Override
                    public Observable<ServerConnection> call(MinecraftServerHandler server) {
                        ServerConfig config = server.getServerConfig();
                        return ServerConnection.create(server.getThing().getUID(), config.getHostname(),
                                config.getPort());
                    }
                });
    }

    /**
     * Submit the discovered Devices to the Smarthome inbox,
     *
     * @param bridgeUID
     * @param name name of the player
     */
    private void submitPlayerDiscoveryResults(ThingUID bridgeUID, String name) {
        String id = deviceNameToId(name);
        ThingUID uid = new ThingUID(MinecraftBindingConstants.THING_TYPE_PLAYER, bridgeUID, id);

        Map<String, Object> properties = new HashMap<>();
        properties.put(MinecraftBindingConstants.PARAMETER_PLAYER_NAME, name);
        thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties).withBridge(bridgeUID)
                .withLabel(name).build());
    }

    /**
     * Submit the discovered Signs to the Smarthome inbox,
     *
     * @param bridgeUID
     * @param sign data describing sign
     */
    private void submitSignDiscoveryResults(ThingUID bridgeUID, SignData sign) {
        String id = deviceNameToId(sign.getName());
        ThingUID uid = new ThingUID(MinecraftBindingConstants.THING_TYPE_SIGN, bridgeUID, id);

        Map<String, Object> properties = new HashMap<>();
        properties.put(MinecraftBindingConstants.PARAMETER_SIGN_NAME, sign.getName());
        thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties).withBridge(bridgeUID)
                .withLabel(sign.getName()).build());
    }

    /**
     * Cleanup device name so it can be used as id.
     *
     * @param name the name of device.
     * @return id of device.
     */
    private String deviceNameToId(String name) {
        if (name == null) {
            return "";
        }
        return name.replaceAll("[^a-zA-Z0-9]+", "");
    }
}
