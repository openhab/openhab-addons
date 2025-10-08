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

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.FAKE_PROPERTY_CHANNEL_TYPE_UID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.discovery.HomekitChildDiscoveryService;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.dto.Characteristic;
import org.openhab.binding.homekit.internal.dto.Service;
import org.openhab.binding.homekit.internal.enums.ServiceType;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for HomeKit bridge devices.
 * It marshals the communications with multiple HomeKit child accessories within a HomeKit bridge server.
 * It uses the /accessories endpoint to discover embedded accessories and their services.
 * It notifies the {@link HomekitChildDiscoveryService} when accessories are discovered.
 * It does not currently handle commands for channels, that is left to the child accessory handlers.
 * It extends {@link HomekitBaseAccessoryHandler} to handle pairing and secure session setup.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitBridgeHandler extends HomekitBaseAccessoryHandler implements BridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HomekitBridgeHandler.class);
    private final HomekitChildDiscoveryService discoveryService;

    public HomekitBridgeHandler(Bridge bridge, HomekitTypeProvider typeProvider,
            HomekitChildDiscoveryService discoveryService) {
        super(bridge, typeProvider);
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
        // do nothing
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        // do nothing
    }

    @Override
    protected void accessoriesLoaded() {
        logger.debug("Bridge accessories loaded {}", accessories.size());
        discoveryService.addBridgeHandler(this); // discover child accessories
        createProperties(); // create properties from accessory information
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // do nothing
    }

    @Override
    public void dispose() {
        discoveryService.removeBridgeHandler(this);
        super.dispose();
    }

    /**
     * Creates properties for the bridge based on the characteristics within the ACCESSORY_INFORMATION
     * service (if any).
     */
    private void createProperties() {
        if (accessories.isEmpty()) {
            return;
        }
        Integer accessoryId = getAccessoryId();
        if (accessoryId == null) {
            return;
        }
        Accessory accessory = accessories.get(accessoryId);
        if (accessory == null) {
            return;
        }
        // search for the accessory information service and collect its properties
        for (Service service : accessory.services) {
            if (ServiceType.ACCESSORY_INFORMATION == service.getServiceType()) {
                for (Characteristic characteristic : service.characteristics) {
                    ChannelDefinition channelDef = characteristic.buildAndRegisterChannelDefinition(thing.getUID(),
                            typeProvider);
                    if (channelDef != null && FAKE_PROPERTY_CHANNEL_TYPE_UID.equals(channelDef.getChannelTypeUID())) {
                        String name = channelDef.getId();
                        String value = channelDef.getLabel();
                        if (value != null) {
                            thing.setProperty(name, value);
                        }
                    }
                }
                break; // only one accessory information service per accessory
            }
        }
    }
}
