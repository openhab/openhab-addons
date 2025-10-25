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
 * Discovery service component that publishes newly discovered child accessories of a HomeKit bridge accessory.
 * Discovered accessories are published with a ThingUID based on their accessory ID (aid) and service ID (iid).
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class)
public class HomekitChildDiscoveryService extends AbstractThingHandlerDiscoveryService<HomekitBridgeHandler> {

    private static final int TIMEOUT_SECONDS = 10;

    public HomekitChildDiscoveryService() {
        super(HomekitBridgeHandler.class, Set.of(THING_TYPE_ACCESSORY), TIMEOUT_SECONDS);
    }

    @Override
    public void startScan() {
        discoverChildren(thingHandler.getThing(), thingHandler.getAccessories());
    }

    private void discoverChildren(Thing bridge, Collection<Accessory> accessories) {
        accessories.forEach(accessory -> {
            if (accessory.aid instanceof Integer aid && aid != 1 && accessory.services != null) {
                ThingUID uid = new ThingUID(THING_TYPE_ACCESSORY, bridge.getUID(), aid.toString());
                String thingLabel = "%s (%d)".formatted(accessory.getAccessoryInstanceLabel(), accessory.aid);
                thingDiscovered(DiscoveryResultBuilder.create(uid) //
                        .withBridge(bridge.getUID()) //
                        .withLabel(THING_LABEL_FMT.formatted(thingLabel, bridge.getLabel())) //
                        .withProperty(CONFIG_HOST, "n/a") //
                        .withProperty(CONFIG_PAIRING_CODE, "n/a") //
                        .withProperty(PROPERTY_ACCESSORY_UID, uid.toString()) //
                        .withRepresentationProperty(PROPERTY_ACCESSORY_UID).build());
            }
        });
    }
}
