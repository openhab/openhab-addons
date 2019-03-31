/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.minecraft.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.minecraft.internal.handler.MinecraftPlayerHandler;
import org.openhab.binding.minecraft.internal.handler.MinecraftServerHandler;
import org.openhab.binding.minecraft.internal.handler.MinecraftSignHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MinecraftHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mattias Markehed - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.minecraft")
public class MinecraftHandlerFactory extends BaseThingHandlerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(MinecraftBindingConstants.THING_TYPE_SERVER);
        SUPPORTED_THING_TYPES_UIDS.add(MinecraftBindingConstants.THING_TYPE_PLAYER);
        SUPPORTED_THING_TYPES_UIDS.add(MinecraftBindingConstants.THING_TYPE_SIGN);
    }

    private static List<MinecraftServerHandler> minecraftServers = new ArrayList<MinecraftServerHandler>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(MinecraftBindingConstants.THING_TYPE_SERVER)) {
            MinecraftServerHandler serverHandler = new MinecraftServerHandler((Bridge) thing);
            minecraftServers.add(serverHandler);
            return serverHandler;
        } else if (thingTypeUID.equals(MinecraftBindingConstants.THING_TYPE_PLAYER)) {
            MinecraftPlayerHandler playerHandler = new MinecraftPlayerHandler(thing);
            return playerHandler;
        } else if (thingTypeUID.equals(MinecraftBindingConstants.THING_TYPE_SIGN)) {
            MinecraftSignHandler signHandler = new MinecraftSignHandler(thing);
            return signHandler;
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        minecraftServers.remove(thingHandler);
        super.removeHandler(thingHandler);
    }

    /**
     * Get all Minecraft handlers created.
     *
     * @return the Minecraft handlers created,
     */
    public static List<MinecraftServerHandler> getMinecraftServers() {
        LOGGER.debug("getMinecraftServers {}", minecraftServers.size());
        return new ArrayList<MinecraftServerHandler>(minecraftServers);
    }
}
