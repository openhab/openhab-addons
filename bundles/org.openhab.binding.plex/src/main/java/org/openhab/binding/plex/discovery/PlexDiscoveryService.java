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
package org.openhab.binding.plex.discovery;

import static org.openhab.binding.plex.internal.PlexBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plex.internal.handler.PlexServerHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * @author Brian Homeyer - Initial contribution
 * @author Aron Beurskens - Binding development
 */
@NonNullByDefault
public class PlexDiscoveryService extends AbstractDiscoveryService {
    private final PlexServerHandler bridgeHandler;

    public PlexDiscoveryService(PlexServerHandler bridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, 10, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        for (String machineId : bridgeHandler.getAvailablePlayers()) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            ThingTypeUID thingTypeUID = UID_PLAYER;
            ThingUID playerThingUid = new ThingUID(UID_PLAYER, bridgeUID, machineId);

            Map<String, Object> properties = new HashMap<>();
            properties.put(CONFIG_PLAYER_ID, machineId);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(playerThingUid).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withRepresentationProperty(CONFIG_PLAYER_ID)
                    .withLabel("PLEX Player (" + machineId + ")").build();

            thingDiscovered(discoveryResult);
        }
    }
}
