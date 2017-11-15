/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.enocean.EnOceanBindingConstants;
import org.openhab.binding.enocean.handler.EnOceanBridgeHandler;
import org.openhab.binding.enocean.handler.EnOceanRockerSwitchHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnOceanHandlerFactory} is responsible for EnOcean thing handlers.
 *
 * @author Jan Kemmler - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.enocean")
public class EnOceanHandlerFactory extends BaseThingHandlerFactory {

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(EnOceanRockerSwitchHandler.class);

    @SuppressWarnings("null")
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(EnOceanBindingConstants.THING_TYPE_ROCKER_SWITCH, EnOceanBindingConstants.THING_TYPE_SERIAL_BRIDGE)
            .collect(Collectors.toSet());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @SuppressWarnings("null")
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(EnOceanBindingConstants.THING_TYPE_ROCKER_SWITCH)) {
            return new EnOceanRockerSwitchHandler(thing);
        }

        if (thingTypeUID.equals(EnOceanBindingConstants.THING_TYPE_SERIAL_BRIDGE)) {
            return new EnOceanBridgeHandler((Bridge) thing);
        }

        return null;
    }
}
