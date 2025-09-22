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
package org.openhab.binding.homekit.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.discovery.HomekitChildDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for HomeKit bridge devices.
 * It marshals the communications with multiple HomeKit child accessories within a HomeKit bridge server.
 * It uses the /accessories endpoint to discover embedded accessories and their services.
 * It notifies the {@link HomekitChildDiscoveryService} when accessories are discovered.
 * It does not currently handle commands for channels, that is left to the child accessory handlers.
 * It extends {@link HomekitBaseServerHandler} to handle pairing and secure session setup.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitBridgeHandler extends HomekitBaseServerHandler implements BridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HomekitBridgeHandler.class);
    private final HomekitChildDiscoveryService discoveryService;

    public HomekitBridgeHandler(Bridge bridge, HomekitChildDiscoveryService discoveryService) {
        super(bridge);
        this.discoveryService = discoveryService;
    }

    @Override
    public Bridge getThing() {
        return (Bridge) super.getThing();
    }

    /**
     * Creates a bridge builder, which allows to modify the bridge. The 'updateThing(Thing)' method
     * must be called to persist the changes.
     *
     * @return {@link BridgeBuilder} which builds an exact copy of the bridge
     */
    @Override
    protected BridgeBuilder editThing() {
        return BridgeBuilder.create(thing.getThingTypeUID(), thing.getUID()).withBridge(thing.getBridgeUID())
                .withChannels(thing.getChannels()).withConfiguration(thing.getConfiguration())
                .withLabel(thing.getLabel()).withLocation(thing.getLocation()).withProperties(thing.getProperties())
                .withSemanticEquipmentTag(thing.getSemanticEquipmentTag());
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof HomekitDeviceHandler homekitDeviceHandler) {
            homekitDeviceHandler.accessoriesLoaded();
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        // do nothing
    }

    @Override
    protected void accessoriesLoaded() {
        logger.debug("Bridge accessories loaded {}", accessories.size());
        discoveryService.devicesDiscovered(thing, accessories.values()); // discover child accessories
    }
}
