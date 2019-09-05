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
package org.openhab.binding.heos.internal.discovery;

import static org.openhab.binding.heos.HeosBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.resources.HeosGroup;
import org.openhab.binding.heos.internal.resources.HeosPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosPlayerDiscovery} discovers the player and groups within
 * the HEOS network and reacts on changed groups or player.
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosPlayerDiscovery extends AbstractDiscoveryService implements HeosPlayerDiscoveryListener {
    private final Logger logger = LoggerFactory.getLogger(HeosPlayerDiscovery.class);

    private static final int SEARCH_TIME = 5;
    private static final int INITIAL_DELAY = 5;
    private static final int SCAN_INTERVAL = 20;

    private HeosBridgeHandler bridge;

    private ScheduledFuture<?> scanningJob;

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
        logger.debug("Start scan for HEOS Player");

        Map<String, HeosPlayer> playerMap = new HashMap<>();
        playerMap = bridge.getNewPlayer();

        if (playerMap == null) {
            return;
        } else {
            logger.debug("Found: {} new Player", playerMap.size());
            ThingUID bridgeUID = bridge.getThing().getUID();

            for (String playerPID : playerMap.keySet()) {
                HeosPlayer player = playerMap.get(playerPID);
                ThingUID uid = new ThingUID(THING_TYPE_PLAYER, playerMap.get(playerPID).getPid());
                Map<String, Object> properties = new HashMap<>();
                properties.put(PROP_NAME, player.getName());
                properties.put(PROP_PID, player.getPid());
                properties.put(PROP_MODEL, player.getModel());
                properties.put(PROP_VERSION, player.getVersion());
                properties.put(PROP_NETOWRK, player.getNetwork());
                properties.put(PROP_IP, player.getIp());
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(player.getName())
                        .withProperties(properties).withBridge(bridgeUID).build();
                thingDiscovered(result);
            }
        }

        logger.debug("Start scan for HEOS Groups");

        Map<String, HeosGroup> groupMap = new HashMap<>();

        groupMap = bridge.getNewGroups();

        if (groupMap == null) {
            return;
        } else {
            if (!groupMap.isEmpty()) {
                logger.debug("Found: {} new Groups", groupMap.size());
                ThingUID bridgeUID = bridge.getThing().getUID();

                for (String groupGID : groupMap.keySet()) {
                    HeosGroup group = groupMap.get(groupGID);
                    String groupMemberHash = group.getGroupMemberHash();
                    // Using an unsigned hashCode from the group members to identify
                    // the group and generates the Thing UID.
                    // This allows identifying the group even if the sorting within the group has changed
                    ThingUID uid = new ThingUID(THING_TYPE_GROUP, groupMemberHash);
                    Map<String, Object> properties = new HashMap<>();
                    properties.put(PROP_NAME, group.getName());
                    properties.put(PROP_GID, group.getGid());
                    properties.put(PROP_GROUP_MEMBERS, group.getGroupMembersAsString());
                    properties.put(PROP_GROUP_LEADER, group.getLeader());
                    properties.put(PROP_GROUP_HASH, group.getGroupMemberHash());
                    DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(group.getName())
                            .withProperties(properties).withBridge(bridgeUID).build();
                    thingDiscovered(result);
                    bridge.setGroupOnline(group, uid);
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
        Map<String, HeosGroup> removedGroupMap = new HashMap<>();
        removedGroupMap = bridge.getRemovedGroups();
        if (!removedGroupMap.isEmpty()) {
            for (String key : removedGroupMap.keySet()) {
                // The same as above!
                ThingUID uid = new ThingUID(THING_TYPE_GROUP, removedGroupMap.get(key).getGroupMemberHash());
                logger.debug("Removed HEOS Group: {}", uid);
                thingRemoved(uid);
                bridge.setGroupOffline(removedGroupMap.get(key).getGroupMemberHash());
            }
        }
    }

    // Informs the system of removed players by using the thingRemoved method.
    private void removedPlayers() {
        Map<String, HeosPlayer> removedPlayerMap = new HashMap<>();
        removedPlayerMap = bridge.getRemovedPlayer();
        if (!removedPlayerMap.isEmpty()) {
            for (String key : removedPlayerMap.keySet()) {
                // The same as above!
                ThingUID uid = new ThingUID(THING_TYPE_PLAYER, removedPlayerMap.get(key).getPid());
                logger.debug("Removed HEOS Player: {} ", uid);
                thingRemoved(uid);
            }
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("Start HEOS Player background discovery");
        if (scanningJob == null || scanningJob.isCancelled()) {
            this.scanningJob = scheduler.scheduleWithFixedDelay(this::startScan, INITIAL_DELAY, SCAN_INTERVAL,
                    TimeUnit.SECONDS);
        }
        logger.trace("scanningJob active");
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop HEOS Player background discovery");
        if (scanningJob != null && !scanningJob.isCancelled()) {
            scanningJob.cancel(true);
            scanningJob = null;
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    public void scanForNewPlayers() {
        removeOlderResults(getTimestampOfLastScan());
        startScan();
    }

    @Override
    public void playerChanged() {
        scanForNewPlayers();
    }
}
