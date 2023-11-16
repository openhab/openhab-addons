/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.neeo.internal.discovery;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.neeo.internal.NeeoBrainApi;
import org.openhab.binding.neeo.internal.NeeoBrainConfig;
import org.openhab.binding.neeo.internal.NeeoConstants;
import org.openhab.binding.neeo.internal.handler.NeeoBrainHandler;
import org.openhab.binding.neeo.internal.models.NeeoBrain;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link AbstractDiscoveryService} that will discover the rooms in a NEEO brain;
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoRoomDiscoveryService extends AbstractDiscoveryService {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoRoomDiscoveryService.class);

    /** The room bridge type we support */
    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Set.of(NeeoConstants.BRIDGE_TYPE_ROOM);

    /** The timeout (in seconds) for searching the brain */
    private static final int SEARCH_TIME = 10;

    /** The brain handler that we will use */
    private final NeeoBrainHandler brainHandler;

    /**
     * Constructs the discover service from the brain handler
     *
     * @param brainHandler a non-null brain handler
     */
    public NeeoRoomDiscoveryService(NeeoBrainHandler brainHandler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, SEARCH_TIME);
        Objects.requireNonNull(brainHandler, "brainHandler cannot be null");
        this.brainHandler = brainHandler;
    }

    @Override
    protected void startScan() {
        final String brainId = brainHandler.getNeeoBrainId();

        final Bridge brainBridge = brainHandler.getThing();
        final ThingUID brainUid = brainBridge.getUID();

        final NeeoBrainApi api = brainHandler.getNeeoBrainApi();
        if (api == null) {
            logger.debug("Brain API was not available for {} - skipping", brainId);
            return;
        }

        try {
            final NeeoBrain brain = api.getBrain();
            final NeeoBrainConfig config = brainBridge.getConfiguration().as(NeeoBrainConfig.class);
            final NeeoRoom[] rooms = brain.getRooms().getRooms();

            if (rooms.length == 0) {
                logger.debug("Brain {} ({}) found - but there were no rooms - skipping", brain.getName(), brainId);
                return;
            }

            logger.debug("Brain {} ({}) found, scanning {} rooms in it", brain.getName(), brainId, rooms.length);
            for (NeeoRoom room : rooms) {
                final String roomKey = room.getKey();
                if (roomKey == null || roomKey.isEmpty()) {
                    logger.debug("Room didn't have a room key: {}", room);
                    continue;
                }

                if (room.getDevices().getDevices().length == 0 && room.getRecipes().getRecipes().length == 0
                        && !config.isDiscoverEmptyRooms()) {
                    logger.debug("Room {} ({}) found but has no devices or recipes, ignoring - {}", roomKey, brainId,
                            room.getName());
                    continue;
                }

                final ThingUID thingUID = new ThingUID(NeeoConstants.BRIDGE_TYPE_ROOM, brainUid, roomKey);

                final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(NeeoConstants.CONFIG_ROOMKEY, roomKey)
                        .withProperty(NeeoConstants.CONFIG_EXCLUDE_THINGS, true).withBridge(brainUid)
                        .withLabel(room.getName() + " (NEEO " + brainId + ")").build();
                thingDiscovered(discoveryResult);
            }
        } catch (IOException e) {
            logger.debug("IOException occurred getting brain info ({}): {}", brainId, e.getMessage(), e);
        }
    }
}
