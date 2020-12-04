/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.discovery.HDPowerViewShadeDiscoveryService;
import org.openhab.binding.hdpowerview.internal.handler.HDPowerViewHubHandler;
import org.openhab.binding.hdpowerview.internal.handler.HDPowerViewShadeHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HDPowerViewHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.hdpowerview")
public class HDPowerViewHandlerFactory extends BaseThingHandlerFactory {
    private final ClientBuilder clientBuilder;

    @Activate
    public HDPowerViewHandlerFactory(@Reference ClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return HDPowerViewBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(HDPowerViewBindingConstants.THING_TYPE_HUB)) {
            HDPowerViewHubHandler handler = new HDPowerViewHubHandler((Bridge) thing, clientBuilder);
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
