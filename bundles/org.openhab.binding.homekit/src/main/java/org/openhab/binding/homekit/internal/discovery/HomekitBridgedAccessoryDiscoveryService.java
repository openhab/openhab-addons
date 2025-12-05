/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.discovery;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.handler.HomekitBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * Discovery service component that publishes newly discovered bridged accessories of a HomeKit bridge
 * accessory. Discovered devices are published as Things with thingUID based on accessory ID (aid) of type
 * {@link org.openhab.binding.homekit.internal.HomekitBindingConstants#THING_TYPE_BRIDGED_ACCESSORY} .
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class)
public class HomekitBridgedAccessoryDiscoveryService
        extends AbstractThingHandlerDiscoveryService<HomekitBridgeHandler> {

    private static final int TIMEOUT_SECONDS = 10;

    public HomekitBridgedAccessoryDiscoveryService() {
        super(HomekitBridgeHandler.class, Set.of(THING_TYPE_BRIDGED_ACCESSORY), TIMEOUT_SECONDS);
    }

    @Override
    public void initialize() {
        super.initialize();
        thingHandler.registerDiscoveryService(this);
    }

    @Override
    public void dispose() {
        thingHandler.unregisterDiscoveryService();
        super.dispose();
    }

    @Override
    public void startScan() {
        if (thingHandler instanceof HomekitBridgeHandler handler) {
            discoverBridgedAccessories(handler.getThing(), handler.getAccessories().values());
        }
    }

    private void discoverBridgedAccessories(Thing bridge, Collection<Accessory> accessories) {
        String bridgeMacAddress = thingHandler.getThing().getConfiguration()
                .get(CONFIG_MAC_ADDRESS) instanceof String mac ? mac : null;
        if (bridgeMacAddress == null) {
            return;
        }
        accessories.forEach(accessory -> {
            if (accessory.aid instanceof Long aid && aid != 1L && accessory.services != null) {
                ThingUID uid = new ThingUID(THING_TYPE_BRIDGED_ACCESSORY, bridge.getUID(), aid.toString());
                String uniqueId = STRING_AID_FMT.formatted(bridgeMacAddress, aid);
                String label = THING_LABEL_FMT.formatted(accessory.getAccessoryInstanceLabel(), uniqueId);
                thingDiscovered(DiscoveryResultBuilder.create(uid) //
                        .withBridge(bridge.getUID()) //
                        .withLabel(label) //
                        .withProperty(CONFIG_ACCESSORY_ID, aid.toString()) //
                        .withProperty(PROPERTY_REPRESENTATION, uniqueId)
                        .withRepresentationProperty(PROPERTY_REPRESENTATION).build());
            }
        });
    }
}
