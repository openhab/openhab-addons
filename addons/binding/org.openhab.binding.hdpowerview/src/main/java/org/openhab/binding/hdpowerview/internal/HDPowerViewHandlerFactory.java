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
package org.openhab.binding.hdpowerview.internal;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.hdpowerview.internal.discovery.HDPowerViewShadeDiscoveryService;
import org.openhab.binding.hdpowerview.internal.handler.HDPowerViewHubHandler;
import org.openhab.binding.hdpowerview.internal.handler.HDPowerViewShadeHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link HDPowerViewHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andy Lintner - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.hdpowerview")
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
        bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
    }
}
