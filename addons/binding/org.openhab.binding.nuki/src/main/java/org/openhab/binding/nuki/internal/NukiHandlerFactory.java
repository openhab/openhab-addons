/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.nuki.NukiBindingConstants;
import org.openhab.binding.nuki.handler.NukiBridgeHandler;
import org.openhab.binding.nuki.handler.NukiSmartLockHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link NukiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Katter - Initial contribution
 */
@Component(immediate = true, service = ThingHandlerFactory.class)
public class NukiHandlerFactory extends BaseThingHandlerFactory {

    private final static Logger logger = LoggerFactory.getLogger(NukiHandlerFactory.class);
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .union(NukiBindingConstants.THING_TYPE_BRIDGE_UIDS, NukiBindingConstants.THING_TYPE_SMARTLOCK_UIDS);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.trace("NukiHandlerFactory:createHandler({})", thing);
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (NukiBindingConstants.THING_TYPE_BRIDGE_UIDS.contains(thingTypeUID)) {
            return new NukiBridgeHandler((Bridge) thing);
        } else if (NukiBindingConstants.THING_TYPE_SMARTLOCK_UIDS.contains(thingTypeUID)) {
            return new NukiSmartLockHandler(thing);
        }
        logger.trace("No valid Handler found for Thing[{}]!", thingTypeUID);
        return null;
    }

}
