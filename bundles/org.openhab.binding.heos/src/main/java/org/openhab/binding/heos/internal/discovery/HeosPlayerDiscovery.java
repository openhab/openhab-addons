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
package org.openhab.binding.heos.internal.discovery;

import static org.openhab.binding.heos.internal.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.handler.FutureUtil.cancel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.handler.HeosPlayerHandler;
import org.openhab.binding.heos.internal.json.payload.Group;
import org.openhab.binding.heos.internal.json.payload.Player;
import org.openhab.binding.heos.internal.resources.HeosGroup;
import org.openhab.binding.heos.internal.resources.Telnet;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
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

    private final HeosBridgeHandler bridge;

    private Map<Integer, Player> players = new HashMap<>();
    private Map<String, Group> groups = new HashMap<>();

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

        scanForPlayers();
        scanForGroups();
    }

    private void scanForPlayers() {
        logger.debug("Start scan for HEOS Player");

        try {
            Map<Integer, Player> currentPlayers = new HashMap<>();

            for (Player player : bridge.getPlayers()) {
                currentPlayers.put(player.playerId, player);
            }

            handleRemovedPlayers(findRemovedEntries(currentPlayers, players));
            handleDiscoveredPlayers(currentPlayers);

            players = currentPlayers;
        } catch (IOException | Telnet.ReadException e) {
            logger.debug("Failed getting/processing groups", e);
        }
    }

    private void handleDiscoveredPlayers(Map<Integer, Player> currentPlayers) {
        logger.debug("Found: {} player", currentPlayers.size());
        ThingUID bridgeUID = bridge.getThing().getUID();

        for (Player player : currentPlayers.values()) {
            ThingUID uid = new ThingUID(THING_TYPE_PLAYER, bridgeUID, String.valueOf(player.playerId));
            Map<String, Object> properties = new HashMap<>();
            HeosPlayerHandler.propertiesFromPlayer(properties, player);

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(player.name)
                    .withProperties(properties).withBridge(bridgeUID)
                    .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
            thingDiscovered(result);
        }
    }

    private void handleRemovedPlayers(Map<Integer, Player> removedPlayers) {
        for (Player player : removedPlayers.values()) {
            // The same as above!
            ThingUID uid = new ThingUID(THING_TYPE_PLAYER, String.valueOf(player.playerId));
            logger.debug("Removed HEOS Player: {} ", uid);
            thingRemoved(uid);
        }
    }

    private void scanForGroups() {
        logger.debug("Start scan for HEOS Groups");

        try {
            HashMap<String, Group> currentGroups = new HashMap<>();

            for (Group group : bridge.getGroups()) {
                logger.debug("Found: Group {} with {} Players", group.name, group.players.size());
                currentGroups.put(HeosGroup.calculateGroupMemberHash(group), group);
            }

            handleRemovedGroups(findRemovedEntries(currentGroups, groups));
            handleDiscoveredGroups(currentGroups);

            groups = currentGroups;
        } catch (IOException | Telnet.ReadException e) {
            logger.debug("Failed getting/processing groups", e);
        }
    }

    private void handleDiscoveredGroups(HashMap<String, Group> currentGroups) {
        if (currentGroups.isEmpty()) {
            logger.debug("No HEOS Groups found");
            return;
        }
        logger.debug("Found: {} new Groups", currentGroups.size());
        ThingUID bridgeUID = bridge.getThing().getUID();

        for (Map.Entry<String, Group> entry : currentGroups.entrySet()) {
            Group group = entry.getValue();
            String groupMemberHash = entry.getKey();
            // Using an unsigned hashCode from the group members to identify
            // the group and generates the Thing UID.
            // This allows identifying the group even if the sorting within the group has changed
            ThingUID uid = new ThingUID(THING_TYPE_GROUP, bridgeUID, groupMemberHash);
            Map<String, Object> properties = new HashMap<>();
            properties.put(PROP_NAME, group.name);
            properties.put(PROP_GID, group.id);
            String groupMembers = group.players.stream().map(p -> p.id).collect(Collectors.joining(";"));
            properties.put(PROP_GROUP_MEMBERS, groupMembers);
            properties.put(PROP_GROUP_LEADER, group.players.get(0).id);
            properties.put(PROP_GROUP_HASH, groupMemberHash);
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(group.name).withProperties(properties)
                    .withBridge(bridgeUID).withRepresentationProperty(PROP_GROUP_HASH).build();
            thingDiscovered(result);
            bridge.setGroupOnline(groupMemberHash, group.id);
        }
    }

    private void handleRemovedGroups(Map<String, Group> removedGroups) {
        for (String groupMemberHash : removedGroups.keySet()) {
            // The same as above!
            ThingUID uid = new ThingUID(THING_TYPE_GROUP, groupMemberHash);
            logger.debug("Removed HEOS Group: {}", uid);
            thingRemoved(uid);
            bridge.setGroupOffline(groupMemberHash);
        }
    }

    private <K, V> Map<K, V> findRemovedEntries(Map<K, V> mapNew, Map<K, V> mapOld) {
        Map<K, V> removedItems = new HashMap<>();
        for (K key : mapOld.keySet()) {
            if (!mapNew.containsKey(key)) {
                removedItems.put(key, mapOld.get(key));
            }
        }
        return removedItems;
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
        logger.debug("Stop HEOS Player background discovery");
        cancel(scanningJob);
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
