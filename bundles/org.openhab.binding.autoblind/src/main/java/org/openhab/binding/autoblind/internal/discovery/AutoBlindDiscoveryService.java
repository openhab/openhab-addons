/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.autoblind.internal.discovery;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.autoblind.internal.AutoBlindBindingConstants;
import org.openhab.binding.autoblind.internal.api.dto.AllPeripheralResponse;
import org.openhab.binding.autoblind.internal.api.dto.PeripheralInfo;
import org.openhab.binding.autoblind.internal.handler.AutoBlindHubHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers shades connected to an AutoBlind hub by querying the GetAllPeripheral endpoint.
 *
 * @author Stephen Berg (@BiloxiGeek) - Initial contribution
 */
@NonNullByDefault
public class AutoBlindDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(AutoBlindDiscoveryService.class);
    private final AutoBlindHubHandler hubHandler;

    public AutoBlindDiscoveryService(AutoBlindHubHandler hubHandler) {
        super(Set.of(AutoBlindBindingConstants.THING_TYPE_SHADE), 30, true);
        this.hubHandler = hubHandler;
    }

    @Override
    protected void startScan() {
        @Nullable
        AllPeripheralResponse response = hubHandler.getAllPeripherals();
        if (response == null) {
            logger.debug("No response from hub during shade discovery");
            return;
        }

        ThingUID bridgeUid = hubHandler.getThing().getUID();

        for (AllPeripheralResponse.Room room : response.results.roomList) {
            for (AllPeripheralResponse.Group group : room.groupList) {
                for (PeripheralInfo peripheral : group.peripheralList) {
                    String uid = peripheral.peripheralUid;
                    String label = peripheral.peripheralName.isBlank() ? room.roomName + " " + group.groupName
                            : peripheral.peripheralName;

                    ThingUID thingUid = new ThingUID(AutoBlindBindingConstants.THING_TYPE_SHADE, bridgeUid, uid);

                    DiscoveryResult result = DiscoveryResultBuilder.create(thingUid).withBridge(bridgeUid)
                            .withLabel(label + " (" + room.roomName + "/" + group.groupName + ")")
                            .withProperty(AutoBlindBindingConstants.CONFIG_PERIPHERAL_UID, Integer.parseInt(uid))
                            .withRepresentationProperty(AutoBlindBindingConstants.CONFIG_PERIPHERAL_UID).build();

                    thingDiscovered(result);
                    logger.debug("Discovered shade: {} (UID {})", label, uid);
                }
            }
        }
    }
}
