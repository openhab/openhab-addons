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
package org.openhab.binding.homekit.internal.factory;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.handler.HomekitAccessoryHandler;
import org.openhab.binding.homekit.internal.handler.HomekitBridgeHandler;
import org.openhab.binding.homekit.internal.persistence.HomekitKeyStore;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ChannelGroupTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Creates things and thing handlers. Supports HomeKit bridges and accessories.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class)
public class HomekitHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_ACCESSORY);

    private final HomekitTypeProvider typeProvider;
    private final ChannelTypeRegistry channelTypeRegistry;
    private final ChannelGroupTypeRegistry channelGroupTypeRegistry;
    private final HomekitKeyStore keyStore;

    @Activate
    public HomekitHandlerFactory(@Reference HomekitTypeProvider typeProvider,
            @Reference ChannelTypeRegistry channelTypeRegistry,
            @Reference ChannelGroupTypeRegistry channelGroupTypeRegistry, @Reference HomekitKeyStore keyStore) {
        this.typeProvider = typeProvider;
        this.channelTypeRegistry = channelTypeRegistry;
        this.channelGroupTypeRegistry = channelGroupTypeRegistry;
        this.keyStore = keyStore;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new HomekitBridgeHandler((Bridge) thing, typeProvider, keyStore);
        } else if (THING_TYPE_ACCESSORY.equals(thingTypeUID)) {
            return new HomekitAccessoryHandler(thing, typeProvider, channelTypeRegistry, channelGroupTypeRegistry,
                    keyStore);
        }
        return null;
    }
}
