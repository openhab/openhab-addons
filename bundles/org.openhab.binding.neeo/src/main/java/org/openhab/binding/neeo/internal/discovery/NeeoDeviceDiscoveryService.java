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
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.neeo.internal.NeeoBrainApi;
import org.openhab.binding.neeo.internal.NeeoConstants;
import org.openhab.binding.neeo.internal.NeeoRoomConfig;
import org.openhab.binding.neeo.internal.UidUtils;
import org.openhab.binding.neeo.internal.handler.NeeoRoomHandler;
import org.openhab.binding.neeo.internal.models.NeeoDevice;
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
 * Implementation of {@link AbstractDiscoveryService} that will discover the devices in a NEEO room;
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoDeviceDiscoveryService extends AbstractDiscoveryService {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoDeviceDiscoveryService.class);

    /** The device thing type we support */
    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .singleton(NeeoConstants.THING_TYPE_DEVICE);

    /** The timeout (in seconds) for searching the room */
    private static final int SEARCH_TIME = 10;

    /** The room handler to search */
    private final NeeoRoomHandler roomHandler;

    /**
     * Constructs the discovery service from the room handler
     *
     * @param roomHandler a non-null room handler
     */
    public NeeoDeviceDiscoveryService(NeeoRoomHandler roomHandler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, SEARCH_TIME);
        Objects.requireNonNull(roomHandler, "roomHandler cannot be null");
        this.roomHandler = roomHandler;
    }

    @Override
    protected void startScan() {
        final Bridge roomBridge = roomHandler.getThing();
        final ThingUID roomUid = roomBridge.getUID();

        final String brainId = roomHandler.getNeeoBrainId();
        if (brainId == null || brainId.isEmpty()) {
            logger.debug("Unknown brain ID for roomHandler: {}", roomHandler);
            return;
        }

        final NeeoBrainApi api = roomHandler.getNeeoBrainApi();
        if (api == null) {
            logger.debug("Brain API was not available for {} - skipping", brainId);
            return;
        }

        final NeeoRoomConfig config = roomBridge.getConfiguration().as(NeeoRoomConfig.class);
        final String roomKey = config.getRoomKey();
        if (roomKey == null || roomKey.isEmpty()) {
            logger.debug("RoomKey wasn't configured for {} - skipping", brainId);
            return;
        }

        try {
            final NeeoRoom room = api.getRoom(roomKey);
            final NeeoDevice[] devices = room.getDevices().getDevices();

            if (devices.length == 0) {
                logger.debug("Room {} found - but there were no devices - skipping", room.getName());
                return;
            }

            logger.debug("Room {} found, scanning {} devices in it", room.getName(), devices.length);
            for (NeeoDevice device : devices) {
                final String deviceKey = device.getKey();
                if (deviceKey == null || deviceKey.isEmpty()) {
                    logger.debug("Device key wasn't found for device: {}", device);
                    continue;
                }

                if (config.isExcludeThings() && UidUtils.isThing(device)) {
                    logger.debug("Found openHAB thing but ignoring per configuration: {}", device);
                    continue;
                }

                logger.debug("Device #{} found - {}", deviceKey, device.getName());

                final ThingUID thingUID = new ThingUID(NeeoConstants.THING_TYPE_DEVICE, roomUid, deviceKey);

                final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(NeeoConstants.CONFIG_DEVICEKEY, deviceKey).withBridge(roomUid)
                        .withLabel(device.getName() + " (NEEO " + brainId + ")").build();
                thingDiscovered(discoveryResult);
            }
        } catch (IOException e) {
            logger.debug("IOException occurred getting brain info ({}): {}", brainId, e.getMessage(), e);
        }
    }
}
