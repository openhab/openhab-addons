/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.handler.YamahaBridgeHandler;
import org.openhab.binding.yamahareceiver.handler.YamahaZoneThingHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link YamahaReceiverHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff -- Intial contribution
 */
@Component(immediate = true, service = ThingHandlerFactory.class)
public class YamahaReceiverHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.union(
            YamahaReceiverBindingConstants.BRIDGE_THING_TYPES_UIDS,
            YamahaReceiverBindingConstants.ZONE_THING_TYPES_UIDS);
    private Logger logger = LoggerFactory.getLogger(YamahaReceiverHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(YamahaReceiverBindingConstants.BRIDGE_THING_TYPE)) {
            return new YamahaBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(YamahaReceiverBindingConstants.ZONE_THING_TYPE)) {
            return new YamahaZoneThingHandler(thing);
        }

        logger.error("Unexpected thing encountered in factory: {}", thingTypeUID.getAsString());
        return null;
    }
}
