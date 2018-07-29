/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.discovery;

import static org.openhab.binding.heos.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

import com.google.common.collect.Sets;

/**
 * The {@link HeosPlayerDiscovery} discovers the player and groups within
 * the HEOS network and reacts on changed groups or player.
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosPlayerDiscovery extends AbstractDiscoveryService {
    private static final int SEARCH_TIME = 20;
    private static final int INITIAL_DELAY = 5;
    private static final int SCAN_INTERVAL = 20;

    private Logger logger = LoggerFactory.getLogger(HeosPlayerDiscovery.class);

    private HeosBridgeHandler bridge;

    private PlayerScan scanningRunnable;

    private ScheduledFuture<?> scanningJob;

    public HeosPlayerDiscovery(HeosBridgeHandler bridge) throws IllegalArgumentException {
        super(20);
        this.bridge = bridge;
        this.scanningRunnable = new PlayerScan();
        bridge.setHeosPlayerDiscovery(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        Set<ThingTypeUID> supportedThings = Sets.newHashSet(THING_TYPE_GROUP, THING_TYPE_PLAYER);

        return supportedThings;
    }

    @Override
    protected void startScan() {
        logger.info("Start scan for HEOS Player");

        Map<String, HeosPlayer> playerMap = new HashMap<>();
        playerMap = bridge.getNewPlayer();

        if (playerMap == null) {
            return;
        } else {
            logger.info("Found: {} new Player", playerMap.size());
            ThingUID bridgeUID = bridge.getThing().getUID();

            for (String playerPID : playerMap.keySet()) {
                HeosPlayer player = playerMap.get(playerPID);
                ThingUID uid = new ThingUID(THING_TYPE_PLAYER, playerMap.get(playerPID).getPid());
                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put(NAME, player.getName());
                properties.put(PID, player.getPid());
                properties.put(PLAYER_TYPE, player.getModel());
                properties.put(HOST, player.getIp());
                properties.put(TYPE, PLAYER);
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(player.getName())
                        .withProperties(properties).withBridge(bridgeUID).build();
                thingDiscovered(result);
            }
        }

        logger.info("Start scan for HEOS Groups");

        Map<String, HeosGroup> groupMap = new HashMap<>();

        groupMap = bridge.getNewGroups();

        if (groupMap == null) {
            return;
        } else {
            if (!groupMap.isEmpty()) {
                logger.info("Found: {} new Groups", groupMap.size());
                ThingUID bridgeUID = bridge.getThing().getUID();

                for (String groupGID : groupMap.keySet()) {
                    HeosGroup group = groupMap.get(groupGID);

                    // uses an unsigned hashCode from the group name to identify the group and generates the Thing UID.
                    // Only the name does not work because it can consists non allowed characters. This also making it
                    // possible to add player to a group. Keeping the Name lets the binding still identifying the group
                    // as known

                    ThingUID uid = new ThingUID(THING_TYPE_GROUP, group.getGroupMemberHash());
                    HashMap<String, Object> properties = new HashMap<String, Object>();
                    properties.put(NAME, group.getName());
                    properties.put(GID, group.getGid());
                    properties.put(LEADER, group.getLeader());
                    properties.put(TYPE, GROUP);
                    properties.put(NAME_HASH, group.getNameHash());
                    properties.put(GROUP_MEMBER_HASH, group.getGroupMemberHash());
                    properties.put(GROUP_MEMBER_PID_LIST, group.getGroupMemberPidList());
                    DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(group.getName())
                            .withProperties(properties).withBridge(bridgeUID).build();
                    thingDiscovered(result);
                }
            } else {
                logger.info("No HEOS Groups found");
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
                logger.info("Removed HEOS Group: {}", uid);
                thingRemoved(uid);
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
                logger.info("Removed HEOS Player: {} ", uid);
                thingRemoved(uid);
            }
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("Start HEOS Player background discovery");
        if (scanningJob == null || scanningJob.isCancelled()) {
            this.scanningJob = scheduler.scheduleWithFixedDelay(this.scanningRunnable, INITIAL_DELAY, SCAN_INTERVAL,
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
        scanningRunnable.run();
    }

    public class PlayerScan implements Runnable {
        @Override
        public void run() {
            startScan();
        }
    }
}
