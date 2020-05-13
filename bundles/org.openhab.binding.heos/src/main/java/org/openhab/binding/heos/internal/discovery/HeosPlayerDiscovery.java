/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.heos.internal.discovery;

import static org.openhab.binding.heos.internal.HeosBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.heos.internal.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.handler.HeosPlayerHandler;
import org.openhab.binding.heos.internal.json.payload.Group;
import org.openhab.binding.heos.internal.json.payload.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosPlayerDiscovery} discovers the player and groups within
 * the HEOS network and reacts on changed groups or player.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosPlayerDiscovery extends AbstractDiscoveryService implements HeosPlayerDiscoveryListener {
    private final Logger logger = LoggerFactory.getLogger(HeosPlayerDiscovery.class);

    private static final int SEARCH_TIME = 5;
    private static final int INITIAL_DELAY = 5;
    private static final int SCAN_INTERVAL = 20;

    private HeosBridgeHandler bridge;

    private @Nullable ScheduledFuture<?> scanningJob;

    public HeosPlayerDiscovery(HeosBridgeHandler bridge) throws IllegalArgumentException {
        super(SEARCH_TIME);
        this.bridge = bridge;
        bridge.registerPlayerDiscoverListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return Stream.of(THING_TYPE_GROUP, THING_TYPE_PLAYER).collect(Collectors.toSet());
    }

    @Override
    protected void startScan() {
        if (!bridge.isBridgeConnected()) {
            logger.debug("Scan for Players not possible. HEOS Bridge is not connected");
            return;
        }
        logger.debug("Start scan for HEOS Player");

        Map<Integer, Player> playerMap = bridge.getNewPlayers();

        if (playerMap == null) {
            return;
        } else {
            logger.debug("Found: {} new Player", playerMap.size());
            ThingUID bridgeUID = bridge.getThing().getUID();

            for (Player player : playerMap.values()) {
                ThingUID uid = new ThingUID(THING_TYPE_PLAYER, "" + player.playerId);
                Map<String, Object> properties = new HashMap<>();
                HeosPlayerHandler.propertiesFromPlayer(properties, player);

                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(player.name)
                        .withProperties(properties).withBridge(bridgeUID)
                        .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
                thingDiscovered(result);
            }
        }

        logger.debug("Start scan for HEOS Groups");

        Map<String, Group> groupMap = bridge.getNewGroups();

        if (groupMap == null) {
            return;
        } else {
            if (!groupMap.isEmpty()) {
                logger.debug("Found: {} new Groups", groupMap.size());
                ThingUID bridgeUID = bridge.getThing().getUID();

                for (Map.Entry<String, Group> entry : groupMap.entrySet()) {
                    Group group = entry.getValue();
                    String groupMemberHash = entry.getKey();
                    // Using an unsigned hashCode from the group members to identify
                    // the group and generates the Thing UID.
                    // This allows identifying the group even if the sorting within the group has changed
                    ThingUID uid = new ThingUID(THING_TYPE_GROUP, groupMemberHash);
                    Map<String, Object> properties = new HashMap<>();
                    properties.put(PROP_NAME, group.name);
                    properties.put(PROP_GID, group.id);
                    String groupMembers = group.players.stream().map(p -> p.id).collect(Collectors.joining(";"));
                    properties.put(PROP_GROUP_MEMBERS, groupMembers);
                    properties.put(PROP_GROUP_LEADER, group.players.get(0).id);
                    properties.put(PROP_GROUP_HASH, groupMemberHash);
                    DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(group.name)
                            .withProperties(properties).withBridge(bridgeUID)
                            .withRepresentationProperty(PROP_GROUP_HASH).build();
                    thingDiscovered(result);
                    bridge.setGroupOnline(groupMemberHash, group.id);
                }
            } else {
                logger.debug("No HEOS Groups found");
            }
        }
        removedPlayers();
        removedGroups();
    }

    // Informs the system of removed groups by using the thingRemoved method.
    private void removedGroups() {
        Map<String, Group> removedGroupMap = bridge.getRemovedGroups();
        for (String groupMemberHash : removedGroupMap.keySet()) {
            // The same as above!
            ThingUID uid = new ThingUID(THING_TYPE_GROUP, groupMemberHash);
            logger.debug("Removed HEOS Group: {}", uid);
            thingRemoved(uid);
            bridge.setGroupOffline(groupMemberHash);
        }
    }

    // Informs the system of removed players by using the thingRemoved method.
    private void removedPlayers() {
        Map<Integer, Player> removedPlayerMap = bridge.getRemovedPlayers();
        for (Player player : removedPlayerMap.values()) {
            // The same as above!
            ThingUID uid = new ThingUID(THING_TYPE_PLAYER, String.valueOf(player.playerId));
            logger.debug("Removed HEOS Player: {} ", uid);
            thingRemoved(uid);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> runningScanningJob = this.scanningJob;
        if (runningScanningJob == null || runningScanningJob.isCancelled()) {
            this.scanningJob = scheduler.scheduleWithFixedDelay(this::startScan, INITIAL_DELAY, SCAN_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> runningScanningJob = this.scanningJob;
        logger.debug("Stop HEOS Player background discovery");
        if (runningScanningJob != null && !runningScanningJob.isCancelled()) {
            runningScanningJob.cancel(true);
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    private void scanForNewPlayers() {
        removeOlderResults(getTimestampOfLastScan());
        startScan();
    }

    @Override
    public void playerChanged() {
        scanForNewPlayers();
    }
}
