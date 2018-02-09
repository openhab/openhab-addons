/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.internal;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.hdpowerview.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.handler.HDPowerViewHubHandler;
import org.openhab.binding.hdpowerview.handler.HDPowerViewShadeHandler;
import org.openhab.binding.hdpowerview.internal.discovery.HDPowerViewShadeDiscoveryService;

/**
 * The {@link HDPowerViewHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andy Lintner - Initial contribution
 */
public class HDPowerViewHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return HDPowerViewBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(HDPowerViewBindingConstants.THING_TYPE_HUB)) {
            HDPowerViewHubHandler handler = new HDPowerViewHubHandler((Bridge) thing);
            registerService(new HDPowerViewShadeDiscoveryService(handler));
            return handler;
        } else if (thingTypeUID.equals(HDPowerViewBindingConstants.THING_TYPE_SHADE)) {
            return new HDPowerViewShadeHandler(thing);
        }

        return null;
    }

    private void registerService(DiscoveryService discoveryService) {
        bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }
}
